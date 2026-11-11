package com.pitchcoach.app.controller;

import android.content.Context;

import com.pitchcoach.app.model.AttemptRow;
import com.pitchcoach.app.model.PracticeSession;
import com.pitchcoach.app.model.repository.ProgressRepository;

import java.util.List;

public class ProgressController {

    private final ProgressRepository repository;

    public ProgressController() {
        this.repository = ProgressRepository.getInstance();
    }

    public float[] getScoreValues() {
        return repository.getScoreValues();
    }

    public float[] getFillerValues() {
        return repository.getFillerValues();
    }

    public float[] getSpeedValues() {
        return repository.getSpeedValues();
    }

    public int[] getSpeedDisplayValues() {
        return new int[]{165, 150, 140};
    }

    public List<PracticeSession> getProgressSessions(Context context) {
        return repository.getProgressSessions(context);
    }

    public List<AttemptRow> getAttempts(Context context) {
        return repository.getAttempts(context);
    }
}
