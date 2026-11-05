package ua.lpnu.kzp;

import java.util.Objects;

public final class LiveTrack extends Track {
    private final String venue;

    public LiveTrack(String artist, String title, String album, int durationSeconds, String genre, String venue) {
        super(artist, title, album, durationSeconds, genre, TrackKind.LIVE);
        this.venue = Objects.requireNonNull(venue, "Майданчик не може бути null");
        if (venue.isBlank()) {
            throw new IllegalArgumentException("Концертний майданчик не може бути порожнім");
        }
    }

    public LiveTrack(String artist, String title, String album, int durationSeconds, String genre) {
        this(artist, title, album, durationSeconds, genre, "Концертний зал");
    }

    public String getVenue() {
        return venue;
    }

    @Override
    public double calculateAudioWeight() {
        return getDurationSeconds() * 0.45;
    }
}