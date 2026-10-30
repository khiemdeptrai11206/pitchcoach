package com.pitchcoach.app.ui.util;

import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.pitchcoach.app.R;

public final class NavigationHelper {

    private NavigationHelper() {
    }

    public static void navigateToBottomTab(NavController navController, int destinationId) {
        if (navController == null) {
            return;
        }
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) {
            return;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false, true)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build();
        navController.navigate(destinationId, null, options);
    }
}
