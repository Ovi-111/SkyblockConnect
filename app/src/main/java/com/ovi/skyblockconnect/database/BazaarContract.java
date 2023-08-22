package com.ovi.skyblockconnect.database;

import android.provider.BaseColumns;

public class BazaarContract {

    public static final class BazaarEntry implements BaseColumns {
        public static final String TABLE_NAME = "bazaar";
        public static final String COLUMN_ITEM_NAME = "item_name";
        public static final String COLUMN_BUY_PRICE = "buy_price";
        public static final String COLUMN_SELL_PRICE = "sell_price";
        public static final String COLUMN_FAVORITE_STATUS = "favorite_status";
    }
}
