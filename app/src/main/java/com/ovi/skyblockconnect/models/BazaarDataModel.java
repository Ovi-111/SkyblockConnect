package com.ovi.skyblockconnect.models;

import java.util.HashMap;
import java.util.Map;

public class BazaarDataModel {
    private Map<String, BzData> productData;

    public BazaarDataModel() {
        this.productData = new HashMap<>();
    }

    public Map<String, BzData> getProductData() {
        return productData;
    }

    public void setProductData(Map<String, BzData> productData) {
        this.productData = productData;
    }

    public void addProduct(String productId, BzData bzData) {
        productData.put(productId, bzData);
    }

    public static class BzData {
        private Map<String, PriceData> bz;

        public BzData() {
            this.bz = new HashMap<>();
        }

        public Map<String, PriceData> getBz() {
            return bz;
        }

        public void setBz(Map<String, PriceData> bz) {
            this.bz = bz;
        }

        public void addPriceData(String time, PriceData priceData) {
            bz.put(time, priceData);
        }
    }

    public static class PriceData {
        private double b;
        private double s;

        public PriceData() {
        }

        public double getB() {
            return b;
        }

        public void setB(double b) {
            this.b = b;
        }

        public double getS() {
            return s;
        }

        public void setS(double s) {
            this.s = s;
        }
    }
}
