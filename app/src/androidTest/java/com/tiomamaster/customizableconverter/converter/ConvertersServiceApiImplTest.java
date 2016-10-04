package com.tiomamaster.customizableconverter.converter;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Log;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersServiceApiImpl;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Locale;

/**
 * Created by Artyom on 27.08.2016.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ConvertersServiceApiImplTest {

    private static Context context;

    private static ConvertersServiceApiImpl api;

    private static String language = "en";

    @BeforeClass
    public static void setUp() {
        context = InstrumentationRegistry.getTargetContext();

        Locale l = new Locale(language);
        Locale.setDefault(l);

        api = new ConvertersServiceApiImpl(context);
    }

    @Test
    public void getAllConvertersTypesTest() throws Exception {
//        String[] result = api.getAllConvertersTypes();
//        String[] expected;
//        if (TextUtils.equals(language, "ru"))
//            expected = context.getResources().getStringArray(R.array.translation_for_files_ru);
//        else expected = context.getAssets().list(language);
//        assertEquals(expected.length, result.length);
//        assertArrayEquals(expected, result);
    }

    @Test
    public void getConverter() throws Exception {
//        String[] expectedFilesNames;
//        if (TextUtils.equals(language, "ru"))
//            expectedFilesNames = context.getResources().getStringArray(R.array.translation_for_files_ru);
//        else expectedFilesNames = context.getAssets().list(language);
//        for (String name : expectedFilesNames) {
//            Converter result = api.getConverter(name);
//            assertNotNull(result);
//            assertEquals(name, result.getName());
//            assertNull(result.getErrors());
//        }
    }
}
