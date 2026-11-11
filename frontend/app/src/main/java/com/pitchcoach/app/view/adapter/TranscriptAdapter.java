package com.pitchcoach.app.view.adapter;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.pitchcoach.app.R;
import com.pitchcoach.app.model.TranscriptLine;

import java.util.ArrayList;
import java.util.List;

public class TranscriptAdapter extends RecyclerView.Adapter<TranscriptAdapter.TranscriptViewHolder> {

    public interface Listener {
        void onLineSelected(TranscriptLine line, int position);
    }

    private final List<TranscriptLine> lines = new ArrayList<>();
    private final Listener listener;
    private int playingPosition = RecyclerView.NO_POSITION;

    public TranscriptAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<TranscriptLine> items) {
        lines.clear();
        lines.addAll(items);
        notifyDataSetChanged();
    }

    public void setPlayingPosition(int position) {
        int previous = playingPosition;
        playingPosition = position;
        if (previous != RecyclerView.NO_POSITION) {
            notifyItemChanged(previous);
        }
        if (position != RecyclerView.NO_POSITION) {
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public TranscriptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transcript, parent, false);
        return new TranscriptViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TranscriptViewHolder holder, int position) {
        holder.bind(position, lines.get(position), position == playingPosition, listener);
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    static class TranscriptViewHolder extends RecyclerView.ViewHolder {
        private final TextView sentenceNumber;
        private final TextView sentence;
        private final ImageView playIcon;

        TranscriptViewHolder(@NonNull View itemView) {
            super(itemView);
            sentenceNumber = itemView.findViewById(R.id.sentence_number);
            sentence = itemView.findViewById(R.id.sentence);
            playIcon = itemView.findViewById(R.id.play_icon);
        }

        void bind(int position, TranscriptLine line, boolean playing, Listener listener) {
            sentenceNumber.setText(itemView.getContext().getString(R.string.step_number, position + 1));

            SpannableString text = new SpannableString(line.sentence);
            if (line.highlight != null && !line.highlight.isEmpty()) {
                int start = line.sentence.indexOf(line.highlight);
                if (start >= 0) {
                    int end = start + line.highlight.length();
                    text.setSpan(new BackgroundColorSpan(ContextCompat.getColor(
                                    itemView.getContext(), R.color.transcript_highlight_bg)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.setSpan(new ForegroundColorSpan(ContextCompat.getColor(
                                    itemView.getContext(), R.color.transcript_highlight_text)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    text.setSpan(new StyleSpan(Typeface.BOLD), start, end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            sentence.setText(text);
            playIcon.setAlpha(playing ? 1f : 0f);
            itemView.setOnClickListener(v -> listener.onLineSelected(line, position));
        }
    }
}
