package com.sam_chordas.android.stockhawk.widget;

import android.app.IntentService;
import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by oroblesr on 7/20/16.
 */
public class StockWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }
}
