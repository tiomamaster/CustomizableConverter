package com.tiomamaster.customizableconverter.data;

import android.provider.BaseColumns;

/**
 * Created by Artyom on 08.02.2017.
 */
final class ConvertersDbContract {

    private ConvertersDbContract() {}

    static abstract class ConverterEntry implements BaseColumns {
        static final String TABLE_NAME = "Converter";
        static final String COLUMN_NAME_NAME = "Name";
        static final String COLUMN_NAME_ORDER_POSITION = "OrderPosition";
        static final String COLUMN_NAME_IS_ENABLED = "IsEnabled";
        static final String COLUMN_NAME_IS_LAST_SELECTED = "IsLastSelected";
        static final String COLUMN_NAME_LAST_SELECTED_UNIT_POS = "LastSelectedUnitPos";
        static final String COLUMN_NAME_LAST_QUANTITY_TEXT = "LastQuantityText";
        static final String COLUMN_NAME_ERRORS = "Errors";
    }

    static abstract class UnitEntry implements BaseColumns {
        static final String TABLE_NAME = "Unit";
        static final String COLUMN_NAME_NAME = "Name";
        static final String COLUMN_NAME_VALUE = "Value";
        static final String COLUMN_NAME_ORDER_POSITION = "OrderPosition";
        static final String COLUMN_NAME_IS_ENABLED = "IsEnabled";
        static final String COLUMN_NAME_CONVERTER_ID= "ConverterId";
    }
}
