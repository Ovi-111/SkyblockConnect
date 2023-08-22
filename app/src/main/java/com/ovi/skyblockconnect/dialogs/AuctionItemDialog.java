package com.ovi.skyblockconnect.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.utilities.MiniTasks;
import com.ovi.skyblockconnect.utilities.TimeFormatChanger;

public class AuctionItemDialog {
    public static boolean itemDialogShowing = false;
    private static Dialog dialog;
    @SuppressLint("StaticFieldLeak")
    private static View view;
    private static CountDownTimer endTimerCountDownTimer;

    @SuppressLint("InflateParams")
    public static void showDialog(@NonNull Context context, String name, String lore, long price, long auctionEnd) {

        itemDialogShowing = true;
        dialog = new Dialog(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.dialog_auction_item, null);
        TextView itemName = view.findViewById(R.id.item_name);
        itemName.setText(Html.fromHtml(name));
        TextView itemLore = view.findViewById(R.id.item_lore);
        itemLore.setText(Html.fromHtml(lore));
        TextView itemPrice = view.findViewById(R.id.item_price);
        itemPrice.setText(MiniTasks.formatPriceWithComma(price));
        TextView remainingTime = view.findViewById(R.id.remaining_time);
        TextView remainingTimeText = view.findViewById(R.id.remaining_time_text);

        Button dismissButton = view.findViewById(R.id.dismiss_button);
        dismissButton.setOnClickListener(v -> {
            dialog.dismiss();
            itemDialogShowing = false;
        });

        // Set a transparent background for the dialog window
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the custom content view
        dialog.setContentView(view);

        // Set the width and height of the dialog window
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);

        // Show the dialog
        dialog.show();

        startTimer(auctionEnd, remainingTime, remainingTimeText);

        dialog.setOnCancelListener(dialogInterface -> {
            if (itemDialogShowing) {
                itemDialogShowing = false;
            }
        });
    }

    public static void startTimer(long endTimeInMillis, TextView endTimerTextView, TextView TimerText) {
        if (endTimerCountDownTimer != null) {
            endTimerCountDownTimer.cancel();
        }
        endTimerCountDownTimer = new CountDownTimer(endTimeInMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                int timeRemainingInSeconds = (int) (millisUntilFinished / 1000);
                String timeRemainingFormatted = TimeFormatChanger.convertShortTimestamp(timeRemainingInSeconds);
                endTimerTextView.setText(timeRemainingFormatted);
            }

            @SuppressLint("SetTextI18n")
            public void onFinish() {
                endTimerTextView.setText("Sold");
                TimerText.setText("");
            }

        }.start();
    }
}
