package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.PresentationDocument;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/presentations")
public class PresentationController {

    private final FirestoreService firestoreService;

    public PresentationController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listPresentations(
            HttpServletRequest request,
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "filter", defaultValue = "all") String filter,
            @RequestParam(name = "sort", defaultValue = "deadline") String sort) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        List<PresentationDocument> items = firestoreService.getPresentations(uid, query, filter, sort);

        long upcomingCount = items.stream().filter(i -> !i.getIsPast()).count();
        long pastCount = items.stream().filter(PresentationDocument::getIsPast).count();

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("totalCount", items.size());
        response.put("upcomingCount", upcomingCount);
        response.put("pastCount", pastCount);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PresentationDocument> createPresentation(
            HttpServletRequest request,
            @RequestBody PresentationDocument body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        PresentationDocument created = firestoreService.createOrUpdatePresentation(uid, body);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresentationDocument> getPresentationDetail(
            HttpServletRequest request,
            @PathVariable String id) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        PresentationDocument doc = firestoreService.getPresentationById(uid, id);
        if (doc == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doc);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PresentationDocument> updatePresentation(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody PresentationDocument body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        body.setId(id);
        PresentationDocument updated = firestoreService.createOrUpdatePresentation(uid, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePresentation(
            HttpServletRequest request,
            @PathVariable String id) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.softDeletePresentation(uid, id));
    }

    @PostMapping("/{id}/regenerate-outline")
    public ResponseEntity<PresentationDocument> regenerateOutline(
            HttpServletRequest request,
            @PathVariable String id,
            @RequestBody Map<String, String> body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        String rawOutline = body != null ? body.get("rawOutline") : "";
        return ResponseEntity.ok(firestoreService.regenerateOutline(uid, id, rawOutline));
    }
}
