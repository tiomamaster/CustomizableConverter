package com.tiomamaster.customizableconverter.data;

import android.provider.BaseColumns;

/**
 * Created by Artyom on 08.02.2017.
 */
public final class ConvertersDbContract {

    private ConvertersDbContract() {}

    public static abstract class ConverterEntry implements BaseColumns {
        public static final String TABLE_NAME = "Converter";
        public static final String COLUMN_NAME_NAME = "Name";
        public static final String COLUMN_NAME_ORDER_POSITION = "OrderPosition";
        public static final String COLUMN_NAME_IS_ENABLED = "IsEnabled";
        public static final String COLUMN_NAME_IS_LAST_SELECTED = "IsLastSelected";
        public static final String COLUMN_NAME_LAST_SELECTED_UNIT_ID = "LastSelectedUnitId";
        public static final String COLUMN_NAME_LAST_QUANTITY_TEXT = "LastQuantityText";
        public static final String COLUMN_NAME_ERRORS = "Errors";
    }

    public static abstract class UnitEntry implements BaseColumns {
        public static final String TABLE_NAME = "Unit";
        public static final String COLUMN_NAME_NAME = "Name";
        public static final String COLUMN_NAME_VALUE = "Value";
        public static final String COLUMN_NAME_ORDER_POSITION = "OrderPosition";
        public static final String COLUMN_NAME_IS_ENABLED = "IsEnabled";
        public static final String COLUMN_NAME_CONVERTER_ID= "ConverterId";
    }
}
