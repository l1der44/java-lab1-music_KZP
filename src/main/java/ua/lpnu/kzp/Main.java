package ua.lpnu.kzp;

public class Main {
    public static void main(String[] args) {
        MusicManager manager = new MusicManager();
        manager.loadFromCsv("data/input.csv");
        manager.printReport();
    }
}