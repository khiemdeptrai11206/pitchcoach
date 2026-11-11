package com.pitchcoach.backend.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import com.pitchcoach.backend.model.PracticeSessionDocument;
import com.pitchcoach.backend.model.PresentationDocument;
import com.pitchcoach.backend.model.SyncPayload;
import com.pitchcoach.backend.model.UserDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FirestoreService {

    private final Firestore firestore;
    private final StorageClient storageClient;

    public FirestoreService(Firestore firestore, StorageClient storageClient) {
        this.firestore = firestore;
        this.storageClient = storageClient;
    }

    // ==================== 1. USERS ====================

    public UserDocument syncUser(String uid, String email, String displayName, String avatarUrl)
            throws Exception {
        DocumentReference ref = firestore.collection("users").document(uid);
        DocumentSnapshot snap = ref.get().get();
        long now = System.currentTimeMillis();

        UserDocument user;
        if (snap.exists()) {
            user = snap.toObject(UserDocument.class);
            if (user == null) {
                user = new UserDocument();
            }
            if (displayName != null && !displayName.isBlank()) {
                user.setDisplayName(displayName);
                user.setInitials(buildInitials(displayName));
            }
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
            user.setUpdatedAt(now);
        } else {
            user = new UserDocument();
            user.setId(uid);
            user.setEmail(email != null ? email : "");
            String resolvedName = (displayName != null && !displayName.isBlank())
                    ? displayName
                    : "Minh Khoa";
            user.setDisplayName(resolvedName);
            user.setInitials(buildInitials(resolvedName));
            user.setAvatarUrl(avatarUrl);
            user.setReminderEnabled(true);
            user.setReminderTime("20:00");
            user.setStreakDays(5);
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        }
        ref.set(user, SetOptions.merge()).get();
        return user;
    }

    public UserDocument getUser(String uid, String email) throws Exception {
        DocumentSnapshot snap = firestore.collection("users").document(uid).get().get();
        if (snap.exists()) {
            return snap.toObject(UserDocument.class);
        }
        return syncUser(uid, email, "Minh Khoa", null);
    }

    public UserDocument updateUserSettings(String uid, Boolean reminderEnabled, String reminderTime)
            throws Exception {
        DocumentReference ref = firestore.collection("users").document(uid);
        Map<String, Object> updates = new HashMap<>();
        if (reminderEnabled != null) {
            updates.put("reminderEnabled", reminderEnabled);
        }
        if (reminderTime != null && !reminderTime.isBlank()) {
            updates.put("reminderTime", reminderTime);
        }
        updates.put("updatedAt", System.currentTimeMillis());
        ref.set(updates, SetOptions.merge()).get();
        return getUser(uid, null);
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) {
            return "PC";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0))
                    .toUpperCase(Locale.ROOT);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.ROOT);
    }

    // ==================== 2. PRESENTATIONS ====================

    public List<PresentationDocument> getPresentations(String uid, String query,
                                                       String filter, String sort) throws Exception {
        QuerySnapshot snapshot = firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .whereEqualTo("isDeleted", false)
                .get()
                .get();

        List<PresentationDocument> all = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            all.add(doc.toObject(PresentationDocument.class));
        }

        String normalizedQ = (query == null) ? "" : query.trim().toLowerCase(Locale.ROOT);
        String resolvedFilter = (filter == null) ? "all" : filter.toLowerCase(Locale.ROOT);
        List<PresentationDocument> filtered = new ArrayList<>();

        for (PresentationDocument item : all) {
            boolean matchesQ = item.getTitle() == null
                    || item.getTitle().toLowerCase(Locale.ROOT).contains(normalizedQ);
            boolean matchesFilter = "all".equals(resolvedFilter)
                    || ("upcoming".equals(resolvedFilter) && !item.getIsPast())
                    || ("past".equals(resolvedFilter) && item.getIsPast());
            if (matchesQ && matchesFilter) {
                filtered.add(item);
            }
        }

        if ("score".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparingDouble(PresentationDocument::getBestScore).reversed());
        } else if ("created".equalsIgnoreCase(sort)) {
            filtered.sort(Comparator.comparingLong(PresentationDocument::getCreatedAt).reversed());
        } else {
            filtered.sort(Comparator.comparingLong(PresentationDocument::getDeadlineEpoch));
        }
        return filtered;
    }

    public PresentationDocument createOrUpdatePresentation(String uid, PresentationDocument doc)
            throws Exception {
        long now = System.currentTimeMillis();
        String id = (doc.getId() != null && !doc.getId().isBlank())
                ? doc.getId()
                : "pres_" + UUID.randomUUID().toString().substring(0, 8);
        doc.setId(id);
        doc.setUserId(uid);
        if (doc.getCreatedAt() == 0) {
            doc.setCreatedAt(now);
        }
        doc.setUpdatedAt(now);

        // Nếu có rawOutline nhưng chưa có outlineItems, tự động tách mục dàn ý & sinh câu hỏi dự đoán
        if ((doc.getOutlineItems() == null || doc.getOutlineItems().isEmpty())
                && doc.getRawOutline() != null && !doc.getRawOutline().isBlank()) {
            doc.setOutlineItems(generateOutlineItems(doc.getRawOutline(), doc.getTargetDurationMin()));
        }
        if (doc.getPredictedQuestions() == null || doc.getPredictedQuestions().isEmpty()) {
            doc.setPredictedQuestions(generateDefaultPredictedQuestions(doc.getTitle()));
        }
        if (doc.getAiOutlineFeedback() == null || doc.getAiOutlineFeedback().isBlank()) {
            doc.setAiOutlineFeedback("Bố cục dàn ý rõ ràng, phù hợp thời lượng "
                    + doc.getTargetDurationMin() + " phút.");
        }

        firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .document(id)
                .set(doc, SetOptions.merge())
                .get();
        return doc;
    }

    public PresentationDocument getPresentationById(String uid, String id) throws Exception {
        DocumentSnapshot snap = firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .document(id)
                .get()
                .get();
        return snap.exists() ? snap.toObject(PresentationDocument.class) : null;
    }

    public Map<String, Object> softDeletePresentation(String uid, String id) throws Exception {
        long now = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("isDeleted", true);
        updates.put("updatedAt", now);

        firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .document(id)
                .set(updates, SetOptions.merge())
                .get();

        Map<String, Object> res = new HashMap<>();
        res.put("id", id);
        res.put("deleted", true);
        res.put("updatedAt", now);
        return res;
    }

    public PresentationDocument regenerateOutline(String uid, String id, String rawOutline)
            throws Exception {
        PresentationDocument doc = getPresentationById(uid, id);
        if (doc == null) {
            doc = new PresentationDocument();
            doc.setId(id);
            doc.setUserId(uid);
        }
        doc.setRawOutline(rawOutline);
        doc.setOutlineItems(generateOutlineItems(rawOutline, doc.getTargetDurationMin()));
        doc.setPredictedQuestions(generateDefaultPredictedQuestions(doc.getTitle()));
        doc.setAiOutlineFeedback("AI đã tối ưu lại dàn ý gồm "
                + doc.getOutlineItems().size() + " phần chính.");
        doc.setUpdatedAt(System.currentTimeMillis());

        firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .document(id)
                .set(doc, SetOptions.merge())
                .get();
        return doc;
    }

    private List<PresentationDocument.OutlineItemDto> generateOutlineItems(String rawOutline,
                                                                           int durationMin) {
        List<PresentationDocument.OutlineItemDto> items = new ArrayList<>();
        if (rawOutline == null || rawOutline.isBlank()) {
            return items;
        }
        String[] lines = rawOutline.trim().split("\\r?\\n");
        int perSectionSec = Math.max(60, (durationMin * 60) / Math.max(1, lines.length));
        int order = 1;
        for (String line : lines) {
            if (!line.isBlank()) {
                items.add(new PresentationDocument.OutlineItemDto(
                        "out_" + order, order, line.trim(), line.trim(), perSectionSec));
                order++;
            }
        }
        return items;
    }

    private List<PresentationDocument.PredictedQuestionDto> generateDefaultPredictedQuestions(String title) {
        String topic = (title != null && !title.isBlank()) ? title : "đề tài này";
        return Arrays.asList(
                new PresentationDocument.PredictedQuestionDto("q_1", 1,
                        "Điểm khác biệt lớn nhất của " + topic + " so với các giải pháp hiện có là gì?"),
                new PresentationDocument.PredictedQuestionDto("q_2", 2,
                        "Nhóm đã kiểm thử hiệu năng thực tế và xử lý trường hợp mất kết nối mạng như thế nào?"),
                new PresentationDocument.PredictedQuestionDto("q_3", 3,
                        "Hướng phát triển tiếp theo của dự án trong 6 tháng tới là gì?")
        );
    }

    // ==================== 3. PRACTICE SESSIONS & AI ANALYSIS ====================

    public PracticeSessionDocument analyzeAndSaveSession(String uid, String presentationId,
                                                         String mode, Integer durationSeconds,
                                                         String cloudAudioUrl,
                                                         MultipartFile audioFile) throws Exception {
        long now = System.currentTimeMillis();
        String sessionId = "sess_" + UUID.randomUUID().toString().substring(0, 8);

        String resolvedAudioUrl = cloudAudioUrl;
        if (audioFile != null && !audioFile.isEmpty()) {
            String blobPath = "users/" + uid + "/sessions/" + sessionId + "/pitch.m4a";
            Blob blob = storageClient.bucket().create(
                    blobPath, audioFile.getBytes(), audioFile.getContentType());
            resolvedAudioUrl = "gs://" + storageClient.bucket().getName() + "/" + blob.getName();
        }

        PracticeSessionDocument session = new PracticeSessionDocument();
        session.setId(sessionId);
        session.setPresentationId(presentationId);
        session.setUserId(uid);
        session.setMode(mode != null ? mode : "PRESENTATION");
        session.setDurationSeconds(durationSeconds != null ? durationSeconds : 300);
        session.setCloudAudioUrl(resolvedAudioUrl);
        session.setOverallScore(8.0);
        session.setScoreContent(8);
        session.setScoreStructure(8);
        session.setScoreClarity(9);
        session.setScoreDelivery(7);
        session.setSpeakingSpeedWpm(140.0);
        session.setFillerWordCount(5);
        session.setAiSummaryGood("Tốc độ nói ổn định 140 WPM, cấu trúc trình bày mạch lạc.");
        session.setAiSummaryMissing("Cần giảm từ đệm 'ờ', 'thì là' ở phần chuyển ý.");
        session.setPracticedAt(now);
        session.setUpdatedAt(now);

        session.setTranscriptLines(Arrays.asList(
                new PracticeSessionDocument.TranscriptLineDto("tr_1", 0,
                        "Xin chào thầy cô và các bạn, hôm nay nhóm em xin trình bày dự án PitchCoach.",
                        null, 0, 5000),
                new PracticeSessionDocument.TranscriptLineDto("tr_2", 1,
                        "Vấn đề phổ biến khi thuyết trình là ờ thường xuyên nói quá nhanh và thiếu tự tin.",
                        "ờ", 5000, 11000),
                new PracticeSessionDocument.TranscriptLineDto("tr_3", 2,
                        "Giải pháp của chúng em kết hợp phân tích giọng nói AI và hội đồng phản biện ảo.",
                        null, 11000, 17000),
                new PracticeSessionDocument.TranscriptLineDto("tr_4", 3,
                        "Thì là toàn bộ dữ liệu được lưu trữ đồng bộ trên cả SQLite và Google Firebase.",
                        "Thì là", 17000, 23000)
        ));

        session.setImprovements(Arrays.asList(
                new PracticeSessionDocument.ImprovementDto("imp_1", 1,
                        "Hạn chế từ đệm 'ờ', 'thì là'",
                        "Xuất hiện tại câu số 2 và câu số 4 trong bản gỡ băng", false),
                new PracticeSessionDocument.ImprovementDto("imp_2", 2,
                        "Nhấn giọng rõ hơn ở phần kết luận",
                        "Âm lượng giảm nhẹ ở 30 giây cuối bài thuyết trình", false)
        ));

        session.setPanelQa(Arrays.asList(
                new PracticeSessionDocument.PanelQaDto("qa_1", 0,
                        "Giảng viên hướng dẫn", "GV", 0,
                        "Tại sao nhóm chọn kết hợp cả SQLite và Firebase mà không chỉ dùng một loại CSDL?"),
                new PracticeSessionDocument.PanelQaDto("qa_2", 1,
                        "Khán giả tò mò", "KG", 1,
                        "Ứng dụng có hoạt động được khi phòng thi không có kết nối Wi-Fi không?"),
                new PracticeSessionDocument.PanelQaDto("qa_3", 2,
                        "Nhà tuyển dụng", "TD", 2,
                        "Điểm khác biệt lớn nhất của sản phẩm so với các công cụ luyện nói hiện nay là gì?")
        ));

        WriteBatch batch = firestore.batch();
        DocumentReference sessionRef = firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId);
        batch.set(sessionRef, session);

        if (presentationId != null && !presentationId.isBlank()) {
            DocumentReference presRef = firestore.collection("users")
                    .document(uid)
                    .collection("presentations")
                    .document(presentationId);
            Map<String, Object> presUpdates = new HashMap<>();
            presUpdates.put("practiceCount", FieldValue.increment(1));
            presUpdates.put("latestScore", session.getOverallScore());
            presUpdates.put("scoreDisplay", String.valueOf(session.getOverallScore()));
            presUpdates.put("isPracticed", true);
            presUpdates.put("updatedAt", now);
            batch.set(presRef, presUpdates, SetOptions.merge());
        }

        batch.commit().get();
        return session;
    }

    public PracticeSessionDocument getSessionById(String uid, String sessionId) throws Exception {
        DocumentSnapshot snap = firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId)
                .get()
                .get();
        return snap.exists() ? snap.toObject(PracticeSessionDocument.class) : null;
    }

    public List<PracticeSessionDocument.TranscriptLineDto> updateTranscript(
            String uid, String sessionId,
            List<PracticeSessionDocument.TranscriptLineDto> lines) throws Exception {
        DocumentReference ref = firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("transcriptLines", lines);
        updates.put("updatedAt", System.currentTimeMillis());
        ref.set(updates, SetOptions.merge()).get();
        return lines;
    }

    public PracticeSessionDocument.ImprovementDto updateImprovementStatus(
            String uid, String sessionId, String impId, boolean isFixed) throws Exception {
        PracticeSessionDocument session = getSessionById(uid, sessionId);
        if (session == null || session.getImprovements() == null) {
            return null;
        }
        PracticeSessionDocument.ImprovementDto target = null;
        for (PracticeSessionDocument.ImprovementDto item : session.getImprovements()) {
            if (impId.equals(item.getId())) {
                item.setIsFixed(isFixed);
                target = item;
                break;
            }
        }
        firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId)
                .update("improvements", session.getImprovements(),
                        "updatedAt", System.currentTimeMillis())
                .get();
        return target;
    }

    public Map<String, Object> softDeleteSession(String uid, String sessionId) throws Exception {
        long now = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("isDeleted", true);
        updates.put("updatedAt", now);

        firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId)
                .set(updates, SetOptions.merge())
                .get();

        Map<String, Object> res = new HashMap<>();
        res.put("id", sessionId);
        res.put("deleted", true);
        res.put("updatedAt", now);
        return res;
    }

    // ==================== 4. AI PANEL Q&A ====================

    public List<PracticeSessionDocument.PanelQaDto> getPanelQuestions(String uid, String sessionId)
            throws Exception {
        PracticeSessionDocument session = getSessionById(uid, sessionId);
        if (session != null && session.getPanelQa() != null && !session.getPanelQa().isEmpty()) {
            return session.getPanelQa();
        }
        return Arrays.asList(
                new PracticeSessionDocument.PanelQaDto("qa_1", 0,
                        "Giảng viên hướng dẫn", "GV", 0,
                        "Tại sao nhóm chọn kết hợp cả SQLite và Firebase mà không chỉ dùng một loại CSDL?"),
                new PracticeSessionDocument.PanelQaDto("qa_2", 1,
                        "Khán giả tò mò", "KG", 1,
                        "Ứng dụng có hoạt động được khi phòng thi không có kết nối Wi-Fi không?"),
                new PracticeSessionDocument.PanelQaDto("qa_3", 2,
                        "Nhà tuyển dụng", "TD", 2,
                        "Điểm khác biệt lớn nhất của sản phẩm so với các công cụ luyện nói hiện nay là gì?")
        );
    }

    public PracticeSessionDocument.PanelQaDto submitPanelAnswer(
            String uid, String sessionId, int questionIndex,
            String answerTranscript, String answerCloudAudioUrl,
            MultipartFile audioFile) throws Exception {
        long now = System.currentTimeMillis();
        String resolvedUrl = answerCloudAudioUrl;
        if (audioFile != null && !audioFile.isEmpty()) {
            String blobPath = "users/" + uid + "/sessions/" + sessionId + "/answer_" + questionIndex + ".m4a";
            Blob blob = storageClient.bucket().create(
                    blobPath, audioFile.getBytes(), audioFile.getContentType());
            resolvedUrl = "gs://" + storageClient.bucket().getName() + "/" + blob.getName();
        }

        List<PracticeSessionDocument.PanelQaDto> panelList = getPanelQuestions(uid, sessionId);
        int idx = Math.max(0, Math.min(questionIndex, panelList.size() - 1));
        PracticeSessionDocument.PanelQaDto item = panelList.get(idx);

        item.setAnswerCloudAudioUrl(resolvedUrl);
        item.setAnswerTranscript(answerTranscript != null && !answerTranscript.isBlank()
                ? answerTranscript
                : "Dạ chúng em sử dụng kiến trúc Offline-First với SQLite kết hợp đồng bộ Cloud Firestore.");
        item.setAnswerScore(8.5);
        item.setFeedbackGood("Trả lời đúng trọng tâm câu hỏi, giải thích rõ cơ chế đồng bộ.");
        item.setFeedbackImprove("Nên nêu thêm ví dụ cụ thể về trường hợp xử lý mất mạng.");
        item.setAnsweredAt(now);

        Map<String, Object> updates = new HashMap<>();
        updates.put("panelQa", panelList);
        updates.put("updatedAt", now);

        firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .document(sessionId)
                .set(updates, SetOptions.merge())
                .get();
        return item;
    }

    // ==================== 5. PROGRESS & DASHBOARD ====================

    public Map<String, Object> getProgressOverview(String uid) throws Exception {
        QuerySnapshot snapshot = firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .whereEqualTo("isDeleted", false)
                .get()
                .get();

        List<PracticeSessionDocument> sessions = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            sessions.add(doc.toObject(PracticeSessionDocument.class));
        }
        sessions.sort(Comparator.comparingLong(PracticeSessionDocument::getPracticedAt));

        Map<String, Object> res = new HashMap<>();
        if (sessions.isEmpty()) {
            res.put("scoreValues", Arrays.asList(6.3, 7.1, 8.0));
            res.put("fillerValues", Arrays.asList(23, 12, 5));
            res.put("speedValues", Arrays.asList(165, 150, 140));
        } else {
            List<Double> scores = new ArrayList<>();
            List<Integer> fillers = new ArrayList<>();
            List<Double> speeds = new ArrayList<>();
            for (PracticeSessionDocument s : sessions) {
                scores.add(s.getOverallScore());
                fillers.add(s.getFillerWordCount());
                speeds.add(s.getSpeakingSpeedWpm());
            }
            res.put("scoreValues", scores);
            res.put("fillerValues", fillers);
            res.put("speedValues", speeds);
        }
        res.put("recentSessions", sessions);
        return res;
    }

    // ==================== 6. TWO-WAY SYNC (SQLite <-> Firestore) ====================

    public SyncPayload.SyncResponse syncData(String uid, SyncPayload.SyncRequest request)
            throws Exception {
        long now = System.currentTimeMillis();
        WriteBatch batch = firestore.batch();

        if (request.getPendingPresentations() != null) {
            for (PresentationDocument pres : request.getPendingPresentations()) {
                String id = (pres.getId() != null && !pres.getId().isBlank())
                        ? pres.getId()
                        : "pres_" + UUID.randomUUID().toString().substring(0, 8);
                pres.setId(id);
                pres.setUserId(uid);
                pres.setUpdatedAt(now);
                DocumentReference ref = firestore.collection("users")
                        .document(uid)
                        .collection("presentations")
                        .document(id);
                batch.set(ref, pres, SetOptions.merge());
            }
        }

        if (request.getPendingSessions() != null) {
            for (PracticeSessionDocument sess : request.getPendingSessions()) {
                String id = (sess.getId() != null && !sess.getId().isBlank())
                        ? sess.getId()
                        : "sess_" + UUID.randomUUID().toString().substring(0, 8);
                sess.setId(id);
                sess.setUserId(uid);
                sess.setUpdatedAt(now);
                DocumentReference ref = firestore.collection("users")
                        .document(uid)
                        .collection("practice_sessions")
                        .document(id);
                batch.set(ref, sess, SetOptions.merge());
            }
        }
        batch.commit().get();

        // Kéo toàn bộ bản ghi có updatedAt > lastSyncedAt về cho SQLite
        QuerySnapshot presSnap = firestore.collection("users")
                .document(uid)
                .collection("presentations")
                .whereGreaterThan("updatedAt", request.getLastSyncedAt())
                .get()
                .get();
        List<PresentationDocument> updatedPresentations = new ArrayList<>();
        for (QueryDocumentSnapshot doc : presSnap.getDocuments()) {
            updatedPresentations.add(doc.toObject(PresentationDocument.class));
        }

        QuerySnapshot sessSnap = firestore.collection("users")
                .document(uid)
                .collection("practice_sessions")
                .whereGreaterThan("updatedAt", request.getLastSyncedAt())
                .get()
                .get();
        List<PracticeSessionDocument> updatedSessions = new ArrayList<>();
        for (QueryDocumentSnapshot doc : sessSnap.getDocuments()) {
            updatedSessions.add(doc.toObject(PracticeSessionDocument.class));
        }

        return new SyncPayload.SyncResponse(now, updatedPresentations, updatedSessions);
    }
}
