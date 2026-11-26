import csv
from pathlib import Path

from .errors import DataError

REQUIRED_EN = {"performer", "title", "album", "duration", "genre"}
REQUIRED_UA = {"виконавець", "назва", "альбом", "тривалість", "жанр"}
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
    return {UA_TO_EN[k]: v for k, v in raw_row.items() if k in UA_TO_EN}


def _require_text(row: dict[str, str | None], field: str, row_number: int) -> str:
    raw = row.get(field)
    if raw is None or not raw.strip():
        raise DataError(f"Рядок {row_number}: відсутнє або порожнє поле {field}")
    return raw.strip()


def _parse_duration(row: dict[str, str | None], row_number: int) -> int:
    raw = _require_text(row, "duration", row_number)
    try:
        duration = int(raw)
    except ValueError as error:
        raise DataError(f"Рядок {row_number}: поле duration має бути цілим числом") from error
    if duration <= 0:
        raise DataError(f"Рядок {row_number}: поле duration має бути > 0")
    return duration


def load_tracks(path: Path) -> list[dict[str, object]]:
    try:
        with path.open("r", encoding="utf-8", newline="") as source:
            reader = csv.DictReader(source)
            if reader.fieldnames is None:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )
            header = set(reader.fieldnames)
            if header == REQUIRED_EN:
                is_ua = False
            elif header == REQUIRED_UA:
                is_ua = True
            else:
                raise DataError(
                    "Некоректний заголовок: потрібні performer, title, album, duration, genre"
                )

            tracks: list[dict[str, object]] = []
            for row_number, raw_row in enumerate(reader, start=2):
                if None in raw_row:
                    raise DataError(f"Рядок {row_number}: зайві значення поза межами заголовка")
                row = _normalize_row(raw_row, is_ua)
                tracks.append(
                    {
                        "performer": _require_text(row, "performer", row_number),
                        "title": _require_text(row, "title", row_number),
                        "album": _require_text(row, "album", row_number),
                        "duration": _parse_duration(row, row_number),
                        "genre": _require_text(row, "genre", row_number).upper(),
                    }
                )
            return tracks
    except OSError as error:
        raise DataError(f"Не вдалося прочитати файл: {path}") from error


load_records = load_tracks
