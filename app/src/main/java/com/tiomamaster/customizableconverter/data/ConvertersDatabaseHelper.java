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
final class ConvertersDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "ConvertersDB";
    private static final String CONVERTER_TABLE_NAME = "Converter";
    private static final String UNIT_TABLE_NAME = "Unit";

    private static final String CONVERTER_TABLE_CREATE = "create table " + CONVERTER_TABLE_NAME +
            " (Id  integer primary key, " +
            "Name text unique not null check(Name not like ''), " +
            "OrderPosition integer not null, " +
            "IsEnabled boolean default true, " +
            "IsLastSelected boolean default false, " +
            "LastSelectedUnitId integer default 1, " +
            "LastQuantityText text default '', " +
            "Errors text default null)";

    private static final String UNIT_TABLE_CREATE = "create table " + UNIT_TABLE_NAME +
            " (Id  integer primary key, " +
            "Name text not null check(Name not like ''), " +
            "Value double not null check(Value > 0), " +
            "OrderPosition integer not null, " +
            "IsEnabled boolean default true, " +
            "ConverterId integer not null, "  +
            "foreign key(ConverterId) references " + CONVERTER_TABLE_NAME + "(Id) " +
            "on delete cascade)";

    private static final String ENABLE_FK = "PRAGMA foreign_keys = ON;";

    private Context mContext;

    private String mDbLang;

    ConvertersDatabaseHelper(Context c, String dbLang) {
        super(c, DATABASE_NAME + dbLang, null, DATABASE_VERSION);
        mContext = c;
        mDbLang = dbLang;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        enableFk(db);

        // create tables
        db.execSQL(CONVERTER_TABLE_CREATE);
        db.execSQL(UNIT_TABLE_CREATE);

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
                contentValues.put("Name", translate[i]);
            else
                contentValues.put("Name", s);
            contentValues.put("OrderPosition", ++i);
            db.insert(CONVERTER_TABLE_NAME, null, contentValues);
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
                                contentValues.put("Name", name);
                                contentValues.put("Value", value);
                                contentValues.put("ConverterId", i);
                                contentValues.put("OrderPosition", lineNum);
                                db.insert(UNIT_TABLE_NAME, null, contentValues);
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
                contentValues.put("Errors", errors.toString());
                db.update(CONVERTER_TABLE_NAME, contentValues, "Id = ?",
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
        String name = "Name";
        String isEnabled = "IsEnabled";
        Cursor c = db.query(CONVERTER_TABLE_NAME, new String[]{name, isEnabled},
                null, null, null, null, "OrderPosition");
        int nameInd = c.getColumnIndex(name);
        int isEnabledInd = c.getColumnIndex(isEnabled);
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
        String idCol = "Id";
        String errorsCol = "Errors";
        Cursor c = db.query(CONVERTER_TABLE_NAME, new String[]{idCol, errorsCol},
                "Name = ?", new String[]{name}, null, null, null);
        c.moveToFirst();
        int converterId = c.getInt(c.getColumnIndex(idCol));
        String errors = c.getString(c.getColumnIndex(errorsCol));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors);
    }

    Converter createLastConverter() {
        SQLiteDatabase db = getReadableDatabase();
        String idCol = "Id";
        String nameCol = "Name";
        String errorsCol = "Errors";
        Cursor c = db.query(CONVERTER_TABLE_NAME, new String[]{idCol, nameCol, errorsCol},
                "IsLastSelected = ?", new String[]{"true"}, null, null, null);
        if (!c.moveToFirst()) {
            // this case is first run, so simply return first converter
            c = db.query(CONVERTER_TABLE_NAME, new String[]{idCol, nameCol, errorsCol},
                    "Id = ?", new String[]{"1"}, null, null, null);
        }
        int converterId = c.getInt(c.getColumnIndex(idCol));
        String name = c.getString(c.getColumnIndex(nameCol));
        String errors = c.getString(c.getColumnIndex(errorsCol));
        c.close();
        return new Converter(name, getUnits(db, converterId), errors);
    }

    void saveLastConverter(String name) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("IsLastSelected", true);
            db.update(CONVERTER_TABLE_CREATE, values, "Name = ?", new String[]{name});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @NonNull
    private List<Converter.Unit> getUnits(SQLiteDatabase db, int converterId) {
        Cursor c;
        String nameCol = "Name";
        String valueCol = "Value";
        String isEnabledCol = "IsEnabled";
        c = db.query(UNIT_TABLE_NAME, new String[]{nameCol, valueCol, isEnabledCol},
                "ConverterId = ?", new String[]{String.valueOf(converterId)}, null, null, "OrderPosition");
        int nameColInd = c.getColumnIndex(nameCol);
        int valueColInd = c.getColumnIndex(valueCol);
        int isEnabledColInd = c.getColumnIndex(isEnabledCol);
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