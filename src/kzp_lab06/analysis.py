from collections.abc import Iterable


def filter_long_tracks(
    records: Iterable[dict[str, object]],
    min_duration: int = 180,
) -> list[dict[str, object]]:
    return [record for record in records if int(record["duration"]) > min_duration]


def unique_genres(records: Iterable[dict[str, object]]) -> set[str]:
    return {str(record["genre"]) for record in records}


def duration_by_performer(records: list[dict[str, object]]) -> dict[str, int]:
    performers = {str(record["performer"]) for record in records}
    return {
        performer: sum(
            int(record["duration"]) for record in records if record["performer"] == performer
        )
        for performer in sorted(performers)
    }


def total_duration(records: Iterable[dict[str, object]]) -> int:
    return sum(int(record["duration"]) for record in records)


def find_longest_track(records: list[dict[str, object]]) -> dict[str, object] | None:
    if not records:
        return None
    return max(records, key=lambda record: (int(record["duration"]), str(record["title"])))


def make_summary(
    records: list[dict[str, object]],
    *metrics: str,
    min_duration: int = 180,
    **labels: str,
) -> dict[str, object]:
    selected = filter_long_tracks(records, min_duration=min_duration)
    summary: dict[str, object] = {
        "total_input": len(records),
        "count": len(selected),
        "min_duration": min_duration,
        "genres": unique_genres(selected),
        "duration_by_performer": duration_by_performer(selected),
        "total_duration": total_duration(selected),
        "longest_track": find_longest_track(selected),
    }

    for metric in metrics:
        if records and any(metric not in record for record in records):
            raise ValueError(f"Невідома метрика: {metric}")
        label = labels.get(metric, metric)
        summary[label] = sum(float(record[metric]) for record in selected)

    return summary
