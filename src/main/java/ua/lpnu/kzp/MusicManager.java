package ua.lpnu.kzp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MusicManager {
    private final List<Track> tracks = new ArrayList<>();
    private int errorCount = 0;

    public void loadFromCsv(String filePath) {
        tracks.clear();
        errorCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";");
                if (parts.length < 5) {
                    errorCount++;
                    continue;
                }

                try {
                    String artist = parts[0].trim();
                    String title = parts[1].trim();
                    String album = parts[2].trim();
                    int duration = Integer.parseInt(parts[3].trim());
                    String genre = parts[4].trim();

                    if (duration <= 0) {
                        errorCount++;
                        continue;
                    }

                    tracks.add(new Track(artist, title, album, duration, genre));
                } catch (NumberFormatException e) {
                    errorCount++;
                }
            }
        } catch (IOException e) {
            System.err.println("Помилка читання файлу: " + e.getMessage());
        }
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getTotalDuration() {
        return tracks.stream().mapToInt(Track::durationInSeconds).sum();
    }

    public double getAverageDuration() {
        return tracks.isEmpty() ? 0 : (double) getTotalDuration() / tracks.size();
    }

    public Track getLongestTrack() {
        return tracks.stream()
                .max((t1, t2) -> Integer.compare(t1.durationInSeconds(), t2.durationInSeconds()))
                .orElse(null);
    }

    public void printReport() {
        System.out.println("=== ЗВІТ ОБРОБКИ МУЗИЧНОЇ БІБЛІОТЕКИ ===");
        System.out.println("Успішно зчитано треків: " + tracks.size());
        System.out.println("Записів із помилками: " + errorCount);
        System.out.println("----------------------------------------");
        
        if (!tracks.isEmpty()) {
            System.out.println("Список треків:");
            tracks.forEach(t -> System.out.println(" - " + t));
            System.out.println("----------------------------------------");
            System.out.println("Загальна тривалість: " + getTotalDuration() + " сек");
            System.out.printf("Середня тривалість: %.2f сек%n", getAverageDuration());
            System.out.println("Найдовший трек: " + getLongestTrack());
        }
    }
}

