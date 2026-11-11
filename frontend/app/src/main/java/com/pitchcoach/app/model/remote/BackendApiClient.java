package com.pitchcoach.app.model.remote;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.local.PitchCoachDbHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Giao tiếp trực tiếp từ Android Frontend (10.0.2.2:8080 trên máy ảo) tới Spring Boot Backend (/api/v1/...),
 * tự động đính kèm Firebase ID Token (Authorization: Bearer <token>).
 */
public class BackendApiClient {

    private static final String TAG = "BackendApiClient";
    // 10.0.2.2 là địa chỉ trỏ từ máy ảo Android Emulator về localhost:8080 của máy tính chạy Spring Boot
    public static final String BASE_URL = "http://10.0.2.2:8080/api/v1";

    private static BackendApiClient instance;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface PresentationsCallback {
        void onResult(List<PresentationItem> items);
    }

    private BackendApiClient() {
    }

    public static synchronized BackendApiClient getInstance() {
        if (instance == null) {
            instance = new BackendApiClient();
        }
        return instance;
    }

    public void syncUserWithBackend(String uid, String displayName, String avatarUrl) {
        withAuthToken(token -> executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("displayName", displayName != null ? displayName : "Minh Khoa");
                if (avatarUrl != null) {
                    body.put("avatarUrl", avatarUrl);
                }
                request("POST", "/auth/sync-user", token, uid, body.toString());
                Log.d(TAG, "Synced user with Spring Boot backend: " + uid);
            } catch (Exception e) {
                Log.d(TAG, "Spring Boot backend offline, using Firebase/SQLite fallback: " + e.getMessage());
            }
        }));
    }

    public void updateSettingsOnBackend(boolean reminderEnabled, String reminderTime) {
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        withAuthToken(token -> executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("reminderEnabled", reminderEnabled);
                if (reminderTime != null) {
                    body.put("reminderTime", reminderTime);
                }
                request("PATCH", "/users/me/settings", token, uid, body.toString());
            } catch (Exception e) {
                Log.d(TAG, "Backend updateSettings fallback: " + e.getMessage());
            }
        }));
    }

    public void createPresentationOnBackend(PresentationItem item, int durationMin,
                                            String rawOutline, Runnable onSynced) {
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        withAuthToken(token -> executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("id", item.id);
                body.put("userId", uid);
                body.put("title", item.title);
                body.put("audienceType", item.audience);
                body.put("targetDurationMin", durationMin);
                body.put("deadlineDisplay", item.deadline);
                body.put("countdownDisplay", item.countdown);
                body.put("rawOutline", rawOutline != null ? rawOutline : "");
                body.put("practiceCount", item.practices);
                body.put("scoreDisplay", item.score);
                body.put("isPast", item.past);
                body.put("isPracticed", item.practiced);
                body.put("createdOrder", item.createdOrder);

                request("POST", "/presentations", token, uid, body.toString());
                if (onSynced != null) {
                    mainHandler.post(onSynced);
                }
            } catch (Exception e) {
                Log.d(TAG, "Backend createPresentation fallback: " + e.getMessage());
            }
        }));
    }

    public void deletePresentationOnBackend(String presentationId) {
        if (presentationId == null) {
            return;
        }
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        withAuthToken(token -> executor.execute(() -> {
            try {
                request("DELETE", "/presentations/" + presentationId, token, uid, null);
            } catch (Exception e) {
                Log.d(TAG, "Backend deletePresentation fallback: " + e.getMessage());
            }
        }));
    }

    public void fetchPresentationsFromBackend(PresentationsCallback callback) {
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        withAuthToken(token -> executor.execute(() -> {
            try {
                String json = request("GET", "/presentations", token, uid, null);
                JSONObject root = new JSONObject(json);
                JSONArray arr = root.optJSONArray("items");
                List<PresentationItem> list = new ArrayList<>();
                if (arr != null) {
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        list.add(new PresentationItem(
                                obj.optString("id"),
                                obj.optString("title"),
                                obj.optString("audienceType", ""),
                                obj.optString("deadlineDisplay", ""),
                                obj.optString("countdownDisplay", ""),
                                obj.optInt("practiceCount", 0),
                                obj.optString("scoreDisplay", "—"),
                                obj.optBoolean("isPast", false),
                                obj.optBoolean("isPracticed", false),
                                obj.optInt("createdOrder", 0)
                        ));
                    }
                }
                if (callback != null && !list.isEmpty()) {
                    mainHandler.post(() -> callback.onResult(list));
                }
            } catch (Exception e) {
                Log.d(TAG, "Backend fetchPresentations fallback: " + e.getMessage());
            }
        }));
    }

    private interface TokenConsumer {
        void onToken(String idToken);
    }

    private void withAuthToken(TokenConsumer consumer) {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                user.getIdToken(false)
                        .addOnSuccessListener(result -> consumer.onToken(result.getToken()))
                        .addOnFailureListener(e -> consumer.onToken(null));
                return;
            }
        } catch (Exception ignored) {
        }
        consumer.onToken(null);
    }

    private String request(String method, String path, String bearerToken,
                           String fallbackUid, String jsonBody) throws Exception {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(6000);
        conn.setRequestProperty("Accept", "application/json");
        if (bearerToken != null && !bearerToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + bearerToken);
        } else {
            conn.setRequestProperty("X-User-Id",
                    fallbackUid != null ? fallbackUid : PitchCoachDbHelper.DEFAULT_USER_ID);
        }

        if (jsonBody != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
        }

        int code = conn.getResponseCode();
        InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        if (is != null) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
        }
                if (code < 200 || code >= 300) {
            throw new IllegalStateException("HTTP " + code + ": " + sb);
        }
        return sb.toString();
    }

    public void analyzeRecordingOnBackend(android.content.Context context,
                                          java.io.File audioFile,
                                          int durationSeconds) {
        android.content.Context appContext = context.getApplicationContext();
        String uid = FirebaseDataSource.getInstance().getCurrentUid();
        withAuthToken(token -> executor.execute(() -> {
            try {
                String boundary = "----PitchCoachBoundary" + System.currentTimeMillis();
                URL url = new URL(BASE_URL + "/sessions/analyze");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(30000);
                conn.setDoOutput(true);
                if (token != null && !token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                } else {
                    conn.setRequestProperty("X-User-Id",
                            uid != null ? uid : PitchCoachDbHelper.DEFAULT_USER_ID);
                }
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                try (OutputStream os = conn.getOutputStream()) {
                    writeFormField(os, boundary, "mode", "PRESENTATION");
                    writeFormField(os, boundary, "durationSeconds", String.valueOf(durationSeconds));
                    if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                        writeFilePart(os, boundary, "audioFile", audioFile);
                    }
                    os.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    JSONObject root = new JSONObject(sb.toString());
                    float overallScore = (float) root.optDouble("overallScore", 8.0);
                    int scoreContent = root.optInt("scoreContent", 8);
                    int scoreStructure = root.optInt("scoreStructure", 8);
                    int scoreClarity = root.optInt("scoreClarity", 9);
                    int scoreDelivery = root.optInt("scoreDelivery", 7);
                    float speedWpm = (float) root.optDouble("speakingSpeedWpm", 140.0);
                    int fillerCount = root.optInt("fillerWordCount", 5);

                    List<com.pitchcoach.app.model.TranscriptLine> lines = new ArrayList<>();
                    JSONArray trArr = root.optJSONArray("transcriptLines");
                    if (trArr != null) {
                        for (int i = 0; i < trArr.length(); i++) {
                            JSONObject o = trArr.getJSONObject(i);
                            String hl = o.isNull("highlightFiller") ? null : o.optString("highlightFiller", null);
                            lines.add(new com.pitchcoach.app.model.TranscriptLine(
                                    o.optString("sentence", ""), hl));
                        }
                    }

                    List<com.pitchcoach.app.model.Improvement> imps = new ArrayList<>();
                    JSONArray impArr = root.optJSONArray("improvements");
                    if (impArr != null) {
                        for (int i = 0; i < impArr.length(); i++) {
                            JSONObject o = impArr.getJSONObject(i);
                            imps.add(new com.pitchcoach.app.model.Improvement(
                                    o.optString("title", ""), o.optString("evidence", "")));
                        }
                    }

                    List<com.pitchcoach.app.model.Panelist> panelists = new ArrayList<>();
                    JSONArray pArr = root.optJSONArray("panelQa");
                    if (pArr != null) {
                        for (int i = 0; i < pArr.length(); i++) {
                            JSONObject o = pArr.getJSONObject(i);
                            panelists.add(new com.pitchcoach.app.model.Panelist(
                                    o.optString("panelistName", "Giám khảo AI"),
                                    o.optString("panelistInitials", "AI"),
                                    o.optString("questionText", ""),
                                    o.optInt("panelistTone", i % 3)
                            ));
                        }
                    }

                    PitchCoachDbHelper.getInstance(appContext).saveLatestAnalysisToDefaultSession(
                            overallScore, scoreContent, scoreStructure, scoreClarity, scoreDelivery,
                            speedWpm, fillerCount, lines, imps, panelists
                    );
                }
            } catch (Exception e) {
                Log.d(TAG, "Backend analyzeSession fallback: " + e.getMessage());
            }
        }));
    }

    private void writeFormField(OutputStream os, String boundary, String name, String value)
            throws Exception {
        String part = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n"
                + value + "\r\n";
        os.write(part.getBytes(StandardCharsets.UTF_8));
    }

    private void writeFilePart(OutputStream os, String boundary, String fieldName, java.io.File file)
            throws Exception {
        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n"
                + "Content-Type: audio/mp4\r\n\r\n";
        os.write(header.getBytes(StandardCharsets.UTF_8));
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = fis.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        }
        os.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
}
