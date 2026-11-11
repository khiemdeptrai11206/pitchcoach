package com.pitchcoach.app.view;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pitchcoach.app.R;
import com.pitchcoach.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            window.setNavigationBarColor(getColor(R.color.text_primary));
        } else {
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            setupBottomNavigation();
            observeDestinations();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = binding.bottomNav;
        bottomNav.setOnItemSelectedListener(item -> navigateToTab(item));
    }

    private boolean navigateToTab(@NonNull MenuItem item) {
        int destinationId = item.getItemId();
        if (navController == null) {
            return false;
        }
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == destinationId) {
            return true;
        }
        NavOptions options = new NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, false, true)
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .build();
        try {
            navController.navigate(destinationId, null, options);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private void observeDestinations() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean showBottomNav = id == R.id.homeFragment
                    || id == R.id.practiceFragment
                    || id == R.id.progressFragment
                    || id == R.id.profileFragment;
            binding.bottomNav.setVisibility(showBottomNav ? View.VISIBLE : View.GONE);
            if (showBottomNav) {
                MenuItem item = binding.bottomNav.getMenu().findItem(id);
                if (item != null) {
                    item.setChecked(true);
                }
            }
        });
    }
}
