package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 14.07.2016.
 */
public class Converter {

    // counter for default ordering
    private static int count;

    @NonNull
    private final String mName;

    @NonNull
    private final LinkedHashMap<String, Double> mUnits;

    @Nullable
    private final String mErrors;

    private int mOrderPosition;

    private int mLastUnitPosition;

    @Nullable
    private String mLastQuantity;

    public Converter(@NonNull String name, @NonNull LinkedHashMap<String, Double> units,
                     @Nullable String errors, int orderPosition, int lastUnit, @Nullable String lastQuantity) {
        checkNotNull(name);
        checkNotNull(units);
        mUnits = units;
        mName = name;
        mErrors = errors;
        mOrderPosition = orderPosition;
        mLastUnitPosition = lastUnit;
        mLastQuantity = lastQuantity;
    }

    public Converter(@NonNull String mName, @NonNull LinkedHashMap<String, Double> mUnits, String mErrors) {
        this.mName = mName;
        this.mUnits = mUnits;
        this.mErrors = mErrors;
    }

    public Converter(@NonNull String name, @NonNull LinkedHashMap<String, Double> units) {
        this(name, units, null, count++, 0, "");
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @NonNull
    public Map<String, Double> getUnits() {
        return mUnits;
    }

    public double convertFromTo(double quantity, String fromUnit, String toUnit) {
        return quantity * mUnits.get(fromUnit) / mUnits.get(toUnit);
    }

    @NonNull
    public double[] convertAll(double quantity, String from) {
        double[] result = new double[mUnits.size()];
        String[] to = getAllUnitsName();
        for (int i = 0; i < result.length; i++) {
            result[i] = convertFromTo(quantity, from, to[i]);
        }
        return result;
    }

    /**
     * @return
     * [i][0] - unit name,
     * <br>[i][1] - conversion result.
     */
    @NonNull
    public String[][] convertAllExt(double quantity, String from) {
        String[][] result = new String[mUnits.size()][2];
        String[] to = getAllUnitsName();
        double[] results = convertAll(quantity, from);
        for (int i = 0; i < result.length; i++) {
            result[i][0] = to[i];
            result[i][1] = String.valueOf(results[i]);
        }
        return result;
    }

    @NonNull
    public String[] getAllUnitsName() {
        return mUnits.keySet().toArray(new String[]{});
    }

    @Nullable
    public String getErrors() {
        return mErrors;
    }

    public int getOrderPosition() {
        return mOrderPosition;
    }

    public void setOrderPosition(int orderPosition) {
        mOrderPosition = orderPosition;
    }

    public int getLastUnitPosition() {
        return mLastUnitPosition;
    }

    public void setLastUnitPosition(int mLastUnit) {
        this.mLastUnitPosition = mLastUnit;
    }

    public String getLastQuantity() {
        if (mLastQuantity == null)
            return "";
        return mLastQuantity;
    }

    public void setLastQuantity(String mLastQuantity) {
        this.mLastQuantity = mLastQuantity;
    }

}