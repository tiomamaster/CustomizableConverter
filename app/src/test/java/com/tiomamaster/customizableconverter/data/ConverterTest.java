package com.tiomamaster.customizableconverter.data;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Artyom on 26.07.2016.
 */
public class ConverterTest {

    private static double delta = 0.000001;

    private static String[] unitsName = {"Meter", "Centimeter", "Fathom", "League", "Mile"};

    private static double[] unitsValue = {1, 0.01, 1.8288, 4828.032, 1609.344};

    private static LinkedHashMap<String, Double> units;

    private static Converter converter;

    @BeforeClass
    public static void setUp() {
        units = new LinkedHashMap<>();
        for (int i = 0; i < unitsName.length; i++) {
            units.put(unitsName[i], unitsValue[i]);
        }
        converter = new Converter("Test", units);
    }

    @Test
    public void convertFromToTest() {
        assertEquals(200, converter.convertFromTo(2, unitsName[0], unitsName[1]), delta);
        assertEquals(0.01025, converter.convertFromTo(1.025, unitsName[1], unitsName[0]), delta);
    }

    @Test
    public void convertTest() {
        String from = unitsName[4];
        double quantity = 0.125;
        double[] expected = new double[unitsName.length];
        for (int i = 0; i < unitsName.length; i++) {
            expected[i] = converter.convertFromTo(quantity, from, unitsName[i]);
        }
        assertArrayEquals(expected, converter.convertAll(quantity, from), delta);
    }

    @Test
    public void convertAllExtTest() {
        String from = unitsName[1];
        double quantity = 5.2225;
        String expectedNames[] = converter.getAllUnitsName();
        double[] expectedResults = converter.convertAll(quantity, from);
        String[][] actual = converter.convertAllExt(quantity, from);
        for (int i = 0; i < actual.length; i++) {
            assertEquals(expectedNames[i], actual[i][0]);
            assertEquals(String.valueOf(expectedResults[i]), actual[i][1]);
        }
    }

    @Test
    public void convertAllExtFormattedTest() {
        String from = unitsName[1];
        double quantity = 5.2225;
        String expectedNames[] = converter.getAllUnitsName();
        double[] expectedResults = converter.convertAll(quantity, from);
        String[][] actual = converter.convertAllExtFormatted(quantity, from);

        // check result not contains from unit
        assertEquals(expectedResults.length - 1, actual.length);
        assertEquals(expectedNames.length - 1, actual.length);
        for (String[] anActual : actual) {
            assertNotEquals(from, anActual[0]);
        }
    }

    @Test
    public void getAllUnitsTest() {
        assertArrayEquals(unitsName, converter.getAllUnitsName());
    }
}
