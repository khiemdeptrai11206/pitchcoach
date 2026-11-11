package com.pitchcoach.app.view.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavBackStackEntry;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.PracticeController;
import com.pitchcoach.app.databinding.FragmentAiPanelBinding;
import com.pitchcoach.app.view.adapter.PanelistAdapter;

public class AiPanelFragment extends Fragment {

    public static final String NEXT_INDEX_KEY = "next_index";

    private FragmentAiPanelBinding binding;
    private final PracticeController controller = new PracticeController();
    private PanelistAdapter adapter;
    private int current = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAiPanelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            current = getArguments().getInt("current", 0);
        }

        binding.appBar.appBarTitle.setText(R.string.panel_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        adapter = new PanelistAdapter((panelist, position) -> {
            current = position;
            Bundle args = new Bundle();
            args.putInt("index", position);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_panel_to_answer, args);
        });
        binding.panelistList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.panelistList.setAdapter(adapter);
        adapter.submitList(controller.getPanelists(requireContext()));
        adapter.setSelectedPosition(current);
        updateProgress();

        NavBackStackEntry previous = NavHostFragment.findNavController(this)
                .getPreviousBackStackEntry();
        if (previous != null) {
            previous.getSavedStateHandle()
                    .<Integer>getLiveData(NEXT_INDEX_KEY)
                    .observe(getViewLifecycleOwner(), value -> {
                        if (value != null) {
                            current = value;
                            adapter.setSelectedPosition(current);
                            updateProgress();
                        }
                    });
        }
    }

    private void updateProgress() {
        binding.panelProgress.setProgress((current + 1) * 100);
        binding.panelProgressLabel.setText(
                getString(R.string.panel_question_progress, current + 1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
