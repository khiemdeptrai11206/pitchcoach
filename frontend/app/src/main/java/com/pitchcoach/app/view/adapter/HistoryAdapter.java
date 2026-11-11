package com.pitchcoach.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pitchcoach.app.R;
import com.pitchcoach.app.model.HistoryEntry;

public class HistoryAdapter extends ListAdapter<HistoryEntry, HistoryAdapter.HistoryViewHolder> {

    public interface Listener {
        void onOpen(HistoryEntry entry);
    }

    private static final DiffUtil.ItemCallback<HistoryEntry> DIFF =
            new DiffUtil.ItemCallback<HistoryEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull HistoryEntry oldItem,
                                               @NonNull HistoryEntry newItem) {
                    return oldItem.number.equals(newItem.number);
                }

                @Override
                public boolean areContentsTheSame(@NonNull HistoryEntry oldItem,
                                                  @NonNull HistoryEntry newItem) {
                    return oldItem.number.equals(newItem.number)
                            && oldItem.title.equals(newItem.title)
                            && oldItem.date.equals(newItem.date)
                            && oldItem.score.equals(newItem.score)
                            && oldItem.amber == newItem.amber;
                }
            };

    private final Listener listener;

    public HistoryAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history_entry, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView number;
        private final TextView title;
        private final TextView date;
        private final TextView score;
        private final LinearLayout scoreBadge;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            score = itemView.findViewById(R.id.score);
            scoreBadge = itemView.findViewById(R.id.score_badge);
        }

        void bind(HistoryEntry entry, Listener listener) {
            number.setText(entry.number);
            title.setText(entry.title);
            date.setText(entry.date);
            score.setText(entry.score);
            int background = entry.amber
                    ? R.drawable.bg_score_badge_amber
                    : R.drawable.bg_score_badge_green;
            int foreground = entry.amber ? R.color.amber_text : R.color.green_dark;
            scoreBadge.setBackgroundResource(background);
            score.setTextColor(ContextCompat.getColor(score.getContext(), foreground));
            itemView.setOnClickListener(v -> listener.onOpen(entry));
        }
    }
}
