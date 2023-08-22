package com.ovi.skyblockconnect.models;

import android.content.Context;

public class AuctionModelClass {

    private final String name_textview, lore_textview, status_textview;
    private final long price_textview;
    private final int end_timer_textview;
    private final Context context;

    public AuctionModelClass(Context context, String name_textview, String lore_textview, long price_textview, int end_timer_textview, String status_textview) {
        this.name_textview = name_textview;
        this.lore_textview = lore_textview;
        this.price_textview = price_textview;
        this.end_timer_textview = end_timer_textview;
        this.status_textview = status_textview;
        this.context = context;

    }


    public String getName_textview() {
        return name_textview;
    }

    public String getLore_textview() {
        return lore_textview;
    }

    public long getPrice_textview() {
        return price_textview;
    }

    public String getStatus_textview() {
        return status_textview;
    }

    public long getEndTimer_textview() {
        return end_timer_textview;
    }

    public Context getContext() {
        return context;
    }
}
