package com.pitchcoach.backend.controller;

import com.pitchcoach.backend.model.SyncPayload;
import com.pitchcoach.backend.security.FirebaseAuthFilter;
import com.pitchcoach.backend.service.FirestoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final FirestoreService firestoreService;

    public SyncController(FirestoreService firestoreService) {
        this.firestoreService = firestoreService;
    }

    @PostMapping
    public ResponseEntity<SyncPayload.SyncResponse> sync(
            HttpServletRequest request,
            @RequestBody SyncPayload.SyncRequest syncRequest) throws Exception {
        String uid = (String) request.getAttribute(FirebaseAuthFilter.ATTR_UID);
        return ResponseEntity.ok(firestoreService.syncData(uid, syncRequest));
    }
}
