package com.pitchcoach.backend.model;

import java.util.ArrayList;
import java.util.List;

public class SyncPayload {

    public static class SyncRequest {
        private long lastSyncedAt = 0;
        private List<PresentationDocument> pendingPresentations = new ArrayList<>();
        private List<PracticeSessionDocument> pendingSessions = new ArrayList<>();

        public SyncRequest() {
        }

        public long getLastSyncedAt() {
            return lastSyncedAt;
        }

        public void setLastSyncedAt(long lastSyncedAt) {
            this.lastSyncedAt = lastSyncedAt;
        }

        public List<PresentationDocument> getPendingPresentations() {
            return pendingPresentations;
        }

        public void setPendingPresentations(List<PresentationDocument> pendingPresentations) {
            this.pendingPresentations = pendingPresentations;
        }

        public List<PracticeSessionDocument> getPendingSessions() {
            return pendingSessions;
        }

        public void setPendingSessions(List<PracticeSessionDocument> pendingSessions) {
            this.pendingSessions = pendingSessions;
        }
    }

    public static class SyncResponse {
        private long serverTimestamp;
        private List<PresentationDocument> updatedPresentations = new ArrayList<>();
        private List<PracticeSessionDocument> updatedSessions = new ArrayList<>();

        public SyncResponse() {
        }

        public SyncResponse(long serverTimestamp,
                            List<PresentationDocument> updatedPresentations,
                            List<PracticeSessionDocument> updatedSessions) {
            this.serverTimestamp = serverTimestamp;
            this.updatedPresentations = updatedPresentations;
            this.updatedSessions = updatedSessions;
        }

        public long getServerTimestamp() {
            return serverTimestamp;
        }

        public void setServerTimestamp(long serverTimestamp) {
            this.serverTimestamp = serverTimestamp;
        }

        public List<PresentationDocument> getUpdatedPresentations() {
            return updatedPresentations;
        }

        public void setUpdatedPresentations(List<PresentationDocument> updatedPresentations) {
            this.updatedPresentations = updatedPresentations;
        }

        public List<PracticeSessionDocument> getUpdatedSessions() {
            return updatedSessions;
        }

        public void setUpdatedSessions(List<PracticeSessionDocument> updatedSessions) {
            this.updatedSessions = updatedSessions;
        }
    }
}
