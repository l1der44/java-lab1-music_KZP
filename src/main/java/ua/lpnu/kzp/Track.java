package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

public abstract class Track {
    private final String artist;
    private final String title;
    private final String album;
    private final int durationSeconds;
    private final String genre;
    private final TrackKind kind;

    protected Track(String artist, String title, String album, int durationSeconds, String genre, TrackKind kind) {
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

        this.kind = Objects.requireNonNull(kind, "Категорія треку не може бути null");
        this.durationSeconds = durationSeconds;
    }

    /**
     * Поліморфний метод: обчислює умовний розмір/вагу аудіофайлу у МБ.
     */
    public abstract double calculateAudioWeight();

    public String getArtist() { return artist; }
    public String getTitle() { return title; }
    public String getAlbum() { return album; }
    public int getDurationSeconds() { return durationSeconds; }
    public String getGenre() { return genre; }
    public TrackKind getKind() { return kind; }

    public String getFormattedDuration() {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format(Locale.ROOT, "%d:%02d", minutes, seconds);
    }

    public static Track fromCsv(String line) {
        Objects.requireNonNull(line, "Рядок не може бути null");
        String[] fields = line.split(";", -1);

        if (fields.length < 5) {
            throw new IllegalArgumentException("Очікується щонайменше 5 полів у CSV-рядку");
        }

        try {
            String artist = fields[0].trim();
            String title = fields[1].trim();
            String album = fields[2].trim();
            int duration = Integer.parseInt(fields[3].trim());
            String genre = fields[4].trim();

            if (fields.length >= 6) {
                String kindStr = fields[5].trim().toUpperCase(Locale.ROOT);
                if ("LIVE".equals(kindStr)) {
                    String venue = fields.length >= 7 ? fields[6].trim() : "Концертний зал";
                    return new LiveTrack(artist, title, album, duration, genre, venue);
                }
            }

            String studio = fields.length >= 7 ? fields[6].trim() : "Студія звукозапису";
            return new StudioTrack(artist, title, album, duration, genre, studio);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Числове поле має помилковий формат", e);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        Track track = (Track) other;
        return artist.equals(track.artist) && title.equals(track.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), artist, title);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s - %s [%s] (%s) | %s | Вага: %.2f МБ",
                artist, title, album, getFormattedDuration(), kind.label(), calculateAudioWeight());
    }
}