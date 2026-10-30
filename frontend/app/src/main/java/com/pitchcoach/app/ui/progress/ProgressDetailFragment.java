package com.pitchcoach.app.ui.progress;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
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
import com.pitchcoach.app.data.SampleData;
import com.pitchcoach.app.databinding.FragmentProgressDetailBinding;
import com.pitchcoach.app.ui.adapter.AttemptAdapter;

public class ProgressDetailFragment extends Fragment {

    private FragmentProgressDetailBinding binding;
    private String[] labels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProgressDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.progress_detail_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        labels = new String[]{
                getString(R.string.attempt_label, 1),
                getString(R.string.attempt_label, 2),
                getString(R.string.attempt_label, 3)
        };

        binding.segmentScore.setOnClickListener(v -> selectMode("score"));
        binding.segmentFiller.setOnClickListener(v -> selectMode("filler"));
        binding.segmentSpeed.setOnClickListener(v -> selectMode("speed"));
        selectMode("score");

        addComparisonRow(getString(R.string.score_content), 7, 8);
        addComparisonRow(getString(R.string.score_structure), 6, 8);
        addComparisonRow(getString(R.string.score_clarity), 7, 9);
        addComparisonRow(getString(R.string.score_delivery), 5, 7);

        AttemptAdapter attemptAdapter = new AttemptAdapter(row ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_progress_detail_to_result));
        attemptAdapter.submitList(SampleData.createAttempts(requireContext()));
        binding.attemptList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.attemptList.setAdapter(attemptAdapter);

        String insight = getString(R.string.insight_bold) + getString(R.string.insight_rest);
        binding.insightText.setText(insight);

        binding.taskProgressFill.post(() -> {
            ViewGroup parent = (ViewGroup) binding.taskProgressFill.getParent();
            ViewGroup.LayoutParams params = binding.taskProgressFill.getLayoutParams();
            params.width = (int) (parent.getWidth() * 0.66f);
            binding.taskProgressFill.setLayoutParams(params);
        });

        binding.practiceRoundFourButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_progress_detail_to_record));
    }

    private void selectMode(String mode) {
        int selected = getResources().getColor(R.color.text_primary, null);
        int unselected = getResources().getColor(R.color.text_muted, null);
        View active = null;
        if ("score".equals(mode)) {
            binding.segmentScore.setBackgroundResource(R.drawable.bg_segment_selected);
            binding.segmentScore.setTextColor(selected);
            binding.segmentFiller.setBackgroundResource(android.R.color.transparent);
            binding.segmentFiller.setTextColor(unselected);
            binding.segmentSpeed.setBackgroundResource(android.R.color.transparent);
            binding.segmentSpeed.setTextColor(unselected);
            active = binding.segmentScore;
            binding.detailChart.setData(SampleData.SCORE_VALUES, labels,
                    Color.parseColor("#5B4BDB"), 5f, 9f, false);
            binding.speedNote.setVisibility(View.GONE);
        } else if ("filler".equals(mode)) {
            binding.segmentFiller.setBackgroundResource(R.drawable.bg_segment_selected);
            binding.segmentFiller.setTextColor(selected);
            binding.segmentScore.setBackgroundResource(android.R.color.transparent);
            binding.segmentScore.setTextColor(unselected);
            binding.segmentSpeed.setBackgroundResource(android.R.color.transparent);
            binding.segmentSpeed.setTextColor(unselected);
            active = binding.segmentFiller;
            binding.detailChart.setData(SampleData.FILLER_VALUES, labels,
                    Color.parseColor("#2EB67D"), 0f, 25f, false);
            binding.speedNote.setVisibility(View.GONE);
        } else {
            binding.segmentSpeed.setBackgroundResource(R.drawable.bg_segment_selected);
            binding.segmentSpeed.setTextColor(selected);
            binding.segmentScore.setBackgroundResource(android.R.color.transparent);
            binding.segmentScore.setTextColor(unselected);
            binding.segmentFiller.setBackgroundResource(android.R.color.transparent);
            binding.segmentFiller.setTextColor(unselected);
            active = binding.segmentSpeed;
            binding.detailChart.setData(SampleData.SPEED_VALUES, labels,
                    Color.parseColor("#FF8A3D"), 110f, 175f, true);
            binding.detailChart.setTargetZone(120f, 150f);
            binding.speedNote.setVisibility(View.VISIBLE);
        }
        if (active != null) {
            active.postInvalidate();
        }
    }

    private void addComparisonRow(String label, int oldScore, int newScore) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        row.setPadding(0, margin, 0, margin);

        TextView labelView = new TextView(requireContext());
        labelView.setText(label);
        labelView.setTextSize(10);
        labelView.setTextColor(getResources().getColor(R.color.text_muted, null));
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.1f);
        row.addView(labelView, labelParams);

        LinearLayout bars = new LinearLayout(requireContext());
        bars.setOrientation(LinearLayout.VERTICAL);
        int height = (int) (7 * getResources().getDisplayMetrics().density);
        int gap = (int) (3 * getResources().getDisplayMetrics().density);

        View oldBar = new View(requireContext());
        oldBar.setBackgroundResource(R.drawable.bg_compare_old_bar);
        LinearLayout.LayoutParams oldParams = new LinearLayout.LayoutParams(0, height);
        oldParams.width = (int) (oldScore * 10 * getResources().getDisplayMetrics().density * 1.2f);
        oldParams.bottomMargin = gap;
        bars.addView(oldBar, oldParams);

        View newBar = new View(requireContext());
        newBar.setBackgroundResource(R.drawable.bg_compare_new_bar);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(0, height);
        newParams.width = (int) (newScore * 10 * getResources().getDisplayMetrics().density * 1.2f);
        bars.addView(newBar, newParams);

        LinearLayout.LayoutParams barsParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f);
        row.addView(bars, barsParams);

        TextView scoreView = new TextView(requireContext());
        scoreView.setText(oldScore + " → " + newScore);
        scoreView.setTextSize(10);
        scoreView.setTypeface(null, android.graphics.Typeface.BOLD);
        scoreView.setTextColor(getResources().getColor(R.color.text_primary, null));
        scoreView.setGravity(Gravity.END);
        LinearLayout.LayoutParams scoreParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.9f);
        row.addView(scoreView, scoreParams);

        binding.comparisonRows.addView(row, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
