package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class CurrencyConverter extends Converter {

    /**
     * Representation of time when units was lastly updated.
     */
    private long mLastUpdate;

    public CurrencyConverter(@NonNull String name, @NonNull List<Unit> units,
                             @Nullable String errors, int lastUnit,
                             @Nullable String lastQuantity) {
        super(name, units, errors, lastUnit, lastQuantity);
    }

    public CurrencyConverter(@NonNull String name, @NonNull List<Unit> units) {
        super(name, units);
    }

    public static class CurrencyUnit extends Unit {
        public String charCode;

        public CurrencyUnit(@NonNull String name, double value, boolean isEnabled, @NonNull String charCode) {
            super(name, value, isEnabled);
            this.charCode = checkNotNull(charCode);
        }
    }
}