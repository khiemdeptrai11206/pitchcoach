package com.pitchcoach.app.model;

public class TranscriptLine {
    public final String sentence;
    public final String highlight;

    public TranscriptLine(String sentence, String highlight) {
        this.sentence = sentence;
        this.highlight = highlight;
    }
}
