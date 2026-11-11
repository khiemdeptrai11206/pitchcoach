package com.pitchcoach.app.model.repository;

import android.content.Context;

import com.pitchcoach.app.model.HistoryEntry;
import com.pitchcoach.app.model.PresentationItem;

import java.util.ArrayList;
import java.util.List;

public class PresentationRepository {

    private static PresentationRepository instance;
    private List<PresentationItem> cachedItems;

    private PresentationRepository() {
    }

    public static synchronized PresentationRepository getInstance() {
        if (instance == null) {
            instance = new PresentationRepository();
        }
        return instance;
    }

    public List<PresentationItem> getPresentations(Context context) {
        if (cachedItems == null) {
            cachedItems = SampleData.createPresentations(context.getApplicationContext());
        }
        return new ArrayList<>(cachedItems);
    }

    public List<PresentationItem> deleteByTitle(Context context, String title) {
        List<PresentationItem> current = getPresentations(context);
        List<PresentationItem> updated = new ArrayList<>();
        for (PresentationItem item : current) {
            if (!item.title.equals(title)) {
                updated.add(item);
            }
        }
        cachedItems = updated;
        return new ArrayList<>(cachedItems);
    }

    public List<String> getPredictedQuestions(Context context) {
        return SampleData.createQuestions(context);
    }

    public List<HistoryEntry> getDetailHistory(Context context) {
        return SampleData.createDetailHistory(context);
    }
}
