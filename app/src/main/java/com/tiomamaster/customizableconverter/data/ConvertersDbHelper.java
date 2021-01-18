package com.tiomamaster.customizableconverter.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.ConvertersDbContract.ConverterEntry;
import com.tiomamaster.customizableconverter.data.ConvertersDbContract.UnitEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ConvertersDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final byte DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "Converters.db";

    private static final String CREATE_TABLE_CONVERTER =
            "create table " + ConverterEntry.TABLE_NAME +
                    " (" + ConverterEntry._ID + " integer primary key, " +
                    ConverterEntry.COLUMN_NAME_NAME + " text unique not null" +
                    " check(" + ConverterEntry.COLUMN_NAME_NAME + " not like ''), " +
                    ConverterEntry.COLUMN_NAME_ORDER_POSITION + " integer not null, " +
                    ConverterEntry.COLUMN_NAME_IS_ENABLED + " boolean default 'true', " +
                    ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " boolean, " +
                    ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS + " integer default 0, " +
                    ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT + " text, " +
                    ConverterEntry.COLUMN_NAME_ERRORS + " text default null, " +
                    ConverterEntry.COLUMN_NAME_TYPE + " integer not null default 0, " +
                    ConverterEntry.COLUMN_NAME_LAST_UPDATE + " integer default null)";

    private static final String CREATE_TABLE_UNIT =
            "create table " + UnitEntry.TABLE_NAME +
                    " (" + UnitEntry._ID + "  integer primary key, " +
                    UnitEntry.COLUMN_NAME_NAME + " text not null " +
                    "check(" + UnitEntry.COLUMN_NAME_NAME + " not like ''), " +
                    UnitEntry.COLUMN_NAME_VALUE + " double not null " +
                    "check(" + UnitEntry.COLUMN_NAME_VALUE + " > 0), " +
                    UnitEntry.COLUMN_NAME_ORDER_POSITION + " integer not null, " +
                    UnitEntry.COLUMN_NAME_IS_ENABLED + " boolean default 'true', " +
                    UnitEntry.COLUMN_NAME_CONVERTER_ID + " integer not null, " +
                    UnitEntry.COLUMN_NAME_CHAR_CODE + " text default null, " +
                    "foreign key(" + UnitEntry.COLUMN_NAME_CONVERTER_ID + ") " +
                    "references " + ConverterEntry.TABLE_NAME + "(" + ConverterEntry._ID + ") " +
                    "on delete cascade)";

    private static final String TRIGGER_UPDATE_CONVERTER =
            "create trigger ConverterIsLastSelectedUpdateToTrue before " +
                    "update of " + ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED +
                    " on " + ConverterEntry.TABLE_NAME +
                    " when new." + ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " = 'true' " +
                    "begin update " + ConverterEntry.TABLE_NAME +
                    " set " + ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " = 'false'; end";

    private static final String ENABLE_FK = "PRAGMA foreign_keys = ON;";

    private final Context mContext;

    private final String mDbLang;

    private static final byte TEMPERATURE_CONVERTER_TYPE = 1;
    private static final byte CURRENCY_CONVERTER_TYPE = 2;

    ConvertersDbHelper(Context c, String dbLang) {
        super(c, dbLang + DATABASE_NAME, null, DATABASE_VERSION);
        mContext = c;
        mDbLang = dbLang;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        enableFk(db);

        // create tables
        db.execSQL(CREATE_TABLE_CONVERTER);
        db.execSQL(CREATE_TABLE_UNIT);

        // trigger for update IsLastSelected in Converter
        db.execSQL(TRIGGER_UPDATE_CONVERTER);

        // inflate db
        AssetManager am = mContext.getAssets();
        String[] assets = null;
        try {
            assets = am.list(mDbLang);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] translate = null;
        if (TextUtils.equals(mDbLang, "ru")) {
            translate = mContext.getResources().getStringArray(R.array.translation_for_files_ru);
        }
        int i = 0;
        ContentValues contentValues = new ContentValues();
        for (String s : assets) {
            // insert into table converter
            // by default converter type is 0, but for temperature set type to 1
            if (TextUtils.equals(s, "Temperature")) {
                contentValues.put(ConverterEntry.COLUMN_NAME_TYPE, TEMPERATURE_CONVERTER_TYPE);
            }
            if (TextUtils.equals(mDbLang, "ru")) {
                contentValues.put(ConverterEntry.COLUMN_NAME_NAME, translate[i]);
            } else {
                contentValues.put(ConverterEntry.COLUMN_NAME_NAME, s);
            }
            contentValues.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, ++i);
            db.insert(ConverterEntry.TABLE_NAME, null, contentValues);
            contentValues.clear();
            // insert into table unit
            insertUnitsFromAssets(db, i, s, contentValues);
        }

        // insert currency converter
        contentValues.clear();
        if (TextUtils.equals(mDbLang, "ru"))
            contentValues.put(ConverterEntry.COLUMN_NAME_NAME, "Валюта");
        else contentValues.put(ConverterEntry.COLUMN_NAME_NAME, "Currency");
        contentValues.put(ConverterEntry.COLUMN_NAME_TYPE, CURRENCY_CONVERTER_TYPE);
        contentValues.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, ++i);
        db.insert(ConverterEntry.TABLE_NAME, null, contentValues);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        enableFk(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            // add column type in to converter table
            db.execSQL("alter table " + ConverterEntry.TABLE_NAME + " add column " +
                    ConverterEntry.COLUMN_NAME_TYPE + " integer not null default 0");

            // insert temperature converter
            String converterName = "Temperature";
            long id = insertNewConverter(db, converterName, "Температура", TEMPERATURE_CONVERTER_TYPE);
            // insert temperature converter units
            insertUnitsFromAssets(db, id, converterName, new ContentValues());

            // add column last update in to converter table
            updateDbFrom2to3(db);
        } else if (oldVersion == 2) {
            updateDbFrom2to3(db);
        }
    }

    Pair<String, List<Pair<String, Boolean>>> getAllConverters() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry.COLUMN_NAME_NAME,
                        ConverterEntry.COLUMN_NAME_IS_ENABLED,
                        ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED},
                null, null, null, null, ConverterEntry.COLUMN_NAME_ORDER_POSITION);

        int nameInd = c.getColumnIndex(ConverterEntry.COLUMN_NAME_NAME);
        int isEnabledInd = c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_ENABLED);
        int isLastSelectedInd = c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED);
        List<Pair<String, Boolean>> converters = new ArrayList<>(c.getCount());
        String lastSelConverterName = null;
        if (c.moveToNext()) {
            // set default value
            lastSelConverterName = c.getString(nameInd);
            do {
                converters.add(new Pair<>(c.getString(nameInd), Boolean.parseBoolean(c.getString(isEnabledInd))));
                if (Boolean.parseBoolean(c.getString(isLastSelectedInd)))
                    lastSelConverterName = c.getString(nameInd);
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        return new Pair<>(lastSelConverterName, converters);
    }

    @Nullable
    Converter create(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry._ID,
                        ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS,
                        ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT,
                        ConverterEntry.COLUMN_NAME_ERRORS,
                        ConverterEntry.COLUMN_NAME_TYPE,
                        ConverterEntry.COLUMN_NAME_LAST_UPDATE},
                ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name}, null, null, null);
        if (!c.moveToFirst()) return null;
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));
        int lastSelUnitPos = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS));
        String lastQuantity = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT));
        String errors = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ERRORS));
        int type = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_TYPE));
        long lastUpdate = c.getLong(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_UPDATE));
        c.close();
        if (type == TEMPERATURE_CONVERTER_TYPE) {
            return new TemperatureConverter(name, getUnits(db, converterId, false),
                    errors, lastSelUnitPos, lastQuantity);
        } else if (type == CURRENCY_CONVERTER_TYPE) {
            return new CurrencyConverter(name, getUnits(db, converterId, true),
                    errors, lastSelUnitPos, lastQuantity, lastUpdate);
        }
        return new Converter(name, getUnits(db, converterId, false), errors, lastSelUnitPos, lastQuantity);
    }

    CurrencyConverter create() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME, new String[]{ConverterEntry.COLUMN_NAME_NAME},
                ConverterEntry.COLUMN_NAME_TYPE + " = ?",
                new String[]{String.valueOf(CURRENCY_CONVERTER_TYPE)}, null, null, null);
        c.moveToFirst();
        String name = c.getString(0);
        c.close();
        return (CurrencyConverter) create(name);
    }

    void saveLastConverter(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED, "true");
            db.update(ConverterEntry.TABLE_NAME, values,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void saveLastUnit(String converterName, int pos) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS, pos);
            db.update(ConverterEntry.TABLE_NAME, values,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{converterName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void saveLastQuantity(String converterName, String quantity) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT, quantity);
            db.update(ConverterEntry.TABLE_NAME, values,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{converterName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void delete(String converterName) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(ConverterEntry.TABLE_NAME,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?",
                    new String[]{converterName});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    boolean save(Converter converter, String oldName) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        if (oldName.isEmpty()) {
            return saveNewConverter(converter, db);
        } else {
            return updateConverter(converter, oldName, db);
        }
    }

    void saveOrder(List<Pair<String, Boolean>> converters) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < converters.size(); i++) {
                String name = converters.get(i).first;
                ContentValues values = new ContentValues();
                values.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, i + 1);
                db.update(ConverterEntry.TABLE_NAME, values,
                        ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    void saveState(String name, boolean state) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_IS_ENABLED, String.valueOf(state));
            db.update(ConverterEntry.TABLE_NAME, values,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @NonNull
    List<Converter.Unit> updateOrInsertCourses(List<CurrencyConverter.CurrencyUnit> units) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor c = db.query(ConverterEntry.TABLE_NAME, new String[]{ConverterEntry._ID},
                ConverterEntry.COLUMN_NAME_TYPE + " = ?",
                new String[]{String.valueOf(CURRENCY_CONVERTER_TYPE)}, null, null, null);
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));

        db.beginTransaction();
        try {
            // update last time when units was updated
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_LAST_UPDATE, System.currentTimeMillis());
            db.update(ConverterEntry.TABLE_NAME, values,
                    ConverterEntry.COLUMN_NAME_TYPE + " = ?",
                    new String[]{String.valueOf(CURRENCY_CONVERTER_TYPE)});
            values.clear();

            c = db.query(UnitEntry.TABLE_NAME, null, UnitEntry.COLUMN_NAME_CHAR_CODE + " not null",
                    null, null, null, null);

            if (c.getCount() == 0) {
                // insert currency units
                int unitOrderPos = 1;
                for (CurrencyConverter.CurrencyUnit unit : units) {
                    values.put(UnitEntry.COLUMN_NAME_NAME, unit.name);
                    values.put(UnitEntry.COLUMN_NAME_VALUE, unit.value);
                    values.put(UnitEntry.COLUMN_NAME_ORDER_POSITION, unitOrderPos++);
                    values.put(UnitEntry.COLUMN_NAME_CONVERTER_ID, converterId);
                    values.put(UnitEntry.COLUMN_NAME_CHAR_CODE, unit.charCode);
                    db.insert(UnitEntry.TABLE_NAME, null, values);
                    values.clear();
                }
            } else {
                // update currency units values
                for (CurrencyConverter.CurrencyUnit unit : units) {
                    values.put(UnitEntry.COLUMN_NAME_VALUE, unit.value);
                    db.update(UnitEntry.TABLE_NAME, values,
                            UnitEntry.COLUMN_NAME_CHAR_CODE + " = ?", new String[]{unit.charCode});
                    values.clear();
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            c.close();
        }

        return getUnits(db, converterId, true);
    }

    private boolean saveNewConverter(Converter converter, SQLiteDatabase db) {
        // save new converter at the bottom by order position, of the converters list
        ContentValues values = new ContentValues();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{"max(" + ConverterEntry.COLUMN_NAME_ORDER_POSITION + ")"},
                null, null, null, null, null);
        if (!c.moveToFirst()) return false;
        values.put(ConverterEntry.COLUMN_NAME_NAME, converter.getName());
        values.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, c.getInt(0));
        c.close();

        try {
            // insert into Converter table
            long converterId = db.insert(ConverterEntry.TABLE_NAME, null, values);
            if (converterId == -1) return false;
            values.clear();

            int unitOrderPos = 1;
            for (Converter.Unit unit : converter.getUnits()) {
                values.put(UnitEntry.COLUMN_NAME_NAME, unit.name);
                values.put(UnitEntry.COLUMN_NAME_VALUE, unit.value);
                values.put(UnitEntry.COLUMN_NAME_ORDER_POSITION, unitOrderPos++);
                values.put(UnitEntry.COLUMN_NAME_CONVERTER_ID, converterId);
                // check that insertion is successful
                if (db.insert(UnitEntry.TABLE_NAME, null, values) == -1) return false;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    private boolean updateConverter(Converter converter, String oldName, SQLiteDatabase db) {
        // get useful information about old edited converter before delete it
        Cursor c = db.query(ConverterEntry.TABLE_NAME, new String[]{
                        ConverterEntry.COLUMN_NAME_ORDER_POSITION,
                        ConverterEntry.COLUMN_NAME_IS_ENABLED,
                        ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED,
                        ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS},
                ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{oldName}, null, null, null);
        if (!c.moveToFirst()) return false;
        int orderPosition = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ORDER_POSITION));
        boolean isEnabled = Boolean.parseBoolean(
                c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_ENABLED)));
        boolean isLastSelected = Boolean.parseBoolean(
                c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED)));
        int lastSelUnitPos = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS));
        c.close();

        // delete old converter
        delete(oldName);

        // insert edited converter as new
        ContentValues values = new ContentValues();
        values.put(ConverterEntry.COLUMN_NAME_NAME, converter.getName());
        values.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, orderPosition);
        values.put(ConverterEntry.COLUMN_NAME_IS_ENABLED, String.valueOf(isEnabled));
        values.put(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED, String.valueOf(isLastSelected));
        values.put(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS, lastSelUnitPos);
        values.put(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT, converter.getLastQuantity());
        values.put(ConverterEntry.COLUMN_NAME_ERRORS, converter.getErrors());

        // handle special cases
        boolean isCurrencyConverter = false;
        if (converter instanceof TemperatureConverter) {
            values.put(ConverterEntry.COLUMN_NAME_TYPE, TEMPERATURE_CONVERTER_TYPE);
        } else if (converter instanceof CurrencyConverter) {
            values.put(ConverterEntry.COLUMN_NAME_TYPE, CURRENCY_CONVERTER_TYPE);
            values.put(ConverterEntry.COLUMN_NAME_LAST_UPDATE,
                    ((CurrencyConverter) converter).getLastUpdateTime());
            isCurrencyConverter = true;
        }
        try {
            long converterId = db.insert(ConverterEntry.TABLE_NAME, null, values);
            if (converterId == -1) return false;
            values.clear();

            // insert units
            int unitOrderPos = 1;
            for (Converter.Unit unit : converter.getUnits()) {
                values.put(UnitEntry.COLUMN_NAME_NAME, unit.name);
                values.put(UnitEntry.COLUMN_NAME_VALUE, unit.value);
                values.put(UnitEntry.COLUMN_NAME_IS_ENABLED, String.valueOf(unit.isEnabled));
                values.put(UnitEntry.COLUMN_NAME_ORDER_POSITION, unitOrderPos++);
                values.put(UnitEntry.COLUMN_NAME_CONVERTER_ID, converterId);
                if (isCurrencyConverter) values.put(UnitEntry.COLUMN_NAME_CHAR_CODE,
                        ((CurrencyConverter.CurrencyUnit) unit).charCode);
                // check that insertion is successful
                if (db.insert(UnitEntry.TABLE_NAME, null, values) == -1) return false;
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    @NonNull
    private List<Converter.Unit> getUnits(SQLiteDatabase db, int converterId, boolean currency) {
        Cursor c = db.query(UnitEntry.TABLE_NAME,
                new String[]{UnitEntry.COLUMN_NAME_NAME,
                        UnitEntry.COLUMN_NAME_VALUE,
                        UnitEntry.COLUMN_NAME_IS_ENABLED,
                        UnitEntry.COLUMN_NAME_CHAR_CODE},
                UnitEntry.COLUMN_NAME_CONVERTER_ID + " = ?",
                new String[]{String.valueOf(converterId)}, null, null,
                UnitEntry.COLUMN_NAME_ORDER_POSITION);

        int nameColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_NAME);
        int valueColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_VALUE);
        int isEnabledColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_IS_ENABLED);
        int charCodeInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_CHAR_CODE);
        List<Converter.Unit> units = new ArrayList<>();
        while (c.moveToNext()) {
            if (currency) {
                units.add(new CurrencyConverter.CurrencyUnit(c.getString(nameColInd), c.getDouble(valueColInd),
                        Boolean.parseBoolean(c.getString(isEnabledColInd)), c.getString(charCodeInd)));
            } else {
                units.add(new Converter.Unit(c.getString(nameColInd), c.getDouble(valueColInd),
                        Boolean.parseBoolean(c.getString(isEnabledColInd))));
            }
        }
        c.close();
        db.close();

        return units;
    }

    private void enableFk(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            db.execSQL(ENABLE_FK);
        }
    }

    private void insertUnitsFromAssets(SQLiteDatabase db, long converterId,
                                       String converterName, ContentValues contentValues) {
        AssetManager am = mContext.getAssets();
        BufferedReader reader = null;
        StringBuilder errors = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(am.open(mDbLang + "/" + converterName)));
            String line;
            Pattern pattern = Pattern.compile(" +\\d+([.]|[,])?\\d* *");
            Matcher matcher = pattern.matcher("");
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                matcher.reset(line);
                if (matcher.find()) {
                    double value = Double.parseDouble(matcher.group().trim());
                    if (value != 0) {
                        String name = matcher.replaceAll("").trim();
                        if (!TextUtils.isEmpty(name)) {
                            contentValues.put(UnitEntry.COLUMN_NAME_NAME, name);
                            contentValues.put(UnitEntry.COLUMN_NAME_VALUE, value);
                            contentValues.put(UnitEntry.COLUMN_NAME_CONVERTER_ID, converterId);
                            contentValues.put(UnitEntry.COLUMN_NAME_ORDER_POSITION, lineNum);
                            db.insert(UnitEntry.TABLE_NAME, null, contentValues);
                            contentValues.clear();
                        } else {
                            Log.e(TAG, "onCreate: unit name is empty in line "
                                    + lineNum + " in file " + converterName);
                            errors.append("Unit name is empty in line ").append(lineNum).append("\n");
                        }
                    } else {
                        Log.e(TAG, "onCreate: unit value is 0 in line "
                                + lineNum + " in file " + converterName);
                        errors.append("Unit value is 0 in line ").append(lineNum).append("\n");
                    }
                } else {
                    Log.e(TAG, "onCreate: unit value is empty in line "
                            + lineNum + " in file " + converterName);
                    errors.append("Unit value is empty in line ").append(lineNum).append("\n");
                }
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (errors.length() != 0) {
            // delete last \n
            errors.deleteCharAt(errors.length() - 1);
            contentValues.put(ConverterEntry.COLUMN_NAME_ERRORS, errors.toString());
            db.update(ConverterEntry.TABLE_NAME, contentValues, ConverterEntry._ID + " = ?",
                    new String[]{String.valueOf(converterId)});
        }
    }

    private long insertNewConverter(SQLiteDatabase db, String enName, String ruName, byte type) {
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{"max(" + ConverterEntry.COLUMN_NAME_ORDER_POSITION + ")"},
                null, null, null, null, null);
        c.moveToFirst();
        int converterOrderPos = c.getInt(0) + 1;
        c.close();
        ContentValues contentValues = new ContentValues();
        if (TextUtils.equals(mDbLang, "ru")) {
            contentValues.put(ConverterEntry.COLUMN_NAME_NAME, ruName);
        } else {
            contentValues.put(ConverterEntry.COLUMN_NAME_NAME, enName);
        }
        contentValues.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, converterOrderPos);
        contentValues.put(ConverterEntry.COLUMN_NAME_TYPE, type);
        return db.insert(ConverterEntry.TABLE_NAME, null, contentValues);
    }

    private void updateDbFrom2to3(SQLiteDatabase db) {
        // add column last update in to converter table
        db.execSQL("alter table " + ConverterEntry.TABLE_NAME + " add column " +
                ConverterEntry.COLUMN_NAME_LAST_UPDATE + " integer default null");

        // add column char code in to unit table
        db.execSQL("alter table " + UnitEntry.TABLE_NAME + " add column " +
                UnitEntry.COLUMN_NAME_CHAR_CODE + " text default null");

        // insert currency converter
        insertNewConverter(db, "Currency", "Валюта", CURRENCY_CONVERTER_TYPE);
    }
}