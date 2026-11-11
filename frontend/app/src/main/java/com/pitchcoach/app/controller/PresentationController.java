package com.pitchcoach.app.controller;

import android.content.Context;

import com.pitchcoach.app.model.HistoryEntry;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.repository.PresentationRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PresentationController {

    private final PresentationRepository repository;

    public PresentationController() {
        this.repository = PresentationRepository.getInstance();
    }

    public List<PresentationItem> getPresentations(Context context) {
        return repository.getPresentations(context);
    }

    public void syncFromFirebase(Context context, PresentationRepository.SyncListener listener) {
        repository.syncFromFirebase(context, listener);
    }

    public PresentationItem createPresentation(Context context, String title, String audience,
                                               int durationMin, String deadlineDisplay,
                                               String rawOutline) {
        return repository.createPresentation(
                context, title, audience, durationMin, deadlineDisplay, rawOutline);
    }

    public List<PresentationItem> deletePresentation(Context context, String title) {
        return repository.deleteByTitle(context, title);
    }

    public int countUpcoming(List<PresentationItem> items) {
        if (items == null) {
            return 0;
        }
        int upcoming = 0;
        for (PresentationItem item : items) {
            if (!item.past) {
                upcoming++;
            }
        }
        return upcoming;
    }

    public int countPast(List<PresentationItem> items) {
        if (items == null) {
            return 0;
        }
        int past = 0;
        for (PresentationItem item : items) {
            if (item.past) {
                past++;
            }
        }
        return past;
    }

    public List<PresentationItem> filterAndSort(List<PresentationItem> items, String query,
                                                String filter, int sortIndex) {
        if (items == null) {
            return new ArrayList<>();
        }
        String normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<PresentationItem> visible = new ArrayList<>();
        for (PresentationItem item : items) {
            boolean matchesQuery = item.title.toLowerCase(Locale.ROOT).contains(normalized);
            boolean matchesFilter = "all".equals(filter)
                    || ("upcoming".equals(filter) && !item.past)
                    || ("past".equals(filter) && item.past);
            if (matchesQuery && matchesFilter) {
                visible.add(item);
            }
        }
        if (sortIndex == 2) {
            Collections.sort(visible, Comparator.comparingDouble(
                    item -> -parseScore(item.score)));
        } else if (sortIndex == 1) {
            Collections.sort(visible, Comparator.comparingInt(item -> item.createdOrder));
        }
        return visible;
    }

    private double parseScore(String score) {
        try {
            return Double.parseDouble(score);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    public String formatDate(int dayOfMonth, int monthZeroBased, int year) {
        return String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, monthZeroBased + 1, year);
    }

    public List<String> getPredictedQuestions(Context context) {
        return repository.getPredictedQuestions(context);
    }

    public List<HistoryEntry> getDetailHistory(Context context) {
        return repository.getDetailHistory(context);
    }
}
