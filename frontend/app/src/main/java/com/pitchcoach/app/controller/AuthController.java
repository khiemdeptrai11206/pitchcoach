package com.pitchcoach.app.controller;

public class AuthController {

    public interface AuthCallback {
        void onLoginSuccess();
        void onLogoutSuccess();
    }

    public void loginWithGoogle(AuthCallback callback) {
        if (callback != null) {
            callback.onLoginSuccess();
        }
    }

    public void loginWithEmail(AuthCallback callback) {
        if (callback != null) {
            callback.onLoginSuccess();
        }
    }

    public void logout(AuthCallback callback) {
        if (callback != null) {
            callback.onLogoutSuccess();
        }
    }
}
