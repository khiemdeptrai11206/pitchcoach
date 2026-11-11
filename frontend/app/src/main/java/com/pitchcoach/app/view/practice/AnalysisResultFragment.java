package com.pitchcoach.app.view.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PracticeController;
import com.pitchcoach.app.databinding.FragmentAnalysisResultBinding;
import com.pitchcoach.app.databinding.ViewScoreBarBinding;
import com.pitchcoach.app.view.adapter.ImprovementAdapter;
import com.pitchcoach.app.view.adapter.TranscriptAdapter;
import com.pitchcoach.app.view.util.UiUtils;

public class AnalysisResultFragment extends Fragment {

    private FragmentAnalysisResultBinding binding;
    private final PracticeController controller = new PracticeController();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalysisResultBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.result_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        addScoreBar(getString(R.string.score_content), 8);
        addScoreBar(getString(R.string.score_structure), 8);
        addScoreBar(getString(R.string.score_clarity), 9);
        addScoreBar(getString(R.string.score_delivery), 7);

        binding.scoreRing.setScore(controller.getOverallScore());
        binding.speedGauge.setSpeed(controller.getSpeakingSpeed());

        TranscriptAdapter transcriptAdapter = new TranscriptAdapter((line, position) ->
                UiUtils.showSnackbar(binding.getRoot(), line.sentence));
        transcriptAdapter.setItems(controller.getTranscript(requireContext()));
        binding.transcriptList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.transcriptList.setAdapter(transcriptAdapter);

        ImprovementAdapter improvementAdapter = new ImprovementAdapter();
        improvementAdapter.submitList(controller.getImprovements(requireContext()));
        binding.improvementList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.improvementList.setAdapter(improvementAdapter);

        binding.editTranscriptButton.setOnClickListener(v ->
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.snackbar_placeholder)));

        binding.meetPanelButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_result_to_panel));

        binding.practiceAgainButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_result_to_record));
    }

    private void addScoreBar(String label, int score) {
        ViewScoreBarBinding bar = ViewScoreBarBinding.inflate(
                getLayoutInflater(), binding.scoreBarsContainer, false);
        bar.barLabel.setText(label);
        bar.barValue.setText(String.valueOf(score));
        bar.barProgress.setProgress(score * 10);
        binding.scoreBarsContainer.addView(bar.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
