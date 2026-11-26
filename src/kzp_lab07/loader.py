import csv
from pathlib import Path

from .errors import DataError
from .models import LiveTrack, StudioTrack, Track

REQUIRED_EN = {"performer", "title", "album", "duration", "genre"}
REQUIRED_UA = {"виконавець", "назва", "альбом", "тривалість", "жанр"}
OPTIONAL_COLS = {"kind", "extra"}
UA_TO_EN = {
    "виконавець": "performer",
    "назва": "title",
    "альбом": "album",
    "тривалість": "duration",
    "жанр": "genre",
    "kind": "kind",
    "extra": "extra",
}


def _normalize_row(raw_row: dict[str, str | None], is_ua: bool) -> dict[str, str | None]:
    if not is_ua:
        return raw_row
    return {UA_TO_EN[k]: v for k, v in raw_row.items() if k in UA_TO_EN}


def _require_text(row: dict[str, str | None], field: str, row_number: int) -> str:
    raw = row.get(field)
    if raw is None or not raw.strip():
        raise DataError(f"Рядок {row_number}: відсутнє значення поля {field}")
    return raw.strip()


def _parse_duration(row: dict[str, str | None], row_number: int) -> int:
    raw = _require_text(row, "duration", row_number)
    try:
        duration = int(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле duration має бути цілим числом") from error
    if duration <= 0:
        raise DataError(f"Рядок {row_number}: поле duration має бути більшим за 0")
    return duration


def load_tracks(path: Path) -> list[Track]:
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )
            header = set(reader.fieldnames)
            if REQUIRED_EN.issubset(header) and header.issubset(REQUIRED_EN | OPTIONAL_COLS):
                is_ua = False
            elif REQUIRED_UA.issubset(header) and header.issubset(REQUIRED_UA | OPTIONAL_COLS):
                is_ua = True
            else:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )

            tracks: list[Track] = []
            for row_number, raw_row in enumerate(reader, start=2):
                if None in raw_row:
                    raise DataError(f"Рядок {row_number}: зайві значення поза межами заголовка")
                row = _normalize_row(raw_row, is_ua)
                performer = _require_text(row, "performer", row_number)
                title = _require_text(row, "title", row_number)
                album = _require_text(row, "album", row_number)
                duration = _parse_duration(row, row_number)
                genre = _require_text(row, "genre", row_number).upper()
                kind = (row.get("kind") or "STUDIO").strip().upper()
                extra = (row.get("extra") or "").strip()

                try:
                    if kind == "LIVE":
                        track: Track = (
                            LiveTrack(performer, title, album, duration, genre, venue=extra)
                            if extra
                            else LiveTrack(performer, title, album, duration, genre)
                        )
                    elif kind in ("", "STUDIO"):
                        track = (
                            StudioTrack(performer, title, album, duration, genre, studio=extra)
                            if extra
                            else StudioTrack(performer, title, album, duration, genre)
                        )
                    else:
                        raise DataError(f"Рядок {row_number}: невідомий тип треку {kind}")
                except ValueError as error:
                    raise DataError(f"Рядок {row_number}: {error}") from error

                tracks.append(track)
            return tracks
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error
