package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by oroblesr on 7/5/16.
 */

public class StocksDetailFragment extends Fragment {
    private Context mContext;
    private LineChart lineChart;
    private int[] startDate;
    private int[] endDate;
    String stockSymbol;
    String stockName;

    // Please note that Month value is 0-based. e.g., 0 for January.
    final int JAN = 0;
    final int FEB = 1;
    final int MAR = 2;
    final int APR = 3;
    final int MAY = 4;
    final int JUN = 5;
    final int JUL = 6;
    final int AUG = 7;
    final int SEP = 8;
    final int OCT = 9;
    final int NOV = 10;
    final int DEC = 11;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        stockName = intent.getStringExtra(Utils.NAME_INTENT);
        stockSymbol = intent.getStringExtra(Utils.SYMBOL_INTENT);

        getActivity().setTitle(getContext().getString(R.string.historical_title) + stockName);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
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
        // TODO correct description
        lineChart.setDescription("This a test");
        lineChart.setNoDataTextDescription(mContext.getString(R.string.no_data_text_description));

        // enable touch gestures
        lineChart.setTouchEnabled(true);




    }

    void setTabletLayout(View rootView) {

        final DatePicker startDatePicker = (DatePicker) rootView.findViewById(R.id.start_date_picker);
        final DatePicker endDatePicker = (DatePicker) rootView.findViewById(R.id.end_date_picker);

        Button lastMonthButton = (Button) rootView.findViewById(R.id.last_month_button);
        Button lastYearButton = (Button) rootView.findViewById(R.id.last_year_button);

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

                HistoricalAsyncTask historicalDB = new HistoricalAsyncTask(getContext(), lineChart);
                historicalDB.getHistoricalStocksInRange(startDate, endDate, stockSymbol, stockName);
                historicalDB.execute();

            }
        });

        lastMonthButton.setOnClickListener(monthButtonListener);
        lastYearButton.setOnClickListener(yearButtonListener);
    }

    void setPhoneLayout(View rootView) {

        Button startDateButton = (Button) rootView.findViewById(R.id.start_date_button);
        Button endDateButton = (Button) rootView.findViewById(R.id.end_date_button);

        Button lastMonthButton = (Button) rootView.findViewById(R.id.last_month_button);
        Button lastYearButton = (Button) rootView.findViewById(R.id.last_year_button);

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getFragmentManager(), "datePicker");
            }
        });


        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment dialogFragment = new DatePickerFragment();
                dialogFragment.show(getFragmentManager(), "datePicker");
            }
        });

        lastMonthButton.setOnClickListener(monthButtonListener);
        lastYearButton.setOnClickListener(yearButtonListener);
    }

    private View.OnClickListener monthButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Calendar calendar = new GregorianCalendar();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentYear = calendar.get(Calendar.YEAR);
            int startDay, startMonth, startYear;
            int endDay, endMonth, endYear;

            if (currentMonth == JAN){
                startDay = currentDay;
                startMonth = DEC;
                startYear = currentYear - 1;

            }
            else {
                startYear = currentYear;

                // Simplifying by querying the current month, if the current day is > 28
                if (currentDay < 28){
                    startDay = currentDay;
                    startMonth = currentMonth - 1;
                }
                else {
                    startDay = 1;
                    startMonth = currentMonth;
                }

            }

            endDay = currentDay;
            endMonth = currentMonth;
            endYear = currentYear;

            startDate = new int[]{startDay, startMonth, startYear};
            endDate = new int[] {endDay,endMonth,endYear};

            HistoricalAsyncTask historicalDB = new HistoricalAsyncTask(getContext(), lineChart);
            historicalDB.getHistoricalStocksInRange(startDate, endDate, stockSymbol, stockName);
            historicalDB.execute();

        }
    };


    private View.OnClickListener yearButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Calendar calendar = new GregorianCalendar();
            int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentYear = calendar.get(Calendar.YEAR);
            int startDay, startMonth, startYear;
            int endDay, endMonth, endYear;

            startDay = currentDay;
            startMonth = currentMonth;
            startYear = currentYear - 1;

            endDay = currentDay;
            endMonth = currentMonth;
            endYear = currentYear;

            // Handling leap year day
            if (currentDay == 29 && currentMonth == FEB){
                startDay = 28;
            }

            startDate = new int[]{startDay, startMonth, startYear};
            endDate = new int[] {endDay,endMonth,endYear};

            HistoricalAsyncTask historicalDB = new HistoricalAsyncTask(getContext(), lineChart);
            historicalDB.getHistoricalStocksInRange(startDate, endDate, stockSymbol, stockName);
            historicalDB.execute();

        }
    };


}