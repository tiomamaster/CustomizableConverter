package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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

    private DecimalFormat mDecimalFormat;
    private boolean isStandardFormOfNumber = false;
    private int mGroupingSize = 3;
    private int mMaximumFractionDigits = 10;

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

        mDecimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        if (isStandardFormOfNumber) {
            mDecimalFormat.applyPattern("0.0E0");
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            decimalFormatSymbols.setExponentSeparator("×10");
            mDecimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        }
        mDecimalFormat.setGroupingSize(mGroupingSize);
        mDecimalFormat.setMaximumFractionDigits(mMaximumFractionDigits);
    }

    public Converter(@NonNull String name, @NonNull LinkedHashMap<String, Double> units, String errors) {
        this(name, units, errors, count++, 0, "");
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

    /**
     * @return
     * String[][] excepting the unit from
     * <br>[i][0] - unit name,
     * <br>[i][1] - formatted conversion result.
     */
    @NonNull
    public String[][] convertAllExtFormatted(double quantity, String from) {
        String[][] result = new String[mUnits.size() - 1][2];
        String[] to = getAllUnitsName();
        double[] results = convertAll(quantity, from);

        for (int j = 0, i = 0; i < result.length + 1; i++) {
            if (from.equals(to[i])) {
                j++;
                continue;
            }
            result[i - j][0] = to[i];
            result[i - j][1] = mDecimalFormat.format(results[i]);
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