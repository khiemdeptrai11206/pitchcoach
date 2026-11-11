package com.pitchcoach.app.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.pitchcoach.app.R;
import com.pitchcoach.app.model.AttemptRow;

public class AttemptAdapter extends ListAdapter<AttemptRow, AttemptAdapter.AttemptViewHolder> {

    public interface Listener {
        void onAttemptSelected(AttemptRow row);
    }

    private static final DiffUtil.ItemCallback<AttemptRow> DIFF =
            new DiffUtil.ItemCallback<AttemptRow>() {
                @Override
                public boolean areItemsTheSame(@NonNull AttemptRow oldItem, @NonNull AttemptRow newItem) {
                    return oldItem.round.equals(newItem.round);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AttemptRow oldItem, @NonNull AttemptRow newItem) {
                    return oldItem.round.equals(newItem.round)
                            && oldItem.date.equals(newItem.date)
                            && oldItem.duration.equals(newItem.duration)
                            && oldItem.score.equals(newItem.score)
                            && oldItem.speed.equals(newItem.speed)
                            && oldItem.filler.equals(newItem.filler);
                }
            };

    private final Listener listener;

    public AttemptAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AttemptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attempt, parent, false);
        return new AttemptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttemptViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class AttemptViewHolder extends RecyclerView.ViewHolder {
        private final TextView round;
        private final TextView date;
        private final TextView duration;
        private final TextView score;
        private final TextView speed;
        private final TextView filler;

        AttemptViewHolder(@NonNull View itemView) {
            super(itemView);
            round = itemView.findViewById(R.id.round);
            date = itemView.findViewById(R.id.date);
            duration = itemView.findViewById(R.id.duration);
            score = itemView.findViewById(R.id.score);
            speed = itemView.findViewById(R.id.speed);
            filler = itemView.findViewById(R.id.filler);
        }

        void bind(AttemptRow row, Listener listener) {
            round.setText(row.round);
            date.setText(row.date);
            duration.setText(row.duration);
            score.setText(row.score);
            speed.setText(row.speed);
            filler.setText(row.filler);
            itemView.setOnClickListener(v -> listener.onAttemptSelected(row));
        }
    }
}
