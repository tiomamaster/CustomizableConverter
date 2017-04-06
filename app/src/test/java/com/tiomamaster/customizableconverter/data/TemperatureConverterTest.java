package com.tiomamaster.customizableconverter.data;

import android.support.v4.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class TemperatureConverterTest {

    private static int sIndex;

    private static Converter.Unit[] sUnits = {new Converter.Unit("Celsius",    0, true),
                                              new Converter.Unit("Kelvin",     1, true),
                                              new Converter.Unit("Fahrenheit", 2, true),
                                              new Converter.Unit("Rankine",    3, true),
                                              new Converter.Unit("Delisle",    4, true),
                                              new Converter.Unit("Newton",     5, true),
                                              new Converter.Unit("Réaumur(modern scale)",   6, true),
                                              new Converter.Unit("Réaumur(original scale)", 7, true),
                                              new Converter.Unit("Rømer",                   8, true)};

    private static Converter sTempConverter = new TemperatureConverter("T", Arrays.asList(sUnits));

    private List<Double> mExpected;

    @Parameterized.Parameters
    public static Object[] data() {
        return new Object[] {
                // results for convert from Celsius
                Arrays.asList(283.15, 50.0, 509.67, 135.0, 3.3, 8.0, 10.81, 12.75),
                // results for convert from Kelvin
                Arrays.asList(-263.15, -441.67, 18.00, 544.73, -86.84, -210.52, -284.486, -130.65)};
    }

    public TemperatureConverterTest(List<Double> expected) {
        this.mExpected = expected;
    }

    @Test
    public void convertAll() {
        List<Pair<String, String>> result = sTempConverter.convertAll(10, sUnits[sIndex].name);
        for (int i = 0; i < result.size(); i++) {
            Pair<String, String> pair = result.get(i);
            assertEquals(mExpected.get(i), Double.valueOf(pair.second), 0.05);
        }
        sIndex++;
    }
}