package com.pitchcoach.app.view.profile;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.AuthController;
import com.pitchcoach.app.controller.ProfileController;
import com.pitchcoach.app.databinding.FragmentProfileBinding;
import com.pitchcoach.app.view.util.UiUtils;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private final ProfileController profileController = new ProfileController();
    private final AuthController authController = new AuthController();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.reminderTime.setText(profileController.getReminderTime(requireContext()));

        binding.reminderSwitch.setOnCheckedChangeListener((button, checked) -> {
            profileController.saveReminderSettings(
                    requireContext(), checked, binding.reminderTime.getText().toString());
            UiUtils.showSnackbar(
                    binding.getRoot(),
                    getString(checked ? R.string.setting_reminder : R.string.setting_time));
        });

        binding.timeRow.setOnClickListener(v -> openTimePicker());

        binding.logoutButton.setOnClickListener(v ->
                authController.logout(new AuthController.AuthCallback() {
                    @Override
                    public void onLoginSuccess() {
                    }

                    @Override
                    public void onLogoutSuccess() {
                        NavHostFragment.findNavController(ProfileFragment.this)
                                .navigate(R.id.action_profile_to_login);
                    }
                }));
    }

    private void openTimePicker() {
        new TimePickerDialog(requireContext(), (picker, hourOfDay, minute) -> {
            String formatted = profileController.formatReminderTime(hourOfDay, minute);
            binding.reminderTime.setText(formatted);
            profileController.saveReminderSettings(
                    requireContext(), binding.reminderSwitch.isChecked(), formatted);
        }, 20, 0, true).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
