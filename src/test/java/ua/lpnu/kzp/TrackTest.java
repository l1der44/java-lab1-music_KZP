package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    @Test
    void subtypesCalculateAudioWeightDifferently() {
        Track studio = new StudioTrack("Океан Ельзи", "Човен", "Альбом", 200, "Рок", "Abbey Road");
        Track live = new LiveTrack("Океан Ельзи", "Човен", "Альбом", 200, "Рок", "Оперний");

        assertNotEquals(studio.calculateAudioWeight(), live.calculateAudioWeight());
        assertEquals(70.0, studio.calculateAudioWeight(), 0.001);
        assertEquals(90.0, live.calculateAudioWeight(), 0.001);
    }

    @Test
    void equalsAndHashCodeDeduplicateInHashSet() {
        Set<Track> set = new HashSet<>();
        set.add(new StudioTrack("Океан Ельзи", "Човен", "Альбом", 200, "Рок"));
        set.add(new StudioTrack("Океан Ельзи", "Човен", "Інший Альбом", 300, "Рок"));

        assertEquals(1, set.size());
    }

    @Test
    void constructorRejectsInvalidSpecificValue() {
        assertThrows(IllegalArgumentException.class,
                () -> new StudioTrack("Artist", "Title", "Album", 100, "Genre", "   "));
        assertThrows(IllegalArgumentException.class,
                () -> new LiveTrack("Artist", "Title", "Album", -50, "Genre", "Venue"));
    }

    @Test
    void fromCsvParsesStudioAndLiveTracks() {
        Track studio = Track.fromCsv("Океан Ельзи;Човен;Альбом;200;Рок;STUDIO;Abbey Road");
        Track live = Track.fromCsv("Океан Ельзи;Човен;Альбом;200;Рок;LIVE;Оперний");

        assertEquals(TrackKind.STUDIO, studio.getKind());
        assertEquals(TrackKind.LIVE, live.getKind());
    }
}