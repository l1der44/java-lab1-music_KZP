package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackTest {

    @Test
    void constructorCreatesValidTrack() {
        Track track = new Track("Океан Ельзи", "Човен", "Альбом", 200, "Рок");
        assertEquals("Океан Ельзи", track.getArtist());
        assertEquals("Човен", track.getTitle());
        assertEquals("Альбом", track.getAlbum());
        assertEquals(200, track.getDurationSeconds());
        assertEquals("Рок", track.getGenre());
    }

    @Test
    void constructorRejectsNullOrBlankFields() {
        assertThrows(NullPointerException.class,
                () -> new Track(null, "Title", "Album", 100, "Genre"));
        assertThrows(IllegalArgumentException.class,
                () -> new Track("   ", "Title", "Album", 100, "Genre"));
    }

    @Test
    void constructorRejectsNegativeDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new Track("Artist", "Title", "Album", -10, "Genre"));
    }

    @Test
    void fromCsvCreatesValidTrack() {
        Track track = Track.fromCsv("Океан Ельзи;Там, де нас нема;Альбом;269;Рок");
        assertEquals("Океан Ельзи", track.getArtist());
        assertEquals(269, track.getDurationSeconds());
    }

    @Test
    void fromCsvRejectsInvalidFormatAndNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> Track.fromCsv("Artist;Title;Album"));
        assertThrows(IllegalArgumentException.class,
                () -> Track.fromCsv("Artist;Title;Album;not_a_number;Genre"));
    }

    @Test
    void recordUsesValueEquality() {
        DurationGenre first = new DurationGenre(3, 782, 260.67, 275);
        DurationGenre second = new DurationGenre(3, 782, 260.67, 275);
        assertEquals(first, second);
    }
}