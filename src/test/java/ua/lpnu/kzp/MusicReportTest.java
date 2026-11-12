package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;

import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MusicReportTest {

    private final List<Track> sampleTracks = List.of(
            new StudioTrack("Океан Ельзи", "Там, де нас нема", "Там, де нас нема", 269, "Рок", "Abbey Road"),
            new LiveTrack("Один в каное", "Човен", "Один в каное", 275, "Інді", "Оперний театр"),
            new StudioTrack("Бумбокс", "Вахтерам", "Family Business", 238, "Хіп-хоп", "Sound Studio"),
            new StudioTrack("Скрябін", "Мовчати", "Натура", 238, "Рок", "Скрябін Студія"),
            new LiveTrack("Антитіла", "TDME", "Сонце", 210, "Поп-рок", "Палац Спорту"),
            new StudioTrack("Океан Ельзи", "Квітка", "Модель", 180, "Рок", "Abbey Road"),
            new LiveTrack("Один в каное", "Човен", "Акустика", 275, "Інді", "Філармонія")
    );

    @Test
    void filtersTracksStrictlyLongerThan180Seconds() {
        List<Track> filtered = MusicReport.filterLongerThan180(sampleTracks);

        assertEquals(6, filtered.size());
        assertFalse(MusicReport.getTrackTitles(filtered).contains("Квітка"));
    }

    @Test
    void mapsTrackTitles() {
        List<String> titles = MusicReport.getTrackTitles(sampleTracks);

        assertEquals(7, titles.size());
        assertEquals("Там, де нас нема", titles.get(0));
        assertEquals("Квітка", titles.get(5));
    }

    @Test
    void groupsTotalDurationByArtist() {
        Map<String, Integer> grouped = MusicReport.totalDurationByArtist(sampleTracks);

        assertEquals(449, grouped.get("Океан Ельзи"));
        assertEquals(550, grouped.get("Один в каное"));
        assertEquals(238, grouped.get("Бумбокс"));
        assertEquals(238, grouped.get("Скрябін"));
        assertEquals(210, grouped.get("Антитіла"));
    }

    @Test
    void summarizesDurationAndMatchesLegacyLoop() {
        IntSummaryStatistics stats = MusicReport.durationStatistics(sampleTracks);

        int legacySum = 0;
        int legacyMax = 0;
        for (Track t : sampleTracks) {
            legacySum += t.getDurationSeconds();
            legacyMax = Math.max(legacyMax, t.getDurationSeconds());
        }

        assertEquals(7, stats.getCount());
        assertEquals(legacySum, stats.getSum());
        assertEquals(legacyMax, stats.getMax());
        assertEquals(180, stats.getMin());
    }

    @Test
    void topTracksOrdersByDurationDescThenTitleAscAndRespectsLimit() {
        List<Track> top5 = MusicReport.topTracksByDuration(sampleTracks, 5);
        List<String> top5Titles = MusicReport.getTrackTitles(top5);

        assertEquals(List.of("Човен", "Човен", "Там, де нас нема", "Вахтерам", "Мовчати"), top5Titles);
        assertEquals(List.of(), MusicReport.topTracksByDuration(sampleTracks, 0));
        assertEquals(7, MusicReport.topTracksByDuration(sampleTracks, 20).size());
    }

    @Test
    void rejectsNegativeLimit() {
        assertThrows(IllegalArgumentException.class,
                () -> MusicReport.topTracksByDuration(sampleTracks, -1));
    }

    @Test
    void findByTitleReturnsFirstMatchOrEmptyOptional() {
        Optional<Track> found = MusicReport.findByTitle(sampleTracks, "Човен");
        assertTrue(found.isPresent());
        assertEquals("Один в каное", found.get().getAlbum());

        Optional<Track> missing = MusicReport.findByTitle(sampleTracks, "Неіснуюча пісня");
        assertTrue(missing.isEmpty());
    }

    @Test
    void doesNotMutateInputListAndHandlesEmptyInput() {
        List<Track> copyBefore = List.copyOf(sampleTracks);
        MusicReport.filterLongerThan180(sampleTracks);
        MusicReport.topTracksByDuration(sampleTracks, 3);
        MusicReport.totalDurationByArtist(sampleTracks);
        assertEquals(copyBefore, sampleTracks);

        assertTrue(MusicReport.filterLongerThan180(List.of()).isEmpty());
        assertEquals(0, MusicReport.durationStatistics(List.of()).getCount());
        assertTrue(MusicReport.findByTitle(List.of(), "Човен").isEmpty());
    }
}