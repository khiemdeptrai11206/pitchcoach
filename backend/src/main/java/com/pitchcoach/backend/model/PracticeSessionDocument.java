package com.pitchcoach.backend.model;

import java.util.ArrayList;
import java.util.List;

public class PracticeSessionDocument {
    private String id;
    private String presentationId;
    private String userId;
    private int attemptNumber = 1;
    private String mode = "PRESENTATION";
    private String cloudAudioUrl;
    private int durationSeconds = 300;
    private double overallScore = 8.0;
    private int scoreContent = 8;
    private int scoreStructure = 8;
    private int scoreClarity = 9;
    private int scoreDelivery = 7;
    private double speakingSpeedWpm = 140.0;
    private int fillerWordCount = 5;
    private String aiSummaryGood;
    private String aiSummaryMissing;
    private boolean isDeleted = false;
    private long practicedAt;
    private long updatedAt;

    private List<TranscriptLineDto> transcriptLines = new ArrayList<>();
    private List<ImprovementDto> improvements = new ArrayList<>();
    private List<PanelQaDto> panelQa = new ArrayList<>();

    public PracticeSessionDocument() {
    }

    public static class TranscriptLineDto {
        private String id;
        private int lineIndex;
        private String sentence;
        private String highlightFiller;
        private int startMs;
        private int endMs;

        public TranscriptLineDto() {
        }

        public TranscriptLineDto(String id, int lineIndex, String sentence,
                                 String highlightFiller, int startMs, int endMs) {
            this.id = id;
            this.lineIndex = lineIndex;
            this.sentence = sentence;
            this.highlightFiller = highlightFiller;
            this.startMs = startMs;
            this.endMs = endMs;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getLineIndex() {
            return lineIndex;
        }

        public void setLineIndex(int lineIndex) {
            this.lineIndex = lineIndex;
        }

        public String getSentence() {
            return sentence;
        }

        public void setSentence(String sentence) {
            this.sentence = sentence;
        }

        public String getHighlightFiller() {
            return highlightFiller;
        }

        public void setHighlightFiller(String highlightFiller) {
            this.highlightFiller = highlightFiller;
        }

        public int getStartMs() {
            return startMs;
        }

        public void setStartMs(int startMs) {
            this.startMs = startMs;
        }

        public int getEndMs() {
            return endMs;
        }

        public void setEndMs(int endMs) {
            this.endMs = endMs;
        }
    }

    public static class ImprovementDto {
        private String id;
        private int sortOrder;
        private String title;
        private String evidence;
        private boolean isFixed;

        public ImprovementDto() {
        }

        public ImprovementDto(String id, int sortOrder, String title, String evidence, boolean isFixed) {
            this.id = id;
            this.sortOrder = sortOrder;
            this.title = title;
            this.evidence = evidence;
            this.isFixed = isFixed;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getEvidence() {
            return evidence;
        }

        public void setEvidence(String evidence) {
            this.evidence = evidence;
        }

        public boolean getIsFixed() {
            return isFixed;
        }

        public void setIsFixed(boolean fixed) {
            isFixed = fixed;
        }
    }

    public static class PanelQaDto {
        private String id;
        private int questionIndex;
        private String panelistName;
        private String panelistInitials;
        private int panelistTone;
        private String questionText;
        private String answerCloudAudioUrl;
        private String answerTranscript;
        private Double answerScore;
        private String feedbackGood;
        private String feedbackImprove;
        private Long answeredAt;

        public PanelQaDto() {
        }

        public PanelQaDto(String id, int questionIndex, String panelistName,
                          String panelistInitials, int panelistTone, String questionText) {
            this.id = id;
            this.questionIndex = questionIndex;
            this.panelistName = panelistName;
            this.panelistInitials = panelistInitials;
            this.panelistTone = panelistTone;
            this.questionText = questionText;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public int getQuestionIndex() {
            return questionIndex;
        }

        public void setQuestionIndex(int questionIndex) {
            this.questionIndex = questionIndex;
        }

        public String getPanelistName() {
            return panelistName;
        }

        public void setPanelistName(String panelistName) {
            this.panelistName = panelistName;
        }

        public String getPanelistInitials() {
            return panelistInitials;
        }

        public void setPanelistInitials(String panelistInitials) {
            this.panelistInitials = panelistInitials;
        }

        public int getPanelistTone() {
            return panelistTone;
        }

        public void setPanelistTone(int panelistTone) {
            this.panelistTone = panelistTone;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getAnswerCloudAudioUrl() {
            return answerCloudAudioUrl;
        }

        public void setAnswerCloudAudioUrl(String answerCloudAudioUrl) {
            this.answerCloudAudioUrl = answerCloudAudioUrl;
        }

        public String getAnswerTranscript() {
            return answerTranscript;
        }

        public void setAnswerTranscript(String answerTranscript) {
            this.answerTranscript = answerTranscript;
        }

        public Double getAnswerScore() {
            return answerScore;
        }

        public void setAnswerScore(Double answerScore) {
            this.answerScore = answerScore;
        }

        public String getFeedbackGood() {
            return feedbackGood;
        }

        public void setFeedbackGood(String feedbackGood) {
            this.feedbackGood = feedbackGood;
        }

        public String getFeedbackImprove() {
            return feedbackImprove;
        }

        public void setFeedbackImprove(String feedbackImprove) {
            this.feedbackImprove = feedbackImprove;
        }

        public Long getAnsweredAt() {
            return answeredAt;
        }

        public void setAnsweredAt(Long answeredAt) {
            this.answeredAt = answeredAt;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPresentationId() {
        return presentationId;
    }

    public void setPresentationId(String presentationId) {
        this.presentationId = presentationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCloudAudioUrl() {
        return cloudAudioUrl;
    }

    public void setCloudAudioUrl(String cloudAudioUrl) {
        this.cloudAudioUrl = cloudAudioUrl;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(double overallScore) {
        this.overallScore = overallScore;
    }

    public int getScoreContent() {
        return scoreContent;
    }

    public void setScoreContent(int scoreContent) {
        this.scoreContent = scoreContent;
    }

    public int getScoreStructure() {
        return scoreStructure;
    }

    public void setScoreStructure(int scoreStructure) {
        this.scoreStructure = scoreStructure;
    }

    public int getScoreClarity() {
        return scoreClarity;
    }

    public void setScoreClarity(int scoreClarity) {
        this.scoreClarity = scoreClarity;
    }

    public int getScoreDelivery() {
        return scoreDelivery;
    }

    public void setScoreDelivery(int scoreDelivery) {
        this.scoreDelivery = scoreDelivery;
    }

    public double getSpeakingSpeedWpm() {
        return speakingSpeedWpm;
    }

    public void setSpeakingSpeedWpm(double speakingSpeedWpm) {
        this.speakingSpeedWpm = speakingSpeedWpm;
    }

    public int getFillerWordCount() {
        return fillerWordCount;
    }

    public void setFillerWordCount(int fillerWordCount) {
        this.fillerWordCount = fillerWordCount;
    }

    public String getAiSummaryGood() {
        return aiSummaryGood;
    }

    public void setAiSummaryGood(String aiSummaryGood) {
        this.aiSummaryGood = aiSummaryGood;
    }

    public String getAiSummaryMissing() {
        return aiSummaryMissing;
    }

    public void setAiSummaryMissing(String aiSummaryMissing) {
        this.aiSummaryMissing = aiSummaryMissing;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getPracticedAt() {
        return practicedAt;
    }

    public void setPracticedAt(long practicedAt) {
        this.practicedAt = practicedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TranscriptLineDto> getTranscriptLines() {
        return transcriptLines;
    }

    public void setTranscriptLines(List<TranscriptLineDto> transcriptLines) {
        this.transcriptLines = transcriptLines;
    }

    public List<ImprovementDto> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<ImprovementDto> improvements) {
        this.improvements = improvements;
    }

    public List<PanelQaDto> getPanelQa() {
        return panelQa;
    }

    public void setPanelQa(List<PanelQaDto> panelQa) {
        this.panelQa = panelQa;
    }
}
