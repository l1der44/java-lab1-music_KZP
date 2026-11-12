package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CsvExporter {
    private CsvExporter() {
    }

    /** Записує анотовані поля об'єктів у CSV-файл із явним кодуванням UTF-8. */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records) throws DataStorageException {
        Objects.requireNonNull(path, "Шлях до файлу не може бути null");
        Objects.requireNonNull(type, "Клас записів не може бути null");
        Objects.requireNonNull(records, "Список записів не може бути null");

        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new DataStorageException("Клас " + type.getSimpleName() + " не має полів із @CsvColumn");
        }

        String header = fields.stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));

        StringBuilder csv = new StringBuilder(header)
                .append(System.lineSeparator());

        for (T record : records) {
            if (record == null) {
                throw new DataStorageException("Список містить null-запис");
            }
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                try {
                    values.add(escape(field.get(record)));
                } catch (IllegalAccessException exception) {
                    throw new DataStorageException(
                            "Не вдалося прочитати поле " + field.getName(), exception);
                }
            }
            csv.append(String.join(",", values))
                    .append(System.lineSeparator());
        }

        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(
                    path,
                    csv.toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати CSV-файл: " + path, exception);
        }
    }

    private static <T> List<Field> annotatedFields(Class<T> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            fields.addAll(Arrays.stream(current.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                    .toList());
        }
        fields.sort(Comparator.comparing(Field::getName));
        for (Field field : fields) {
            if (!field.trySetAccessible()) {
                throw new DataStorageException("Немає доступу до поля " + field.getName());
            }
        }
        return List.copyOf(fields);
    }

    static String escape(Object value) {
        String text = Objects.toString(value, "");
        boolean quoted = text.contains(",")
                || text.contains("\"")
                || text.contains("\r")
                || text.contains("\n");
        return quoted ? "\"" + text.replace("\"", "\"\"") + "\"" : text;
    }
}