from collections.abc import Iterable

from .models import LibrarySummary, PlayableTrack, Track


def filter_long_tracks(tracks: Iterable[Track], min_duration: int = 180) -> list[Track]:
    return [track for track in tracks if track.duration > min_duration]


def unique_genres(tracks: Iterable[PlayableTrack]) -> set[str]:
    return {track.genre for track in tracks}


def duration_by_performer(tracks: list[PlayableTrack]) -> dict[str, int]:
    performers = {track.performer for track in tracks}
    return {
        performer: sum(track.duration for track in tracks if track.performer == performer)
        for performer in sorted(performers)
    }


def find_longest_track(tracks: list[Track]) -> Track | None:
    return max(tracks, key=lambda track: (track.duration, track.title), default=None)


def best_scored_track(tracks: Iterable[PlayableTrack]) -> PlayableTrack | None:
    return max(tracks, key=lambda track: track.playlist_score(), default=None)


def make_summary(tracks: list[Track], min_duration: int = 180) -> LibrarySummary:
    selected = filter_long_tracks(tracks, min_duration=min_duration)
    genres = tuple(sorted(unique_genres(selected)))
    by_performer = tuple(duration_by_performer(selected).items())
    total_seconds = sum(track.duration for track in selected)
    avg_score = (
        round(sum(track.playlist_score() for track in selected) / len(selected), 2)
        if selected
        else 0.0
    )

    return LibrarySummary(
        total_input=len(tracks),
        count=len(selected),
        min_duration=min_duration,
        total_duration=total_seconds,
        genres=genres,
        duration_by_performer=by_performer,
        longest_track=find_longest_track(selected),
        best_scored_track=best_scored_track(selected),
        average_score=avg_score,
    )
