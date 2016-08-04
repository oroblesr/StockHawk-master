package com.sam_chordas.android.stockhawk.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sam_chordas.android.stockhawk.R;

/**
 * Created by oroblesr on 7/5/16.
 */

public class StocksDetailActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        if (savedInstanceState == null) {
            StocksDetailFragment stocksDetailFragment = new StocksDetailFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.detail_container, stocksDetailFragment)
                    .commit();
        }
    }
}

