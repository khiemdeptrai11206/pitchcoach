package com.pitchcoach.app.model;

public class AttemptRow {
    public final String round;
    public final String date;
    public final String duration;
    public final String score;
    public final String speed;
    public final String filler;

    public AttemptRow(String round, String date, String duration, String score,
                      String speed, String filler) {
        this.round = round;
        this.date = date;
        this.duration = duration;
        this.score = score;
        this.speed = speed;
        this.filler = filler;
    }
}
