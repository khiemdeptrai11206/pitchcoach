package com.pitchcoach.app.ui.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.pitchcoach.app.R;
import com.pitchcoach.app.model.Panelist;
import com.pitchcoach.app.ui.util.UiUtils;

public class PanelistAdapter extends ListAdapter<Panelist, PanelistAdapter.PanelistViewHolder> {

    public interface Listener {
        void onAnswer(Panelist panelist, int position);
    }

    private static final DiffUtil.ItemCallback<Panelist> DIFF =
            new DiffUtil.ItemCallback<Panelist>() {
                @Override
                public boolean areItemsTheSame(@NonNull Panelist oldItem, @NonNull Panelist newItem) {
                    return oldItem.name.equals(newItem.name);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Panelist oldItem, @NonNull Panelist newItem) {
                    return oldItem.name.equals(newItem.name)
                            && oldItem.initials.equals(newItem.initials)
                            && oldItem.question.equals(newItem.question)
                            && oldItem.tone == newItem.tone;
                }
            };

    private final Listener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public PanelistAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previous = selectedPosition;
        selectedPosition = position;
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public PanelistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panelist, parent, false);
        return new PanelistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PanelistViewHolder holder, int position) {
        holder.bind(getItem(position), position, position == selectedPosition, listener);
    }

    static class PanelistViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView card;
        private final TextView avatar;
        private final TextView name;
        private final TextView question;
        private final MaterialButton answerButton;

        PanelistViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.persona_card);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            question = itemView.findViewById(R.id.question);
            answerButton = itemView.findViewById(R.id.answer_button);
        }

        void bind(Panelist panelist, int position, boolean selected, Listener listener) {
            name.setText(panelist.name);
            avatar.setText(panelist.initials);
            question.setText(itemView.getContext().getString(R.string.quoted_question, panelist.question));
            answerButton.setContentDescription(
                    itemView.getContext().getString(R.string.panel_answer_description, panelist.name));

            int background;
            int foreground;
            if (panelist.tone == Panelist.TONE_ORANGE) {
                background = R.drawable.bg_persona_orange;
                foreground = R.color.audience_orange_text;
            } else if (panelist.tone == Panelist.TONE_GREEN) {
                background = R.drawable.bg_persona_green;
                foreground = R.color.green_dark;
            } else {
                background = R.drawable.bg_persona_purple;
                foreground = R.color.primary;
            }
            avatar.setBackgroundResource(background);
            avatar.setTextColor(ContextCompat.getColor(itemView.getContext(), foreground));

            if (selected) {
                card.setStrokeWidth(UiUtils.dp(itemView.getContext(), 1.5f));
                card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
            } else {
                card.setStrokeWidth(UiUtils.dp(itemView.getContext(), 0f));
                card.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.transparent));
            }

            answerButton.setOnClickListener(v -> listener.onAnswer(panelist, position));
            itemView.setAlpha(selected ? 1f : 0.92f);
        }
    }
}
