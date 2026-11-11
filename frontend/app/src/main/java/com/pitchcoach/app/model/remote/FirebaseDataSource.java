package com.pitchcoach.app.model.remote;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.local.PitchCoachDbHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseDataSource {

    private static final String TAG = "FirebaseDataSource";
    private static FirebaseDataSource instance;

    public interface RemotePresentationsCallback {
        void onLoaded(List<PresentationItem> items);
    }

    public interface UploadAudioCallback {
        void onSuccess(String cloudAudioUrl);
        void onError(String message);
    }

    private FirebaseDataSource() {
    }

    public static synchronized FirebaseDataSource getInstance() {
        if (instance == null) {
            instance = new FirebaseDataSource();
        }
        return instance;
    }

    public String getCurrentUid() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                return user.getUid();
            }
        } catch (Exception e) {
            Log.w(TAG, "FirebaseAuth not initialized: " + e.getMessage());
        }
        return PitchCoachDbHelper.DEFAULT_USER_ID;
    }

    public void syncUserToFirestore(String uid, String email, String displayName, String avatarUrl) {
        try {
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            long now = System.currentTimeMillis();
            Map<String, Object> data = new HashMap<>();
            data.put("id", uid);
            data.put("email", email != null ? email : "");
            data.put("displayName", displayName != null ? displayName : "PitchCoach User");
            data.put("avatarUrl", avatarUrl);
            data.put("updatedAt", now);

            firestore.collection("users")
                    .document(uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> Log.d(TAG, "Synced user to Firestore: " + uid))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to sync user: " + e.getMessage()));
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable: " + e.getMessage());
        }
    }

    public void updateReminderInFirestore(boolean enabled, String reminderTime) {
        try {
            String uid = getCurrentUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put("reminderEnabled", enabled);
            if (reminderTime != null) {
                updates.put("reminderTime", reminderTime);
            }
            updates.put("updatedAt", System.currentTimeMillis());

            firestore.collection("users")
                    .document(uid)
                    .set(updates, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable: " + e.getMessage());
        }
    }

    public void savePresentationToFirestore(PresentationItem item, int durationMin, String rawOutline,
                                            Runnable onSynced) {
        try {
            String uid = getCurrentUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            long now = System.currentTimeMillis();

            Map<String, Object> doc = new HashMap<>();
            doc.put("id", item.id);
            doc.put("userId", uid);
            doc.put("title", item.title);
            doc.put("audienceType", item.audience);
            doc.put("targetDurationMin", durationMin);
            doc.put("deadlineDisplay", item.deadline);
            doc.put("countdownDisplay", item.countdown);
            doc.put("rawOutline", rawOutline);
            doc.put("practiceCount", item.practices);
            doc.put("scoreDisplay", item.score);
            doc.put("isPast", item.past);
            doc.put("isPracticed", item.practiced);
            doc.put("createdOrder", item.createdOrder);
            doc.put("isDeleted", false);
            doc.put("updatedAt", now);

            firestore.collection("users")
                    .document(uid)
                    .collection("presentations")
                    .document(item.id)
                    .set(doc, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Log.d(TAG, "Presentation synced to Firestore: " + item.id);
                        if (onSynced != null) {
                            onSynced.run();
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to sync presentation: " + e.getMessage()));
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable: " + e.getMessage());
        }
    }

    public void softDeletePresentationInFirestore(String presentationId) {
        if (presentationId == null) {
            return;
        }
        try {
            String uid = getCurrentUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Map<String, Object> updates = new HashMap<>();
            updates.put("isDeleted", true);
            updates.put("updatedAt", System.currentTimeMillis());

            firestore.collection("users")
                    .document(uid)
                    .collection("presentations")
                    .document(presentationId)
                    .set(updates, SetOptions.merge());
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable: " + e.getMessage());
        }
    }

    public void fetchPresentationsFromFirestore(RemotePresentationsCallback callback) {
        try {
            String uid = getCurrentUid();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            firestore.collection("users")
                    .document(uid)
                    .collection("presentations")
                    .whereEqualTo("isDeleted", false)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<PresentationItem> remoteItems = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            String id = doc.getString("id");
                            String title = doc.getString("title");
                            String audience = doc.getString("audienceType");
                            String deadline = doc.getString("deadlineDisplay");
                            String countdown = doc.getString("countdownDisplay");
                            Long practices = doc.getLong("practiceCount");
                            String score = doc.getString("scoreDisplay");
                            Boolean isPast = doc.getBoolean("isPast");
                            Boolean isPracticed = doc.getBoolean("isPracticed");
                            Long order = doc.getLong("createdOrder");

                            if (title != null) {
                                remoteItems.add(new PresentationItem(
                                        id != null ? id : doc.getId(),
                                        title,
                                        audience != null ? audience : "",
                                        deadline != null ? deadline : "",
                                        countdown != null ? countdown : "",
                                        practices != null ? practices.intValue() : 0,
                                        score != null ? score : "—",
                                        isPast != null && isPast,
                                        isPracticed != null && isPracticed,
                                        order != null ? order.intValue() : 0
                                ));
                            }
                        }
                        if (callback != null) {
                            callback.onLoaded(remoteItems);
                        }
                    })
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to fetch presentations: " + e.getMessage()));
        } catch (Exception e) {
            Log.w(TAG, "Firestore unavailable: " + e.getMessage());
        }
    }

    public void uploadRecordingToStorage(String sessionId, File localAudioFile,
                                         UploadAudioCallback callback) {
        try {
            String uid = getCurrentUid();
            StorageReference ref = FirebaseStorage.getInstance()
                    .getReference("users/" + uid + "/sessions/" + sessionId + "/pitch.m4a");
            ref.putFile(Uri.fromFile(localAudioFile))
                    .continueWithTask(task -> {
                        if (!task.isSuccessful() && task.getException() != null) {
                            throw task.getException();
                        }
                        return ref.getDownloadUrl();
                    })
                    .addOnSuccessListener(uri -> {
                        if (callback != null) {
                            callback.onSuccess(uri.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (callback != null) {
                            callback.onError(e.getMessage());
                        }
                    });
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
}
