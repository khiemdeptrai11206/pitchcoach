package com.pitchcoach.app.view.presentation;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PresentationController;
import com.pitchcoach.app.databinding.FragmentCreatePresentationBinding;

import java.util.Calendar;

public class CreatePresentationFragment extends Fragment {

    private FragmentCreatePresentationBinding binding;
    private final PresentationController controller = new PresentationController();
    private final Handler saveHandler = new Handler(Looper.getMainLooper());
    private boolean saving = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePresentationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.create_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.durationSlider.addOnChangeListener((slider, value, fromUser) ->
                binding.durationValue.setText(getString(R.string.duration_minutes, (int) value)));

        binding.dateInput.setOnClickListener(v -> openDatePicker());
        binding.dateLayout.setEndIconOnClickListener(v -> openDatePicker());

        binding.saveButton.setOnClickListener(v -> saveAndReview());
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) -> {
            String text = controller.formatDate(dayOfMonth, month, year);
            binding.dateInput.setText(text);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void saveAndReview() {
        if (saving) {
            return;
        }
        saving = true;
        binding.saveButton.setEnabled(false);
        binding.saveButton.setText(R.string.ai_reviewing_outline);

        String title = binding.nameInput.getText() != null
                && !binding.nameInput.getText().toString().trim().isEmpty()
                ? binding.nameInput.getText().toString().trim()
                : getString(R.string.presentation_mobile);
        String audience = resolveSelectedAudience();
        int durationMin = (int) binding.durationSlider.getValue();
        String deadline = binding.dateInput.getText() != null
                && !binding.dateInput.getText().toString().trim().isEmpty()
                ? binding.dateInput.getText().toString().trim()
                : getString(R.string.deadline_30_10);
        String rawOutline = binding.outlineInput.getText() != null
                ? binding.outlineInput.getText().toString().trim()
                : "";

        controller.createPresentation(
                requireContext(), title, audience, durationMin, deadline, rawOutline);

        saveHandler.postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }
            saving = false;
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_create_to_detail);
        }, 2000);
    }

    private String resolveSelectedAudience() {
        int checkedId = binding.audienceGroup.getCheckedChipId();
        if (checkedId == R.id.audience_friends) {
            return getString(R.string.audience_friends);
        } else if (checkedId == R.id.audience_recruiter) {
            return getString(R.string.audience_recruiter);
        } else if (checkedId == R.id.audience_other) {
            return getString(R.string.audience_other);
        }
        return getString(R.string.audience_lecturer);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveHandler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
