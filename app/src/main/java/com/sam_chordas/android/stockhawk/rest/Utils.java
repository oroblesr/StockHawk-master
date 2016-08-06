package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    // These are not displayed to the user. No need to move them to .xml
    private final static String YQL_QUERY = "query";
    private final static String YQL_COUNT = "count";
    private final static String YQL_RESULTS = "results";
    private final static String YQL_QUOTE = "quote";

    private final static String YQL_DATE_FORMAT = "yyyy-MM-dd";


    public final static String SYMBOL_INTENT = "symbol";
    public final static String NAME_INTENT = "name";

    public static int INT_FALSE = 0;
    public static int INT_TRUE = 1;

    public static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject(YQL_QUERY);
                int count = Integer.parseInt(jsonObject.getString(YQL_COUNT));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject(YQL_RESULTS)
                            .getJSONObject(YQL_QUOTE);
                    if (!isJSONNull(jsonObject)) {
                        batchOperations.add(buildBatchOperation(jsonObject));
                    } else {
                        //TODO display error message
                        Log.e("TEST----------", "WRONG WRONG AND WRONG");
                    }

                } else {
                    resultsArray = jsonObject.getJSONObject(YQL_RESULTS).getJSONArray(YQL_QUOTE);

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            if (!isJSONNull(jsonObject)) {
                                batchOperations.add(buildBatchOperation(jsonObject));
                            } else {
                                //TODO display error message
                                Log.e("TEST----------", "WRONG WRONG AND WRONG");
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }


    public static ArrayList quoteHistoricalJsonToContentVals(String JSON, Context context,
                                                             String symbol) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject(YQL_QUERY);
                int count = Integer.parseInt(jsonObject.getString(YQL_COUNT));
                if (count < 1) {
                    //TODO display Historical error message
                    Log.e("TEST Historical 1 ----", "WRONG WRONG AND WRONG");
                }
                else if (count == 1) {
                    jsonObject = jsonObject.getJSONObject(YQL_RESULTS)
                            .getJSONObject(YQL_QUOTE);
                    if (!entryExists(jsonObject, context, symbol)) {
                        batchOperations.add(buildHistoricalBatchOperation(jsonObject));
                    }
                } else {
                    resultsArray = jsonObject.getJSONObject(YQL_RESULTS).getJSONArray(YQL_QUOTE);
                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            if (!entryExists(jsonObject, context, symbol)) {
                                batchOperations.add(buildHistoricalBatchOperation(jsonObject));
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return batchOperations;
    }



    private static boolean entryExists(JSONObject jsonObject, Context context, String symbol) {
        final String YQL_DATE = "Date";
        final String MILLIS_COLUMN = "millis_epoch";
        final String SYMBOL_COLUMN = "symbol";
        final int MILLIS_EPOCH = 0;
        final int SYMBOL = 1;

        try {
            String date = jsonObject.getString(YQL_DATE);

            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(YQL_DATE_FORMAT);
            Long millisEpoch = dateTimeFormatter.parseMillis(date);

            String stockSelection = MILLIS_COLUMN + " = ?" + " AND " + SYMBOL_COLUMN + " = ? ";
            String[] selectArgs = {Long.toString(millisEpoch), symbol};


            Cursor dbCursor = context.getContentResolver().query(
                    QuoteProvider.Historical.HISTORICAL_URI,
                    new String[] {MILLIS_COLUMN,SYMBOL_COLUMN}, // The columns to return for each row
                    stockSelection,     // Selection criteria
                    selectArgs,               // Selection criteria
                    null);              // The sort order for the returned rows

            if (dbCursor != null && dbCursor.moveToFirst()){
                if (millisEpoch == dbCursor.getLong(MILLIS_EPOCH)
                        && symbol.equals(dbCursor.getString(SYMBOL))) {
                    dbCursor.close();
                    return true;
                }
            }

            if (dbCursor != null) {
                dbCursor.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        return false;
    }



    public static boolean isJSONNull(JSONObject jsonObject) {
        try {
            jsonObject.getString("Bid");
        } catch (JSONException e) {
            return true;
        }
        return false;
    }




    public static String truncateBidPrice(String bidPrice) {
        //TODO Correct solution
        if (bidPrice.equals("null")){
            return "0";
        }
        bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        // TODO Check if correct
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;



        change = String.format("%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {

        final String YQL_SYMBOL = "Symbol";
        final String YQL_CHANGE = "Change";
        final String YQL_BIDPRICE = "Bid";
        final String YQL_CHANGE_IN_PERCENT = "ChangeinPercent";
        final String YQL_NAME = "Name";


        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString(YQL_CHANGE);
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString(YQL_SYMBOL));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString(YQL_BIDPRICE )));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString(YQL_CHANGE_IN_PERCENT), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, INT_TRUE);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, INT_FALSE);
            } else {
                builder.withValue(QuoteColumns.ISUP, INT_TRUE);
            }
            builder.withValue(QuoteColumns.NAME, jsonObject.getString(YQL_NAME));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static ContentProviderOperation buildHistoricalBatchOperation(JSONObject jsonObject) {

        final String YQL_SYMBOL = "Symbol";
        final String YQL_DATE = "Date";
        final String YQL_OPEN = "Open";
        final String YQL_HIGH = "High";
        final String YQL_LOW = "Low";
        final String YQL_CLOSE = "Close";
        final String YQL_VOLUME = "Volume";

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Historical.HISTORICAL_URI);
        try {
            String date = jsonObject.getString(YQL_DATE);
            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(YQL_DATE_FORMAT);

            DateTime jodaTime = dateTimeFormatter.parseDateTime(date);

            int year = jodaTime.getYear();
            int month = jodaTime.getMonthOfYear();
            int day = jodaTime.getDayOfMonth();

            long millis_epoch = dateTimeFormatter.parseMillis(date);

            builder.withValue(HistoricalQuoteColumns.SYMBOL, jsonObject.getString(YQL_SYMBOL));

            builder.withValue(HistoricalQuoteColumns.MILLIS_EPOCH, millis_epoch);

            builder.withValue(HistoricalQuoteColumns.DATE,date );

            builder.withValue(HistoricalQuoteColumns.DAY,day );
            builder.withValue(HistoricalQuoteColumns.MONTH,month );
            builder.withValue(HistoricalQuoteColumns.YEAR,year );

            builder.withValue(HistoricalQuoteColumns.SYMBOL, jsonObject.getString(YQL_SYMBOL));

            builder.withValue(HistoricalQuoteColumns.OPEN,
                    truncateBidPrice(jsonObject.getString(YQL_OPEN)));

            builder.withValue(HistoricalQuoteColumns.HIGH,
                    truncateBidPrice(jsonObject.getString(YQL_HIGH)));
            builder.withValue(HistoricalQuoteColumns.LOW,
                    truncateBidPrice(jsonObject.getString(YQL_LOW)));
            builder.withValue(HistoricalQuoteColumns.CLOSE,
                    truncateBidPrice(jsonObject.getString(YQL_CLOSE)));

            builder.withValue(HistoricalQuoteColumns.VOLUME, jsonObject.getInt(YQL_VOLUME));
            builder.withValue(HistoricalQuoteColumns.IS_CURRENT, INT_TRUE);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    public static Long getEpochTime(int[] arrayDate){

        final  String DATE_FORMAT = "yyyy-MM-dd";

        String dd = String.format("%02d", arrayDate[0]);
        String MM = String.format("%02d", arrayDate[1]);
        String YYYY = String.format("%04d", arrayDate[2]);

        String date = YYYY + "-" + MM + "-" + dd;
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_FORMAT);
        return dateTimeFormatter.parseMillis(date);
    }


}