package com.tiomamaster.customizableconverter.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.ConvertersDbContract.ConverterEntry;
import com.tiomamaster.customizableconverter.data.ConvertersDbContract.UnitEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.name;
import static android.R.attr.value;
import static android.R.attr.y;
import static com.tiomamaster.customizableconverter.R.id.quantity;
import static com.tiomamaster.customizableconverter.R.id.transition_current_scene;

/**
 * Created by Artyom on 26.08.2016.
 */
final class ConvertersDbHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Converters.db";

    private static final String CREATE_TABLE_CONVERTER =
            "create table " + ConverterEntry.TABLE_NAME +
                    " (" + ConverterEntry._ID + " integer primary key, " +
                    ConverterEntry.COLUMN_NAME_NAME + " text unique not null" +
                    " check(" + ConverterEntry.COLUMN_NAME_NAME + " not like ''), " +
                    ConverterEntry.COLUMN_NAME_ORDER_POSITION + " integer not null, " +
                    ConverterEntry.COLUMN_NAME_IS_ENABLED + " boolean default true, " +
                    ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " boolean default false, " +
                    ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS + " integer default 1, " +
                    ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT + " text default '1', " +                 // TODO: set default empty and handle this case in ConverterPresenter
                    ConverterEntry.COLUMN_NAME_ERRORS + " text default null)";

    private static final String CREATE_TABLE_UNIT =
            "create table " + UnitEntry.TABLE_NAME +
                    " (" + UnitEntry._ID + "  integer primary key, " +
                    UnitEntry.COLUMN_NAME_NAME + " text not null " +
                    "check(" + UnitEntry.COLUMN_NAME_NAME + " not like ''), " +
                    UnitEntry.COLUMN_NAME_VALUE + " double not null " +
                    "check(" + UnitEntry.COLUMN_NAME_VALUE + " > 0), " +
                    UnitEntry.COLUMN_NAME_ORDER_POSITION + " integer not null, " +
                    UnitEntry.COLUMN_NAME_IS_ENABLED + " boolean default true, " +
                    UnitEntry.COLUMN_NAME_CONVERTER_ID + " integer not null, " +
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

    private Context mContext;

    private String mDbLang;

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
        for (String s : assets) {
            // insert into table converter
            ContentValues contentValues = new ContentValues();
            if (TextUtils.equals(mDbLang, "ru"))
                contentValues.put(ConverterEntry.COLUMN_NAME_NAME, translate[i]);
            else
                contentValues.put(ConverterEntry.COLUMN_NAME_NAME, s);
            contentValues.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, ++i);
            db.insert(ConverterEntry.TABLE_NAME, null, contentValues);
            contentValues.clear();

            // read file line by line and insert into table unit
            BufferedReader reader = null;
            StringBuilder errors = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(am.open(mDbLang + "/" + s)));
                String line;
                Pattern pattern = Pattern.compile(" +\\d+([.]|[,])?\\d* *");
                Matcher matcher = pattern.matcher("");
                int lineNum = 1;
                while ((line = reader.readLine()) != null) {
                    matcher.reset(line);
                    if (matcher.find()) {
                        Double value = Double.valueOf(matcher.group().trim());
                        if (value != 0) {
                            String name = matcher.replaceAll("").trim();
                            if (!TextUtils.isEmpty(name)) {
                                contentValues.put(UnitEntry.COLUMN_NAME_NAME, name);
                                contentValues.put(UnitEntry.COLUMN_NAME_VALUE, value);
                                contentValues.put(UnitEntry.COLUMN_NAME_CONVERTER_ID, i);
                                contentValues.put(UnitEntry.COLUMN_NAME_ORDER_POSITION, lineNum);
                                db.insert(UnitEntry.TABLE_NAME, null, contentValues);
                                contentValues.clear();
                            } else {
                                Log.e(TAG, "onCreate: unit name is empty in line "
                                        + lineNum + " in file " + s);
                                errors.append("Unit name is empty in line "
                                        + lineNum + "\n");
                            }
                        } else {
                            Log.e(TAG, "onCreate: unit value is 0 in line "
                                    + lineNum + " in file " + s);
                            errors.append("Unit value is 0 in line "
                                    + lineNum + "\n");
                        }
                    } else {
                        Log.e(TAG, "onCreate: unit value is empty in line "
                                + lineNum + " in file " + s);
                        errors.append("Unit value is empty in line "
                                + lineNum + "\n");
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
            // update converter if any showConverterExistError occurs after reading asset file
            if (errors.length() != 0) {
                // delete last \n
                errors.deleteCharAt(errors.length() - 1);
                contentValues.put(ConverterEntry.COLUMN_NAME_ERRORS, errors.toString());
                db.update(ConverterEntry.TABLE_NAME, contentValues, ConverterEntry._ID + " = ?",
                        new String[]{String.valueOf(i)});
            }
        }

        // TODO: delete this after adding and testing asynchronous call
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        enableFk(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    List<Pair<String, Boolean>> getAllConverters() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry.COLUMN_NAME_NAME,
                        ConverterEntry.COLUMN_NAME_IS_ENABLED},
                null, null, null, null, ConverterEntry.COLUMN_NAME_ORDER_POSITION);

        int nameInd = c.getColumnIndex(ConverterEntry.COLUMN_NAME_NAME);
        int isEnabledInd = c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_ENABLED);
        List<Pair<String, Boolean>> result = new ArrayList<>(c.getCount());
        while (c.moveToNext()) {
            result.add(new Pair<>(c.getString(nameInd), Boolean.parseBoolean(c.getString(isEnabledInd))));
        }
        c.close();
        db.close();

        // TODO: delete this after adding and testing asynchronous call
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    Converter createConverter(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry._ID,
                        ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS,
                        ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT,
                        ConverterEntry.COLUMN_NAME_ERRORS},
                ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name}, null, null, null);
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));
        int lastSelUnitPos = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS));
        String lastQuantity = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT));
        String errors = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ERRORS));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors, lastSelUnitPos, lastQuantity);
    }

    Converter createLastConverter() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry._ID,
                        ConverterEntry.COLUMN_NAME_NAME,
                        ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS,
                        ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT,
                        ConverterEntry.COLUMN_NAME_ERRORS},
                ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " = ?", new String[]{"true"},
                null, null, null);
        if (!c.moveToFirst()) {
            // this case is first run, so simply return first converter
            c = db.query(ConverterEntry.TABLE_NAME,
                    new String[]{ConverterEntry._ID,
                            ConverterEntry.COLUMN_NAME_NAME,
                            ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS,
                            ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT,
                            ConverterEntry.COLUMN_NAME_ERRORS},
                            null, null, null, null, ConverterEntry.COLUMN_NAME_ORDER_POSITION);
        }
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));
        String name = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_NAME));
        int lastSelUnitPos = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_POS));
        String lastQuantity = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT));
        String errors = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ERRORS));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors, lastSelUnitPos, lastQuantity);
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
        // update existing one
        // get useful information about old edited converter before delete it
        Cursor c = db.query(ConverterEntry.TABLE_NAME, new String[]{
                        ConverterEntry.COLUMN_NAME_ORDER_POSITION,
                        ConverterEntry.COLUMN_NAME_IS_ENABLED,
                        ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED},
                ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{oldName}, null, null, null);
        if (!c.moveToFirst()) return false;
        int orderPosition = c.getInt(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ORDER_POSITION));
        boolean isEnabled = Boolean.parseBoolean(
                c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_ENABLED)));
        boolean isLastSelected = Boolean.parseBoolean(
                c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED)));
        c.close();

        // delete old converter
        delete(oldName);

        // insert edited converter as new
        ContentValues values = new ContentValues();
        values.put(ConverterEntry.COLUMN_NAME_NAME, converter.getName());
        values.put(ConverterEntry.COLUMN_NAME_ORDER_POSITION, orderPosition);
        values.put(ConverterEntry.COLUMN_NAME_IS_ENABLED, isEnabled);
        values.put(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED, isLastSelected);
        values.put(ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT, converter.getLastQuantity());
        values.put(ConverterEntry.COLUMN_NAME_ERRORS, converter.getErrors());
        try {
            long converterId = db.insert(ConverterEntry.TABLE_NAME, null, values);
            if (converterId == -1) return false;
            values.clear();

            int unitOrderPos = 1;
            for (Converter.Unit unit : converter.getUnits()) {
                values.put(UnitEntry.COLUMN_NAME_NAME, unit.name);
                values.put(UnitEntry.COLUMN_NAME_VALUE, unit.value);
                values.put(UnitEntry.COLUMN_NAME_IS_ENABLED, String.valueOf(unit.isEnabled));
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

    @NonNull
    private List<Converter.Unit> getUnits(SQLiteDatabase db, int converterId) {
        Cursor c = db.query(UnitEntry.TABLE_NAME,
                new String[]{UnitEntry.COLUMN_NAME_NAME,
                        UnitEntry.COLUMN_NAME_VALUE,
                        UnitEntry.COLUMN_NAME_IS_ENABLED},
                UnitEntry.COLUMN_NAME_CONVERTER_ID + " = ?",
                new String[]{String.valueOf(converterId)}, null, null,
                UnitEntry.COLUMN_NAME_ORDER_POSITION);

        int nameColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_NAME);
        int valueColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_VALUE);
        int isEnabledColInd = c.getColumnIndex(UnitEntry.COLUMN_NAME_IS_ENABLED);
        List<Converter.Unit> units = new ArrayList<>();
        while (c.moveToNext()) {
            units.add(new Converter.Unit(c.getString(nameColInd), c.getDouble(valueColInd),
                    Boolean.parseBoolean(c.getString(isEnabledColInd))));
        }
        c.close();
        db.close();

        // TODO: delete this after adding and testing asynchronous call
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return units;
    }

    private void enableFk(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            db.execSQL(ENABLE_FK);
        }
    }
}