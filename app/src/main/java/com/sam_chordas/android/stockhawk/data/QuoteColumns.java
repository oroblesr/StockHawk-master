package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 */
public interface QuoteColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";

    @DataType(DataType.Type.TEXT) @NotNull String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT) @NotNull String PERCENT_CHANGE = "percent_change";
    @DataType(DataType.Type.TEXT) @NotNull String CHANGE = "change";
    @DataType(DataType.Type.TEXT) @NotNull String BIDPRICE = "bid_price";
    @DataType(DataType.Type.TEXT) String CREATED = "created";
    @DataType(DataType.Type.INTEGER) @NotNull String ISUP = "is_up";
    @DataType(DataType.Type.INTEGER) @NotNull String ISCURRENT = "is_current";
    @DataType(DataType.Type.TEXT) @NotNull String NAME = "name";



}
