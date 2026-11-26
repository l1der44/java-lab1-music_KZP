from pathlib import Path

from kzp_lab06.analysis import make_summary
from kzp_lab06.cli import main
from kzp_lab06.reporting import format_report, write_report


def test_write_and_format_report_utf8(tmp_path: Path) -> None:
    summary = make_summary([])
    text = format_report(summary)
    output_file = tmp_path / "nested" / "summary.txt"

    write_report(output_file, text)
    content = output_file.read_text(encoding="utf-8")

    assert "Звіт по фонотеці (Варіант 13)" in content
    assert "Найдовший трек: відсутній" in content


def test_cli_success_and_error_exit_codes(tmp_path: Path) -> None:
    valid_csv = tmp_path / "valid.csv"
    valid_csv.write_text(
        "performer,title,album,duration,genre\nОдин в каное,Човен,Один в каное,275,INDIE\n",
        encoding="utf-8",
    )
    report_path = tmp_path / "out" / "report.txt"

    exit_ok = main([str(valid_csv), str(report_path)])
    assert exit_ok == 0
    assert report_path.exists()
    assert "Один в каное - Човен (275 с)" in report_path.read_text(encoding="utf-8")

    broken_csv = tmp_path / "broken.csv"
    broken_csv.write_text("wrong,header\n1,2\n", encoding="utf-8")

    exit_err = main([str(broken_csv), str(report_path)])
    assert exit_err == 2
