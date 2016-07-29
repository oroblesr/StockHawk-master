package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;

/**
 * Created by oroblesr on 7/20/16.
 * Credit to Advanced_Android_Development:
 * https://github.com/udacity/Advanced_Android_Development
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

    // TODO FIX repeated
    private static final String[] STOCKS_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.SYMBOL,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.CHANGE,
            QuoteColumns.BIDPRICE,
            QuoteColumns.CREATED,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT,
            QuoteColumns.NAME
    };

    // These indices are taken from QuoteColumns.java
    static final int STOCK_ID_COLUMN = 0;
    static final int SYMBOL_COLUMN = 1;
    static final int PERCENT_COLUMN = 2;
    static final int CHANGE_COLUMN = 3;
    static final int BIDPRICE_COLUMN = 4;
    static final int CREATED = 5;
    static final int ISUP_COLUMN = 6;
    static final int ISCURRENT_COLUMN = 7;
    static final int NAME_COLUMN = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        final int appWidgetId = intent.getIntExtra("appWidgetId",0);
        return new RemoteViewsFactory() {
            private final int NUMBER_OF_VIEWS = 1;
            private Cursor cursor;
            //The same id always refers to the same object.
            private final boolean HAS_STABLE_IDS = true;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {
                if (cursor != null){
                    cursor.close();
                }

                final long identityToken = Binder.clearCallingIdentity();

                try {
                    cursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                            null,
                            QuoteColumns.ISCURRENT + " = 1",
                            null,
                            null);

                }
                finally {

                    Binder.restoreCallingIdentity(identityToken);

                }

            }

            @Override
            public void onDestroy() {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }

            }

            @Override
            public int getCount() {
                return cursor!= null ? cursor.getCount() : 0;
            }

            @Override
            public RemoteViews getViewAt(int i) {
                if (i == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(i)) {
                    return null;
                }


                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_list_item);


                //TODO Complete

                AppWidgetManager appWidgetManager =
                        AppWidgetManager.getInstance(getApplicationContext());
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    int widgetWidth = getWidgetWidth(appWidgetManager,appWidgetId);
                    //TODO 172 266 360
                    if (widgetWidth < 200){
                        remoteViews.setTextViewText(R.id.widget_symbol, cursor.getString(SYMBOL_COLUMN));
                        remoteViews.setTextViewText(R.id.widget_bid_price, cursor.getString(BIDPRICE_COLUMN));
                        remoteViews.setViewVisibility(R.id.widget_change, View.GONE);
                        //remoteViews.setInt(R.id.widget_change, "setVisibility", View.GONE);
                        //remoteViews.setTextViewText(R.id.widget_change, cursor.getString(CHANGE_COLUMN));


                    }
                    else if (widgetWidth < 300) {
                        remoteViews.setTextViewText(R.id.widget_symbol, cursor.getString(SYMBOL_COLUMN));
                        remoteViews.setTextViewText(R.id.widget_bid_price, cursor.getString(BIDPRICE_COLUMN));
                        remoteViews.setViewVisibility(R.id.widget_change, View.VISIBLE);
                        remoteViews.setTextViewText(R.id.widget_change, cursor.getString(CHANGE_COLUMN));

                    }
                    else {
                        remoteViews.setTextViewText(R.id.widget_symbol, cursor.getString(NAME_COLUMN));
                        remoteViews.setTextViewText(R.id.widget_bid_price, cursor.getString(BIDPRICE_COLUMN));
                        remoteViews.setViewVisibility(R.id.widget_change, View.VISIBLE);

                        remoteViews.setTextViewText(R.id.widget_change, cursor.getString(CHANGE_COLUMN));
                    }



                }
                else {
                    remoteViews.setTextViewText(R.id.widget_symbol, cursor.getString(SYMBOL_COLUMN));
                    remoteViews.setTextViewText(R.id.widget_bid_price, cursor.getString(BIDPRICE_COLUMN));
                    remoteViews.setTextViewText(R.id.widget_change, cursor.getString(CHANGE_COLUMN));


                }



                if (cursor.getInt(ISUP_COLUMN) == 1) {
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_green);
                } else {
                    remoteViews.setInt(R.id.widget_change, "setBackgroundResource",
                            R.drawable.percent_change_pill_red);
                }


                final Intent fillInIntent = new Intent(getApplicationContext(),
                        StocksDetailActivity.class);

                remoteViews.setOnClickFillInIntent(R.id.widget_frame_list_item, fillInIntent);

                return remoteViews;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_stock);
            }

            @Override
            public int getViewTypeCount() {
                return NUMBER_OF_VIEWS;
            }

            @Override
            public long getItemId(int i) {
                if (cursor.moveToPosition(i))
                    return cursor.getLong(STOCK_ID_COLUMN);
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return HAS_STABLE_IDS;
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId){
                Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
                return options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            }

        };
    }
}
