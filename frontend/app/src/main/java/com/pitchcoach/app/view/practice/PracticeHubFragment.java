package com.pitchcoach.app.view.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.databinding.FragmentPracticeBinding;

public class PracticeHubFragment extends Fragment {

    private FragmentPracticeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPracticeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.continuePracticeButton.setOnClickListener(v -> startRecording());
        binding.continueCard.setOnClickListener(v -> startRecording());
        binding.quickPracticeCard.setOnClickListener(v -> startRecording());
        binding.practiceRowOne.setOnClickListener(v -> startRecording());
        binding.practiceRowTwo.setOnClickListener(v -> startRecording());

        binding.panelCard.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_practice_to_panel));

        binding.viewAllButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("mode", "practice");
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_practice_to_all, args);
        });

        binding.continueProgressFill.post(() -> {
            ViewGroup parent = (ViewGroup) binding.continueProgressFill.getParent();
            int width = (int) (parent.getWidth() * 0.72f);
            ViewGroup.LayoutParams params = binding.continueProgressFill.getLayoutParams();
            params.width = width;
            binding.continueProgressFill.setLayoutParams(params);
        });
    }

    private void startRecording() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_practice_to_record);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
