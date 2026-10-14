package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MusicManager {
    private final List<Track> tracks = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();

    public void loadFromCsv(String filePath) {
        tracks.clear();
        errors.clear();
        int lineNumber = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    Track track = Track.fromCsv(line);
                    tracks.add(track);
                } catch (IllegalArgumentException exception) {
                    errors.add("Рядок %d: %s".formatted(lineNumber, exception.getMessage()));
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка зчитання файлу: " + e.getMessage());
        }
    }

    public List<Track> getTracks() {
        return new ArrayList<>(tracks);
    }

    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }

    public DurationGenre getSummary() {
        int count = tracks.size();
        int totalDuration = tracks.stream().mapToInt(Track::getDurationSeconds).sum();
        double averageDuration = tracks.stream().mapToInt(Track::getDurationSeconds).average().orElse(0.0);
        int maxDuration = tracks.stream().mapToInt(Track::getDurationSeconds).max().orElse(0);

        return new DurationGenre(count, totalDuration, averageDuration, maxDuration);
    }

    public Track getLongestTrack() {
        return tracks.stream()
                .max(Comparator.comparingInt(Track::getDurationSeconds))
                .orElse(null);
    }

    public void printReport() {
        System.out.println("Список треків:");
        for (Track t : tracks) {
            System.out.println("- " + t);
        }
        System.out.println("-----------------------------------");

        DurationGenre summary = getSummary();
        System.out.println("Загальна тривалість: " + summary.totalDuration() + " сек");
        System.out.printf(Locale.ROOT, "Середня тривалість: %.2f сек%n", summary.averageDuration());

        Track longest = getLongestTrack();
        if (longest != null) {
            System.out.println("Найдовший трек: " + longest);
        }

        if (!errors.isEmpty()) {
            System.out.println("Помилок: " + errors.size());
            errors.forEach(System.out::println);
        }
    }
}