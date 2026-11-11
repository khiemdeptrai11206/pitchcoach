package com.pitchcoach.backend.model;

import java.util.ArrayList;
import java.util.List;

public class PresentationDocument {
    private String id;
    private String userId;
    private String title;
    private String audienceType = "LECTURER";
    private int targetDurationMin = 10;
    private long deadlineEpoch;
    private String deadlineDisplay;
    private String countdownDisplay = "Còn 4 ngày";
    private String rawOutline;
    private String aiOutlineFeedback;
    private int practiceCount = 0;
    private double bestScore = 0;
    private double latestScore = 0;
    private String scoreDisplay = "Chưa luyện";
    private boolean isPast = false;
    private boolean isPracticed = false;
    private int createdOrder = 0;
    private boolean isDeleted = false;
    private long createdAt;
    private long updatedAt;
    private List<OutlineItemDto> outlineItems = new ArrayList<>();
    private List<PredictedQuestionDto> predictedQuestions = new ArrayList<>();

    public PresentationDocument() {
    }

    public static class OutlineItemDto {
        private String id;
        private int sortOrder;
        private String sectionTitle;
        private String content;
        private int estimatedSeconds = 120;

        public OutlineItemDto() {
        }

        public OutlineItemDto(String id, int sortOrder, String sectionTitle, String content, int estimatedSeconds) {
            this.id = id;
            this.sortOrder = sortOrder;
            this.sectionTitle = sectionTitle;
            this.content = content;
            this.estimatedSeconds = estimatedSeconds;
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

        public String getSectionTitle() {
            return sectionTitle;
        }

        public void setSectionTitle(String sectionTitle) {
            this.sectionTitle = sectionTitle;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getEstimatedSeconds() {
            return estimatedSeconds;
        }

        public void setEstimatedSeconds(int estimatedSeconds) {
            this.estimatedSeconds = estimatedSeconds;
        }
    }

    public static class PredictedQuestionDto {
        private String id;
        private int sortOrder;
        private String questionText;

        public PredictedQuestionDto() {
        }

        public PredictedQuestionDto(String id, int sortOrder, String questionText) {
            this.id = id;
            this.sortOrder = sortOrder;
            this.questionText = questionText;
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

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudienceType() {
        return audienceType;
    }

    public void setAudienceType(String audienceType) {
        this.audienceType = audienceType;
    }

    public int getTargetDurationMin() {
        return targetDurationMin;
    }

    public void setTargetDurationMin(int targetDurationMin) {
        this.targetDurationMin = targetDurationMin;
    }

    public long getDeadlineEpoch() {
        return deadlineEpoch;
    }

    public void setDeadlineEpoch(long deadlineEpoch) {
        this.deadlineEpoch = deadlineEpoch;
    }

    public String getDeadlineDisplay() {
        return deadlineDisplay;
    }

    public void setDeadlineDisplay(String deadlineDisplay) {
        this.deadlineDisplay = deadlineDisplay;
    }

    public String getCountdownDisplay() {
        return countdownDisplay;
    }

    public void setCountdownDisplay(String countdownDisplay) {
        this.countdownDisplay = countdownDisplay;
    }

    public String getRawOutline() {
        return rawOutline;
    }

    public void setRawOutline(String rawOutline) {
        this.rawOutline = rawOutline;
    }

    public String getAiOutlineFeedback() {
        return aiOutlineFeedback;
    }

    public void setAiOutlineFeedback(String aiOutlineFeedback) {
        this.aiOutlineFeedback = aiOutlineFeedback;
    }

    public int getPracticeCount() {
        return practiceCount;
    }

    public void setPracticeCount(int practiceCount) {
        this.practiceCount = practiceCount;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public double getLatestScore() {
        return latestScore;
    }

    public void setLatestScore(double latestScore) {
        this.latestScore = latestScore;
    }

    public String getScoreDisplay() {
        return scoreDisplay;
    }

    public void setScoreDisplay(String scoreDisplay) {
        this.scoreDisplay = scoreDisplay;
    }

    public boolean getIsPast() {
        return isPast;
    }

    public void setIsPast(boolean past) {
        isPast = past;
    }

    public boolean getIsPracticed() {
        return isPracticed;
    }

    public void setIsPracticed(boolean practiced) {
        isPracticed = practiced;
    }

    public int getCreatedOrder() {
        return createdOrder;
    }

    public void setCreatedOrder(int createdOrder) {
        this.createdOrder = createdOrder;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OutlineItemDto> getOutlineItems() {
        return outlineItems;
    }

    public void setOutlineItems(List<OutlineItemDto> outlineItems) {
        this.outlineItems = outlineItems;
    }

    public List<PredictedQuestionDto> getPredictedQuestions() {
        return predictedQuestions;
    }

    public void setPredictedQuestions(List<PredictedQuestionDto> predictedQuestions) {
        this.predictedQuestions = predictedQuestions;
    }
}
