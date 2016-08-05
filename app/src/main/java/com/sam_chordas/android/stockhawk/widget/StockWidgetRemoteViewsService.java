package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
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
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;

/**
 * Created by oroblesr on 7/20/16.
 * Credit to Advanced_Android_Development:
 * https://github.com/udacity/Advanced_Android_Development
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

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

    private final int DEFAULT_WIDTH = 200;
    private final int LARGE_WIDTH = 300;

    private Context mContext;
    private int mAppWidgetId;
    private Cursor cursor;


    //The same id always refers to the same object.
    private final boolean HAS_STABLE_IDS = true;

    private final int NUMBER_OF_VIEWS = 1;


    public StockRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

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
            cursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
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
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                cursor == null || !cursor.moveToPosition(position)) {
            return null;
        }

        // Construct a RemoteViews item based on the app widget item XML file, and set the
        // text based on the position.
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);


        AppWidgetManager appWidgetManager =
                AppWidgetManager.getInstance(mContext.getApplicationContext());

        String name = cursor.getString(NAME_COLUMN);
        String symbol = cursor.getString(SYMBOL_COLUMN);
        String bidPrice = cursor.getString(BIDPRICE_COLUMN);
        String change = cursor.getString(CHANGE_COLUMN);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            int widgetWidth = getWidgetWidth(appWidgetManager, mAppWidgetId);

            if (widgetWidth < DEFAULT_WIDTH){
                remoteViews.setTextViewText(R.id.widget_symbol, symbol);
                remoteViews.setTextViewText(R.id.widget_bid_price, bidPrice);
                remoteViews.setViewVisibility(R.id.widget_change, View.GONE);

            }
            else if (widgetWidth < LARGE_WIDTH) {
                remoteViews.setTextViewText(R.id.widget_symbol, symbol);
                remoteViews.setTextViewText(R.id.widget_bid_price, bidPrice);
                remoteViews.setViewVisibility(R.id.widget_change, View.VISIBLE);
                remoteViews.setTextViewText(R.id.widget_change, change);

            }
            else {
                remoteViews.setTextViewText(R.id.widget_symbol, name);
                remoteViews.setTextViewText(R.id.widget_bid_price, bidPrice);
                remoteViews.setViewVisibility(R.id.widget_change, View.VISIBLE);
                remoteViews.setTextViewText(R.id.widget_change, change);
            }


        }
        else {
            remoteViews.setTextViewText(R.id.widget_symbol, symbol);
            remoteViews.setTextViewText(R.id.widget_bid_price, bidPrice);
            remoteViews.setTextViewText(R.id.widget_change, change);

        }


        if (cursor.getInt(ISUP_COLUMN) == 1) {
            remoteViews.setInt(R.id.widget_change, "setBackgroundResource",
                    R.drawable.percent_change_pill_green);
        } else {
            remoteViews.setInt(R.id.widget_change, "setBackgroundResource",
                    R.drawable.percent_change_pill_red);
        }

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();

        extras.putString(Utils.NAME_INTENT, cursor.getString(NAME_COLUMN));
        extras.putString(Utils.SYMBOL_INTENT, cursor.getString(SYMBOL_COLUMN));

        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);

        // Make it possible to distinguish the individual on-click
        // action of a given item
        remoteViews.setOnClickFillInIntent(R.id.widget_frame_list_item, fillInIntent);

        // Return the RemoteViews object.
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_stock);
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


}