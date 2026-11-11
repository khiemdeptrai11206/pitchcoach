package com.pitchcoach.app.view.practice;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PracticeController;
import com.pitchcoach.app.databinding.FragmentRecordingBinding;
import com.pitchcoach.app.model.remote.BackendApiClient;
import com.pitchcoach.app.view.util.UiUtils;

import java.io.File;
import java.util.Locale;

public class RecordingFragment extends Fragment {

    private static final String TAG = "RecordingFragment";

    private FragmentRecordingBinding binding;
    private final PracticeController controller = new PracticeController();
    private boolean paused = false;
    private boolean outlineOpen = false;
    private ObjectAnimator pulseOne;
    private ObjectAnimator pulseTwo;

    private MediaRecorder mediaRecorder;
    private File audioOutputFile;
    private int elapsedSeconds = 0;
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (!paused && binding != null) {
                elapsedSeconds++;
                int mins = elapsedSeconds / 60;
                int secs = elapsedSeconds % 60;
                binding.recordTime.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    private final ActivityResultLauncher<String> micPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    startAudioRecorder();
                }
            });

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
        binding.stopButton.setOnClickListener(v -> stopRecordingAndAnalyze());

        startPulse();
        checkMicAndStartRecording();
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void checkMicAndStartRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startAudioRecorder();
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startAudioRecorder() {
        try {
            audioOutputFile = new File(requireContext().getCacheDir(), "latest_pitch.m4a");
            if (audioOutputFile.exists()) {
                audioOutputFile.delete();
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioEncodingBitRate(128000);
            mediaRecorder.setAudioSamplingRate(44100);
            mediaRecorder.setOutputFile(audioOutputFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            Log.w(TAG, "MediaRecorder unavailable on emulator/device: " + e.getMessage());
            mediaRecorder = null;
        }
    }

    private void stopAudioRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
            } catch (Exception ignored) {
            }
            try {
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            mediaRecorder = null;
        }
    }

    private void stopRecordingAndAnalyze() {
        timerHandler.removeCallbacksAndMessages(null);
        stopAudioRecorder();
        BackendApiClient.getInstance().analyzeRecordingOnBackend(
                requireContext(), audioOutputFile, Math.max(10, elapsedSeconds));
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_record_to_analyzing);
    }

    private void togglePause() {
        paused = !paused;
        binding.waveform.setPaused(paused);
        binding.statusLabel.setText(paused ? R.string.recording_paused : R.string.recording_status);
        binding.pauseButton.setText(paused ? R.string.resume : R.string.pause);
        binding.pauseButton.setIconResource(paused ? R.drawable.ic_play : R.drawable.ic_pause);
        binding.micButton.setContentDescription(getString(
                paused ? R.string.resume_recording : R.string.pause_recording));
        if (mediaRecorder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                if (paused) {
                    mediaRecorder.pause();
                } else {
                    mediaRecorder.resume();
                }
            } catch (Exception ignored) {
            }
        }
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
        timerHandler.removeCallbacksAndMessages(null);
        stopAudioRecorder();
        pausePulse();
        binding = null;
    }
}
