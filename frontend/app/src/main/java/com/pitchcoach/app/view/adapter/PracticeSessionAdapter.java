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
import com.pitchcoach.app.model.PracticeSession;

public class PracticeSessionAdapter extends ListAdapter<PracticeSession, PracticeSessionAdapter.SessionViewHolder> {

    public interface Listener {
        void onOpen(PracticeSession session);
    }

    private static final DiffUtil.ItemCallback<PracticeSession> DIFF =
            new DiffUtil.ItemCallback<PracticeSession>() {
                @Override
                public boolean areItemsTheSame(@NonNull PracticeSession oldItem,
                                               @NonNull PracticeSession newItem) {
                    return oldItem.title.equals(newItem.title)
                            && oldItem.date.equals(newItem.date);
                }

                @Override
                public boolean areContentsTheSame(@NonNull PracticeSession oldItem,
                                                  @NonNull PracticeSession newItem) {
                    return oldItem.title.equals(newItem.title)
                            && oldItem.date.equals(newItem.date)
                            && oldItem.score.equals(newItem.score);
                }
            };

    private final Listener listener;

    public PracticeSessionAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_practice_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView date;
        private final TextView score;

        SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            date = itemView.findViewById(R.id.date);
            score = itemView.findViewById(R.id.score);
        }

        void bind(PracticeSession session, Listener listener) {
            title.setText(session.title);
            date.setText(session.date);
            score.setText(session.score);
            itemView.setOnClickListener(v -> listener.onOpen(session));
        }
    }
}
