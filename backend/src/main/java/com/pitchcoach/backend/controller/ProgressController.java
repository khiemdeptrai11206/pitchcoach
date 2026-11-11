package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.PresentationDocument;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/progress")
public class ProgressController {

    private final FirestoreService firestoreService;

    public ProgressController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping("/home-dashboard")
    public ResponseEntity<Map<String, Object>> getHomeDashboard(HttpServletRequest request)
            throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        List<PresentationDocument> upcoming =
                firestoreService.getPresentations(uid, "", "upcoming", "deadline");
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("featuredPresentation", upcoming.isEmpty() ? null : upcoming.get(0));
        dashboard.put("upcomingPresentations", upcoming);
        dashboard.put("progressOverview", firestoreService.getProgressOverview(uid));
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview(HttpServletRequest request)
            throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.getProgressOverview(uid));
    }

    @GetMapping("/presentations/{presentationId}")
    public ResponseEntity<Map<String, Object>> getPresentationProgress(
            HttpServletRequest request,
            @PathVariable String presentationId) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        Map<String, Object> detail = new HashMap<>(firestoreService.getProgressOverview(uid));
        detail.put("presentation", firestoreService.getPresentationById(uid, presentationId));
        detail.put("insightText", "Điểm số tăng từ 6.3 lên 8.0 và số từ đệm giảm 78% sau 3 lần luyện tập.");
        return ResponseEntity.ok(detail);
    }
}
