package com.pitchcoach.app.model.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pitchcoach.app.R;
import com.pitchcoach.app.model.AttemptRow;
import com.pitchcoach.app.model.HistoryEntry;
import com.pitchcoach.app.model.Improvement;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.model.PracticeSession;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.TranscriptLine;
import com.pitchcoach.app.model.repository.SampleData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PitchCoachDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "pitchcoach.db";
    public static final int DATABASE_VERSION = 1;

    public static final int SYNC_SYNCED = 0;
    public static final int SYNC_PENDING_UPLOAD = 1;
    public static final int SYNC_PENDING_UPDATE = 2;
    public static final int SYNC_PENDING_DELETE = 3;

    public static final String DEFAULT_USER_ID = "local_user";
    public static final String DEFAULT_SESSION_ID = "sess_default_1";

    private static PitchCoachDbHelper instance;
    private final Context appContext;

    private PitchCoachDbHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context.getApplicationContext();
    }

    public static synchronized PitchCoachDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new PitchCoachDbHelper(context);
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Bảng users
        db.execSQL("CREATE TABLE users ("
                + "id TEXT PRIMARY KEY, "
                + "email TEXT NOT NULL, "
                + "display_name TEXT NOT NULL, "
                + "initials TEXT, "
                + "avatar_url TEXT, "
                + "reminder_enabled INTEGER NOT NULL DEFAULT 1, "
                + "reminder_time TEXT NOT NULL DEFAULT '20:00', "
                + "streak_days INTEGER NOT NULL DEFAULT 0, "
                + "sync_status INTEGER NOT NULL DEFAULT 0, "
                + "created_at INTEGER NOT NULL, "
                + "updated_at INTEGER NOT NULL)");

        // 2. Bảng presentations
        db.execSQL("CREATE TABLE presentations ("
                + "id TEXT PRIMARY KEY, "
                + "user_id TEXT NOT NULL, "
                + "title TEXT NOT NULL, "
                + "audience_type TEXT NOT NULL, "
                + "target_duration_min INTEGER NOT NULL DEFAULT 10, "
                + "deadline_epoch INTEGER NOT NULL, "
                + "deadline_display TEXT NOT NULL, "
                + "countdown_display TEXT, "
                + "raw_outline TEXT, "
                + "ai_outline_feedback TEXT, "
                + "practice_count INTEGER NOT NULL DEFAULT 0, "
                + "best_score REAL NOT NULL DEFAULT 0, "
                + "latest_score REAL NOT NULL DEFAULT 0, "
                + "score_display TEXT NOT NULL, "
                + "is_past INTEGER NOT NULL DEFAULT 0, "
                + "is_practiced INTEGER NOT NULL DEFAULT 0, "
                + "created_order INTEGER NOT NULL DEFAULT 0, "
                + "is_deleted INTEGER NOT NULL DEFAULT 0, "
                + "sync_status INTEGER NOT NULL DEFAULT 0, "
                + "created_at INTEGER NOT NULL, "
                + "updated_at INTEGER NOT NULL, "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE INDEX idx_presentations_user ON presentations(user_id, is_deleted)");

        // 3. Bảng outline_items
        db.execSQL("CREATE TABLE outline_items ("
                + "id TEXT PRIMARY KEY, "
                + "presentation_id TEXT NOT NULL, "
                + "sort_order INTEGER NOT NULL, "
                + "section_title TEXT NOT NULL, "
                + "content TEXT, "
                + "estimated_seconds INTEGER NOT NULL DEFAULT 120, "
                + "FOREIGN KEY (presentation_id) REFERENCES presentations(id) ON DELETE CASCADE)");

        // 4. Bảng predicted_questions
        db.execSQL("CREATE TABLE predicted_questions ("
                + "id TEXT PRIMARY KEY, "
                + "presentation_id TEXT NOT NULL, "
                + "sort_order INTEGER NOT NULL, "
                + "question_text TEXT NOT NULL, "
                + "FOREIGN KEY (presentation_id) REFERENCES presentations(id) ON DELETE CASCADE)");

        // 5. Bảng practice_sessions
        db.execSQL("CREATE TABLE practice_sessions ("
                + "id TEXT PRIMARY KEY, "
                + "presentation_id TEXT, "
                + "user_id TEXT NOT NULL, "
                + "attempt_number INTEGER NOT NULL DEFAULT 1, "
                + "mode TEXT NOT NULL DEFAULT 'PRESENTATION', "
                + "title_display TEXT, "
                + "date_display TEXT, "
                + "duration_display TEXT, "
                + "local_audio_path TEXT, "
                + "cloud_audio_url TEXT, "
                + "duration_seconds INTEGER NOT NULL DEFAULT 0, "
                + "overall_score REAL NOT NULL DEFAULT 0, "
                + "score_display TEXT, "
                + "score_content INTEGER NOT NULL DEFAULT 0, "
                + "score_structure INTEGER NOT NULL DEFAULT 0, "
                + "score_clarity INTEGER NOT NULL DEFAULT 0, "
                + "score_delivery INTEGER NOT NULL DEFAULT 0, "
                + "speaking_speed_wpm REAL NOT NULL DEFAULT 0, "
                + "speed_display TEXT, "
                + "filler_word_count INTEGER NOT NULL DEFAULT 0, "
                + "filler_display TEXT, "
                + "ai_summary_good TEXT, "
                + "ai_summary_missing TEXT, "
                + "is_deleted INTEGER NOT NULL DEFAULT 0, "
                + "sync_status INTEGER NOT NULL DEFAULT 0, "
                + "practiced_at INTEGER NOT NULL, "
                + "updated_at INTEGER NOT NULL, "
                + "FOREIGN KEY (presentation_id) REFERENCES presentations(id) ON DELETE SET NULL, "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

        // 6. Bảng transcript_lines
        db.execSQL("CREATE TABLE transcript_lines ("
                + "id TEXT PRIMARY KEY, "
                + "session_id TEXT NOT NULL, "
                + "line_index INTEGER NOT NULL, "
                + "sentence TEXT NOT NULL, "
                + "highlight_filler TEXT, "
                + "start_ms INTEGER NOT NULL DEFAULT 0, "
                + "end_ms INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY (session_id) REFERENCES practice_sessions(id) ON DELETE CASCADE)");

        // 7. Bảng improvements
        db.execSQL("CREATE TABLE improvements ("
                + "id TEXT PRIMARY KEY, "
                + "session_id TEXT NOT NULL, "
                + "sort_order INTEGER NOT NULL, "
                + "title TEXT NOT NULL, "
                + "evidence TEXT NOT NULL, "
                + "is_fixed INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY (session_id) REFERENCES practice_sessions(id) ON DELETE CASCADE)");

        // 8. Bảng panel_qa
        db.execSQL("CREATE TABLE panel_qa ("
                + "id TEXT PRIMARY KEY, "
                + "session_id TEXT NOT NULL, "
                + "question_index INTEGER NOT NULL, "
                + "panelist_name TEXT NOT NULL, "
                + "panelist_initials TEXT NOT NULL, "
                + "panelist_tone INTEGER NOT NULL DEFAULT 0, "
                + "question_text TEXT NOT NULL, "
                + "answer_local_audio_path TEXT, "
                + "answer_cloud_audio_url TEXT, "
                + "answer_transcript TEXT, "
                + "answer_score REAL, "
                + "feedback_good TEXT, "
                + "feedback_improve TEXT, "
                + "answered_at INTEGER, "
                + "FOREIGN KEY (session_id) REFERENCES practice_sessions(id) ON DELETE CASCADE)");

        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS panel_qa");
        db.execSQL("DROP TABLE IF EXISTS improvements");
        db.execSQL("DROP TABLE IF EXISTS transcript_lines");
        db.execSQL("DROP TABLE IF EXISTS practice_sessions");
        db.execSQL("DROP TABLE IF EXISTS predicted_questions");
        db.execSQL("DROP TABLE IF EXISTS outline_items");
        db.execSQL("DROP TABLE IF EXISTS presentations");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        long now = System.currentTimeMillis();

        // 1. Seed user mặc định
        ContentValues userValues = new ContentValues();
        userValues.put("id", DEFAULT_USER_ID);
        userValues.put("email", "minhkhoa@pitchcoach.app");
        userValues.put("display_name", "Minh Khoa");
        userValues.put("initials", "MK");
        userValues.put("reminder_enabled", 1);
        userValues.put("reminder_time", "20:00");
        userValues.put("streak_days", 5);
        userValues.put("sync_status", SYNC_SYNCED);
        userValues.put("created_at", now);
        userValues.put("updated_at", now);
        db.insert("users", null, userValues);

        // 2. Seed presentations từ SampleData
        List<PresentationItem> samplePresentations = SampleData.createPresentations(appContext);
        String firstPresentationId = null;
        for (PresentationItem item : samplePresentations) {
            if (firstPresentationId == null) {
                firstPresentationId = item.id;
            }
            ContentValues presValues = new ContentValues();
            presValues.put("id", item.id);
            presValues.put("user_id", DEFAULT_USER_ID);
            presValues.put("title", item.title);
            presValues.put("audience_type", item.audience);
            presValues.put("target_duration_min", 10);
            presValues.put("deadline_epoch", now + (item.past ? -86400000L : 345600000L));
            presValues.put("deadline_display", item.deadline);
            presValues.put("countdown_display", item.countdown);
            presValues.put("practice_count", item.practices);
            presValues.put("score_display", item.score);
            presValues.put("is_past", item.past ? 1 : 0);
            presValues.put("is_practiced", item.practiced ? 1 : 0);
            presValues.put("created_order", item.createdOrder);
            presValues.put("is_deleted", 0);
            presValues.put("sync_status", SYNC_PENDING_UPLOAD);
            presValues.put("created_at", now - item.createdOrder * 3600000L);
            presValues.put("updated_at", now);
            db.insert("presentations", null, presValues);
        }

        // 3. Seed outline_items & predicted_questions cho bài thuyết trình đầu tiên
        if (firstPresentationId != null) {
            List<String> outline = SampleData.createOutline(appContext);
            for (int i = 0; i < outline.size(); i++) {
                ContentValues outVal = new ContentValues();
                outVal.put("id", "out_" + (i + 1));
                outVal.put("presentation_id", firstPresentationId);
                outVal.put("sort_order", i + 1);
                outVal.put("section_title", outline.get(i));
                outVal.put("content", outline.get(i));
                outVal.put("estimated_seconds", 120);
                db.insert("outline_items", null, outVal);
            }

            List<String> questions = SampleData.createQuestions(appContext);
            for (int i = 0; i < questions.size(); i++) {
                ContentValues qVal = new ContentValues();
                qVal.put("id", "q_" + (i + 1));
                qVal.put("presentation_id", firstPresentationId);
                qVal.put("sort_order", i + 1);
                qVal.put("question_text", questions.get(i));
                db.insert("predicted_questions", null, qVal);
            }
        }

        // 4. Seed practice_sessions từ SampleData.createAttempts
        List<AttemptRow> attempts = SampleData.createAttempts(appContext);
        List<PracticeSession> sessions = SampleData.createProgressSessions(appContext);
        for (int i = 0; i < attempts.size(); i++) {
            AttemptRow row = attempts.get(i);
            PracticeSession ps = i < sessions.size() ? sessions.get(i) : null;
            String sessionId = (i == 0) ? DEFAULT_SESSION_ID : ("sess_seed_" + (i + 1));

            ContentValues sessVal = new ContentValues();
            sessVal.put("id", sessionId);
            sessVal.put("presentation_id", firstPresentationId);
            sessVal.put("user_id", DEFAULT_USER_ID);
            sessVal.put("attempt_number", attempts.size() - i);
            sessVal.put("mode", "PRESENTATION");
            sessVal.put("title_display", ps != null ? ps.title : row.round);
            sessVal.put("date_display", row.date);
            sessVal.put("duration_display", row.duration);
            sessVal.put("duration_seconds", 300);
            sessVal.put("overall_score", parseFloatSafe(row.score, 8.0f));
            sessVal.put("score_display", row.score);
            sessVal.put("score_content", 8);
            sessVal.put("score_structure", 8);
            sessVal.put("score_clarity", 9);
            sessVal.put("score_delivery", 7);
            sessVal.put("speaking_speed_wpm", 140f);
            sessVal.put("speed_display", row.speed);
            sessVal.put("filler_word_count", 5);
            sessVal.put("filler_display", row.filler);
            sessVal.put("is_deleted", 0);
            sessVal.put("sync_status", SYNC_PENDING_UPLOAD);
            sessVal.put("practiced_at", now - i * 86400000L);
            sessVal.put("updated_at", now);
            db.insert("practice_sessions", null, sessVal);
        }

        // 5. Seed transcript_lines, improvements, panel_qa cho DEFAULT_SESSION_ID
        List<TranscriptLine> transcript = SampleData.createTranscript(appContext);
        for (int i = 0; i < transcript.size(); i++) {
            TranscriptLine line = transcript.get(i);
            ContentValues trVal = new ContentValues();
            trVal.put("id", "tr_" + (i + 1));
            trVal.put("session_id", DEFAULT_SESSION_ID);
            trVal.put("line_index", i);
            trVal.put("sentence", line.sentence);
            trVal.put("highlight_filler", line.highlight);
            trVal.put("start_ms", i * 5000);
            trVal.put("end_ms", (i + 1) * 5000);
            db.insert("transcript_lines", null, trVal);
        }

        List<Improvement> improvements = SampleData.createImprovements(appContext);
        for (int i = 0; i < improvements.size(); i++) {
            Improvement imp = improvements.get(i);
            ContentValues impVal = new ContentValues();
            impVal.put("id", "imp_" + (i + 1));
            impVal.put("session_id", DEFAULT_SESSION_ID);
            impVal.put("sort_order", i + 1);
            impVal.put("title", imp.title);
            impVal.put("evidence", imp.evidence);
            impVal.put("is_fixed", 0);
            db.insert("improvements", null, impVal);
        }

        List<Panelist> panelists = SampleData.createPanelists(appContext);
        for (int i = 0; i < panelists.size(); i++) {
            Panelist p = panelists.get(i);
            ContentValues pVal = new ContentValues();
            pVal.put("id", "qa_" + (i + 1));
            pVal.put("session_id", DEFAULT_SESSION_ID);
            pVal.put("question_index", i);
            pVal.put("panelist_name", p.name);
            pVal.put("panelist_initials", p.initials);
            pVal.put("panelist_tone", p.tone);
            pVal.put("question_text", p.question);
            db.insert("panel_qa", null, pVal);
        }
    }

    private float parseFloatSafe(String text, float fallback) {
        try {
            return Float.parseFloat(text);
        } catch (Exception e) {
            return fallback;
        }
    }

    // ==================== CRUD BẢNG USERS ====================

    public void upsertUser(String id, String email, String displayName, String avatarUrl) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("email", email != null ? email : "");
        values.put("display_name", displayName != null && !displayName.isEmpty() ? displayName : "PitchCoach User");
        values.put("initials", buildInitials(displayName != null ? displayName : email));
        values.put("avatar_url", avatarUrl);
        values.put("updated_at", now);

        Cursor cursor = db.query("users", new String[]{"id"}, "id = ?", new String[]{id}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            db.update("users", values, "id = ?", new String[]{id});
        } else {
            values.put("reminder_enabled", 1);
            values.put("reminder_time", "20:00");
            values.put("streak_days", 1);
            values.put("sync_status", SYNC_SYNCED);
            values.put("created_at", now);
            db.insert("users", null, values);
        }
    }

    public void updateReminderSettings(String userId, boolean enabled, String reminderTime) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("reminder_enabled", enabled ? 1 : 0);
        if (reminderTime != null) {
            values.put("reminder_time", reminderTime);
        }
        values.put("sync_status", SYNC_PENDING_UPDATE);
        values.put("updated_at", System.currentTimeMillis());
        db.update("users", values, "id = ?", new String[]{userId});
    }

    public String getReminderTime(String userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("users", new String[]{"reminder_time"}, "id = ?",
                new String[]{userId}, null, null, null);
        String time = "20:00";
        if (c.moveToFirst()) {
            time = c.getString(0);
        }
        c.close();
        return time;
    }

    private String buildInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "PC";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase(Locale.ROOT);
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase(Locale.ROOT);
    }

    // ==================== CRUD BẢNG PRESENTATIONS ====================

    public List<PresentationItem> getAllPresentations() {
        List<PresentationItem> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("presentations",
                new String[]{"id", "title", "audience_type", "deadline_display", "countdown_display",
                        "practice_count", "score_display", "is_past", "is_practiced", "created_order"},
                "is_deleted = 0", null, null, null, "created_order ASC, created_at DESC");
        while (cursor.moveToNext()) {
            result.add(new PresentationItem(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4) != null ? cursor.getString(4) : appContext.getString(R.string.countdown_four_days),
                    cursor.getInt(5),
                    cursor.getString(6),
                    cursor.getInt(7) == 1,
                    cursor.getInt(8) == 1,
                    cursor.getInt(9)
            ));
        }
        cursor.close();
        return result;
    }

    public PresentationItem insertPresentation(String userId, String title, String audience,
                                               int durationMin, String deadlineDisplay, String rawOutline) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        String id = "pres_" + UUID.randomUUID().toString().substring(0, 8);
        String notPracticed = appContext.getString(R.string.not_practiced);
        String countdown = appContext.getString(R.string.countdown_four_days);

        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("user_id", userId != null ? userId : DEFAULT_USER_ID);
        values.put("title", title);
        values.put("audience_type", audience);
        values.put("target_duration_min", durationMin);
        values.put("deadline_epoch", now + 345600000L);
        values.put("deadline_display", deadlineDisplay);
        values.put("countdown_display", countdown);
        values.put("raw_outline", rawOutline);
        values.put("practice_count", 0);
        values.put("best_score", 0);
        values.put("latest_score", 0);
        values.put("score_display", notPracticed);
        values.put("is_past", 0);
        values.put("is_practiced", 0);
        values.put("created_order", 0);
        values.put("is_deleted", 0);
        values.put("sync_status", SYNC_PENDING_UPLOAD);
        values.put("created_at", now);
        values.put("updated_at", now);

        // Đảm bảo user_id tồn tại trong bảng users trước khi insert FK
        ensureUserExists(db, userId != null ? userId : DEFAULT_USER_ID);
        db.insert("presentations", null, values);

        // Nếu có nhập dàn ý, tách từng dòng lưu vào bảng outline_items
        if (rawOutline != null && !rawOutline.trim().isEmpty()) {
            String[] lines = rawOutline.trim().split("\\r?\\n");
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) {
                    continue;
                }
                ContentValues outVal = new ContentValues();
                outVal.put("id", "out_" + UUID.randomUUID().toString().substring(0, 8));
                outVal.put("presentation_id", id);
                outVal.put("sort_order", i + 1);
                outVal.put("section_title", lines[i].trim());
                outVal.put("content", lines[i].trim());
                outVal.put("estimated_seconds", Math.max(60, (durationMin * 60) / Math.max(1, lines.length)));
                db.insert("outline_items", null, outVal);
            }
        }

        return new PresentationItem(id, title, audience, deadlineDisplay, countdown,
                0, notPracticed, false, false, 0);
    }

    public void upsertPresentationFromRemote(PresentationItem item, String userId) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();
        ensureUserExists(db, userId != null ? userId : DEFAULT_USER_ID);

        ContentValues values = new ContentValues();
        values.put("id", item.id);
        values.put("user_id", userId != null ? userId : DEFAULT_USER_ID);
        values.put("title", item.title);
        values.put("audience_type", item.audience);
        values.put("deadline_epoch", now);
        values.put("deadline_display", item.deadline);
        values.put("countdown_display", item.countdown);
        values.put("practice_count", item.practices);
        values.put("score_display", item.score);
        values.put("is_past", item.past ? 1 : 0);
        values.put("is_practiced", item.practiced ? 1 : 0);
        values.put("created_order", item.createdOrder);
        values.put("is_deleted", 0);
        values.put("sync_status", SYNC_SYNCED);
        values.put("created_at", now);
        values.put("updated_at", now);

        db.insertWithOnConflict("presentations", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String softDeletePresentationByTitle(String title) {
        SQLiteDatabase db = getWritableDatabase();
        String deletedId = null;
        Cursor cursor = db.query("presentations", new String[]{"id"},
                "title = ? AND is_deleted = 0", new String[]{title}, null, null, null);
        if (cursor.moveToFirst()) {
            deletedId = cursor.getString(0);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("is_deleted", 1);
        values.put("sync_status", SYNC_PENDING_DELETE);
        values.put("updated_at", System.currentTimeMillis());
        db.update("presentations", values, "title = ?", new String[]{title});
        return deletedId;
    }

    public void markPresentationSynced(String id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("sync_status", SYNC_SYNCED);
        db.update("presentations", values, "id = ?", new String[]{id});
    }

    private void ensureUserExists(SQLiteDatabase db, String userId) {
        Cursor c = db.query("users", new String[]{"id"}, "id = ?", new String[]{userId}, null, null, null);
        boolean exists = c.moveToFirst();
        c.close();
        if (!exists) {
            long now = System.currentTimeMillis();
            ContentValues u = new ContentValues();
            u.put("id", userId);
            u.put("email", "user@pitchcoach.app");
            u.put("display_name", "PitchCoach User");
            u.put("initials", "PC");
            u.put("reminder_enabled", 1);
            u.put("reminder_time", "20:00");
            u.put("streak_days", 1);
            u.put("sync_status", SYNC_SYNCED);
            u.put("created_at", now);
            u.put("updated_at", now);
            db.insert("users", null, u);
        }
    }

    // ==================== CRUD BẢNG CON & PRACTICE SESSIONS ====================

    public List<String> getOutlineItems() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("outline_items", new String[]{"section_title"},
                null, null, null, null, "sort_order ASC");
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();
        return list.isEmpty() ? SampleData.createOutline(appContext) : list;
    }

    public List<String> getPredictedQuestions() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("predicted_questions", new String[]{"question_text"},
                null, null, null, null, "sort_order ASC");
        while (c.moveToNext()) {
            list.add(c.getString(0));
        }
        c.close();
        return list.isEmpty() ? SampleData.createQuestions(appContext) : list;
    }

    public List<TranscriptLine> getTranscriptLines() {
        List<TranscriptLine> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("transcript_lines", new String[]{"sentence", "highlight_filler"},
                "session_id = ?", new String[]{DEFAULT_SESSION_ID}, null, null, "line_index ASC");
        while (c.moveToNext()) {
            list.add(new TranscriptLine(c.getString(0), c.getString(1)));
        }
        c.close();
        return list.isEmpty() ? SampleData.createTranscript(appContext) : list;
    }

    public List<Improvement> getImprovements() {
        List<Improvement> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("improvements", new String[]{"title", "evidence"},
                "session_id = ?", new String[]{DEFAULT_SESSION_ID}, null, null, "sort_order ASC");
        while (c.moveToNext()) {
            list.add(new Improvement(c.getString(0), c.getString(1)));
        }
        c.close();
        return list.isEmpty() ? SampleData.createImprovements(appContext) : list;
    }

    public List<Panelist> getPanelists() {
        List<Panelist> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("panel_qa",
                new String[]{"panelist_name", "panelist_initials", "question_text", "panelist_tone"},
                "session_id = ?", new String[]{DEFAULT_SESSION_ID}, null, null, "question_index ASC");
        while (c.moveToNext()) {
            list.add(new Panelist(c.getString(0), c.getString(1), c.getString(2), c.getInt(3)));
        }
        c.close();
        return list.isEmpty() ? SampleData.createPanelists(appContext) : list;
    }

    public List<PracticeSession> getProgressSessions() {
        List<PracticeSession> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("practice_sessions",
                new String[]{"title_display", "date_display", "score_display"},
                "is_deleted = 0", null, null, null, "practiced_at DESC");
        while (c.moveToNext()) {
            list.add(new PracticeSession(c.getString(0), c.getString(1), c.getString(2)));
        }
        c.close();
        return list.isEmpty() ? SampleData.createProgressSessions(appContext) : list;
    }

    public List<AttemptRow> getAttemptRows() {
        List<AttemptRow> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("practice_sessions",
                new String[]{"attempt_number", "date_display", "duration_display",
                        "score_display", "speed_display", "filler_display"},
                "is_deleted = 0", null, null, null, "attempt_number DESC");
        while (c.moveToNext()) {
            String roundLabel = appContext.getString(R.string.attempt_label, c.getInt(0));
            list.add(new AttemptRow(
                    roundLabel,
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5)
            ));
        }
        c.close();
        return list.isEmpty() ? SampleData.createAttempts(appContext) : list;
    }

    public List<HistoryEntry> getDetailHistory() {
        return SampleData.createDetailHistory(appContext);
    }

    public void saveLatestAnalysisToDefaultSession(
            float overallScore, int scoreContent, int scoreStructure, int scoreClarity, int scoreDelivery,
            float speakingSpeedWpm, int fillerWordCount,
            List<TranscriptLine> transcriptLines, List<Improvement> improvements, List<Panelist> panelists) {
        SQLiteDatabase db = getWritableDatabase();
        long now = System.currentTimeMillis();

        ContentValues sessVal = new ContentValues();
        sessVal.put("overall_score", overallScore);
        sessVal.put("score_display", String.format(Locale.US, "%.1f", overallScore));
        sessVal.put("score_content", scoreContent);
        sessVal.put("score_structure", scoreStructure);
        sessVal.put("score_clarity", scoreClarity);
        sessVal.put("score_delivery", scoreDelivery);
        sessVal.put("speaking_speed_wpm", speakingSpeedWpm);
        sessVal.put("speed_display", ((int) speakingSpeedWpm) + " từ/p");
        sessVal.put("filler_word_count", fillerWordCount);
        sessVal.put("filler_display", fillerWordCount + " từ");
        sessVal.put("updated_at", now);
        db.update("practice_sessions", sessVal, "id = ?", new String[]{DEFAULT_SESSION_ID});

        if (transcriptLines != null && !transcriptLines.isEmpty()) {
            db.delete("transcript_lines", "session_id = ?", new String[]{DEFAULT_SESSION_ID});
            for (int i = 0; i < transcriptLines.size(); i++) {
                TranscriptLine line = transcriptLines.get(i);
                ContentValues trVal = new ContentValues();
                trVal.put("id", "tr_" + (i + 1));
                trVal.put("session_id", DEFAULT_SESSION_ID);
                trVal.put("line_index", i);
                trVal.put("sentence", line.sentence);
                trVal.put("highlight_filler", line.highlight);
                trVal.put("start_ms", i * 5000);
                trVal.put("end_ms", (i + 1) * 5000);
                db.insert("transcript_lines", null, trVal);
            }
        }

        if (improvements != null && !improvements.isEmpty()) {
            db.delete("improvements", "session_id = ?", new String[]{DEFAULT_SESSION_ID});
            for (int i = 0; i < improvements.size(); i++) {
                Improvement imp = improvements.get(i);
                ContentValues impVal = new ContentValues();
                impVal.put("id", "imp_" + (i + 1));
                impVal.put("session_id", DEFAULT_SESSION_ID);
                impVal.put("sort_order", i + 1);
                impVal.put("title", imp.title);
                impVal.put("evidence", imp.evidence);
                impVal.put("is_fixed", 0);
                db.insert("improvements", null, impVal);
            }
        }

        if (panelists != null && !panelists.isEmpty()) {
            db.delete("panel_qa", "session_id = ?", new String[]{DEFAULT_SESSION_ID});
            for (int i = 0; i < panelists.size(); i++) {
                Panelist p = panelists.get(i);
                ContentValues pVal = new ContentValues();
                pVal.put("id", "qa_" + (i + 1));
                pVal.put("session_id", DEFAULT_SESSION_ID);
                pVal.put("question_index", i);
                pVal.put("panelist_name", p.name);
                pVal.put("panelist_initials", p.initials);
                pVal.put("panelist_tone", p.tone);
                pVal.put("question_text", p.question);
                db.insert("panel_qa", null, pVal);
            }
        }
    }
}
