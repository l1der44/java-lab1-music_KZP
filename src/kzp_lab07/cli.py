import argparse
import sys
from pathlib import Path

from .analysis import make_summary
from .errors import DataError
from .loader import load_tracks
from .reporting import format_report, write_report


def _print_error(message: str) -> None:
    try:
        sys.stderr.reconfigure(encoding="utf-8")
    except (AttributeError, ValueError):
        pass
    print(f"Помилка даних: {message}", file=sys.stderr)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        prog="kzp-lab07",
        description="Об'єктно-орієнтований аналіз музичної фонотеки (Варіант 13).",
    )
    parser.add_argument("input_csv", type=Path, help="Шлях до вхідного CSV-файлу")
    parser.add_argument("output_report", type=Path, help="Шлях до вихідного файлу звіту")
    args = parser.parse_args(argv)

    try:
        tracks = load_tracks(args.input_csv)
        summary = make_summary(tracks)
        report_text = format_report(summary)
        write_report(args.output_report, report_text)
    except DataError as error:
        _print_error(str(error))
        return 2

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
