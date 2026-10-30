package com.pitchcoach.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.pitchcoach.app.R;
import com.pitchcoach.app.databinding.FragmentHomeBinding;
import com.pitchcoach.app.ui.util.NavigationHelper;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.profileAvatar.setOnClickListener(v ->
                NavigationHelper.navigateToBottomTab(
                        NavHostFragment.findNavController(this), R.id.profileFragment));

        binding.openDetailButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_home_to_detail));

        binding.viewProgressButton.setOnClickListener(v ->
                NavigationHelper.navigateToBottomTab(
                        NavHostFragment.findNavController(this), R.id.progressFragment));

        binding.viewAllButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("mode", "detail");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_home_to_all, args);
        });

        binding.otherSessionCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_home_to_detail));

        binding.newSessionButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_home_to_create));

        binding.notificationDemo.setOnClickListener(v -> showNotificationPreview());
    }

    private void showNotificationPreview() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notification_title)
                .setMessage(R.string.notification_body)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
