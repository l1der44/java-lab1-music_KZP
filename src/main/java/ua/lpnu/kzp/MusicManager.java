package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        return List.copyOf(tracks);
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public DurationGenre getSummary() {
        IntSummaryStatistics stats = MusicReport.durationStatistics(tracks);
        int maxDuration = stats.getCount() == 0 ? 0 : stats.getMax();
        return new DurationGenre(
                (int) stats.getCount(),
                (int) stats.getSum(),
                stats.getAverage(),
                maxDuration
        );
    }

    public Track getLongestTrack() {
        return MusicReport.topTracksByDuration(tracks, 1)
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void printReport() {
        System.out.println("Список треків (Поліморфна обробка):");
        tracks.forEach(t -> System.out.println("- " + t));
        double totalWeight = tracks.stream()
                .mapToDouble(Track::calculateAudioWeight)
                .sum();

        System.out.println("-----------------------------------");
        IntSummaryStatistics stats = MusicReport.durationStatistics(tracks);
        if (stats.getCount() == 0) {
            System.out.println("Фонотека порожня.");
        } else {
            System.out.println("Загальна тривалість: " + stats.getSum() + " сек");
            System.out.printf(Locale.ROOT, "Середня тривалість: %.2f сек%n", stats.getAverage());
            System.out.println("Мін / Макс тривалість: " + stats.getMin() + " / " + stats.getMax() + " сек");
        }
        System.out.printf(Locale.ROOT, "Загальна вага фонотеки: %.2f МБ%n", totalWeight);

        Set<Track> uniqueTracks = new HashSet<>(tracks);
        System.out.println("Унікальних треків у HashSet: " + uniqueTracks.size());

        System.out.println("-----------------------------------");
        System.out.println("Результати запитів Stream API (Варіант 13):");

        List<Track> longTracks = MusicReport.filterLongerThan180(tracks);
        System.out.println("1. Треки > 180 с (" + longTracks.size() + " шт.): "
                + MusicReport.getTrackTitles(longTracks));

        System.out.println("2. Усі назви (map): " + MusicReport.getTrackTitles(tracks));

        Map<String, Integer> byArtist = MusicReport.totalDurationByArtist(tracks);
        String groupedStr = byArtist.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue() + " с")
                .collect(Collectors.joining(", ", "{", "}"));
        System.out.println("3. Сумарна тривалість за виконавцем: " + groupedStr);

        List<Track> top5 = MusicReport.topTracksByDuration(tracks, 5);
        System.out.println("4. Top-5 найдовших треків:");
        top5.forEach(t -> System.out.println("   * " + t.getTitle() + " (" + t.getDurationSeconds() + " с) — " + t.getArtist()));

        Optional<Track> found = MusicReport.findByTitle(tracks, "Човен");
        System.out.println("5. Пошук 'Човен': " + found.map(Track::toString).orElse("Не знайдено"));

        Optional<Track> missing = MusicReport.findByTitle(tracks, "Невідома пісня");
        System.out.println("   Пошук 'Невідома пісня': " + missing.map(Track::toString).orElse("Не знайдено"));

        if (!errors.isEmpty()) {
            System.out.println("-----------------------------------");
            System.out.println("Помилок зчитання: " + errors.size());
            errors.forEach(System.out::println);
        }
    }
}