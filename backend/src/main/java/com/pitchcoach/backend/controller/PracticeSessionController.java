package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.PracticeSessionDocument;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
public class PracticeSessionController {

    private final FirestoreService firestoreService;

    public PracticeSessionController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<PracticeSessionDocument> analyzeSession(
            HttpServletRequest request,
            @RequestParam(name = "presentationId", required = false) String presentationId,
            @RequestParam(name = "mode", defaultValue = "PRESENTATION") String mode,
            @RequestParam(name = "durationSeconds", defaultValue = "300") Integer durationSeconds,
            @RequestParam(name = "cloudAudioUrl", required = false) String cloudAudioUrl,
            @RequestParam(name = "audioFile", required = false) MultipartFile audioFile) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        PracticeSessionDocument result = firestoreService.analyzeAndSaveSession(
                uid, presentationId, mode, durationSeconds, cloudAudioUrl, audioFile);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<PracticeSessionDocument> getSession(
            HttpServletRequest request,
            @PathVariable String sessionId) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        PracticeSessionDocument doc = firestoreService.getSessionById(uid, sessionId);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doc);
    }

    @PatchMapping("/{sessionId}/transcript")
    public ResponseEntity<List<PracticeSessionDocument.TranscriptLineDto>> updateTranscript(
            HttpServletRequest request,
            @PathVariable String sessionId,
            @RequestBody List<PracticeSessionDocument.TranscriptLineDto> lines) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.updateTranscript(uid, sessionId, lines));
    }

    @PatchMapping("/{sessionId}/improvements/{impId}")
    public ResponseEntity<PracticeSessionDocument.ImprovementDto> updateImprovement(
            HttpServletRequest request,
            @PathVariable String sessionId,
            @PathVariable String impId,
            @RequestBody Map<String, Boolean> body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        boolean isFixed = body != null && Boolean.TRUE.equals(body.get("isFixed"));
        PracticeSessionDocument.ImprovementDto updated =
                firestoreService.updateImprovementStatus(uid, sessionId, impId, isFixed);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(
            HttpServletRequest request,
            @PathVariable String sessionId) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.softDeleteSession(uid, sessionId));
    }
}
