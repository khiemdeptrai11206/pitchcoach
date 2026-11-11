package com.pitchcoach.app.model.repository;

import android.content.Context;

import com.pitchcoach.app.model.HistoryEntry;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.local.PitchCoachDbHelper;
import com.pitchcoach.app.model.remote.FirebaseDataSource;

import java.util.List;

public class PresentationRepository {

    public interface SyncListener {
        void onRemoteUpdated(List<PresentationItem> updatedItems);
    }

    private static PresentationRepository instance;
    private final FirebaseDataSource firebaseDataSource;

    private PresentationRepository() {
        this.firebaseDataSource = FirebaseDataSource.getInstance();
    }

    public static synchronized PresentationRepository getInstance() {
        if (instance == null) {
            instance = new PresentationRepository();
        }
        return instance;
    }

    public List<PresentationItem> getPresentations(Context context) {
        PitchCoachDbHelper db = PitchCoachDbHelper.getInstance(context);
        return db.getAllPresentations();
    }

    public void syncFromFirebase(Context context, SyncListener listener) {
        PitchCoachDbHelper db = PitchCoachDbHelper.getInstance(context);
        String uid = firebaseDataSource.getCurrentUid();
        firebaseDataSource.fetchPresentationsFromFirestore(remoteItems -> {
            if (remoteItems != null && !remoteItems.isEmpty()) {
                for (PresentationItem item : remoteItems) {
                    db.upsertPresentationFromRemote(item, uid);
                }
                if (listener != null) {
                    listener.onRemoteUpdated(db.getAllPresentations());
                }
            }
        });
    }

    public PresentationItem createPresentation(Context context, String title, String audience,
                                               int durationMin, String deadlineDisplay,
                                               String rawOutline) {
        PitchCoachDbHelper db = PitchCoachDbHelper.getInstance(context);
        String uid = firebaseDataSource.getCurrentUid();
        PresentationItem created = db.insertPresentation(
                uid, title, audience, durationMin, deadlineDisplay, rawOutline);

        firebaseDataSource.savePresentationToFirestore(
                created, durationMin, rawOutline,
                () -> db.markPresentationSynced(created.id));
        return created;
    }

    public List<PresentationItem> deleteByTitle(Context context, String title) {
        PitchCoachDbHelper db = PitchCoachDbHelper.getInstance(context);
        String deletedId = db.softDeletePresentationByTitle(title);
        if (deletedId != null) {
            firebaseDataSource.softDeletePresentationInFirestore(deletedId);
        }
        return db.getAllPresentations();
    }

    public List<String> getPredictedQuestions(Context context) {
        return PitchCoachDbHelper.getInstance(context).getPredictedQuestions();
    }

    public List<HistoryEntry> getDetailHistory(Context context) {
        return PitchCoachDbHelper.getInstance(context).getDetailHistory();
    }
}
