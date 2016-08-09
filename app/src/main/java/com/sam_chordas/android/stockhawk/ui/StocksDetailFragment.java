package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    TextView startText;
    TextView endText;

    private final String START_KEY = "start";
    private final String END_KEY = "end";

    boolean isConnected;

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

        lineChart = (LineChart) rootView.findViewById(R.id.line_chart);
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            HistoricalAsyncTask historicalAsyncTask = new HistoricalAsyncTask(getContext(), lineChart);
            historicalAsyncTask.checkIfCurrent(stockSymbol,stockName);
            historicalAsyncTask.execute();


        } else {

            HistoricalAsyncTask historicalAsyncTask = new HistoricalAsyncTask(getContext(), lineChart);
            historicalAsyncTask.checkIfCurrentOrAvailable(stockSymbol,stockName);
            historicalAsyncTask.execute();

            networkToast();
        }


        if (savedInstanceState != null) {
            startDate = savedInstanceState.getIntArray(START_KEY);
            endDate = savedInstanceState.getIntArray(END_KEY);
        }

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet){
            setTabletLayout(rootView);
        }
        else {
            setPhoneLayout(rootView);
        }





    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (startDate != null){
            outState.putIntArray(START_KEY,startDate);
        }
        if (endDate != null){
            outState.putIntArray(END_KEY,endDate);
        }

    }


    void setTabletLayout(View rootView) {

        final DatePicker startDatePicker = (DatePicker) rootView.findViewById(R.id.start_date_picker);
        final DatePicker endDatePicker = (DatePicker) rootView.findViewById(R.id.end_date_picker);

        Button lastMonthButton = (Button) rootView.findViewById(R.id.last_month_button);
        Button lastYearButton = (Button) rootView.findViewById(R.id.last_year_button);
        Button dateButton = (Button) rootView.findViewById(R.id.date_button);
        if (isConnected) {
            startDatePicker.setEnabled(true);
            endDatePicker.setEnabled(true);
            lastMonthButton.setEnabled(true);
            lastYearButton.setEnabled(true);
            dateButton.setEnabled(true);

            dateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int startDay, startMonth, startYear;
                    int endDay, endMonth, endYear;
                    startYear = startDatePicker.getYear();

                    startDay = startDatePicker.getDayOfMonth();
                    // Months in DatePicker are indexed starting at 0
                    startMonth = startDatePicker.getMonth() + 1;

                    endDay = endDatePicker.getDayOfMonth();
                    // Months in DatePicker are indexed starting at 0
                    endMonth = endDatePicker.getMonth() + 1;
                    endYear = endDatePicker.getYear();

                    startDate = new int[]{startDay, startMonth, startYear};
                    endDate = new int[] {endDay,endMonth,endYear};


                    if (Utils.getEpochTime(endDate) < Utils.getEpochTime(startDate)) {
                        Toast toast = Toast.makeText(getContext(), getString(R.string.wrong_date), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else {
                        HistoricalAsyncTask historicalDB = new HistoricalAsyncTask(getContext(), lineChart);
                        historicalDB.getHistoricalStocksInRange(startDate, endDate, stockSymbol, stockName);
                        historicalDB.execute();
                    }


                }
            });

            lastMonthButton.setOnClickListener(monthButtonListener);
            lastYearButton.setOnClickListener(yearButtonListener);
        }
        else {

            startDatePicker.setEnabled(false);
            endDatePicker.setEnabled(false);
            lastMonthButton.setEnabled(false);
            lastYearButton.setEnabled(false);
            dateButton.setEnabled(false);
        }


    }



    void setPhoneLayout(View rootView) {
        startText = (TextView) rootView.findViewById(R.id.start_date_text);
        endText = (TextView) rootView.findViewById(R.id.end_date_text);

        Button startDateButton = (Button) rootView.findViewById(R.id.start_date_button);
        Button endDateButton = (Button) rootView.findViewById(R.id.end_date_button);

        Button lastMonthButton = (Button) rootView.findViewById(R.id.last_month_button);
        Button lastYearButton = (Button) rootView.findViewById(R.id.last_year_button);

        Button dateButton = (Button) rootView.findViewById(R.id.date_button);


        if (startDate != null){
            startText.setText(Utils.getFormattedDate(startDate));
        }
        if (endDate != null){
            endText.setText(Utils.getFormattedDate(endDate));
        }

        if (isConnected) {
            startDateButton.setEnabled(true);
            endDateButton.setEnabled(true);
            lastMonthButton.setEnabled(true);
            lastYearButton.setEnabled(true);
            dateButton.setEnabled(true);
            startDateButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {
                    DialogFragment newFragment = new DatePickerFragment() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            startDate = new int[3];
                            startDate[0] = day;
                            startDate[1] = month + 1;
                            startDate[2] = year;

                            startText.setText(Utils.getFormattedDate(startDate));
                        }
                    };
                    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                }
            });

            endDateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerFragment() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int day) {
                            endDate = new int[3];
                            endDate[0] = day;
                            endDate[1] = month + 1;
                            endDate[2] = year;

                            endText.setText(Utils.getFormattedDate(endDate));
                        }
                    };
                    newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
                }
            });

            lastMonthButton.setOnClickListener(monthButtonListener);
            lastYearButton.setOnClickListener(yearButtonListener);



            dateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (Utils.getEpochTime(endDate) < Utils.getEpochTime(startDate)) {
                        Toast toast = Toast.makeText(getContext(), getString(R.string.wrong_date), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    else {
                        HistoricalAsyncTask historicalDB = new HistoricalAsyncTask(getContext(), lineChart);
                        historicalDB.getHistoricalStocksInRange(startDate, endDate, stockSymbol, stockName);
                        historicalDB.execute();
                    }

                }
            });
        }
        else {
            startDateButton.setEnabled(false);
            endDateButton.setEnabled(false);
            lastMonthButton.setEnabled(false);
            lastYearButton.setEnabled(false);
            dateButton.setEnabled(false);

        }

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
                    // Previous month
                    startMonth = currentMonth;
                }
                else {
                    startDay = 1;
                    // Current month
                    startMonth = currentMonth + 1;
                }

            }

            endDay = currentDay;
            endMonth = currentMonth + 1;
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



    public void networkToast() {
        Toast.makeText(mContext, getString(R.string.network_toast_detail), Toast.LENGTH_SHORT).show();
    }


}