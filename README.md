# Кросплатформні засоби програмування — Варіант 13 (Музична фонотека)

Наскрізний кросплатформний проєкт (**Java 21** + **Python 3.11+**, `ua.lpnu.kzp` / `kzp_lab06` / `kzp_lab07`), виконаний у межах лабораторних робіт № 1–7 на кафедрі ЕОМ НУ «Львівська політехніка».

## Еволюція проєкту за лабораторними роботами

### Лабораторна робота № 1 — Середовище розробки та базовий консольний застосунок
- Налаштовано кросплатформну інфраструктуру проєкту: **JDK 21**, **Maven** (`pom.xml`), **Git / GitHub** та середовище **VS Code**.
- Реалізовано базову точку входу (`Main.java`) із гарантованим виведенням кирилиці у кодуванні **UTF-8**.

### Лабораторна робота № 2 — Об'єктно-орієнтоване програмування та поліморфізм
- Спроєктовано ієрархію предметної області «Музична фонотека»:
  - базовий клас `Track` (назва, виконавець, альбом, тривалість у секундах);
  - підкласи `StudioTrack` (студійний запис із бітрейтом) та `LiveTrack` (концертний запис із коефіцієнтом якості), що поліморфно обчислюють розмір файлу (`calculateFileSizeMb()`).
- Додано переліки `enum`: `TrackKind` (`STUDIO`, `LIVE`) та `DurationGenre`.
- Перевизначено `toString()`, `equals()` та `hashCode()`.

### Лабораторна робота № 3 — Колекції, файловий ввід/вивід і юніт-тестування
- Реалізовано менеджер фонотеки `MusicManager`, який завантажує дані з `data/input.csv` у кодуванні UTF-8.
- Застосовано колекції `List<Track>` та `Set<Track>` (`HashSet`) для визначення кількості унікальних композицій.
- Додано валідацію вхідних рядків із фіксацією помилок формату та від'ємної тривалості без аварійного завершення програми.
- Написано модульні тести JUnit 5 у `TrackTest.java`.

### Лабораторна робота № 4 — Функціональне програмування (Stream API та Optional)
- Створено клас аналітичної звітності `MusicReport` із використанням декларативних конвеєрів **Java Stream API**:
  1. `filter` — відбір треків, довших за вказаний поріг (> 180 с);
  2. `map` — отримання списку назв усіх композицій;
  3. `groupingBy` + `summingInt` — сумарна тривалість треків за кожним виконавцем;
  4. `summarizingInt` — зведена статистика (`IntSummaryStatistics`: сума, середнє, мінімум, максимум);
  5. `sorted` + `limit` — формування рейтингу Топ-5 найдовших треків.
- Реалізовано безпечний пошук треку за назвою без повернення `null` через `Optional<Track>`.
- Покрито аналітику юніт-тестами у `MusicReportTest.java`.

### Лабораторна робота № 5 — Параметризоване сховище, рефлексія та CSV round-trip
- Реалізовано типобезпечне узагальнене сховище `Repository<T>` із методами `add`, `all` (незмінний знімок) та `find(Predicate<? super T>)`.
- Створено власний checked-виняток `DataStorageException` зі збереженням першопричини (`Throwable cause`).
- Оголошено анотацію `@CsvColumn` (`@Retention(RUNTIME)`, `@Target(FIELD)`) та модель запису `TrackRecord` (із жанрами `TrackGenre`).
- Реалізовано універсальний рефлексивний експортер `CsvExporter` і автоматний парсер `CsvParser` із підтримкою екранування ком, подвійних лапок і перенесень рядків у `UTF-8`.
- Доведено цілісність даних тестом `round-trip` (`original.equals(restored)`) у `StorageExportTest.java`.

### Лабораторна робота № 6 — Модульна обробка CSV, аналітика та CLI на Python
- Реалізовано пакет `kzp_lab06` (`Python 3.11+`) з чітким розділенням відповідальності за модулями:
  - `loader.py` — читання CSV-файлів у кодуванні **UTF-8** із підтримкою англомовних (`performer, title, album, duration, genre`) та україномовних заголовків (`виконавець, назва, альбом, тривалість, жанр`), суворою валідацією значень та порожніх полів;
  - `errors.py` — власна ієрархія винятків (`DataError`) для контрольованої обробки некоректних даних;
  - `analysis.py` — фільтрація треків (`> 180 с`), підрахунок сумарної тривалості за виконавцями, визначення найдовшого треку та унікальних жанрів;
  - `reporting.py` та `cli.py` — форматування звіту (`reports/lab06_summary.txt`) та інтерфейс командного рядка (`python -m kzp_lab06.cli <input.csv> <output.txt>`) із кодами повернення `0` (успіх) та `2` (помилка вхідних даних).
- Покрито тестами `pytest` (`test_loader.py`, `test_analysis.py`, `test_reporting_and_cli.py`).

### Лабораторна робота № 7 — ООП-архітектура на Python, пакування та GitHub Actions CI
- Спроєктовано пакет `kzp_lab07` із типізованими моделями предметної області (`models.py`: `@dataclass(frozen=True)`, `PlayableTrack`, `Track`, `StudioTrack`, `LiveTrack`, `LibrarySummary`) та поліморфним розрахунком рейтингу `playlist_score()` залежно від типу запису (`STUDIO` / `LIVE`).
- У `loader.py` реалізовано універсальний парсер, який підтримує стандартні CSV із заголовком (EN/UA) та експортовані з Java файли з роздільником `;` без заголовка (`data/input.csv`) із автоматичною дедуплікацією записів та пропуском пошкоджених рядків.
- Налаштовано збірку пакета у формат **wheel** (`pyproject.toml`, `setuptools`, `build`), статичний аналіз і форматування коду через **Ruff** (`E, F, W, B, UP`), а також тестування `test_lab07_oop.py` (разом **19 юніт-тестів `pytest`**).
- Реалізовано наскрізну інтеграцію `Main.java` $\rightarrow$ Python CLI через `ProcessBuilder` і автоматизований CI-пайплайн у **GitHub Actions** (`.github/workflows/ci.yml`), що послідовно перевіряє JUnit 5, Ruff, `pytest` та end-to-end запуск `Main.java`.

## Структура проєкту
```text
├── .github/workflows/
│   └── ci.yml                     # CI-пайплайн GitHub Actions (Java JUnit + Ruff + pytest + E2E)
├── data/
│   └── input.csv                  # Вхідні дані фонотеки (UTF-8)
├── reports/
│   ├── lab06_summary.txt          # Еталонний звіт ЛР № 6
│   └── lab07_summary.txt          # Згенерований звіт ЛР № 7
├── src/
│   ├── main/java/ua/lpnu/kzp/
│   │   ├── Main.java              # Демонстрація ЛР 1–5 та інтеграційний запуск Python CLI
│   │   ├── Track.java             # Базовий клас композиції
│   │   ├── StudioTrack.java       # Студійний трек
│   │   ├── LiveTrack.java         # Концертний трек
│   │   ├── TrackKind.java         # Тип запису (enum)
│   │   ├── DurationGenre.java     # Класифікація за тривалістю (enum)
│   │   ├── MusicManager.java      # Завантаження CSV та управління колекцією
│   │   ├── MusicReport.java       # Аналітичні запити Stream API та Optional
│   │   ├── Repository.java        # Параметризоване сховище Repository<T>
│   │   ├── CsvColumn.java         # Анотація метаданих CSV-стовпця
│   │   ├── CsvExporter.java       # Універсальний рефлексивний експортер
│   │   ├── CsvParser.java         # Автоматний CSV-парсер
│   │   ├── DataStorageException.java # Власний виняток із ланцюгом причин
│   │   ├── TrackGenre.java        # Жанри треків (enum)
│   │   └── TrackRecord.java       # Модель запису для CSV round-trip
│   ├── test/java/ua/lpnu/kzp/
│   │   ├── TrackTest.java         # Юніт-тести моделі (ЛР 3)
│   │   ├── MusicReportTest.java   # Юніт-тести Stream API (ЛР 4)
│   │   └── StorageExportTest.java # Юніт-тести сховища та round-trip (ЛР 5)
│   ├── kzp_lab06/                 # Пакет ЛР № 6 (модульна обробка даних на Python)
│   │   ├── __init__.py
│   │   ├── analysis.py
│   │   ├── cli.py
│   │   ├── errors.py
│   │   ├── loader.py
│   │   └── reporting.py
│   └── kzp_lab07/                 # Пакет ЛР № 7 (ООП-модель фонотеки та CLI на Python)
│       ├── __init__.py
│       ├── analysis.py
│       ├── cli.py
│       ├── errors.py
│       ├── loader.py
│       ├── models.py
│       └── reporting.py
├── tests/                         # Тести pytest для ЛР № 6 та ЛР № 7
│   ├── test_analysis.py
│   ├── test_lab07_oop.py
│   ├── test_loader.py
│   └── test_reporting_and_cli.py
├── pom.xml                        # Конфігурація Maven (Java 21, JUnit 5)
├── pyproject.toml                 # Конфігурація Python-пакета, pytest та Ruff
└── REPORT.md                      # Звіт до лабораторної роботи
```

## Збірка та запуск

### Java (Maven + JUnit 5)
```bash
mvn -B test
mvn -B compile exec:java -Dexec.mainClass="ua.lpnu.kzp.Main"
```

### Python (Ruff + pytest + CLI)
```bash
python -m venv .venv
.venv/bin/python -m pip install -e ".[dev]"

# Перевірка стилю коду та запуск 19 юніт-тестів
.venv/bin/ruff check
.venv/bin/ruff format --check
.venv/bin/pytest -v

# Генерація звіту з вхідного CSV
.venv/bin/python -m kzp_lab07.cli data/input.csv reports/lab07_summary.txt
```