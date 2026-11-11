package com.pitchcoach.app.model.repository;

import android.content.Context;

import com.pitchcoach.app.model.AttemptRow;
import com.pitchcoach.app.model.PracticeSession;

import java.util.List;

public class ProgressRepository {

    private static ProgressRepository instance;

    private ProgressRepository() {
    }

    public static synchronized ProgressRepository getInstance() {
        if (instance == null) {
            instance = new ProgressRepository();
        }
        return instance;
    }

    public float[] getScoreValues() {
        return SampleData.SCORE_VALUES;
    }

    public float[] getFillerValues() {
        return SampleData.FILLER_VALUES;
    }

    public float[] getSpeedValues() {
        return SampleData.SPEED_VALUES;
    }

    public List<PracticeSession> getProgressSessions(Context context) {
        return SampleData.createProgressSessions(context);
    }

    public List<AttemptRow> getAttempts(Context context) {
        return SampleData.createAttempts(context);
    }
}
