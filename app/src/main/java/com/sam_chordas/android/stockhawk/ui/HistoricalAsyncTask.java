package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.sam_chordas.android.stockhawk.R;
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
import java.util.List;

/**
 * Created by oroblesr on 7/19/16.
 */
public class HistoricalAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context mContext;
    private int operation;
    private int startDay, startMonth, startYear;
    private int endDay, endMonth, endYear;

    private String LOG_TAG = HistoricalAsyncTask.class.getSimpleName();

    private LineChart lineChart;
    private String stockName;
    private String stockSymbol;

    private OkHttpClient client = new OkHttpClient();
    private StringBuilder mStoredSymbols = new StringBuilder();

    private int[] startDate;
    private int[] endDate;

    private final String IS_CURRENT_COLUMN ="is_current";
    private final String SYMBOL_COLUMN = "symbol";

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

    private boolean infoInCursor = false;

    final private int GET_FOR_DATE_RANGE = 0;
    final private int CHECK_IF_CURRENT = 1;
    final private int CHECK_IF_CURRENT_OR_AVAILABLE = 2;

    private float minInPeriod;
    private float maxInPeriod;

    private Cursor dbCursor;

    public HistoricalAsyncTask(Context context, LineChart lineChart) {
        this.mContext = context;
        this.lineChart = lineChart;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        switch (operation){
            case GET_FOR_DATE_RANGE:

                ContentValues contentValues = new ContentValues();

                contentValues.put(HistoricalQuoteColumns.IS_CURRENT, Utils.INT_FALSE);
                mContext.getContentResolver().update(QuoteProvider.Historical.HISTORICAL_URI,
                        contentValues,
                        null,
                        null);

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

                infoInCursor = setdbCursor();
                break;
            case CHECK_IF_CURRENT:
                infoInCursor = setdbCursor();
                break;
            case CHECK_IF_CURRENT_OR_AVAILABLE:
                infoInCursor = setdbCursor();
                if (!infoInCursor) {
                    infoInCursor = fetchAvaiableData();
                }
                break;



        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (infoInCursor){
            setData();
            setChart();
        }


        if (dbCursor != null) {
            dbCursor.close();
        }



    }


    public void checkIfCurrent(String stockSymbol, String stockName){
        this.operation = CHECK_IF_CURRENT;
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;
    }
    //Date Format for vector {dd,MM,yyyy}
    public void getHistoricalStocksInRange(int[] startDate, int[] endDate,
                                           String stockSymbol, String stockName) {

        this.operation = GET_FOR_DATE_RANGE;
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


    private boolean setdbCursor() {

        String stockSelection = IS_CURRENT_COLUMN + " = ?" + "AND " + SYMBOL_COLUMN +" = ?";
        String[] selectArgs = {String.valueOf(Utils.INT_TRUE), stockSymbol};

        dbCursor = mContext.getContentResolver().query(
                QuoteProvider.Historical.HISTORICAL_URI,
                null,               // The columns to return for each row
                stockSelection,     // Selection criteria
                selectArgs,         // Selection criteria
                null);              // The sort order for the returned rows

        if (dbCursor != null && dbCursor.moveToFirst()){
            return true;

        }
        return false;

    }


    private boolean fetchAvaiableData() {

        String stockSelection = SYMBOL_COLUMN +" = ?";
        String[] selectArgs = {stockSymbol};

        dbCursor = mContext.getContentResolver().query(
                QuoteProvider.Historical.HISTORICAL_URI,
                null,               // The columns to return for each row
                stockSelection,     // Selection criteria
                selectArgs,         // Selection criteria
                null);              // The sort order for the returned rows

        if (dbCursor != null && dbCursor.moveToFirst()){
            return true;

        }
        return false;

    }

    private void setData() {
        List<String> labels = new ArrayList<>();

        if (dbCursor != null && dbCursor.moveToFirst()) {

            List<Entry> yVals = new ArrayList<>();
            View rootView = ((Activity)mContext).getWindow().getDecorView()
                    .findViewById(android.R.id.content);

            TextView minText = (TextView) rootView.findViewById(R.id.min_period_text);
            TextView maxText = (TextView) rootView.findViewById(R.id.max_period_text);
            
            float initValue = Float.parseFloat(dbCursor.getString(CLOSE));

            minInPeriod = initValue;
            maxInPeriod = initValue;

            int totalValues = dbCursor.getCount();

            for (int i = 0; i < totalValues ; i++) {
                String cursorVal = dbCursor.getString(CLOSE);
                float val = Float.parseFloat(cursorVal);

                yVals.add(new Entry(val,i));

                // DAY-MONTH-YEAR
                String date = Utils.getFormattedDate(new int[]{
                        dbCursor.getInt(DAY),
                        dbCursor.getInt(MONTH),
                        dbCursor.getInt(YEAR)});

                labels.add(date);

                dbCursor.moveToNext();

                if (val < minInPeriod){
                    minInPeriod = val;
                }
                else if (val > maxInPeriod){
                    maxInPeriod = val;
                }
            }

            minText.setText(String.valueOf(minInPeriod));
            maxText.setText(String.valueOf(maxInPeriod));

            LineDataSet lineDataSet;

            // create a dataset and give it a type
            lineDataSet = new LineDataSet(yVals, "DataSet 1");
            lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
            lineDataSet.setColor(ColorTemplate.getHoloBlue());

            lineDataSet.setCircleColorHole(Color.BLACK);
            lineDataSet.setDrawFilled(true);

            Drawable drawable = ContextCompat.getDrawable(mContext, R.drawable.blue_gradient);
            lineDataSet.setFillDrawable(drawable);

            int color = Color.rgb(244, 0, 0);
            lineDataSet.setHighLightColor(Color.RED);

            // create a data object with the datasets

            LineData data = new LineData(labels, lineDataSet);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            // set data
            lineChart.setData(data);
        }
    }

    private void setChart() {
        lineChart.setDescription("");
        lineChart.setNoDataTextDescription(mContext.getString(R.string.no_data_text_description));

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);

        lineChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());

        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setTextColor(ColorTemplate.getHoloBlue());

        rightAxis.setDrawGridLines(true);

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

    public void checkIfCurrentOrAvailable(String stockSymbol, String stockName) {
        this.operation = CHECK_IF_CURRENT_OR_AVAILABLE;
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;

    }


}
