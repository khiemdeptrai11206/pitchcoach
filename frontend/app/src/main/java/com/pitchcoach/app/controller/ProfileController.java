package com.pitchcoach.app.controller;

import java.util.Locale;

public class ProfileController {

    public String formatReminderTime(int hourOfDay, int minute) {
        return String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
    }
}
