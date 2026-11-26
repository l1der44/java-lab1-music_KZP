package ua.lpnu.kzp;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));

        MusicManager manager = new MusicManager();
        manager.loadFromCsv("data/input.csv");
        manager.printReport();
    
        runLab5Demo();
    }

    private static void runLab5Demo() {
        System.out.println("----------------------------------------");
        System.out.println("ЛР 5: Repository<T>, рефлексивний CSV-експорт та Round-Trip:");
        Repository<TrackRecord> repository = new Repository<>();
        repository.add(new TrackRecord("Океан Ельзи", "Там, де нас нема", "Там, де нас нема", 269, TrackGenre.ROCK));
        repository.add(new TrackRecord("Один в каное", "Човен", "Один в каное", 275, TrackGenre.INDIE));
        repository.add(new TrackRecord("Бумбокс", "Вахтерам", "Family Business", 238, TrackGenre.POP));
        repository.add(new TrackRecord("Скрябін", "Мовчати", "Натура", 238, TrackGenre.POP));
        repository.add(new TrackRecord("Антитіла", "TDME", "Сонце", 210, TrackGenre.ROCK));
        repository.add(new TrackRecord("Один в каное", "Човен \"Акустика\"", "Live", 275, TrackGenre.ACOUSTIC));

        Path exportPath = Path.of("target", "tracks-export.csv");
        try {
            CsvExporter.write(exportPath, TrackRecord.class, repository.all());
            String csvText = Files.readString(exportPath, StandardCharsets.UTF_8);
            List<List<String>> rows = CsvParser.parseDocument(csvText);

            List<TrackRecord> restored = new ArrayList<>();
            for (int i = 1; i < rows.size(); i++) {
                restored.add(TrackRecord.fromFields(rows.get(i)));
            }

            System.out.println("Записів у Repository<TrackRecord>: " + repository.all().size());
            System.out.println("Пошук за предикатом (жанр ROCK): " + repository.find(r -> r.getGenre() == TrackGenre.ROCK).size() + " шт.");
            System.out.println("CSV-заголовок: " + String.join(",", rows.get(0)));
            System.out.println("Приклад екранованого рядка: " + csvText.lines().skip(1).findFirst().orElse(""));
                        System.out.println("Відновлено з CSV: " + restored.size() + " об'єктів");

            runPythonModule("kzp_lab06.cli", exportPath.toString(), "reports/lab06_summary.txt");
            runPythonModule("kzp_lab07.cli", exportPath.toString(), "reports/lab07_summary.txt");
            System.out.println("\n--- Звіт ЛР 6 (reports/lab06_summary.txt) ---");
            System.out.println(Files.readString(Path.of("reports", "lab06_summary.txt"), StandardCharsets.UTF_8));
            System.out.println("--- Звіт ЛР 7 (reports/lab07_summary.txt) ---");
            System.out.println(Files.readString(Path.of("reports", "lab07_summary.txt"), StandardCharsets.UTF_8));
            System.out.println("Round-trip рівність (original.equals(restored)): " + repository.all().equals(restored));
        } catch (Exception e) {
            System.err.println("Помилка CSV-експорту/імпорту: " + e.getMessage());
        }
    }

    private static void runPythonModule(String moduleName, String inputCsv, String outputReport)
            throws Exception {
        String pythonExec = resolvePythonExecutable();
        ProcessBuilder pb = new ProcessBuilder(pythonExec, "-m", moduleName, inputCsv, outputReport);
        pb.environment().put("PYTHONPATH", "src");
        pb.environment().put("PYTHONUTF8", "1");
        pb.inheritIO();
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new IllegalStateException("Модуль " + moduleName + " завершився з кодом " + exitCode);
        }
    }

    private static String resolvePythonExecutable() {
        Path winVenv = Path.of(".venv", "Scripts", "python.exe");
        if (Files.exists(winVenv)) {
            return winVenv.toString();
        }
        Path unixVenv = Path.of(".venv", "bin", "python");
        if (Files.exists(unixVenv)) {
            return unixVenv.toString();
        }
        return "python";
    }
}
