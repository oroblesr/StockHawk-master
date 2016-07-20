package com.sam_chordas.android.stockhawk.data;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by oroblesr on 7/19/16.
 */
public class DBOperations extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private int operation;
    private int startDay, startMonth, startYear;
    private int endDay, endMonth, endYear;

    private String startDate;
    private final String DAY_COLUMN = "day";
    private final String MONTH_COLUMN = "month";
    private final String YEAR_COLUMN = "year";

    // This order is given by HistoricalQuoteColumns.java
    final int _ID = 0;
    final int SYMBOL = 1;
    final int DATE = 2;
    final int OPEN = 3;
    final int HIGH = 4;
    final int LOW = 5;
    final int CLOSE = 6;
    final int VOLUME = 7;
    final int ISCURRENT = 8;
    final int DAY = 9;
    final int MONTH = 10;
    final int YEAR = 11;





    final private int GET_FOR_DATE_RANGE = 0;

    @Override
    protected Void doInBackground(Void... voids) {
        switch (operation){
            case GET_FOR_DATE_RANGE:
                historicalStocksInRange();
                break;

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }


    //Date Format for vector {dd,MM,yyyy}
    public void getHistoricalStocksInRange(Context context, int[] startDate, int[] endDate) {
        this.mContext = context;
        operation = GET_FOR_DATE_RANGE;
        this.startDay = startDate[0];
        this.startMonth = startDate[1];
        this.startYear = startDate[2];
        this.endDay = endDate[0];
        this.endMonth = endDate[1];
        this.endYear = endDate[2];


    }


    private void historicalStocksInRange() {
        String stockSelection = YEAR_COLUMN + " >= ?";
        String[] selectArgs = {String.valueOf(startYear)};

/*
        String stockSelection = DAY_COLUMN + " > ? AND "  + DAY_COLUMN + " < ? AND "
                + MONTH_COLUMN + " > ? AND " + MONTH_COLUMN + " < ? AND "
                + YEAR_COLUMN + " > ? AND " + YEAR_COLUMN + " < ?";

        String[] selectArgs = {String.valueOf(startDay),String.valueOf(endDay),
                String.valueOf(startMonth),String.valueOf(endMonth),
                String.valueOf(startYear),String.valueOf(endYear)};

        Cursor dbCursor = mContext.getContentResolver().query(
                QuoteProvider.Historical.HISTORICAL_URI,
                null,               // The columns to return for each row
                stockSelection,     // Selection criteria
                selectArgs,         // Selection criteria
                null);              // The sort order for the returned rows

*/
        Cursor dbCursor = mContext.getContentResolver().query(
                QuoteProvider.Historical.HISTORICAL_URI,
                null,               // The columns to return for each row
                stockSelection,     // Selection criteria
                selectArgs,         // Selection criteria
                null);              // The sort order for the returned rows

        if (dbCursor != null && dbCursor.moveToFirst()) {


            for (int i = 0; i < dbCursor.getCount(); i++) {
                Log.e("---test", String.valueOf(dbCursor.getInt(DAY)) + "-"
                        + String.valueOf(dbCursor.getInt(MONTH)) + "-"
                        + String.valueOf(dbCursor.getInt(YEAR)));
                dbCursor.moveToNext();

            }
            dbCursor.close();

        }

        if (dbCursor != null) {
            dbCursor.close();
        }

    }


}
