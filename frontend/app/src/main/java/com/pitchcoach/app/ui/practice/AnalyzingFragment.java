package com.pitchcoach.app.ui.practice;

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
import com.pitchcoach.app.databinding.FragmentAnalyzingBinding;
import com.pitchcoach.app.databinding.ViewAnalysisStepBinding;

public class AnalyzingFragment extends Fragment {

    private FragmentAnalyzingBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int completed = 0;
    private final int[] stepLabels = {
            R.string.analysis_step_one,
            R.string.analysis_step_two,
            R.string.analysis_step_three
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyzingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        for (int index = 0; index < stepLabels.length; index++) {
            ViewAnalysisStepBinding step = ViewAnalysisStepBinding.inflate(
                    getLayoutInflater(), binding.checklistContainer, false);
            step.stepNumber.setText(getString(R.string.step_number, index + 1));
            step.stepLabel.setText(stepLabels[index]);
            binding.checklistContainer.addView(step.getRoot());
        }

        handler.postDelayed(() -> updateStep(1), 1500);
        handler.postDelayed(() -> updateStep(2), 3000);
        handler.postDelayed(() -> updateStep(3), 4500);
        handler.postDelayed(this::goToResult, 5400);
    }

    private void updateStep(int doneCount) {
        if (!isAdded() || binding == null) {
            return;
        }
        completed = doneCount;
        for (int index = 0; index < stepLabels.length; index++) {
            View child = binding.checklistContainer.getChildAt(index);
            if (!(child instanceof View)) {
                continue;
            }
            ViewAnalysisStepBinding step = ViewAnalysisStepBinding.bind(child);
            boolean done = doneCount > index;
            boolean current = doneCount == index;
            step.stepProgress.setVisibility(current ? View.VISIBLE : View.GONE);
            step.stepCheck.setVisibility(done ? View.VISIBLE : View.GONE);
            if (done) {
                step.stepNumber.setVisibility(View.VISIBLE);
                step.stepNumber.setText("");
                step.stepNumber.setBackgroundResource(R.drawable.bg_step_done);
                step.stepLabel.setTextColor(getResources().getColor(R.color.text_primary, null));
            } else if (current) {
                step.stepNumber.setVisibility(View.GONE);
                step.stepLabel.setTextColor(getResources().getColor(R.color.primary, null));
            } else {
                step.stepNumber.setVisibility(View.VISIBLE);
                step.stepNumber.setBackgroundResource(R.drawable.bg_step_badge);
                step.stepLabel.setTextColor(getResources().getColor(R.color.text_muted, null));
            }
        }
    }

    private void goToResult() {
        if (!isAdded()) {
            return;
        }
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_analyzing_to_result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
}
