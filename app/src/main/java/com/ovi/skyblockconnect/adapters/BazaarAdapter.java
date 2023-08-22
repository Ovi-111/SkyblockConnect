package com.ovi.skyblockconnect.adapters;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.database.BazaarDbHelper;
import com.ovi.skyblockconnect.models.BazaarModelClass;
import com.ovi.skyblockconnect.utilities.MiniTasks;

import java.util.List;

public class BazaarAdapter extends RecyclerView.Adapter<BazaarAdapter.BazaarViewHolder> {

    private final Context context;
    private final List<BazaarModelClass> bazaarList;

    public BazaarAdapter(Context context, List<BazaarModelClass> bazaarList) {
        this.context = context;
        this.bazaarList = bazaarList;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearList() {
        bazaarList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BazaarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_bazaar_item, parent, false);
        return new BazaarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BazaarViewHolder holder, int position) {

        BazaarModelClass bazaar = bazaarList.get(position);

        String nameText = bazaar.getProductId();
        holder.nameTextView.setText(Html.fromHtml(nameText));

        double buyPriceText = bazaar.getBuyPrice();
        holder.BuyPriceTextView.setText(MiniTasks.formatPriceWithComma(buyPriceText));

        double sellPriceText = bazaar.getSellPrice();
        holder.SellPriceTextView.setText(MiniTasks.formatPriceWithComma(sellPriceText));

        holder.favoriteStatus.setOnClickListener(v -> {
            bazaar.setFavorite(!bazaar.isFavorite());
            BazaarDbHelper dbHelper = new BazaarDbHelper(context);
            dbHelper.updateFavoriteStatus(bazaar.getProductId(), bazaar.isFavorite());

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(holder.favoriteStatus, "scaleX", 0f, 1.5f, 1f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(holder.favoriteStatus, "scaleY", 0f, 1.5f, 1f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(holder.favoriteStatus, "alpha", 0f, 1f);

            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(holder.favoriteStatus, "rotation", 0f, 360f);
            rotationAnimator.setInterpolator(new DecelerateInterpolator());
            rotationAnimator.setDuration(500);

            animatorSet.playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator, rotationAnimator);
            animatorSet.setDuration(500);

            animatorSet.start();

            if (bazaar.isFavorite()) {
                ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.BLACK, Color.RED);
                colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        holder.favoriteStatus.setColorFilter((int) animation.getAnimatedValue());
                    }
                });
                colorAnimator.setDuration(500);
                colorAnimator.start();
                holder.favoriteStatus.setBackgroundResource(R.drawable.baseline_favorite_24);
            } else {
                ValueAnimator colorAnimator = ValueAnimator.ofArgb(Color.RED, Color.BLACK);
                colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        holder.favoriteStatus.setColorFilter((int) animation.getAnimatedValue());
                    }
                });
                colorAnimator.setDuration(500);
                colorAnimator.start();
                holder.favoriteStatus.setBackgroundResource(R.drawable.baseline_unfavorite_24);
            }
        });

        if (bazaar.isFavorite()) {
            holder.favoriteStatus.setBackgroundResource(R.drawable.baseline_favorite_24);
        } else {
            holder.favoriteStatus.setBackgroundResource(R.drawable.baseline_unfavorite_24);
        }
    }

    public int getItemCount() {
        if (bazaarList == null) {
            return 0;
        }
        return bazaarList.size();
    }

    static class BazaarViewHolder extends RecyclerView.ViewHolder {

        private final TextView nameTextView, BuyPriceTextView, SellPriceTextView;
        private final CardView cardView;
        private final ImageView favoriteStatus;

        BazaarViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_textview);
            BuyPriceTextView = itemView.findViewById(R.id.buy_price_textview);
            SellPriceTextView = itemView.findViewById(R.id.sell_price_textview);
            cardView = itemView.findViewById(R.id.auction_card_view);
            favoriteStatus = itemView.findViewById(R.id.bazaar_favorite_button);
        }
    }
}

