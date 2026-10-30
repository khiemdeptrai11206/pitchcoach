package com.pitchcoach.app.model;

public class HistoryEntry {
    public final String number;
    public final String title;
    public final String date;
    public final String score;
    public final boolean amber;

    public HistoryEntry(String number, String title, String date, String score, boolean amber) {
        this.number = number;
        this.title = title;
        this.date = date;
        this.score = score;
        this.amber = amber;
    }
}
