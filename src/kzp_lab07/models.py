from dataclasses import dataclass
from typing import Protocol


class PlayableTrack(Protocol):
    """Протокол об'єктів фонотеки, для яких обчислюється рейтинг плейлиста."""

    @property
    def performer(self) -> str: ...

    @property
    def title(self) -> str: ...

    @property
    def duration(self) -> int: ...

    @property
    def genre(self) -> str: ...

    def playlist_score(self) -> float: ...


class Track:
    """Базова сутність музичного треку з інкапсульованим і перевіреним станом."""

    def __init__(self, performer: str, title: str, album: str, duration: int, genre: str) -> None:
        self._performer = self._validate_text(performer, "Виконавець")
        self._title = self._validate_text(title, "Назва треку")
        self._album = self._validate_text(album, "Альбом")
        self.duration = duration
        self.genre = genre

    @staticmethod
    def _validate_text(value: str, field_label: str) -> str:
        if not isinstance(value, str) or not value.strip():
            raise ValueError(f"{field_label} не може бути порожнім")
        return value.strip()

    @property
    def performer(self) -> str:
        return self._performer

    @property
    def title(self) -> str:
        return self._title

    @property
    def album(self) -> str:
        return self._album

    @property
    def duration(self) -> int:
        return self._duration

    @duration.setter
    def duration(self, value: int) -> None:
        if isinstance(value, bool) or not isinstance(value, int) or value <= 0:
            raise ValueError("Тривалість треку має бути цілим числом більшим за 0")
        self._duration = value

    @property
    def genre(self) -> str:
        return self._genre

    @genre.setter
    def genre(self, value: str) -> None:
        self._genre = self._validate_text(value, "Жанр").upper()

    def playlist_score(self) -> float:
        raise NotImplementedError

    def __str__(self) -> str:
        return f"{self.performer} - {self.title} ({self.duration} с)"

    def __repr__(self) -> str:
        return (
            f"{type(self).__name__}(performer={self.performer!r}, title={self.title!r}, "
            f"album={self.album!r}, duration={self.duration!r}, genre={self.genre!r})"
        )

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Track):
            return NotImplemented
        return type(self) is type(other) and self.__dict__ == other.__dict__


class StudioTrack(Track):
    """Студійний запис із назвою студії звукозапису."""

    def __init__(
        self,
        performer: str,
        title: str,
        album: str,
        duration: int,
        genre: str,
        studio: str = "Студія звукозапису",
    ) -> None:
        super().__init__(performer, title, album, duration, genre)
        self.studio = self._validate_text(studio, "Назва студії")

    def playlist_score(self) -> float:
        return round(self.duration * 0.5 + 10.0, 2)

    def __repr__(self) -> str:
        return (
            f"StudioTrack(performer={self.performer!r}, title={self.title!r}, "
            f"album={self.album!r}, duration={self.duration!r}, genre={self.genre!r}, "
            f"studio={self.studio!r})"
        )


class LiveTrack(Track):
    """Концертний запис із назвою концертного майданчика."""

    def __init__(
        self,
        performer: str,
        title: str,
        album: str,
        duration: int,
        genre: str,
        venue: str = "Концертний зал",
    ) -> None:
        super().__init__(performer, title, album, duration, genre)
        self.venue = self._validate_text(venue, "Концертний майданчик")

    def playlist_score(self) -> float:
        return round(self.duration * 0.65 + 25.0, 2)

    def __repr__(self) -> str:
        return (
            f"LiveTrack(performer={self.performer!r}, title={self.title!r}, "
            f"album={self.album!r}, duration={self.duration!r}, genre={self.genre!r}, "
            f"venue={self.venue!r})"
        )


@dataclass(frozen=True)
class LibrarySummary:
    """Незмінний підсумок аналізу музичної фонотеки."""

    total_input: int
    count: int
    min_duration: int
    total_duration: int
    genres: tuple[str, ...]
    duration_by_performer: tuple[tuple[str, int], ...]
    longest_track: Track | None
    best_scored_track: PlayableTrack | None
    average_score: float
