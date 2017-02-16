package com.tiomamaster.customizableconverter.data;

import android.support.v4.util.Pair;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.JUnit4;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicReference;

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
import static org.mockito.asm.tree.InsnList.check;

/**
 * Created by Artyom on 26.07.2016.
 */
public class ConverterTest {

    private static double sDelta = 0.000001;

    private static String[] sUnitsName = {"Meter", "Centimeter", "Fathom", "League", "Mile"};

    private static double[] sUnitsValue = {1, 0.01, 1.8288, 4828.032, 1609.344};

    private static boolean[] sIsEnabled = {true, true, true, false, true};

    private static List<Converter.Unit> sUnits;

    private static Converter sConverter;

    @BeforeClass
    public static void setUp() {
        sUnits = new ArrayList<>();
        for (int i = 0; i < sUnitsName.length; i++) {
            sUnits.add(new Converter.Unit(sUnitsName[i], sUnitsValue[i], sIsEnabled[i]));
        }
        sConverter = new Converter("Test", sUnits);
    }

    @Test
    public void convertAllCheckResult() {
        // set default form to unsure correct testing
        Converter.getOnSettingsChangeListener().onSettingsChange(0,0,false,true);

        String from = sUnitsName[1];
        double quantity = 5.2225;
        List<String> expectedNames = sConverter.getEnabledUnitsName();
        List<Pair<String, String>> actual = sConverter.convertAll(quantity, from);

        // check result not contains from unit
        assertEquals(expectedNames.size() - 1, actual.size());
        assertFalse(actual.contains(new Pair<>(sUnitsName[1], quantity + "")));

        // check conversion result
        assertEquals(sUnitsName[4], actual.get(2).first);
        assertEquals(quantity * sUnitsValue[1] / sUnitsValue[4] + "",
                actual.get(2).second);
    }

    @Test
    public void changeSettingsCheckConversionResult() {
        // try to get standard form
        Converter.getOnSettingsChangeListener().onSettingsChange(3, 5, true, false);
        List<Pair<String, String>> actual = sConverter.convertAll(5, sUnitsName[0]);

        // check conversion result contains ×10 in each string
        for (Pair<String, String> anActual : actual) {
            assertTrue(anActual.second.contains("×10"));
        }

        // try to get default view of result
        Converter.getOnSettingsChangeListener().onSettingsChange(3, 5, false, false);
        actual = sConverter.convertAll(5, sUnitsName[0]);

        // check conversion result not contains ×10
        for (Pair<String, String> anActual : actual) {
            assertFalse(anActual.second.contains("×10"));
        }
    }

    @Test
    public void getEnabledUnitsCheckResult() {
        assertFalse(sConverter.getEnabledUnitsName().contains(sUnitsName[3]));
        assertEquals(4, sConverter.getEnabledUnitsName().size());
    }

    @Test
    public void cloneConverter() {
        Converter clone = (Converter) sConverter.clone();

        clone.setName("Clone");
        clone.setLastUnitPosition(10000);
        clone.setLastQuantity("10000");
        clone.getUnits().get(0).name = "Clone";
        clone.getUnits().get(0).value = 10000d;
        clone.getUnits().get(0).isEnabled = false;

        // check that changing of the clone does not affect to the original
        assertNotEquals(sConverter.getName(), clone.getName());
        assertNotEquals(sConverter.getLastUnitPosition(), clone.getLastUnitPosition());
        assertNotEquals(sConverter.getLastQuantity(), clone.getLastQuantity());
        assertNotEquals(sConverter.getUnits().get(0), clone.getUnits().get(0));
        assertNotEquals(sConverter.getUnits().get(0).value, clone.getUnits().get(0).value);
        assertNotEquals(sConverter.getUnits().get(0).isEnabled, clone.getUnits().get(0).isEnabled);
    }

    @Test
    public void equalsUnit() {
        List<Converter.Unit> units = new ArrayList<>();
        Converter.Unit u = new Converter.Unit("One", 1d, true);

        units.add(u);

        // check that double and boolean field not affect for equals
        u = new Converter.Unit(u.name, 2d, false);
        assertTrue(units.contains(u));
    }

    @Test
    public void cloneUnit() {
        Converter.Unit original = new Converter.Unit("Original", 1d, true);
        Converter.Unit clone = (Converter.Unit) original.clone();

        clone.name = "Clone";
        clone.value = 2d;
        clone.isEnabled = false;

        // check that changing of the clone does not affect to the original
        assertFalse(original.equals(clone));
        assertFalse(original.value == clone.value);
        assertFalse(original.isEnabled == clone.isEnabled);
    }
}