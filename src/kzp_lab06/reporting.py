from pathlib import Path


def format_report(summary: dict[str, object]) -> str:
    lines = [
        "Звіт по фонотеці (Варіант 13)",
        f"Усього записів у CSV: {summary['total_input']}",
        f"Відібрано треків (duration > {summary['min_duration']} с): {summary['count']}",
        f"Загальна тривалість відібраних: {summary['total_duration']} с",
    ]

    longest = summary["longest_track"]
    if isinstance(longest, dict):
        track_info = f"{longest['performer']} - {longest['title']} ({longest['duration']} с)"
        lines.append(f"Найдовший трек: {track_info}")
    else:
        lines.append("Найдовший трек: відсутній")

    genres = sorted(str(genre) for genre in summary["genres"])
    lines.append(f"Унікальні жанри ({len(genres)}): {', '.join(genres) if genres else 'немає'}")

    lines.append("Сумарна тривалість за виконавцем:")
    totals: dict[str, int] = summary["duration_by_performer"]
    if totals:
        for performer, seconds in sorted(totals.items()):
            lines.append(f"  {performer}: {seconds} с")
    else:
        lines.append("  немає записів")

    return "\n".join(lines) + "\n"


def write_report(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="\n") as target:
        target.write(text)
