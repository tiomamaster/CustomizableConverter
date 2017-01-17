package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.google.common.base.Objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.name;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.tiomamaster.customizableconverter.R.id.quantity;

/**
 * Created by Artyom on 14.07.2016.
 */
public class Converter implements SettingsRepository.OnSettingsChangeListener {

    // counter for default ordering
    private static int count;

    @NonNull private String mName;

    @NonNull private final List<Unit> mUnits;

    @Nullable private final String mErrors;

    private int mOrderPosition;

    private int mLastUnitPosition;

    @Nullable private String mLastQuantity;

    private static  DecimalFormat sDecimalFormat;

    private static boolean isDefaultForm;

    public Converter(@NonNull String name, @NonNull List<Unit> units,
                     @Nullable String errors, int orderPosition, int lastUnit,
                     @Nullable String lastQuantity) {
        mName = checkNotNull(name, "name cannot be null");
        mUnits = checkNotNull(units, "units cannot be null");
        mErrors = errors;
        mOrderPosition = orderPosition;
        mLastUnitPosition = lastUnit;
        mLastQuantity = lastQuantity;
        sDecimalFormat = (DecimalFormat) DecimalFormat.getInstance();
    }

    public Converter(@NonNull String name, @NonNull List<Unit> units, String errors) {
        this(name, units, errors, count++, 0, "");
    }

    public Converter(@NonNull String name, @NonNull List<Unit> units) {
        this(name, units, null, count++, 0, "");
    }

    @Override
    public void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm) {
        if (stForm) {
            sDecimalFormat.applyPattern("0.0E0");
            DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
            decimalFormatSymbols.setExponentSeparator("×10");
            sDecimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);
        } else {
            // apply default pattern
            sDecimalFormat.applyPattern("#,##0.###");
        }
        sDecimalFormat.setGroupingSize(grSize);
        sDecimalFormat.setMaximumFractionDigits(maxFrDigits);
        isDefaultForm = defForm;
    }

    @NonNull
    public List<Pair<String, String>> convertAll(double quantity, String fromUnit) {
        List<Pair<String, String>> result = new ArrayList<>(mUnits.size());
        Unit from = mUnits.get(mUnits.indexOf(new Unit(fromUnit, 1, true)));

        for (Unit to : mUnits) {
            if (to.isEnabled && !to.name.equals(from.name)) {
                result.add(new Pair<>(to.name, convert(quantity, from.value, to.value)));
            }
        }

        return result;
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public void setName(@NonNull String name) {
        checkNotNull(name);

        mName = name;
    }

    @NonNull
    public List<Unit> getUnits() {
        return mUnits;
    }

    @NonNull
    public List<String> getEnabledUnitsName() {
        List<String> units = new ArrayList<>(mUnits.size());
        for (Unit unit : mUnits) {
            if (unit.isEnabled) units.add(unit.name);
        }
        return units;
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

    @NonNull
    public String getLastQuantity() {
        if (mLastQuantity == null)
            return "";
        return mLastQuantity;
    }

    public void setLastQuantity(String mLastQuantity) {
        this.mLastQuantity = mLastQuantity;
    }

    SettingsRepository.OnSettingsChangeListener getOnSettingsChangeListener() {
        return this;
    }

    private String convert(double quantity, double from, double to){
        if (isDefaultForm) return String.valueOf(quantity * from / to);
        return sDecimalFormat.format(quantity * from / to);
    }

    public static class Unit {
        public @NonNull String name;
        public double value;
        public boolean isEnabled;

        public Unit(@NonNull String name, double value, boolean isEnabled) {
            this.name = checkNotNull(name);
            this.value = value;
            this.isEnabled = isEnabled;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Unit unit = (Unit) o;
            return Objects.equal(name, unit.name);
        }
    }
}