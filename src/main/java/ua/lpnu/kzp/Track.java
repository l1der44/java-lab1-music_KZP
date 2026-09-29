package ua.lpnu.kzp;

public record Track(
    String artist,
    String title,
    String album,
    int durationInSeconds,
    String genre
) {
    @Override
    public String toString() {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%s - %s [%s] (%d:%02d) | Жанр: %s",
                artist, title, album, minutes, seconds, genre);
    }
}
