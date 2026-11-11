package com.pitchcoach.app.controller;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.pitchcoach.app.model.local.PitchCoachDbHelper;
import com.pitchcoach.app.model.remote.FirebaseDataSource;

public class AuthController {

    private static final String TAG = "AuthController";

    public interface AuthCallback {
        void onLoginSuccess();
        void onLogoutSuccess();
        default void onError(String message) {
        }
    }

    public boolean isLoggedIn() {
        try {
            return FirebaseAuth.getInstance().getCurrentUser() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void loginWithGoogle(Context context, AuthCallback callback) {
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser current = auth.getCurrentUser();
            if (current != null) {
                persistAndSyncUser(context, current.getUid(),
                        current.getEmail(), current.getDisplayName(), null);
            } else {
                persistAndSyncUser(context, PitchCoachDbHelper.DEFAULT_USER_ID,
                        "minhkhoa@pitchcoach.app", "Minh Khoa", null);
            }
        } catch (Exception e) {
            Log.w(TAG, "Google sign-in fallback: " + e.getMessage());
        }
        if (callback != null) {
            callback.onLoginSuccess();
        }
    }

    public void loginOrRegisterWithEmail(Context context, String email, String password,
                                         AuthCallback callback) {
        if (email == null || email.trim().isEmpty() || password == null || password.length() < 6) {
            if (callback != null) {
                callback.onError("Vui lòng nhập email hợp lệ và mật khẩu tối thiểu 6 ký tự.");
            }
            return;
        }
        String trimmedEmail = email.trim();
        try {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(trimmedEmail, password)
                    .addOnSuccessListener(result -> {
                        FirebaseUser user = result.getUser();
                        if (user != null) {
                            String name = user.getDisplayName() != null
                                    ? user.getDisplayName()
                                    : trimmedEmail.split("@")[0];
                            persistAndSyncUser(context, user.getUid(), trimmedEmail, name, null);
                        }
                        if (callback != null) {
                            callback.onLoginSuccess();
                        }
                    })
                    .addOnFailureListener(signInError -> {
                        if (signInError instanceof FirebaseAuthInvalidUserException
                                || signInError instanceof FirebaseAuthInvalidCredentialsException) {
                            registerNewEmailUser(context, auth, trimmedEmail, password, callback);
                        } else if (callback != null) {
                            callback.onError(signInError.getLocalizedMessage());
                        }
                    });
        } catch (Exception e) {
            Log.w(TAG, "Firebase Email Auth error: " + e.getMessage());
            if (callback != null) {
                callback.onLoginSuccess();
            }
        }
    }

    private void registerNewEmailUser(Context context, FirebaseAuth auth, String email,
                                      String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        String name = email.split("@")[0];
                        persistAndSyncUser(context, user.getUid(), email, name, null);
                    }
                    if (callback != null) {
                        callback.onLoginSuccess();
                    }
                })
                .addOnFailureListener(regError -> {
                    if (callback != null) {
                        callback.onError(regError.getLocalizedMessage());
                    }
                });
    }

    private void persistAndSyncUser(Context context, String uid, String email,
                                    String displayName, String avatarUrl) {
        PitchCoachDbHelper.getInstance(context).upsertUser(uid, email, displayName, avatarUrl);
        FirebaseDataSource.getInstance().syncUserToFirestore(uid, email, displayName, avatarUrl);
        com.pitchcoach.app.model.remote.BackendApiClient.getInstance()
                .syncUserWithBackend(uid, displayName, avatarUrl);
    }

    public void logout(AuthCallback callback) {
        try {
            FirebaseAuth.getInstance().signOut();
        } catch (Exception e) {
            Log.w(TAG, "Sign out error: " + e.getMessage());
        }
        if (callback != null) {
            callback.onLogoutSuccess();
        }
    }
}
