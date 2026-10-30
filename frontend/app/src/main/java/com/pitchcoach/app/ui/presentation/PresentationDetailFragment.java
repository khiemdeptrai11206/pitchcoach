package com.pitchcoach.app.ui.presentation;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.pitchcoach.app.R;
import com.pitchcoach.app.data.SampleData;
import com.pitchcoach.app.databinding.FragmentPresentationDetailBinding;
import com.pitchcoach.app.ui.adapter.HistoryAdapter;
import com.pitchcoach.app.ui.adapter.QuestionAdapter;

public class PresentationDetailFragment extends Fragment {

    private FragmentPresentationDetailBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPresentationDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.appBar.appBarTitle.setText(R.string.detail_title);
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        QuestionAdapter questionAdapter = new QuestionAdapter();
        questionAdapter.setItems(SampleData.createQuestions(requireContext()));
        binding.questionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.questionList.setAdapter(questionAdapter);

        HistoryAdapter historyAdapter = new HistoryAdapter(entry ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detail_to_progress_detail));
        historyAdapter.submitList(SampleData.createDetailHistory(requireContext()));
        binding.historyList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.historyList.setAdapter(historyAdapter);

        SpannableString note = new SpannableString(
                getString(R.string.progress_note_prefix)
                        + getString(R.string.progress_note_bold)
                        + getString(R.string.progress_note_suffix));
        int boldStart = getString(R.string.progress_note_prefix).length();
        int boldEnd = boldStart + getString(R.string.progress_note_bold).length();
        note.setSpan(new StyleSpan(Typeface.BOLD), boldStart, boldEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.progressNoteText.setText(note);

        binding.detailTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean history = tab.getPosition() == 1;
                binding.outlinePanel.setVisibility(history ? View.GONE : View.VISIBLE);
                binding.historyPanel.setVisibility(history ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.progressNoteButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detail_to_progress_detail));

        binding.startPracticeButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detail_to_record));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
