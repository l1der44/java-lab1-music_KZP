package ua.lpnu.kzp;

public enum TrackKind {
    STUDIO("студійний"),
    LIVE("концертний");

    private final String label;

    TrackKind(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}