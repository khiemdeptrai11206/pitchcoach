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
import com.pitchcoach.app.model.Improvement;

public class ImprovementAdapter extends ListAdapter<Improvement, ImprovementAdapter.ImprovementViewHolder> {

    private static final DiffUtil.ItemCallback<Improvement> DIFF =
            new DiffUtil.ItemCallback<Improvement>() {
                @Override
                public boolean areItemsTheSame(@NonNull Improvement oldItem, @NonNull Improvement newItem) {
                    return oldItem.title.equals(newItem.title);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Improvement oldItem, @NonNull Improvement newItem) {
                    return oldItem.title.equals(newItem.title)
                            && oldItem.evidence.equals(newItem.evidence);
                }
            };

    public ImprovementAdapter() {
        super(DIFF);
    }

    @NonNull
    @Override
    public ImprovementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_improvement, parent, false);
        return new ImprovementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImprovementViewHolder holder, int position) {
        holder.bind(position, getItem(position));
    }

    static class ImprovementViewHolder extends RecyclerView.ViewHolder {
        private final TextView number;
        private final TextView title;
        private final TextView evidence;

        ImprovementViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            title = itemView.findViewById(R.id.title);
            evidence = itemView.findViewById(R.id.evidence);
        }

        void bind(int position, Improvement improvement) {
            number.setText(itemView.getContext().getString(R.string.step_number, position + 1));
            title.setText(improvement.title);
            evidence.setText(improvement.evidence);
        }
    }
}
