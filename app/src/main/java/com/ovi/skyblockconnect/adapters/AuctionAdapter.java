package com.ovi.skyblockconnect.adapters;

import static com.ovi.skyblockconnect.dialogs.AuctionItemDialog.itemDialogShowing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.dialogs.AuctionItemDialog;
import com.ovi.skyblockconnect.models.AuctionModelClass;
import com.ovi.skyblockconnect.utilities.MiniTasks;
import com.ovi.skyblockconnect.utilities.TimeFormatChanger;

import java.util.List;

public class AuctionAdapter extends RecyclerView.Adapter<AuctionAdapter.AuctionViewHolder> {

    private final List<AuctionModelClass> auctionsList;

    public AuctionAdapter(List<AuctionModelClass> auctionsList) {
        this.auctionsList = auctionsList;
    }

    @NonNull
    @Override
    public AuctionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_auction_item, parent, false);
        return new AuctionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuctionViewHolder holder, int position) {
        AuctionModelClass auction = auctionsList.get(position);

        String nameText = auction.getName_textview();
        holder.nameTextView.setText(Html.fromHtml(nameText));

        long priceText = auction.getPrice_textview();
        holder.priceTextView.setText(Html.fromHtml(MiniTasks.formatCoins(priceText)));

        long endTimeInMillis = auction.getEndTimer_textview();
        holder.startTimer(endTimeInMillis, holder.endTimerTextView);


        String statusText = auction.getStatus_textview();
        holder.statusTextView.setText(statusText);


        if (statusText.equals("Sold")) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_white_green));
        } else if (statusText.equals("Expired")) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.light_white_red));
        } else {
            TypedValue typedValue = new TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(androidx.cardview.R.attr.cardBackgroundColor, typedValue, true);
            holder.cardView.setCardBackgroundColor(typedValue.data);
        }


        holder.cardView.setOnClickListener(view -> {
            if (itemDialogShowing)
                return;
            String loreText = auction.getLore_textview();
            String htmlLore = loreText.replaceAll("§0", "</font><font color='#000000'>")
                    .replaceAll("§1", "</font><font color='#0000AA'>")
                    .replaceAll("§2", "</font><font color='#00AA00'>")
                    .replaceAll("§3", "</font><font color='#00AAAA'>")
                    .replaceAll("§4", "</font><font color='#AA0000'>")
                    .replaceAll("§5", "</font><font color='#AA00AA'>")
                    .replaceAll("§6", "</font><font color='#FFAA00'>")
                    .replaceAll("§7", "</font><font color='#AAAAAA'>")
                    .replaceAll("§8", "</font><font color='#555555'>")
                    .replaceAll("§9", "</font><font color='#5555FF'>")
                    .replaceAll("§a", "</font><font color='#55FF55'>")
                    .replaceAll("§b", "</font><font color='#55FFFF'>")
                    .replaceAll("§c", "</font><font color='#FF5555'>")
                    .replaceAll("§d", "</font><font color='#FF55FF'>")
                    .replaceAll("§e", "</font><font color='#FFFF55'>")
                    .replaceAll("§f", "</font><font color='#FFFFFF'>")
                    .replaceAll("§l", "<b>")
                    .replaceAll("§m", "")
                    .replaceAll("§n", "<u>")
                    .replaceAll("§o", "<i>")
                    .replaceAll("§r", "</b></strike></u></i>")
                    .replaceAll("§ka", "<span class=\"obfuscated\"></span>")
                    .replaceAll("§k", "")
                    .replaceAll("\n", "<br>");
            Context context = view.getContext();
            AuctionItemDialog.showDialog(context, nameText, htmlLore, priceText, endTimeInMillis);
        });
    }

    @Override
    public int getItemCount() {
        if (auctionsList == null) {
            return 0;
        }
        return auctionsList.size();
    }

    static class AuctionViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView, priceTextView, endTimerTextView, statusTextView;
        private final CardView cardView;
        private CountDownTimer endTimerCountDownTimer;

        AuctionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_textview);
            priceTextView = itemView.findViewById(R.id.price_textview);
            endTimerTextView = itemView.findViewById(R.id.end_timer_textview);
            statusTextView = itemView.findViewById(R.id.status_textview);
            cardView = itemView.findViewById(R.id.auction_card_view);
        }

        public void startTimer(long endTimeInMillis, TextView endTimerTextView) {
            if (endTimerCountDownTimer != null) {
                endTimerCountDownTimer.cancel();
            }
            endTimerCountDownTimer = new CountDownTimer(endTimeInMillis, 1000) {
                public void onTick(long millisUntilFinished) {
                    String timeRemaining = TimeFormatChanger.convertShortTimestamp(millisUntilFinished / 1000);
                    if (endTimerTextView != null) {
                        endTimerTextView.setText(timeRemaining);
                    }
                }

                @SuppressLint("SetTextI18n")
                public void onFinish() {
                    if (endTimerTextView != null) {
                        endTimerTextView.setText("Sold");
                    }
                }
            }.start();
            endTimerTextView.setText(TimeFormatChanger.convertShortTimestamp(endTimeInMillis / 1000));
        }
    }

}
