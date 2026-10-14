package ua.lpnu.kzp;

public record DurationGenre(int count, int totalDuration, double averageDuration, int maxDuration) {

    public DurationGenre {
        if (count < 0 || totalDuration < 0 || averageDuration < 0 || maxDuration < 0
                || !Double.isFinite(averageDuration)) {
            throw new IllegalArgumentException("Показники не можуть бути від'ємними чи нескінченними");
        }
    }
}