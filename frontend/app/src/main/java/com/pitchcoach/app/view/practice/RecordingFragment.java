package com.pitchcoach.app.view.practice;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PracticeController;
import com.pitchcoach.app.databinding.FragmentRecordingBinding;
import com.pitchcoach.app.view.util.UiUtils;

public class RecordingFragment extends Fragment {

    private FragmentRecordingBinding binding;
    private final PracticeController controller = new PracticeController();
    private boolean paused = false;
    private boolean outlineOpen = false;
    private ObjectAnimator pulseOne;
    private ObjectAnimator pulseTwo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.presentation_mobile);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        for (String item : controller.getOutline(requireContext())) {
            TextView textView = new TextView(requireContext());
            textView.setText(item);
            textView.setTextColor(getResources().getColor(R.color.text_muted, null));
            textView.setTextSize(12);
            textView.setPadding(0, UiUtils.dp(requireContext(), 4), 0,
                    UiUtils.dp(requireContext(), 4));
            binding.outlineList.addView(textView);
        }

        binding.micButton.setOnClickListener(v -> togglePause());
        binding.pauseButton.setOnClickListener(v -> togglePause());
        binding.outlineToggle.setOnClickListener(v -> toggleOutline());
        binding.stopButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_record_to_analyzing));

        startPulse();
    }

    private void togglePause() {
        paused = !paused;
        binding.waveform.setPaused(paused);
        binding.statusLabel.setText(paused ? R.string.recording_paused : R.string.recording_status);
        binding.pauseButton.setText(paused ? R.string.resume : R.string.pause);
        binding.pauseButton.setIconResource(paused ? R.drawable.ic_play : R.drawable.ic_pause);
        binding.micButton.setContentDescription(getString(
                paused ? R.string.resume_recording : R.string.pause_recording));
        if (paused) {
            pausePulse();
        } else {
            startPulse();
        }
    }

    private void toggleOutline() {
        outlineOpen = !outlineOpen;
        binding.outlineList.setVisibility(outlineOpen ? View.VISIBLE : View.GONE);
        binding.outlineChevron.animate()
                .rotation(outlineOpen ? -90f : 0f)
                .setDuration(180)
                .start();
    }

    private void startPulse() {
        pausePulse();
        pulseOne = createPulse(binding.pulseRingOne, 0, 1500);
        pulseTwo = createPulse(binding.pulseRingTwo, 600, 1500);
    }

    private ObjectAnimator createPulse(View target, long startDelay, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "alpha", 0.35f, 1f);
        animator.setStartDelay(startDelay);
        animator.setDuration(duration);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
        return animator;
    }

    private void pausePulse() {
        if (pulseOne != null) {
            pulseOne.cancel();
            pulseOne = null;
        }
        if (pulseTwo != null) {
            pulseTwo.cancel();
            pulseTwo = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        pausePulse();
        binding = null;
    }
}
