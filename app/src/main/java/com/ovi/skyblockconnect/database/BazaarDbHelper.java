package com.ovi.skyblockconnect.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ovi.skyblockconnect.models.BazaarModelClass;

import java.util.ArrayList;
import java.util.List;

public class BazaarDbHelper extends SQLiteOpenHelper {
    public static final int PAGE_SIZE = 10;
    private static final String DATABASE_NAME = "bazaar.db";
    private static final int DATABASE_VERSION = 1;

    public BazaarDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_BAZAAR_TABLE = "CREATE TABLE " + BazaarContract.BazaarEntry.TABLE_NAME + " (" +
                BazaarContract.BazaarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                BazaarContract.BazaarEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, " +
                BazaarContract.BazaarEntry.COLUMN_BUY_PRICE + " TEXT NOT NULL, " +
                BazaarContract.BazaarEntry.COLUMN_SELL_PRICE + " TEXT NOT NULL, " +
                BazaarContract.BazaarEntry.COLUMN_FAVORITE_STATUS + " INTEGER NOT NULL DEFAULT 0);";
        db.execSQL(SQL_CREATE_BAZAAR_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + BazaarContract.BazaarEntry.TABLE_NAME);
        onCreate(db);
    }


    public List<BazaarModelClass> getItems(Context context, int page, String searchQuery, String sortBy, String sortOrderASC) {
        List<BazaarModelClass> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        int offset = page * PAGE_SIZE;

        String[] projection = {
                BazaarContract.BazaarEntry._ID,
                BazaarContract.BazaarEntry.COLUMN_ITEM_NAME,
                BazaarContract.BazaarEntry.COLUMN_BUY_PRICE,
                BazaarContract.BazaarEntry.COLUMN_SELL_PRICE,
                BazaarContract.BazaarEntry.COLUMN_FAVORITE_STATUS
        };


        String sortOrder = BazaarContract.BazaarEntry.COLUMN_FAVORITE_STATUS + " DESC, " +
                sortBy + " " + sortOrderASC;
        String limit = String.valueOf(PAGE_SIZE);
        String offsetStr = String.valueOf(offset);


        String selection = BazaarContract.BazaarEntry.COLUMN_ITEM_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + searchQuery.replace(" ", "%") + "%"};

        Cursor cursor = db.query(
                BazaarContract.BazaarEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder,
                limit + " OFFSET " + offsetStr
        );

        if (cursor.moveToFirst()) {
            do {
                String itemName = cursor.getString(cursor.getColumnIndexOrThrow(BazaarContract.BazaarEntry.COLUMN_ITEM_NAME.toLowerCase()));
                double buyPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(BazaarContract.BazaarEntry.COLUMN_BUY_PRICE));
                double sellPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(BazaarContract.BazaarEntry.COLUMN_SELL_PRICE));
                boolean favorite = cursor.getInt(cursor.getColumnIndexOrThrow(BazaarContract.BazaarEntry.COLUMN_FAVORITE_STATUS)) == 1;

                BazaarModelClass item = new BazaarModelClass(context, itemName, buyPrice, sellPrice);
                item.setFavorite(favorite);
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }



    public void updateFavoriteStatus(String itemName, boolean favoriteStatus) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BazaarContract.BazaarEntry.COLUMN_FAVORITE_STATUS, favoriteStatus ? 1 : 0);

        String selection = BazaarContract.BazaarEntry.COLUMN_ITEM_NAME + " = ?";
        String[] selectionArgs = {itemName};

        db.update(
                BazaarContract.BazaarEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
    }
}
