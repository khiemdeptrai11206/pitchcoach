package com.pitchcoach.app.model;

public class Panelist {
    public static final int TONE_PURPLE = 0;
    public static final int TONE_ORANGE = 1;
    public static final int TONE_GREEN = 2;

    public final String name;
    public final String initials;
    public final String question;
    public final int tone;

    public Panelist(String name, String initials, String question, int tone) {
        this.name = name;
        this.initials = initials;
        this.question = question;
        this.tone = tone;
    }
}
