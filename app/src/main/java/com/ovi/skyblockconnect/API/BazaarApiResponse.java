package com.ovi.skyblockconnect.API;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class BazaarApiResponse {
    @SerializedName("success")
    private boolean success;
    @SerializedName("products")
    private Map<String, Product> products;

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Product> getProducts() {
        return products;
    }

    public static class Product {
        @SerializedName("product_id")
        private String productId;
        @SerializedName("sell_summary")
        private List<SellSummary> sellSummary;
        @SerializedName("buy_summary")
        private List<BuySummary> buySummary;
        @SerializedName("quick_status")
        private QuickStatus quickStatus;

        public String getProductId() {
            return productId;
        }

        public List<SellSummary> getSellSummary() {
            return sellSummary;
        }

        public List<BuySummary> getBuySummary() {
            return buySummary;
        }

        public QuickStatus getQuickStatus() {
            return quickStatus;
        }
    }

    public static class SellSummary {
        @SerializedName("pricePerUnit")
        private double pricePerUnit;
        @SerializedName("amount")
        private int amount;

        public double getPricePerUnit() {
            return pricePerUnit;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class BuySummary {
        @SerializedName("pricePerUnit")
        private double pricePerUnit;
        @SerializedName("amount")
        private int amount;

        public double getPricePerUnit() {
            return pricePerUnit;
        }

        public int getAmount() {
            return amount;
        }
    }

    public static class QuickStatus {
        @SerializedName("productId")
        private String productId;
        @SerializedName("buyPrice")
        private double buyPrice;
        @SerializedName("sellPrice")
        private double sellPrice;
        @SerializedName("buyVolume")
        private double buyVolume;
        @SerializedName("sellVolume")
        private double sellVolume;
        @SerializedName("weekHypixelEarnings")
        private double weekHypixelEarnings;

        public String getProductId() {
            return productId;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public double getSellPrice() {
            return sellPrice;
        }

        public double getBuyVolume() {
            return buyVolume;
        }

        public double getSellVolume() {
            return sellVolume;
        }

        public double getWeekHypixelEarnings() {
            return weekHypixelEarnings;
        }
    }
}
