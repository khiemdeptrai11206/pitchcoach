package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.UserDocument;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final FirestoreService firestoreService;

    public UserController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @PostMapping("/auth/sync-user")
    public ResponseEntity<UserDocument> syncUser(
            HttpServletRequest request,
            @RequestBody(required = false) Map<String, String> body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        String email = (String) request.getAttribute(FirebaseAuthFilter.ATTR_EMAIL);
        String displayName = body != null ? body.get("displayName") : null;
        String avatarUrl = body != null ? body.get("avatarUrl") : null;

        UserDocument user = firestoreService.syncUser(uid, email, displayName, avatarUrl);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserDocument> getMyProfile(HttpServletRequest request) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        String email = (String) request.getAttribute(FirebaseAuthFilter.ATTR_EMAIL);
        return ResponseEntity.ok(firestoreService.getUser(uid, email));
    }

    @PatchMapping("/users/me/settings")
    public ResponseEntity<UserDocument> updateSettings(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        Boolean reminderEnabled = body.containsKey("reminderEnabled")
                ? Boolean.valueOf(String.valueOf(body.get("reminderEnabled")))
                : null;
        String reminderTime = body.containsKey("reminderTime")
                ? String.valueOf(body.get("reminderTime"))
                : null;

        UserDocument updated = firestoreService.updateUserSettings(uid, reminderEnabled, reminderTime);
        return ResponseEntity.ok(updated);
    }
}
