package ua.lpnu.kzp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StorageExportTest {

    @TempDir
    Path tempDir;

    static final class PlaylistNote {
        @CsvColumn("тег")
        private final String tag;

        @CsvColumn("рейтинг")
        private final int rating;

        PlaylistNote(String tag, int rating) {
            this.tag = tag;
            this.rating = rating;
        }
    }

    @Test
    void repositoryStoresFindsAndReturnsImmutableSnapshot() {
        Repository<TrackRecord> repo = new Repository<>();
        TrackRecord t1 = new TrackRecord("Океан Ельзи", "Квітка", "Модель", 180, TrackGenre.ROCK);
        TrackRecord t2 = new TrackRecord("Один в каное", "Човен", "Один в каное", 275, TrackGenre.INDIE);

        repo.add(t1);
        repo.add(t2);

        List<TrackRecord> snapshot = repo.all();
        assertEquals(2, snapshot.size());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(t1));
        assertThrows(NullPointerException.class, () -> repo.add(null));

        List<TrackRecord> indie = repo.find(r -> r.getGenre() == TrackGenre.INDIE);
        assertEquals(List.of(t2), indie);
    }

    @Test
    void csvColumnAnnotationIsPresentAtRuntime() throws NoSuchFieldException {
        Field performerField = TrackRecord.class.getDeclaredField("performer");
        CsvColumn annotation = performerField.getAnnotation(CsvColumn.class);

        assertNotNull(annotation);
        assertEquals("виконавець", annotation.value());
    }

    @Test
    void exportAndRoundTripPreservesCommasQuotesAndMultiline() throws Exception {
        List<TrackRecord> original = List.of(
                new TrackRecord("Океан Ельзи", "Там, де нас нема", "Там, де нас нема", 269, TrackGenre.ROCK),
                new TrackRecord("Один в каное", "Човен \"Акустика\"", "Концерт\nЛьвів", 275, TrackGenre.ACOUSTIC),
                new TrackRecord("Бумбокс", "Вахтерам", "Family Business", 238, TrackGenre.POP)
        );

        Path csvFile = tempDir.resolve("tracks.csv");
        CsvExporter.write(csvFile, TrackRecord.class, original);

        String csv = Files.readString(csvFile, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csv);

        assertEquals(4, rows.size());
        assertEquals(List.of("альбом", "тривалість", "жанр", "виконавець", "назва"), rows.get(0));
        assertTrue(csv.contains("\"Там, де нас нема\""));
        assertTrue(csv.contains("\"Човен \"\"Акустика\"\"\""));

        List<TrackRecord> restored = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            restored.add(TrackRecord.fromFields(rows.get(i)));
        }
        assertEquals(original, restored);
    }

    @Test
    void exporterWorksWithArbitraryAnnotatedClass() throws Exception {
        Path csvFile = tempDir.resolve("notes.csv");
        List<PlaylistNote> notes = List.of(new PlaylistNote("Улюблене, топ", 5));

        CsvExporter.write(csvFile, PlaylistNote.class, notes);
        List<List<String>> rows = CsvParser.parseDocument(Files.readString(csvFile, StandardCharsets.UTF_8));

        assertEquals(2, rows.size());
        assertEquals(List.of("рейтинг", "тег"), rows.get(0));
        assertEquals(List.of("5", "Улюблене, топ"), rows.get(1));
    }

    @Test
    void emptyRepositoryWritesHeaderOnlyAndRestoresEmptyList() throws Exception {
        Path csvFile = tempDir.resolve("empty.csv");
        CsvExporter.write(csvFile, TrackRecord.class, List.of());

        String csv = Files.readString(csvFile, StandardCharsets.UTF_8);
        List<List<String>> rows = CsvParser.parseDocument(csv);

        assertEquals(1, rows.size());
        assertEquals(List.of("альбом", "тривалість", "жанр", "виконавець", "назва"), rows.get(0));
    }

    @Test
    void invalidCsvThrowsDataStorageExceptionWithCause() {
        assertThrows(DataStorageException.class, () -> TrackRecord.fromCsv("Альбом,200,ROCK"));

        DataStorageException badNumber = assertThrows(
                DataStorageException.class,
                () -> TrackRecord.fromCsv("Альбом,не_число,ROCK,Гурт,Пісня")
        );
        assertTrue(badNumber.getCause() instanceof NumberFormatException);

        DataStorageException badEnum = assertThrows(
                DataStorageException.class,
                () -> TrackRecord.fromCsv("Альбом,200,UNKNOWN_GENRE,Гурт,Пісня")
        );
        assertTrue(badEnum.getCause() instanceof IllegalArgumentException);

        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("\"незакриті лапки"));
    }

    @Test
    void fileWriteErrorWrapsIOExceptionAsCause() throws Exception {
        Path directoryInsteadOfFile = tempDir.resolve("folder");
        Files.createDirectory(directoryInsteadOfFile);

        DataStorageException exception = assertThrows(
                DataStorageException.class,
                () -> CsvExporter.write(directoryInsteadOfFile, TrackRecord.class, List.of())
        );
        assertTrue(exception.getCause() instanceof IOException);
    }
}