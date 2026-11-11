package com.pitchcoach.app.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.pitchcoach.app.R;
import com.pitchcoach.app.model.PresentationItem;

public class PresentationAdapter extends ListAdapter<PresentationItem, PresentationAdapter.PresentationViewHolder> {

    public interface Listener {
        void onOpen(PresentationItem item);

        void onEdit(PresentationItem item);

        void onDelete(PresentationItem item);
    }

    private static final DiffUtil.ItemCallback<PresentationItem> DIFF =
            new DiffUtil.ItemCallback<PresentationItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull PresentationItem oldItem,
                                               @NonNull PresentationItem newItem) {
                    return oldItem.title.equals(newItem.title);
                }

                @Override
                public boolean areContentsTheSame(@NonNull PresentationItem oldItem,
                                                  @NonNull PresentationItem newItem) {
                    return oldItem.title.equals(newItem.title)
                            && oldItem.deadline.equals(newItem.deadline)
                            && oldItem.countdown.equals(newItem.countdown)
                            && oldItem.practices == newItem.practices
                            && oldItem.score.equals(newItem.score)
                            && oldItem.past == newItem.past
                            && oldItem.practiced == newItem.practiced;
                }
            };

    private final Listener listener;

    public PresentationAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PresentationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_presentation, parent, false);
        return new PresentationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresentationViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class PresentationViewHolder extends RecyclerView.ViewHolder {
        private final TextView audienceChip;
        private final TextView title;
        private final TextView deadline;
        private final TextView countdown;
        private final TextView practices;
        private final TextView scoreValue;
        private final TextView scoreLabel;
        private final TextView notPracticedBadge;
        private final View scoreBadge;
        private final MaterialButton menuButton;

        PresentationViewHolder(@NonNull View itemView) {
            super(itemView);
            audienceChip = itemView.findViewById(R.id.audience_chip);
            title = itemView.findViewById(R.id.title);
            deadline = itemView.findViewById(R.id.deadline);
            countdown = itemView.findViewById(R.id.countdown);
            practices = itemView.findViewById(R.id.practices);
            scoreValue = itemView.findViewById(R.id.score_value);
            scoreLabel = itemView.findViewById(R.id.score_label);
            notPracticedBadge = itemView.findViewById(R.id.not_practiced_badge);
            scoreBadge = itemView.findViewById(R.id.score_badge);
            menuButton = itemView.findViewById(R.id.menu_button);
        }

        void bind(PresentationItem item, Listener listener) {
            Context context = itemView.getContext();
            audienceChip.setText(item.audience);
            title.setText(item.title);
            deadline.setText(context.getString(R.string.deadline_format, item.deadline));
            countdown.setText(item.countdown);
            practices.setText(context.getString(R.string.practices_count, item.practices));

            int audienceTone = item.createdOrder % 3;
            if (audienceTone == 1) {
                audienceChip.setBackgroundResource(R.drawable.bg_audience_chip_orange);
                audienceChip.setTextColor(ContextCompat.getColor(context, R.color.audience_orange_text));
            } else if (audienceTone == 2) {
                audienceChip.setBackgroundResource(R.drawable.bg_audience_chip_green);
                audienceChip.setTextColor(ContextCompat.getColor(context, R.color.green_dark));
            } else {
                audienceChip.setBackgroundResource(R.drawable.bg_audience_chip);
                audienceChip.setTextColor(ContextCompat.getColor(context, R.color.primary));
            }

            if (item.past) {
                countdown.setBackgroundResource(R.drawable.bg_past_chip);
                countdown.setTextColor(ContextCompat.getColor(context, R.color.text_muted));
            } else {
                countdown.setBackgroundResource(R.drawable.bg_countdown_chip);
                countdown.setTextColor(ContextCompat.getColor(context, R.color.amber_text));
            }

            scoreBadge.setVisibility(item.practiced ? View.VISIBLE : View.GONE);
            notPracticedBadge.setVisibility(item.practiced ? View.GONE : View.VISIBLE);
            notPracticedBadge.setText(R.string.not_practiced);
            scoreValue.setText(item.score);
            scoreLabel.setText(R.string.score_label);
            itemView.setAlpha(item.past ? 0.68f : 1f);

            itemView.setOnClickListener(v -> listener.onOpen(item));
            menuButton.setOnClickListener(v -> showMenu(menuButton, item, listener));
        }

        private void showMenu(View anchor, PresentationItem item, Listener listener) {
            PopupMenu popupMenu = new PopupMenu(anchor.getContext(), anchor);
            popupMenu.getMenu().add(0, 1, 0, R.string.menu_edit);
            popupMenu.getMenu().add(0, 2, 1, R.string.menu_delete);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == 1) {
                    listener.onEdit(item);
                    return true;
                }
                if (menuItem.getItemId() == 2) {
                    listener.onDelete(item);
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }
    }
}
