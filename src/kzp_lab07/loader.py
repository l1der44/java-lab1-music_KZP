import csv
from pathlib import Path

from .errors import DataError
from .models import LiveTrack, StudioTrack, Track

REQUIRED_EN = {"performer", "title", "album", "duration", "genre"}
REQUIRED_UA = {"виконавець", "назва", "альбом", "тривалість", "жанр"}
DEFAULT_FIELDS = ["performer", "title", "album", "duration", "genre", "kind", "extra"]
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


def _build_track(row: dict[str, str | None], row_number: int) -> Track:
    performer = _require_text(row, "performer", row_number)
    title = _require_text(row, "title", row_number)
    album = _require_text(row, "album", row_number)
    duration = _parse_duration(row, row_number)
    genre = _require_text(row, "genre", row_number).upper()
    kind = (row.get("kind") or "STUDIO").strip().upper()
    extra = (row.get("extra") or "").strip()

    try:
        if kind == "LIVE":
            return (
                LiveTrack(performer, title, album, duration, genre, venue=extra)
                if extra
                else LiveTrack(performer, title, album, duration, genre)
            )
        if kind in ("", "STUDIO"):
            return (
                StudioTrack(performer, title, album, duration, genre, studio=extra)
                if extra
                else StudioTrack(performer, title, album, duration, genre)
            )
        raise DataError(f"Рядок {row_number}: невідомий тип треку {kind}")
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: {error}") from error


def load_tracks(path: Path) -> list[Track]:
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            first_line = source.readline()
            if not first_line:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )
            source.seek(0)

            if ";" in first_line and "," not in first_line.split(";")[0]:
                raw_reader = csv.reader(source, delimiter=";")
                tracks: list[Track] = []
                seen: set[tuple[str, str, str, int, str]] = set()
                for row_number, values in enumerate(raw_reader, start=1):
                    if not any(cell.strip() for cell in values) or len(values) < 5:
                        continue
                    row = dict(zip(DEFAULT_FIELDS, values, strict=False))
                    try:
                        track = _build_track(row, row_number)
                    except DataError:
                        continue
                    key = (track.performer, track.title, track.album, track.duration, track.genre)
                    if key not in seen:
                        seen.add(key)
                        tracks.append(track)
                return tracks

            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )
            header = set(reader.fieldnames)
            if REQUIRED_EN.issubset(header):
                is_ua = False
            elif REQUIRED_UA.issubset(header):
                is_ua = True
            else:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )

            tracks = []
            for row_number, raw_row in enumerate(reader, start=2):
                if None in raw_row:
                    raise DataError(f"Рядок {row_number}: зайві значення поза межами заголовка")
                row = _normalize_row(raw_row, is_ua)
                tracks.append(_build_track(row, row_number))
            return tracks
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error
