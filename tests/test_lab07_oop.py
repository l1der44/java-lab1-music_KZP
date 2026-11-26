import dataclasses
from pathlib import Path

import pytest

from kzp_lab07.analysis import best_scored_track, make_summary
from kzp_lab07.cli import main
from kzp_lab07.errors import DataError
from kzp_lab07.loader import load_tracks
from kzp_lab07.models import LibrarySummary, LiveTrack, StudioTrack
from kzp_lab07.reporting import format_report, write_report


def test_property_setter_validation_and_normalization() -> None:
    track = StudioTrack("Океан Ельзи", "Квітка", "Модель", 180, " rock ")
    assert track.duration == 180
    assert track.genre == "ROCK"

    track.duration = 215
    track.genre = " indie "
    assert track.duration == 215
    assert track.genre == "INDIE"

    for invalid in (0, -10, True):
        with pytest.raises(ValueError, match="Тривалість"):
            track.duration = invalid  # type: ignore[assignment]

    with pytest.raises(ValueError, match="Жанр"):
        track.genre = "   "


def test_dataclass_summary_is_immutable() -> None:
    summary = LibrarySummary(
        total_input=1,
        count=1,
        min_duration=180,
        total_duration=250,
        genres=("ROCK",),
        duration_by_performer=(("Океан Ельзи", 250),),
        longest_track=None,
        best_scored_track=None,
        average_score=135.0,
    )
    with pytest.raises(dataclasses.FrozenInstanceError):
        summary.count = 5  # type: ignore[misc]


def test_special_methods_str_repr_and_equality() -> None:
    t1 = StudioTrack("Один в каное", "Човен", "Один в каное", 275, "INDIE")
    t2 = StudioTrack("Один в каное", "Човен", "Один в каное", 275, "INDIE")
    live = LiveTrack("Один в каное", "Човен", "Один в каное", 275, "INDIE")

    assert t1 == t2
    assert t1 != live
    assert (t1 == "Човен") is False
    assert str(t1) == "Один в каное - Човен (275 с)"
    assert "StudioTrack(" in repr(t1)
    assert "LiveTrack(" in repr(live)


def test_subtypes_and_duck_typed_protocol() -> None:
    studio = StudioTrack("Гурт", "Студійна", "Альбом", 200, "ROCK")
    live = LiveTrack("Гурт", "Концертна", "Альбом", 200, "ROCK", venue="Львівська опера")

    assert studio.playlist_score() == 110.0
    assert live.playlist_score() == 155.0
    assert studio.playlist_score() != live.playlist_score()

    best = best_scored_track([studio, live])
    assert best == live
    assert best_scored_track([]) is None


def test_loader_parses_subtypes_and_reports_row_errors(tmp_path: Path) -> None:
    csv_file = tmp_path / "tracks.csv"
    csv_file.write_text(
        "performer,title,album,duration,genre,kind,extra\n"
        'Океан Ельзи,"Там, де нас нема","Там, де нас нема",269,ROCK,STUDIO,Студія\n'
        "Один в каное,Човен,Один в каное,275,INDIE,LIVE,Оперний\n",
        encoding="utf-8",
    )

    tracks = load_tracks(csv_file)
    assert len(tracks) == 2
    assert isinstance(tracks[0], StudioTrack)
    assert isinstance(tracks[1], LiveTrack)

    bad_csv = tmp_path / "bad.csv"
    bad_csv.write_text(
        "performer,title,album,duration,genre\nГурт,Пісня,Альбом,-5,ROCK\n",
        encoding="utf-8",
    )
    with pytest.raises(DataError, match="Рядок 2"):
        load_tracks(bad_csv)


def test_cli_and_report_compatibility(tmp_path: Path) -> None:
    input_csv = Path("data/input.csv")
    out_report = tmp_path / "summary.txt"

    assert main([str(input_csv), str(out_report)]) == 0
    new_text = out_report.read_text(encoding="utf-8")

    old_report = Path("reports/lab06_summary.txt")
    if old_report.exists():
        assert new_text == old_report.read_text(encoding="utf-8")
    else:
        assert "Звіт по фонотеці (Варіант 13)" in new_text

    assert main([str(tmp_path / "missing.csv"), str(out_report)]) == 2

    empty_summary = make_summary([])
    empty_file = tmp_path / "empty.txt"
    write_report(empty_file, format_report(empty_summary))
    assert "Найдовший трек: відсутній" in empty_file.read_text(encoding="utf-8")
