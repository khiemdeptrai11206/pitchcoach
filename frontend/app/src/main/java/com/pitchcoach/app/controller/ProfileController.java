package com.pitchcoach.app.controller;

import android.content.Context;

import com.pitchcoach.app.model.local.PitchCoachDbHelper;
import com.pitchcoach.app.model.remote.FirebaseDataSource;

import java.util.Locale;

public class ProfileController {

    public String formatReminderTime(int hourOfDay, int minute) {
        return String.format(Locale.US, "%02d:%02d", hourOfDay, minute);
    }

    public String getReminderTime(Context context) {
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        return PitchCoachDbHelper.getInstance(context).getReminderTime(uid);
    }

    public void saveReminderSettings(Context context, boolean enabled, String reminderTime) {
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        PitchCoachDbHelper.getInstance(context).updateReminderSettings(uid, enabled, reminderTime);
        FirebaseDataSource.getInstance().updateReminderInFirestore(enabled, reminderTime);
    }
}
