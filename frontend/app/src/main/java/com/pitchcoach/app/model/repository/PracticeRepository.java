package com.pitchcoach.app.model.repository;

import android.content.Context;

import com.pitchcoach.app.model.Improvement;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.model.TranscriptLine;

import java.util.List;

public class PracticeRepository {

    private static PracticeRepository instance;

    private PracticeRepository() {
    }

    public static synchronized PracticeRepository getInstance() {
        if (instance == null) {
            instance = new PracticeRepository();
        }
        return instance;
    }

    public List<String> getOutline(Context context) {
        return SampleData.createOutline(context);
    }

    public List<TranscriptLine> getTranscript(Context context) {
        return SampleData.createTranscript(context);
    }

    public List<Improvement> getImprovements(Context context) {
        return SampleData.createImprovements(context);
    }

    public List<Panelist> getPanelists(Context context) {
        return SampleData.createPanelists(context);
    }
}
