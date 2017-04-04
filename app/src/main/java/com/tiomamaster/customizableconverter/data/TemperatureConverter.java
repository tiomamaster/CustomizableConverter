package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TemperatureConverter extends Converter {

    private static final byte CELSIUS_ID   = 0;
    private static final byte KELVIN_ID    = 1;
    private static final byte FARENEIT_ID  = 2;
    private static final byte RANKINE_ID   = 3;
    private static final byte DELISLE_ID   = 4;
    private static final byte NEWTON_ID    = 5;
    private static final byte RÉAUMUR_ID   = 6;
    private static final byte RØMER_ID     = 7;

    public TemperatureConverter(@NonNull String name, @NonNull List<Unit> units,
                                @Nullable String errors, int lastUnit,
                                @Nullable String lastQuantity) {
        super(name, units, errors, lastUnit, lastQuantity);
    }

    public TemperatureConverter(@NonNull String name, @NonNull List<Unit> units) {
        super(name, units);
    }

    @Override
    protected String convert(double quantity, double from, double to) {
        if (from == CELSIUS_ID) return String.valueOf(convertFromCelsius(quantity, (byte) to));
        if (to == CELSIUS_ID) return String.valueOf(celsiusValueOf((byte) from, quantity));
        return String.valueOf(convertFromCelsius(celsiusValueOf((byte) from, quantity), (byte) to));
    }

    private double celsiusValueOf(byte nameId, double value) {
        switch (nameId) {
            case KELVIN_ID:
                return value - 273.15;

            case FARENEIT_ID:
                return 5/9d * (value - 32);

            case RANKINE_ID:
                return value/1.8 - 273.15;

            case DELISLE_ID:
                return 100 - value * 2/3d;

            case NEWTON_ID:
                return 100/33d * value;

            case RÉAUMUR_ID:
                return value * 1.25;

            case RØMER_ID:
                return 40/21d * (value - 7.5);
        }

        throw new RuntimeException("Unknown temperature unit name.");
    }

    private double convertFromCelsius(double value, byte toId) {
        switch (toId) {
            case KELVIN_ID:
                return value + 273.15;

            case FARENEIT_ID:
                return 9/5d * value + 32;

            case RANKINE_ID:
                return (273.15 + value) * 1.8;

            case DELISLE_ID:
                return (100 - value) * 3/2d;

            case NEWTON_ID:
                return 33/100d * value;

            case RÉAUMUR_ID:
                return 0.8 * value;

            case RØMER_ID:
                return 21/40d * value + 7.5;
        }

        throw new RuntimeException("Unknown temperature unit name.");
    }
}