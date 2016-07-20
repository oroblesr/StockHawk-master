package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.DBOperations;

import java.util.ArrayList;

/**
 * Created by oroblesr on 7/5/16.
 */

public class StocksDetailFragment extends Fragment {
    private Context mContext;
    private LineChart lineChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    private void addMonths(){
        ArrayList<String> labels = new ArrayList<String>();
        labels.add(getString(R.string.january));
        labels.add(getString(R.string.february));
        labels.add(getString(R.string.march));
        labels.add(getString(R.string.april));
        labels.add(getString(R.string.may));
        labels.add(getString(R.string.june));
        labels.add(getString(R.string.july));
        labels.add(getString(R.string.august));
        labels.add(getString(R.string.september));
        labels.add(getString(R.string.october));
        labels.add(getString(R.string.november));
        labels.add(getString(R.string.december));
    }


    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        mContext = getContext();

        final DatePicker startDatePicker = (DatePicker) rootView.findViewById(R.id.start_date_picker);
        final DatePicker endDatePicker = (DatePicker) rootView.findViewById(R.id.end_date_picker);

        Button dateButton = (Button) rootView.findViewById(R.id.date_button);
        //TODO SET predefined date
        // TODO validate end > start

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int startDay, startMonth, startYear;
                int endDay, endMonth, endYear;

                startDay = startDatePicker.getDayOfMonth();
                // Months in DatePicker are indexed starting at 0
                startMonth = startDatePicker.getMonth() + 1;
                startYear = startDatePicker.getYear();

                endDay = endDatePicker.getDayOfMonth();
                // Months in DatePicker are indexed starting at 0
                endMonth = endDatePicker.getMonth() + 1;
                endYear = endDatePicker.getYear();

                int[] startDate = {startDay,startMonth,startYear};
                int[] endDate = {endDay,endMonth,endYear};

                DBOperations historicalDB = new DBOperations();
                historicalDB.getHistoricalStocksInRange(getContext(),startDate, endDate);
                historicalDB.execute();
            }
        });



        lineChart = (LineChart) rootView.findViewById(R.id.line_chart);
        lineChart.setDescription("This a test");
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        lineChart.setTouchEnabled(true);

        // add data
        setData();

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

    private void setData() {
        int count = 5;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {
            float val = (float) 50;
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
}