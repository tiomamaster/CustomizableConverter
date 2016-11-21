package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

/**
 * Created by Artyom on 27.08.2016.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class ConvertersServiceApiImplTest {

    private Context mContext;

    private ConvertersServiceApiImpl mApi;

    private String mLanguage = "en";

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();

        Locale l = new Locale(mLanguage);
        Locale.setDefault(l);

        mApi = new ConvertersServiceApiImpl(mContext);
    }

    @Test
    public void getAllConvertersTypesTest() throws Exception {
//        String[] result = mApi.getEnabledConvertersTypes();
//        String[] expected;
//        if (TextUtils.equals(mLanguage, "ru"))
//            expected = mContext.getResources().getStringArray(R.array.translation_for_files_ru);
//        else expected = mContext.getAssets().list(mLanguage);
//        assertEquals(expected.length, result.length);
//        assertArrayEquals(expected, result);
    }

    @Test
    public void getConverter() throws Exception {
//        String[] expectedFilesNames;
//        if (TextUtils.equals(mLanguage, "ru"))
//            expectedFilesNames = mContext.getResources().getStringArray(R.array.translation_for_files_ru);
//        else expectedFilesNames = mContext.getAssets().list(mLanguage);
//        for (String name : expectedFilesNames) {
//            Converter result = mApi.getConverter(name);
//            assertNotNull(result);
//            assertEquals(name, result.getName());
//            assertNull(result.getErrors());
//        }
    }
}
