package com.pitchcoach.app.view.practice;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.pitchcoach.app.R;
import com.pitchcoach.app.databinding.DialogFeedbackBinding;

public class FeedbackSheetDialogFragment extends BottomSheetDialogFragment {

    public interface Listener {
        void onRetry();

        void onNext(boolean last);
    }

    private DialogFeedbackBinding binding;
    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogFeedbackBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int index = getArguments() != null ? getArguments().getInt("index", 0) : 0;
        binding.nextButton.setText(index >= 2 ? R.string.view_progress : R.string.next_question);
        binding.retryButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRetry();
            }
            dismiss();
        });
        binding.nextButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNext(index >= 2);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
