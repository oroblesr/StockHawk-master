package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by oroblesr on 7/7/16.
 * credit to sam_chordas
 */

public class StockHistoricalService extends GcmTaskService {
    private String LOG_TAG = StockHistoricalService.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;


    public StockHistoricalService(Context context) {
        mContext = context;
    }

    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    @Override
    public int onRunTask(TaskParams params) {
        Cursor initQueryCursor = null;
        if (mContext == null) {
            mContext = this;
        }
        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (params.getTag().equals("init") || params.getTag().equals("periodic")) {
            isUpdate = true;
            initQueryCursor = mContext.getContentResolver()
                    .query(QuoteProvider.Historical.HISTORICAL_URI,
                            null,
                            null,
                            null,
                            null);
            /*
            initQueryCursor = mContext.getContentResolver()
                    .query(QuoteProvider.Historical.HISTORICAL_URI,
                            new String[]{"Distinct " + HistoricalQuoteColumns.DATE},
                            null,
                            null,
                            null);*/
            if (initQueryCursor != null) {
                if (initQueryCursor.getCount() == 0) {
                    // Init task. Populates DB with quotes for the symbols seen below
                    try {
                        urlStringBuilder.append(
                                URLEncoder.encode("\"YHOO\")", "UTF-8"));
                        urlStringBuilder.append(
                                //Please consult https://developer.yahoo.com/yql/guide/yql-execute-intro-ratelimits.html for rate limits
                                // It is preferred to request year by year
                                //TODO FIX
                                URLEncoder.encode("and startDate = \"2015-02-11\"" +
                                        "and endDate   = \"2016-02-11\"", "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    //TODO print to system out
                    //DatabaseUtils.dumpCursor(initQueryCursor);
                    initQueryCursor.moveToFirst();
                    for (int i = 0; i < initQueryCursor.getCount(); i++) {
                        mStoredSymbols.append("\"" +
                                initQueryCursor.getString(initQueryCursor.getColumnIndex("symbol")) + "\",");
                        initQueryCursor.moveToNext();
                    }


                    mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), ")");
                    try {
                        urlStringBuilder.append(URLEncoder.encode(mStoredSymbols.toString(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }


                }
            }

        } else if (params.getTag().equals("add")) {
            isUpdate = false;
            // get symbol from params.getExtra and build query
            String stockInput = params.getExtras().getString("symbol");
            try {
                urlStringBuilder.append(URLEncoder.encode("\"" + stockInput + "\")", "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // finalize the URL for the API query.
        urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                + "org%2Falltableswithkeys&callback=");

        String urlString;
        String getResponse;
        int result = GcmNetworkManager.RESULT_FAILURE;

        if (urlStringBuilder != null) {
            urlString = urlStringBuilder.toString();

            try {
                getResponse = fetchData(urlString);
                result = GcmNetworkManager.RESULT_SUCCESS;

                try {
                    ContentValues contentValues = new ContentValues();
                    // update ISCURRENT to 0 (false) so new data is current
                    if (isUpdate) {
                        contentValues.put(HistoricalQuoteColumns.ISCURRENT, 0);
                        mContext.getContentResolver().update(QuoteProvider.Historical.HISTORICAL_URI,
                                contentValues,
                                null,
                                null);
                    }
                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            Utils.quoteHistoricalJsonToContentVals(getResponse));
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        try {
            if (initQueryCursor != null && !initQueryCursor.isClosed()) initQueryCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

}