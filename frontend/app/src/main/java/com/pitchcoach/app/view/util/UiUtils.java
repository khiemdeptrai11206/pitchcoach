package com.pitchcoach.app.view.util;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public final class UiUtils {

    private UiUtils() {
    }

    public static int dp(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static void showSnackbar(View anchor, String message) {
        if (anchor == null) {
            return;
        }
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(findSnackbarAnchor(anchor))
                .show();
    }

    private static View findSnackbarAnchor(View anchor) {
        View root = anchor.getRootView();
        View bottomNav = root.findViewById(com.pitchcoach.app.R.id.bottom_nav);
        if (bottomNav != null && bottomNav.getVisibility() == View.VISIBLE) {
            return bottomNav;
        }
        return anchor;
    }

    public static void addPressScale(View view) {
        view.setOnTouchListener((target, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    target.animate().scaleX(0.97f).scaleY(0.97f).setDuration(120).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    target.animate().scaleX(1f).scaleY(1f).setDuration(160).start();
                    break;
                default:
                    break;
            }
            return false;
        });
    }
}
