package com.pitchcoach.app.data;

import android.content.Context;

import com.pitchcoach.app.R;
import com.pitchcoach.app.model.AttemptRow;
import com.pitchcoach.app.model.HistoryEntry;
import com.pitchcoach.app.model.Improvement;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.model.PracticeSession;
import com.pitchcoach.app.model.PresentationItem;
import com.pitchcoach.app.model.TranscriptLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class SampleData {

    public static final float[] SCORE_VALUES = {6.3f, 7.1f, 8.0f};
    public static final float[] FILLER_VALUES = {23f, 12f, 5f};
    public static final float[] SPEED_VALUES = {165f, 150f, 140f};

    private SampleData() {
    }

    public static List<PresentationItem> createPresentations(Context context) {
        return new ArrayList<>(Arrays.asList(
                new PresentationItem(
                        context.getString(R.string.presentation_mobile),
                        context.getString(R.string.audience_lecturer),
                        context.getString(R.string.deadline_30_10),
                        context.getString(R.string.countdown_four_days),
                        3,
                        context.getString(R.string.score_eight),
                        false,
                        true,
                        0),
                new PresentationItem(
                        context.getString(R.string.presentation_club),
                        context.getString(R.string.audience_friends),
                        context.getString(R.string.deadline_05_11),
                        context.getString(R.string.countdown_ten_days),
                        0,
                        context.getString(R.string.not_practiced),
                        false,
                        false,
                        1),
                new PresentationItem(
                        context.getString(R.string.presentation_network),
                        context.getString(R.string.audience_lecturer),
                        context.getString(R.string.deadline_20_10),
                        context.getString(R.string.countdown_past),
                        2,
                        context.getString(R.string.score_seven_four),
                        true,
                        true,
                        2)
        ));
    }

    public static List<PracticeSession> createProgressSessions(Context context) {
        return new ArrayList<>(Arrays.asList(
                new PracticeSession(
                        context.getString(R.string.progress_round_three_title),
                        context.getString(R.string.progress_round_three_date),
                        context.getString(R.string.score_eight)),
                new PracticeSession(
                        context.getString(R.string.progress_round_two_title),
                        context.getString(R.string.progress_round_two_date),
                        context.getString(R.string.score_seven_one)),
                new PracticeSession(
                        context.getString(R.string.progress_round_one_title),
                        context.getString(R.string.progress_round_one_date),
                        context.getString(R.string.score_six_three))
        ));
    }

    public static List<HistoryEntry> createDetailHistory(Context context) {
        return new ArrayList<>(Arrays.asList(
                new HistoryEntry(
                        context.getString(R.string.step_number, 1),
                        context.getString(R.string.history_round_one),
                        context.getString(R.string.history_date_one),
                        context.getString(R.string.history_score_six_three),
                        true),
                new HistoryEntry(
                        context.getString(R.string.step_number, 2),
                        context.getString(R.string.history_round_two),
                        context.getString(R.string.history_date_two),
                        context.getString(R.string.history_score_seven_one),
                        false)
        ));
    }

    public static List<Panelist> createPanelists(Context context) {
        return new ArrayList<>(Arrays.asList(
                new Panelist(
                        context.getString(R.string.panelist_lecturer),
                        context.getString(R.string.panelist_initials_lecturer),
                        context.getString(R.string.panelist_question_lecturer),
                        Panelist.TONE_PURPLE),
                new Panelist(
                        context.getString(R.string.panelist_curious),
                        context.getString(R.string.panelist_initials_curious),
                        context.getString(R.string.panelist_question_curious),
                        Panelist.TONE_ORANGE),
                new Panelist(
                        context.getString(R.string.panelist_recruiter),
                        context.getString(R.string.panelist_initials_recruiter),
                        context.getString(R.string.panelist_question_recruiter),
                        Panelist.TONE_GREEN)
        ));
    }

    public static List<String> createQuestions(Context context) {
        return new ArrayList<>(Arrays.asList(
                context.getResources().getStringArray(R.array.predicted_question_list)));
    }

    public static List<String> createOutline(Context context) {
        return new ArrayList<>(Arrays.asList(
                context.getResources().getStringArray(R.array.recording_outline)));
    }

    public static List<TranscriptLine> createTranscript(Context context) {
        String[] sentences = context.getResources().getStringArray(R.array.transcript_sentences);
        List<TranscriptLine> lines = new ArrayList<>();
        for (int index = 0; index < sentences.length; index++) {
            String highlight = null;
            if (index == 1) {
                highlight = "ờ";
            } else if (index == 3) {
                highlight = "Thì là";
            }
            lines.add(new TranscriptLine(sentences[index], highlight));
        }
        return lines;
    }

    public static List<Improvement> createImprovements(Context context) {
        String[] titles = context.getResources().getStringArray(R.array.improvement_titles);
        String[] evidence = context.getResources().getStringArray(R.array.improvement_evidence);
        List<Improvement> improvements = new ArrayList<>();
        for (int index = 0; index < titles.length; index++) {
            improvements.add(new Improvement(titles[index], evidence[index]));
        }
        return improvements;
    }

    public static List<AttemptRow> createAttempts(Context context) {
        return new ArrayList<>(Arrays.asList(
                new AttemptRow(
                        context.getString(R.string.attempt_round_three),
                        context.getString(R.string.attempt_date_three),
                        context.getString(R.string.attempt_duration_three),
                        context.getString(R.string.score_eight),
                        context.getString(R.string.attempt_speed_three),
                        context.getString(R.string.attempt_filler_three)),
                new AttemptRow(
                        context.getString(R.string.attempt_round_two),
                        context.getString(R.string.attempt_date_two),
                        context.getString(R.string.attempt_duration_two),
                        context.getString(R.string.score_seven_one),
                        context.getString(R.string.attempt_speed_two),
                        context.getString(R.string.attempt_filler_two)),
                new AttemptRow(
                        context.getString(R.string.attempt_round_one),
                        context.getString(R.string.attempt_date_one),
                        context.getString(R.string.attempt_duration_one),
                        context.getString(R.string.score_six_three),
                        context.getString(R.string.attempt_speed_one),
                        context.getString(R.string.attempt_filler_one))
        ));
    }
}
