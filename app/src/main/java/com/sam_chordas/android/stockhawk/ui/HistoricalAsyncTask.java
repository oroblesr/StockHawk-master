package com.sam_chordas.android.stockhawk.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.data.HistoricalQuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by oroblesr on 7/19/16.
 */
public class HistoricalAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private int operation;
    private int startDay, startMonth, startYear;
    private int endDay, endMonth, endYear;
    private LineChart lineChart;

    private OkHttpClient client = new OkHttpClient();
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean isUpdate;

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
                fetchHistoricalData();
                historicalStocksInRange();
                break;

        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        switch (operation){
            case GET_FOR_DATE_RANGE:
                setData();
                setChart();
                break;

        }


    }



    //Date Format for vector {dd,MM,yyyy}
    public void getHistoricalStocksInRange(Context context, int[] startDate, int[] endDate, LineChart lineChart) {
        this.mContext = context;
        operation = GET_FOR_DATE_RANGE;
        this.startDay = startDate[0];
        this.startMonth = startDate[1];
        this.startYear = startDate[2];
        this.endDay = endDate[0];
        this.endMonth = endDate[1];
        this.endYear = endDate[2];
        this.lineChart = lineChart;

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

    private void setData() {
        int count = 5;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float val = (float) 88;
            yVals1.add(new Entry(i, val));
        }

        LineDataSet set1;

        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);

            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yVals1, "DataSet 1");
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
            set1.setColor(ColorTemplate.getHoloBlue());
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(2f);
            set1.setCircleRadius(3f);
            set1.setFillAlpha(65);
            set1.setFillColor(ColorTemplate.getHoloBlue());
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setDrawCircleHole(false);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            lineChart.setData(data);
        }
    }

    private void setChart() {
        lineChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend l = lineChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextSize(11f);
        l.setTextColor(Color.WHITE);
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        //TODO
        float yAxisMaxValue = 100f;

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaxValue(yAxisMaxValue);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());
        rightAxis.setAxisMaxValue(yAxisMaxValue);
        rightAxis.setAxisMinValue(0f);
        rightAxis.setDrawGridLines(true);
        rightAxis.setGranularityEnabled(true);
    }


    String fetchData(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private int fetchHistoricalData(){
        Cursor initQueryCursor = null;

        StringBuilder urlStringBuilder = new StringBuilder();
        try {
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                    + "in (", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
        /*
        if (initQueryCursor != null) {
            if (initQueryCursor.getCount() == 0) {

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
*/

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
                    Log.e("StockHawk", "Error applying batch insert", e);
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
