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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            ConverterEntry.COLUMN_NAME_LAST_SELECTED_UNIT_ID + " integer default 1, " +
            ConverterEntry.COLUMN_NAME_LAST_QUANTITY_TEXT + " text default '', " +
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
            UnitEntry.COLUMN_NAME_CONVERTER_ID + " integer not null, "  +
            "foreign key(" + UnitEntry.COLUMN_NAME_CONVERTER_ID + ") " +
            "references " + ConverterEntry.TABLE_NAME + "(" + ConverterEntry._ID + ") " +
            "on delete cascade)";

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

        // TODO: trigger for update IsLastSelected in Converter

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
                    if(matcher.find()) {
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
                            }
                            else {
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
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    Converter createConverter(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry._ID, ConverterEntry.COLUMN_NAME_ERRORS},
                ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name}, null, null, null);
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));
        String errors = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ERRORS));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors);
    }

    Converter createLastConverter() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(ConverterEntry.TABLE_NAME,
                new String[]{ConverterEntry._ID,
                        ConverterEntry.COLUMN_NAME_NAME,
                        ConverterEntry.COLUMN_NAME_ERRORS},
                ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED + " = ?", new String[]{"true"},
                null, null, null);
        if (!c.moveToFirst()) {
            // this case is first run, so simply return first converter
            c = db.query(ConverterEntry.TABLE_NAME,
                    new String[]{ConverterEntry._ID,
                            ConverterEntry.COLUMN_NAME_NAME,
                            ConverterEntry.COLUMN_NAME_ERRORS},
                    ConverterEntry._ID + " = ?", new String[]{"1"}, null, null, null);
        }
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(ConverterEntry._ID));
        String name = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_NAME));
        String errors = c.getString(c.getColumnIndex(ConverterEntry.COLUMN_NAME_ERRORS));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors);
    }

    void saveLastConverter(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(ConverterEntry.COLUMN_NAME_IS_LAST_SELECTED, true);
            db.update(CREATE_TABLE_CONVERTER, values,
                    ConverterEntry.COLUMN_NAME_NAME + " = ?", new String[]{name});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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
        while(c.moveToNext()) {
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