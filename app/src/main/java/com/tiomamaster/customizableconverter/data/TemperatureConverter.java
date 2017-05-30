package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public class TemperatureConverter extends Converter {

    private static final byte CELSIUS_ID   = 1;
    private static final byte KELVIN_ID    = 2;
    private static final byte FAHRENHEIT_ID = 3;
    private static final byte RANKINE_ID   = 4;
    private static final byte DELISLE_ID   = 5;
    private static final byte NEWTON_ID    = 6;
    private static final byte RÉAUMUR_MODERN_ID   = 7;
    private static final byte RÉAUMUR_ORIGINAL_ID = 8;
    private static final byte RØMER_ID            = 9;
    private static final byte LEIDEN_SCALE_ID     = 10;
    private static final byte PLANCK_TEMPERATURE_ID = 11;
    private static final byte HOOKE_ID              = 12;
    private static final byte DALTON_ID             = 13;

    TemperatureConverter(@NonNull String name, @NonNull List<Unit> units,
                         @Nullable String errors, int lastUnit,
                         @Nullable String lastQuantity) {
        super(name, units, errors, lastUnit, lastQuantity);
    }

    public TemperatureConverter(@NonNull String name, @NonNull List<Unit> units) {
        super(name, units);
    }

    @Override
    protected double convert(double quantity, double from, double to) {
        if (from == CELSIUS_ID) return convertFromCelsius(quantity, (byte) to);
        if (to == CELSIUS_ID) return celsiusValueOf((byte) from, quantity);
        return convertFromCelsius(celsiusValueOf((byte) from, quantity), (byte) to);
    }

    private double celsiusValueOf(byte nameId, double value) {
        switch (nameId) {
            case KELVIN_ID:
                return value - 273.15;

            case FAHRENHEIT_ID:
                return 5/9d * (value - 32);

            case RANKINE_ID:
                return value/1.8 - 273.15;

            case DELISLE_ID:
                return 100 - value * 2/3d;

            case NEWTON_ID:
                return 100/33d * value;

            case RÉAUMUR_MODERN_ID:
                return 1.25 * value;

            case RÉAUMUR_ORIGINAL_ID:
                return 0.925 * value;

            case RØMER_ID:
                return 40/21d * (value - 7.5);

            case LEIDEN_SCALE_ID:
                return value - 253;

            case PLANCK_TEMPERATURE_ID:
                return 1.416808 * Math.pow(10, 32) * value - 273.15;

            case HOOKE_ID:
                return 12/5d * value;

            case DALTON_ID:
                return 273.15 * (Math.pow(373.15 / 273.15, value / 100) - 1);
        }

        throw new RuntimeException("Unknown temperature unit name.");
    }

    private double convertFromCelsius(double value, byte toId) {
        switch (toId) {
            case KELVIN_ID:
                return value + 273.15;

            case FAHRENHEIT_ID:
                return 9/5d * value + 32;

            case RANKINE_ID:
                return (273.15 + value) * 1.8;

            case DELISLE_ID:
                return (100 - value) * 3/2d;

            case NEWTON_ID:
                return 33/100d * value;

            case RÉAUMUR_MODERN_ID:
                return 0.8 * value;

            case RÉAUMUR_ORIGINAL_ID:
                return value / 0.925;

            case RØMER_ID:
                return 21/40d * value + 7.5;

            case LEIDEN_SCALE_ID:
                return value + 253;

            case PLANCK_TEMPERATURE_ID:
                return (value + 273.15) / (1.416808 * Math.pow(10, 32));

            case HOOKE_ID:
                return 5/12d * value;

            case DALTON_ID:
                return Math.log(Math.pow(value/273.15 + 1, 100))/Math.log(373.15/273.15);
        }

        throw new RuntimeException("Unknown temperature unit name.");
    }
}