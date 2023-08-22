package com.ovi.skyblockconnect.dialogs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ovi.skyblockconnect.R;
import com.ovi.skyblockconnect.utilities.HypixelTimers;
import com.ovi.skyblockconnect.utilities.TimeFormatChanger;


public class MayorPerksDialog {
    public static AlertDialog dialog;
    public static boolean mayorDialogIsShowing = false;
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    @SuppressLint("SetTextI18n")
    public static void showDialog(Context context, String perks, String name, String time) {

        if (!mayorDialogIsShowing) {
            if (name != null && !name.isEmpty()) {
                MayorPerksDialog.context = context;
                mayorDialogIsShowing = true;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.dialog_mayor_perks, null);
                builder.setView(dialogView);

                dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                TextView mayor_perks = dialogView.findViewById(R.id.mayor_perks2);
                mayor_perks.setText(Html.fromHtml(perks));
                TextView mayor_perks_title = dialogView.findViewById(R.id.mayor_perks_title);
                mayor_perks_title.setText(name + " Perks");

                TextView timer = dialogView.findViewById(R.id.remaining_time);
                if (time.equals("Current")) {
                    final Handler handler = new Handler();
                    final Runnable updateTime = new Runnable() {
                        @Override
                        public void run() {
                            HypixelTimers[] nextEvents = HypixelTimers.getEventsList();
                            HypixelTimers firstEvent = nextEvents[8];
                            String time = "<font color='#cc6c60'>Remaining Time: </font>" + "<font color='#19bf8d'>" + TimeFormatChanger.convertTimestamp(firstEvent.getEventTimer());
                            timer.setText(Html.fromHtml(time));
                            handler.postDelayed(this, 1000);
                        }
                    };
                    handler.post(updateTime);
                }
                if (time.equals("Next")) {
                    {
                        final Handler handler = new Handler();
                        final Runnable updateTime = new Runnable() {
                            @Override
                            public void run() {

                                HypixelTimers[] nextEvents = HypixelTimers.getEventsList();
                                HypixelTimers firstEvent = nextEvents[8];
                                String time = "<font color='#4287f5'>Mayor " + name + " in:  </font>" + "<font color='#19bf8d'>" + TimeFormatChanger.convertTimestamp(firstEvent.getEventTimer());

                                timer.setText(Html.fromHtml(time));
                                handler.postDelayed(this, 1000);
                            }
                        };
                        handler.post(updateTime);

                    }
                }


                Button dismiss_button = dialogView.findViewById(R.id.dismiss_button);

                dismiss_button.setOnClickListener(v -> {
                    dialog.dismiss();
                    mayorDialogIsShowing = false;
                });

                dialog.setOnCancelListener(dialogInterface -> {
                    if (mayorDialogIsShowing) {
                        mayorDialogIsShowing = false;
                    }
                });
            } else {
                Toast.makeText(context, "There is nothing show.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}

