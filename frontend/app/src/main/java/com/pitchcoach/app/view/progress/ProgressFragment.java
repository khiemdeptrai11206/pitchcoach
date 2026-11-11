package com.pitchcoach.app.view.progress;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.pitchcoach.app.R;
import com.pitchcoach.app.controller.ProgressController;
import com.pitchcoach.app.databinding.FragmentProgressBinding;
import com.pitchcoach.app.view.adapter.PracticeSessionAdapter;
import com.pitchcoach.app.view.util.UiUtils;

public class ProgressFragment extends Fragment {

    private FragmentProgressBinding binding;
    private final ProgressController controller = new ProgressController();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.progress_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        String[] labels = {getString(R.string.attempt_label, 1),
                getString(R.string.attempt_label, 2),
                getString(R.string.attempt_label, 3)};

        binding.scoreChart.setData(controller.getScoreValues(), labels,
                Color.parseColor("#5B4BDB"), 5f, 9f, false);
        binding.fillerChart.setData(controller.getFillerValues(), labels,
                Color.parseColor("#2EB67D"), 0f, 25f, false);

        int[] speeds = controller.getSpeedDisplayValues();
        for (int index = 0; index < speeds.length; index++) {
            View item = LayoutInflater.from(requireContext())
                    .inflate(android.R.layout.simple_list_item_2, binding.speedValuesRow, false);
            TextView value = item.findViewById(android.R.id.text1);
            TextView caption = item.findViewById(android.R.id.text2);
            value.setText(String.valueOf(speeds[index]));
            value.setTextSize(16);
            value.setTypeface(null, android.graphics.Typeface.BOLD);
            caption.setText(getString(R.string.attempt_label, index + 1));
            caption.setTextSize(10);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            item.setLayoutParams(params);
            binding.speedValuesRow.addView(item);
        }

        PracticeSessionAdapter sessionAdapter = new PracticeSessionAdapter(session ->
                UiUtils.showSnackbar(binding.getRoot(), getString(R.string.snackbar_placeholder)));
        sessionAdapter.submitList(controller.getProgressSessions(requireContext()));
        binding.sessionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.sessionList.setAdapter(sessionAdapter);

        binding.chartDetailButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_progress_to_detail));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
