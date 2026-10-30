package com.pitchcoach.app.ui.profile;

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
import com.pitchcoach.app.databinding.FragmentProfileBinding;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

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

        binding.reminderSwitch.setOnCheckedChangeListener((button, checked) ->
                com.pitchcoach.app.ui.util.UiUtils.showSnackbar(
                        binding.getRoot(),
                        getString(checked ? R.string.setting_reminder : R.string.setting_time)));

        binding.timeRow.setOnClickListener(v -> openTimePicker());

        binding.logoutButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_profile_to_login));
    }

    private void openTimePicker() {
        new TimePickerDialog(requireContext(), (picker, hourOfDay, minute) ->
                binding.reminderTime.setText(String.format(Locale.US, "%02d:%02d",
                        hourOfDay, minute)), 20, 0, true).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
