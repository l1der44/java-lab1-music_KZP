package ua.lpnu.kzp;

import java.util.List;
import java.util.Objects;

public final class TrackRecord {
    @CsvColumn("виконавець")
    private final String performer;

    @CsvColumn("назва")
    private final String title;

    @CsvColumn("альбом")
    private final String album;

    @CsvColumn("тривалість")
    private final int durationSeconds;

    @CsvColumn("жанр")
    private final TrackGenre genre;

    public TrackRecord(String performer, String title, String album, int durationSeconds, TrackGenre genre) {
        if (performer == null || performer.isBlank()) {
            throw new IllegalArgumentException("Виконавець не може бути порожнім");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Назва треку не може бути порожньою");
        }
        if (album == null || album.isBlank()) {
            throw new IllegalArgumentException("Назва альбому не може бути порожньою");
        }
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("Тривалість треку має бути додатною");
        }
        if (genre == null) {
            throw new IllegalArgumentException("Жанр треку не може бути null");
        }
        this.performer = performer;
        this.title = title;
        this.album = album;
        this.durationSeconds = durationSeconds;
        this.genre = genre;
    }

    public static TrackRecord fromCsv(String line) throws DataStorageException {
        List<String> fields = CsvParser.parse(line);
        return fromFields(fields);
    }

    public static TrackRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields == null || fields.size() != 5) {
            throw new DataStorageException("Очікується 5 полів CSV, отримано: " + (fields == null ? 0 : fields.size()));
        }
        try {
            String album = fields.get(0);
            int durationSeconds = Integer.parseInt(fields.get(1).trim());
            TrackGenre genre = TrackGenre.valueOf(fields.get(2).trim());
            String performer = fields.get(3);
            String title = fields.get(4);
            return new TrackRecord(performer, title, album, durationSeconds, genre);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException("Помилка розбору значень запису треку з CSV", exception);
        }
    }

    public String getPerformer() {
        return performer;
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

    public TrackGenre getGenre() {
        return genre;
    }

    public String roundTripKey() {
        return performer + " :: " + title;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TrackRecord that)) {
            return false;
        }
        return durationSeconds == that.durationSeconds
                && performer.equals(that.performer)
                && title.equals(that.title)
                && album.equals(that.album)
                && genre == that.genre;
    }

    @Override
    public int hashCode() {
        return Objects.hash(performer, title, album, durationSeconds, genre);
    }

    @Override
    public String toString() {
        return performer + " - " + title + " [" + album + "] (" + durationSeconds + " с, " + genre + ")";
    }
}