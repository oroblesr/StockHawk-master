package com.sam_chordas.android.stockhawk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by oroblesr on 7/7/16.
 */
public class StockParcelable  implements Parcelable {


    protected StockParcelable(Parcel in) {
    }

    public static final Creator<StockParcelable> CREATOR = new Creator<StockParcelable>() {
        @Override
        public StockParcelable createFromParcel(Parcel in) {
            return new StockParcelable(in);
        }

        @Override
        public StockParcelable[] newArray(int size) {
            return new StockParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
