package com.pitchcoach.app.ui.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.pitchcoach.app.R;
import com.pitchcoach.app.data.SampleData;
import com.pitchcoach.app.databinding.FragmentAnswerQuestionBinding;
import com.pitchcoach.app.model.Panelist;

public class AnswerQuestionFragment extends Fragment {

    private FragmentAnswerQuestionBinding binding;
    private int index = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAnswerQuestionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            index = getArguments().getInt("index", 0);
        }
        Panelist person = SampleData.createPanelists(requireContext()).get(index);

        binding.appBar.appBarTitle.setText(getString(R.string.answer_title, index + 1));
        binding.appBar.backButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.answerAvatar.setText(person.initials);
        binding.answerName.setText(person.name);
        binding.speechBubble.setText(getString(R.string.quoted_question, person.question));
        binding.answerWaveform.setBarCount(17);

        if (person.tone == Panelist.TONE_ORANGE) {
            binding.answerAvatar.setBackgroundResource(R.drawable.bg_persona_orange);
            binding.answerAvatar.setTextColor(
                    getResources().getColor(R.color.audience_orange_text, null));
        } else if (person.tone == Panelist.TONE_GREEN) {
            binding.answerAvatar.setBackgroundResource(R.drawable.bg_persona_green);
            binding.answerAvatar.setTextColor(
                    getResources().getColor(R.color.green_dark, null));
        }

        binding.answerRecordButton.setOnClickListener(v -> showFeedback());
    }

    private void showFeedback() {
        FeedbackSheetDialogFragment sheet = new FeedbackSheetDialogFragment();
        Bundle args = new Bundle();
        args.putInt("index", index);
        sheet.setArguments(args);
        sheet.setListener(new FeedbackSheetDialogFragment.Listener() {
            @Override
            public void onRetry() {
            }

            @Override
            public void onNext(boolean last) {
                androidx.navigation.NavController navController =
                        NavHostFragment.findNavController(AnswerQuestionFragment.this);
                if (last) {
                    navController.navigate(R.id.action_answer_to_progress);
                    return;
                }
                androidx.navigation.NavBackStackEntry previous =
                        navController.getPreviousBackStackEntry();
                if (previous != null) {
                    previous.getSavedStateHandle()
                            .set(AiPanelFragment.NEXT_INDEX_KEY, index + 1);
                }
                navController.popBackStack();
            }
        });
        sheet.show(getParentFragmentManager(), "feedback");
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
