package com.tiomamaster.customizableconverter.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.tiomamaster.customizableconverter.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Artyom on 26.08.2016.
 */
final class ConvertersDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ServiceApiEndpoint";

    private static final int DATABASE_VERSION = 1;
    private static final String CONVERTER_TABLE_NAME = "Converter";
    private static final String UNIT_TABLE_NAME = "Unit";

    private String mDBName;

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
            "ConverterId integer not null)";


    private static Context c;

    public ConvertersDatabaseHelper(String name) {
        super(c, name, null, DATABASE_VERSION);
        mDBName = name;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create tables
        db.execSQL(CONVERTER_TABLE_CREATE);
        db.execSQL(UNIT_TABLE_CREATE);

        AssetManager am = c.getAssets();
        String[] assets = null;
        try {
            assets = am.list(mDBName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] translate = null;
        if (TextUtils.equals(mDBName, "ru")) {
            translate = c.getResources().getStringArray(R.array.translation_for_files_ru);
        }
        int i = 0;
        for (String s : assets) {
            // insert into table converter
            ContentValues contentValues = new ContentValues();
            if (TextUtils.equals(mDBName, "ru"))
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
                reader = new BufferedReader(new InputStreamReader(am.open(mDBName + "/" + s)));
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    String[] getAllConvertersName() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(CONVERTER_TABLE_NAME, new String[]{"Name"},
                null, null, null, null, "OrderPosition");
        String[] result = new String[c.getCount()];
        int i = 0;
        while (c.moveToNext()) {
            result[i++] = c.getString(0);
        }
        c.close();
        db.close();
        return result;
    }

    Converter createConverter(String name) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(CONVERTER_TABLE_NAME, new String[]{"Id", "Errors"},
                "Name = ?", new String[]{name}, null, null, null);
        c.moveToFirst();
        int converterId = c.getInt(0);
        String errors = c.getString(1);
        c.close();
        c = db.query(UNIT_TABLE_NAME, new String[]{"Name", "Value"},
                "ConverterId = ?", new String[]{String.valueOf(converterId)}, null, null, "OrderPosition");
        LinkedHashMap<String, Double> units = new LinkedHashMap<>();
        while(c.moveToNext()) {
            units.put(c.getString(0), c.getDouble(1));
        }
        c.close();
        db.close();
        return new Converter(name, new ArrayList<Converter.Unit>(), errors);
    }

     static void initialize(Context context) {
         c = context;
         String language = Locale.getDefault().getLanguage();
    }
}