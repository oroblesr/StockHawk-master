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

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
    private String stockName;
    private String stockSymbol;

    private OkHttpClient client = new OkHttpClient();
    private StringBuilder mStoredSymbols = new StringBuilder();

    private int[] startDate;
    private int[] endDate;

    private final String IS_CURRENT_COLUMN ="is_current";

    private final int MILLIS_IN_SECOND = 1000;
    private final int SECONDS_IN_MINUTE = 60;
    private final int MINUTES_IN_HOUR = 60;
    private final int HOURS_IN_DAY = 24;
    private final int DAYS_IN_YEAR = 365;
    final Long ONE_YEAR_IN_MILLIS = (long) MILLIS_IN_SECOND * SECONDS_IN_MINUTE *
            MINUTES_IN_HOUR * HOURS_IN_DAY * DAYS_IN_YEAR ;

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

    private Cursor dbCursor;

    public HistoricalAsyncTask(Context context, LineChart lineChart) {
        this.mContext = context;
        this.lineChart = lineChart;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        switch (operation){
            case GET_FOR_DATE_RANGE:
                //Please consult https://developer.yahoo.com/yql/guide/yql-execute-intro-ratelimits.html for rate limits
                // It is preferred to request year by year
                int startYearPeriod = startYear;
                int endYearPeriod = endYear;
                int yearDifference = endYear - startYear;
                Long differenceInMillis = Utils.getEpochTime(endDate) - Utils.getEpochTime(startDate);
                if ( differenceInMillis > ONE_YEAR_IN_MILLIS){
                    for(int i = 0; i < yearDifference ; i ++) {
                        startYearPeriod = startYear + i;
                        endYearPeriod = startYear + i + 1;

                        fetchHistoricalData(startYearPeriod, endYearPeriod);
                    }
                }
                else {
                    fetchHistoricalData(startYearPeriod, endYearPeriod);
                }

                setdbCursor();
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
                ContentValues contentValues = new ContentValues();

                contentValues.put(HistoricalQuoteColumns.IS_CURRENT, Utils.INT_FALSE);
                mContext.getContentResolver().update(QuoteProvider.Historical.HISTORICAL_URI,
                        contentValues,
                        null,
                        null);
                break;

        }

        if (dbCursor != null) {
            dbCursor.close();
        }



    }


    //Date Format for vector {dd,MM,yyyy}
    public void getHistoricalStocksInRange(int[] startDate, int[] endDate,
                                           String stockSymbol, String stockName) {

        operation = GET_FOR_DATE_RANGE;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startDay = startDate[0];
        this.startMonth = startDate[1];
        this.startYear = startDate[2];
        this.endDay = endDate[0];
        this.endMonth = endDate[1];
        this.endYear = endDate[2];
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;

    }


    private void setdbCursor() {

        String stockSelection = IS_CURRENT_COLUMN + " = ?";
        String[] selectArgs = {String.valueOf(Utils.INT_TRUE)};

        dbCursor = mContext.getContentResolver().query(
                QuoteProvider.Historical.HISTORICAL_URI,
                null,               // The columns to return for each row
                stockSelection,     // Selection criteria
                selectArgs,         // Selection criteria
                null);              // The sort order for the returned rows


    }

    private void setData() {
        if (dbCursor != null && dbCursor.moveToFirst()) {

            ArrayList<Entry> yVals1 = new ArrayList<Entry>();

            for (int i = 0; i <  dbCursor.getCount(); i++) {
                String cursorVal = dbCursor.getString(CLOSE);
                float val = Float.parseFloat(cursorVal);
                yVals1.add(new Entry(i, val));
                dbCursor.moveToNext();
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

    private int fetchHistoricalData(int startYearPeriod, int endYearPeriod){
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

        initQueryCursor = mContext.getContentResolver()
                .query(QuoteProvider.Historical.HISTORICAL_URI,
                        null,
                        null,
                        null,
                        null);



        // Init task. Populates DB with quotes for the symbols seen below
        try {
            String sDay = String.format("%02d", startDay);
            String sMonth = String.format("%02d", startMonth);
            String eDay = String.format("%02d", endDay);
            String eMonth = String.format("%02d", endMonth);

            String startDate = startYearPeriod + "-" + sMonth + "-" + sDay;
            String endDate = endYearPeriod + "-" + eMonth + "-" + eDay;

            String symbol = "\"" + stockSymbol +"\")";
            startDate = "\""+ startDate + "\"";
            endDate = "\""+ endDate + "\"";

            urlStringBuilder.append(
                    URLEncoder.encode(symbol, "UTF-8"));
            urlStringBuilder.append(

                    URLEncoder.encode("and startDate = " + startDate +
                            "and endDate   = " + endDate, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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


                    mContext.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
                            Utils.quoteHistoricalJsonToContentVals(getResponse,mContext,stockSymbol));
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
