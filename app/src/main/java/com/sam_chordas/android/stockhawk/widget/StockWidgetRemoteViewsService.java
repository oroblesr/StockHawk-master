package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by oroblesr on 7/20/16.
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

    // These indices are taken from //TODO
    static final int STOCK_ID = 0;


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
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
                            null,
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
                RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_list_item);

                //TODO Complete
                remoteViews.setTextViewText(R.id.widget_symbol, "Text from service");

                final Intent fillInIntent = new Intent();

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
                    return cursor.getLong(STOCK_ID);
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return HAS_STABLE_IDS;
            }
        };
    }
}
