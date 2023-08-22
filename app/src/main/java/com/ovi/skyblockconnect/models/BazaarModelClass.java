package com.ovi.skyblockconnect.models;

import android.content.Context;

public class BazaarModelClass {
    private final Context context;
    private final String productId;
    private final double buyPrice;
    private final double sellPrice;
    private boolean favorite;

    public BazaarModelClass(Context context, String productId, double buyPrice, double sellPrice) {
        this.context = context;
        this.productId = productId;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.favorite = false;
    }

    public Context getContext() {
        return context;
    }

    public String getProductId() {
        return productId;
    }

    public double getBuyPrice() {
        return Math.round(buyPrice * 100.0) / 100.0;
    }

    public double getSellPrice() {
        return Math.round(sellPrice * 100.0) / 100.0;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

}
