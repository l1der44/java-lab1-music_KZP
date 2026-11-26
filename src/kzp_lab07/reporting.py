from pathlib import Path

from .models import LibrarySummary


def format_report(summary: LibrarySummary) -> str:
    genres_text = ", ".join(summary.genres) if summary.genres else "немає"
    lines = [
        "Звіт по фонотеці (Варіант 13)",
        f"Усього записів у CSV: {summary.total_input}",
        f"Відібрано треків (duration > {summary.min_duration} с): {summary.count}",
        f"Загальна тривалість відібраних: {summary.total_duration} с",
        (
            f"Найдовший трек: {summary.longest_track}"
            if summary.longest_track
            else "Найдовший трек: відсутній"
        ),
        f"Унікальні жанри ({len(summary.genres)}): {genres_text}",
        "Сумарна тривалість за виконавцем:",
    ]
    lines.extend(
        [f"  {p}: {sec} с" for p, sec in summary.duration_by_performer] or ["  немає записів"]
    )
    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8", newline="\n")
