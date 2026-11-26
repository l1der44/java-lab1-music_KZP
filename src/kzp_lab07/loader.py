import csv
from pathlib import Path

from .errors import DataError
from .models import LiveTrack, StudioTrack, Track

BASE_EN = {"performer", "title", "album", "duration", "genre"}
BASE_UA = {"виконавець", "назва", "альбом", "тривалість", "жанр"}
OPTIONAL_FIELDS = {"kind", "extra"}
UA_TO_EN = {
    "виконавець": "performer",
    "назва": "title",
    "альбом": "album",
    "тривалість": "duration",
    "жанр": "genre",
}


def _normalize_row(raw_row: dict[str, str | None], is_ua: bool) -> dict[str, str | None]:
    if not is_ua:
        return raw_row
    return {UA_TO_EN.get(k, k): v for k, v in raw_row.items() if k is not None}


def _require_text(row: dict[str, str | None], field: str, row_number: int) -> str:
    raw = row.get(field)
    if raw is None or not raw.strip():
        raise DataError(f"Рядок {row_number}: відсутнє або порожнє поле {field}")
    return raw.strip()


def _parse_duration(row: dict[str, str | None], row_number: int) -> int:
    raw = _require_text(row, "duration", row_number)
    try:
        return int(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле duration має бути цілим числом") from error


def build_track(row: dict[str, str | None], row_number: int) -> Track:
    performer = _require_text(row, "performer", row_number)
    title = _require_text(row, "title", row_number)
    album = _require_text(row, "album", row_number)
    duration = _parse_duration(row, row_number)
    genre = _require_text(row, "genre", row_number)

    raw_kind = (row.get("kind") or "STUDIO").strip().upper()
    raw_extra = (row.get("extra") or "").strip()

    try:
        if raw_kind == "STUDIO":
            return (
                StudioTrack(performer, title, album, duration, genre, raw_extra)
                if raw_extra
                else StudioTrack(performer, title, album, duration, genre)
            )
        if raw_kind == "LIVE":
            return (
                LiveTrack(performer, title, album, duration, genre, raw_extra)
                if raw_extra
                else LiveTrack(performer, title, album, duration, genre)
            )
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: {error}") from error

    raise DataError(f"Рядок {row_number}: невідомий тип запису kind={raw_kind!r}")


def load_tracks(path: Path) -> list[Track]:
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )
            header = set(reader.fieldnames)
            if BASE_EN.issubset(header) and header.issubset(BASE_EN | OPTIONAL_FIELDS):
                is_ua = False
            elif BASE_UA.issubset(header) and header.issubset(BASE_UA | OPTIONAL_FIELDS):
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
                tracks.append(build_track(row, row_number))
            return tracks
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error
