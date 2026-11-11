package com.pitchcoach.app.model;

import java.util.Objects;
import java.util.UUID;

public class PresentationItem {
    public final String id;
    public final String title;
    public final String audience;
    public final String deadline;
    public final String countdown;
    public final int practices;
    public final String score;
    public final boolean past;
    public final boolean practiced;
    public final int createdOrder;

    public PresentationItem(String title, String audience, String deadline, String countdown,
                            int practices, String score, boolean past, boolean practiced,
                            int createdOrder) {
        this("pres_" + UUID.nameUUIDFromBytes(title.getBytes()).toString().substring(0, 8),
                title, audience, deadline, countdown, practices, score, past, practiced,
                createdOrder);
    }

    public PresentationItem(String id, String title, String audience, String deadline,
                            String countdown, int practices, String score, boolean past,
                            boolean practiced, int createdOrder) {
        this.id = id;
        this.title = title;
        this.audience = audience;
        this.deadline = deadline;
        this.countdown = countdown;
        this.practices = practices;
        this.score = score;
        this.past = past;
        this.practiced = practiced;
        this.createdOrder = createdOrder;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PresentationItem)) {
            return false;
        }
        PresentationItem that = (PresentationItem) other;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title);
    }
}
