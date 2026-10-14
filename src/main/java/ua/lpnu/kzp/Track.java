package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

public final class Track {
    private final String artist;
    private final String title;
    private final String album;
    private final int durationSeconds;
    private final String genre;

    public Track(String artist, String title, String album, int durationSeconds, String genre) {
        this.artist = Objects.requireNonNull(artist, "Виконавець не може бути null");
        if (artist.isBlank()) {
            throw new IllegalArgumentException("Виконавець не може бути порожнім");
        }

        this.title = Objects.requireNonNull(title, "Назва треку не може бути null");
        if (title.isBlank()) {
            throw new IllegalArgumentException("Назва треку не може бути порожньою");
        }

        this.album = Objects.requireNonNull(album, "Альбом не може бути null");
        if (album.isBlank()) {
            throw new IllegalArgumentException("Альбом не може бути порожнім");
        }

        if (durationSeconds < 0) {
            throw new IllegalArgumentException("Тривалість має бути не від'ємною");
        }

        this.genre = Objects.requireNonNull(genre, "Жанр не може бути null");
        if (genre.isBlank()) {
            throw new IllegalArgumentException("Жанр не може бути порожнім");
        }

        this.durationSeconds = durationSeconds;
    }

    public static Track fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("Очікується 5 полів у CSV-рядку");
        }
        try {
            return new Track(
                    fields[0].trim(),
                    fields[1].trim(),
                    fields[2].trim(),
                    Integer.parseInt(fields[3].trim()),
                    fields[4].trim()
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Числове поле має помилковий формат", e);
        }
    }

    public String getArtist() {
        return artist;
    }

    public String getTitle() {
        return title;
    }

    public String getAlbum() {
        return album;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getGenre() {
        return genre;
    }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s - %s [%s] (%s) | Жанр: %s",
                artist, title, album, getFormattedDuration(), genre);
    }
}