from pathlib import Path

import pytest

from kzp_lab06.errors import DataError
from kzp_lab06.loader import load_records


def test_load_valid_records_with_comma_and_normalization(tmp_path: Path) -> None:
    csv_file = tmp_path / "tracks.csv"
    csv_file.write_text(
        "performer,title,album,duration,genre\n"
        'Океан Ельзи,"Там, де нас нема","Там, де нас нема",269,rock\n'
        "Один в каное,Човен,Один в каное,180, Indie \n",
        encoding="utf-8",
    )

    records = load_records(csv_file)
    assert len(records) == 2
    assert records[0]["title"] == "Там, де нас нема"
    assert records[0]["duration"] == 269
    assert records[0]["genre"] == "ROCK"
    assert records[1]["genre"] == "INDIE"


def test_load_empty_csv_with_header_returns_empty_list(tmp_path: Path) -> None:
    csv_file = tmp_path / "empty.csv"
    csv_file.write_text("performer,title,album,duration,genre\n", encoding="utf-8")

    assert load_records(csv_file) == []


def test_load_missing_column_raises_data_error(tmp_path: Path) -> None:
    csv_file = tmp_path / "bad_header.csv"
    csv_file.write_text(
        "performer,title,album,duration\nГурт,Пісня,Альбом,200\n",
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Некоректний заголовок"):
        load_records(csv_file)


def test_load_invalid_duration_includes_row_number_and_cause(tmp_path: Path) -> None:
    csv_file = tmp_path / "bad_number.csv"
    csv_file.write_text(
        "performer,title,album,duration,genre\nГурт,Пісня,Альбом,не_число,ROCK\n",
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Рядок 2: поле duration") as exc_info:
        load_records(csv_file)
    assert isinstance(exc_info.value.__cause__, ValueError)


def test_load_non_positive_duration_raises_data_error(tmp_path: Path) -> None:
    csv_file = tmp_path / "zero_duration.csv"
    csv_file.write_text(
        "performer,title,album,duration,genre\nГурт,Пісня,Альбом,0,ROCK\n",
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Рядок 2: поле duration має бути більшим за 0"):
        load_records(csv_file)


def test_load_missing_value_in_row_raises_data_error(tmp_path: Path) -> None:
    csv_file = tmp_path / "missing_cell.csv"
    csv_file.write_text(
        "performer,title,album,duration,genre\nГурт,Пісня,Альбом,200\n",
        encoding="utf-8",
    )

    with pytest.raises(DataError, match="Рядок 2: відсутнє значення поля genre"):
        load_records(csv_file)
