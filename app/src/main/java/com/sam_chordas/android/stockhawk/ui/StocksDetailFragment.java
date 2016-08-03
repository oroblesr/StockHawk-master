package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;

import java.util.ArrayList;

/**
 * Created by oroblesr on 7/5/16.
 */

public class StocksDetailFragment extends Fragment {
    private Context mContext;
    private LineChart lineChart;
    private int[] startDate;
    private int[] endDate;

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

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet){
            setTabletLayout(rootView);
        }
        else {
            setPhoneLayout(rootView);
        }

        lineChart = (LineChart) rootView.findViewById(R.id.line_chart);
        lineChart.setDescription("This a test");
        lineChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        lineChart.setTouchEnabled(true);




    }

    void setTabletLayout(View rootView) {

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

                startDate = new int[]{startDay, startMonth, startYear};
                endDate = new int[] {endDay,endMonth,endYear};

                if (startDate != null && endDate != null) {
                    HistoricalAsyncTask historicalDB = new HistoricalAsyncTask();
                    historicalDB.getHistoricalStocksInRange(getContext(),startDate, endDate, lineChart);
                    historicalDB.execute();

                }

            }
        });
    }

    void setPhoneLayout(View rootView) {

    }

}