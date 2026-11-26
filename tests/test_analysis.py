import pytest

from kzp_lab06.analysis import (
    duration_by_performer,
    filter_long_tracks,
    find_longest_track,
    make_summary,
    unique_genres,
)


def _sample_records() -> list[dict[str, object]]:
    return [
        {
            "performer": "Океан Ельзи",
            "title": "Квітка",
            "album": "Модель",
            "duration": 180,
            "genre": "ROCK",
        },
        {
            "performer": "Океан Ельзи",
            "title": "Там, де нас нема",
            "album": "Там, де нас нема",
            "duration": 269,
            "genre": "ROCK",
        },
        {
            "performer": "Один в каное",
            "title": "Човен",
            "album": "Один в каное",
            "duration": 275,
            "genre": "INDIE",
        },
        {
            "performer": "Бумбокс",
            "title": "Вахтерам",
            "album": "Family Business",
            "duration": 238,
            "genre": "POP",
        },
    ]


def test_filter_long_tracks_boundary_180() -> None:
    filtered = filter_long_tracks(_sample_records(), min_duration=180)
    assert len(filtered) == 3
    assert all(int(track["duration"]) > 180 for track in filtered)


def test_unique_genres_deduplicates() -> None:
    assert unique_genres(_sample_records()) == {"ROCK", "INDIE", "POP"}


def test_duration_by_performer_sorted() -> None:
    totals = duration_by_performer(_sample_records())
    assert list(totals.keys()) == ["Бумбокс", "Один в каное", "Океан Ельзи"]
    assert totals["Океан Ельзи"] == 449


def test_find_longest_track_and_empty() -> None:
    longest = find_longest_track(_sample_records())
    assert longest is not None
    assert longest["title"] == "Човен"
    assert find_longest_track([]) is None


def test_make_summary_with_args_defaults_and_kwargs() -> None:
    summary = make_summary(
        _sample_records(),
        "duration",
        min_duration=200,
        duration="Сума секунд",
    )
    assert summary["count"] == 3
    assert summary["Сума секунд"] == 782.0

    with pytest.raises(ValueError, match="Невідома метрика"):
        make_summary(_sample_records(), "unknown_field")
