package com.pitchcoach.app.controller;

import android.content.Context;

import com.pitchcoach.app.model.Improvement;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.model.TranscriptLine;
import com.pitchcoach.app.model.repository.PracticeRepository;

import java.util.List;

public class PracticeController {

    private final PracticeRepository repository;

    public PracticeController() {
        this.repository = PracticeRepository.getInstance();
    }

    public List<String> getOutline(Context context) {
        return repository.getOutline(context);
    }

    public List<TranscriptLine> getTranscript(Context context) {
        return repository.getTranscript(context);
    }

    public List<Improvement> getImprovements(Context context) {
        return repository.getImprovements(context);
    }

    public List<Panelist> getPanelists(Context context) {
        return repository.getPanelists(context);
    }

    public Panelist getPanelistAt(Context context, int index) {
        List<Panelist> panelists = repository.getPanelists(context);
        if (index >= 0 && index < panelists.size()) {
            return panelists.get(index);
        }
        return panelists.get(0);
    }

    public float getOverallScore() {
        return 8f;
    }

    public float getSpeakingSpeed() {
        return 140f;
    }
}
