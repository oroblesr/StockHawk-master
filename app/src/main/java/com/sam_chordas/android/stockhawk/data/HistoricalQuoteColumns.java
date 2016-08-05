package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by oroblesr on 7/7/16.
 */
public interface HistoricalQuoteColumns {

    @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement String _ID = "_id";

    @DataType(DataType.Type.INTEGER) @References(table = QuoteDatabase.QUOTES,
            column = QuoteColumns.SYMBOL) @NotNull String SYMBOL = "symbol";

    @DataType(DataType.Type.TEXT) @NotNull String DATE = "date";
    @DataType(DataType.Type.TEXT) @NotNull String OPEN = "open";
    @DataType(DataType.Type.TEXT) @NotNull String HIGH = "high";
    @DataType(DataType.Type.TEXT) @NotNull String LOW = "low";
    @DataType(DataType.Type.TEXT) @NotNull String CLOSE = "close";
    @DataType(DataType.Type.INTEGER) @NotNull String VOLUME = "volume";
    @DataType(DataType.Type.INTEGER) @NotNull String IS_CURRENT = "is_current";
    @DataType(DataType.Type.INTEGER) @NotNull String DAY = "day";
    @DataType(DataType.Type.INTEGER) @NotNull String MONTH = "month";
    @DataType(DataType.Type.INTEGER) @NotNull String YEAR = "year";

    @DataType(DataType.Type.INTEGER) @NotNull String MILLIS_EPOCH = "millis_epoch";


}
