package com.pitchcoach.app.model.repository;

import android.content.Context;

import com.pitchcoach.app.model.Improvement;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.model.TranscriptLine;
import com.pitchcoach.app.model.local.PitchCoachDbHelper;

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
        return PitchCoachDbHelper.getInstance(context).getOutlineItems();
    }

    public List<TranscriptLine> getTranscript(Context context) {
        return PitchCoachDbHelper.getInstance(context).getTranscriptLines();
    }

    public List<Improvement> getImprovements(Context context) {
        return PitchCoachDbHelper.getInstance(context).getImprovements();
    }

    public List<Panelist> getPanelists(Context context) {
        return PitchCoachDbHelper.getInstance(context).getPanelists();
    }
}
