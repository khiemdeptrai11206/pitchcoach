package com.pitchcoach.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.pitchcoach.app.R;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final List<String> questions = new ArrayList<>();

    public void setItems(List<String> items) {
        questions.clear();
        questions.addAll(items);
        notifyDataSetChanged();
    }

    public String getItem(int position) {
        return questions.get(position);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        holder.bind(position, questions.get(position));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView number;
        private final TextView question;

        QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.number);
            question = itemView.findViewById(R.id.question);
        }

        void bind(int position, String text) {
            number.setText(itemView.getContext().getString(R.string.step_number, position + 1));
            question.setText(text);
        }
    }
}
