package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.PracticeSessionDocument;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/panel")
public class AiPanelController {

    private final FirestoreService firestoreService;

    public AiPanelController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping
    public ResponseEntity<List<PracticeSessionDocument.PanelQaDto>> getPanelQuestions(
            HttpServletRequest request,
            @PathVariable String sessionId) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.getPanelQuestions(uid, sessionId));
    }

    @PostMapping("/{questionIndex}/answer")
    public ResponseEntity<PracticeSessionDocument.PanelQaDto> submitAnswer(
            HttpServletRequest request,
            @PathVariable String sessionId,
            @PathVariable int questionIndex,
            @RequestParam(name = "answerTranscript", required = false) String answerTranscript,
            @RequestParam(name = "answerCloudAudioUrl", required = false) String answerCloudAudioUrl,
            @RequestParam(name = "audioFile", required = false) MultipartFile audioFile) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        PracticeSessionDocument.PanelQaDto feedback = firestoreService.submitPanelAnswer(
                uid, sessionId, questionIndex, answerTranscript, answerCloudAudioUrl, audioFile);
        return ResponseEntity.ok(feedback);
    }
}
