package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MusicReport {

    public static final int DURATION_THRESHOLD_SECONDS = 180;

    public static final Comparator<Track> BY_DURATION_DESC_THEN_TITLE =
            Comparator.comparingInt(Track::getDurationSeconds)
                    .reversed()
                    .thenComparing(Track::getTitle);

    private MusicReport() {
    }

    public static List<Track> filterLongerThan180(List<Track> tracks) {
        return streamOf(tracks)
                .filter(track -> track.getDurationSeconds() > DURATION_THRESHOLD_SECONDS)
                .toList();
    }

    public static List<String> getTrackTitles(List<Track> tracks) {
        return streamOf(tracks)
                .map(Track::getTitle)
                .toList();
    }

    public static Map<String, Integer> totalDurationByArtist(List<Track> tracks) {
        return streamOf(tracks)
                .collect(Collectors.groupingBy(
                        Track::getArtist,
                        TreeMap::new,
                        Collectors.summingInt(Track::getDurationSeconds)
                ));
    }

    public static IntSummaryStatistics durationStatistics(List<Track> tracks) {
        return streamOf(tracks)
                .collect(Collectors.summarizingInt(Track::getDurationSeconds));
    }

    public static List<Track> topTracksByDuration(List<Track> tracks, int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Ліміт не може бути від'ємним: " + limit);
        }
        return streamOf(tracks)
                .sorted(BY_DURATION_DESC_THEN_TITLE)
                .limit(limit)
                .toList();
    }

    public static Optional<Track> findByTitle(List<Track> tracks, String title) {
        Objects.requireNonNull(title, "Назва треку для пошуку не може бути null");
        return streamOf(tracks)
                .filter(track -> track.getTitle().equals(title))
                .findFirst();
    }

    private static Stream<Track> streamOf(List<Track> tracks) {
        return Objects.requireNonNull(tracks, "Список треків не може бути null").stream();
    }
}