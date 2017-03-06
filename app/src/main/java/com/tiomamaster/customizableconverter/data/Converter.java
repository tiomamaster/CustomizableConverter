package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.google.common.base.Objects;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Converter implements Cloneable {

    private static  DecimalFormat sDecimalFormat;

    private static boolean isDefaultForm;

    @NonNull private String mName;

    @NonNull private List<Unit> mUnits;

    @Nullable private final String mErrors;

    private int mLastUnitPosition;

    @Nullable private String mLastQuantity;

    public Converter(@NonNull String name, @NonNull List<Unit> units,
                     @Nullable String errors, int lastUnit,
                     @Nullable String lastQuantity) {
        mName = checkNotNull(name, "name cannot be null");
        mUnits = checkNotNull(units, "units cannot be null");
        mErrors = errors;
        mLastUnitPosition = lastUnit;
        mLastQuantity = lastQuantity;
    }

    public Converter(@NonNull String name, @NonNull List<Unit> units, String errors) {
        this(name, units, errors, 0, "1");
    }

    public Converter(@NonNull String name, @NonNull List<Unit> units) {
        this(name, units, null);
    }

    static SettingsRepository.OnSettingsChangeListener getOnSettingsChangeListener() {
        return new SettingsRepository.OnSettingsChangeListener() {
            @Override
            public void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm,
                                         boolean langChanged) {
                // initialize at first call and further update DecimalFormat instance if app lang change
                sDecimalFormat = (DecimalFormat) DecimalFormat.getInstance();

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
        };
    }

    @Override
    public Object clone() {
        Converter o = null;
        try {
            o = (Converter) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        o.mUnits = new ArrayList<>(mUnits.size());
        for (Unit unit : mUnits) {
            o.mUnits.add((Unit) unit.clone());
        }
        return o;
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

    public int getLastUnitPosition() {
        return mLastUnitPosition;
    }

    public void setLastUnitPosition(int lastUnit) {
        mLastUnitPosition = lastUnit;
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

    private String convert(double quantity, double from, double to){
        if (isDefaultForm) return String.valueOf(quantity * from / to);
        return sDecimalFormat.format(quantity * from / to);
    }

    public static class Unit implements Cloneable {
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

        @Override
        public Object clone() {
            Object o = null;
            try {
                o = super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return o;
        }
    }
}