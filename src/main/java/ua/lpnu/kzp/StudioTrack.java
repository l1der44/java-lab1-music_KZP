package ua.lpnu.kzp;

import java.util.Objects;

public final class StudioTrack extends Track {
    private final String studioName;

    public StudioTrack(String artist, String title, String album, int durationSeconds, String genre, String studioName) {
        super(artist, title, album, durationSeconds, genre, TrackKind.STUDIO);
        this.studioName = Objects.requireNonNull(studioName, "Назва студії не може бути null");
        if (studioName.isBlank()) {
            throw new IllegalArgumentException("Назва студії не може бути порожньою");
        }
    }

    public StudioTrack(String artist, String title, String album, int durationSeconds, String genre) {
        this(artist, title, album, durationSeconds, genre, "Студія звукозапису");
    }

    public String getStudioName() {
        return studioName;
    }

    @Override
    public double calculateAudioWeight() {
        return getDurationSeconds() * 0.35;
    }
}