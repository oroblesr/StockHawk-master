package com.sam_chordas.android.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.StocksDetailActivity;

/**
 * Created by oroblesr on 7/20/16.
 */
public class StockWidgetProvider extends AppWidgetProvider {
    public static final String DETAIL_ACTION = "com.sam_chordas.android.stockhawk.widget.TOAST_ACTION";
    public static final String EXTRA_ITEM = "com.sam_chordas.android.stockhawk.widget.EXTRA_ITEM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){

        for (int appWidgetId : appWidgetIds) {
            // Sets up the intent that points to the StockWidgetRemoteViewsService that will
            // provide the views for this collection.
            Intent intent = new Intent(context, StockWidgetRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock);
            remoteViews.setRemoteAdapter(R.id.widget_list, intent);

            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);


            // This section makes it possible for items to have individualized behavior.
            // It does this by setting up a pending intent template. Individuals items of a collection
            // cannot set up their own pending intents. Instead, the collection as a whole sets
            // up a pending intent template, and the individual items set a fillInIntent
            // to create unique behavior on an item-by-item basis.
            Intent toastIntent = new Intent(context, StockWidgetProvider.class);

            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting TOAST_ACTION.
            toastIntent.setAction(StockWidgetProvider.DETAIL_ACTION);
            toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }


    }


    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is DETAIL_ACTION. If it is, the app widget
    // open StocksDetailActivity
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        if (intent.getAction().equals(DETAIL_ACTION)) {
            String stockName = intent.getStringExtra(Utils.NAME_INTENT);
            String stockSymbol = intent.getStringExtra(Utils.SYMBOL_INTENT);

            Intent detailsIntent = new Intent(context, StocksDetailActivity.class);

            detailsIntent.putExtra(Utils.NAME_INTENT, stockName);
            detailsIntent.putExtra(Utils.SYMBOL_INTENT, stockSymbol);
            detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(detailsIntent);
        }

        super.onReceive(context, intent);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, getClass()));

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

    }
}
