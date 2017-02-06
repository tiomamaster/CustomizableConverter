package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tiomamaster.customizableconverter.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

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
    public void getAllConvertersTypes() throws Exception {
        final String[] expected;
        if (TextUtils.equals(mLanguage, "ru"))
            expected = mContext.getResources().getStringArray(R.array.translation_for_files_ru);
        else expected = mContext.getAssets().list(mLanguage);

        mApi.getAllConvertersTypes(new ConvertersServiceApi.LoadCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                assertEquals(expected.length, converters.size());
                for (int i = 0; i < expected.length; i++) {
                    assertEquals(expected[i], converters.get(i).first);
                }
            }
        });
    }

    @Test
    public void getConverter() throws IOException {
        String[] expectedFilesNames;
        if (TextUtils.equals(mLanguage, "ru"))
            expectedFilesNames = mContext.getResources().getStringArray(R.array.translation_for_files_ru);
        else expectedFilesNames = mContext.getAssets().list(mLanguage);
        for (final String name : expectedFilesNames) {
            mApi.getConverter(name, new ConvertersServiceApi.LoadCallback<Converter>() {
                @Override
                public void onLoaded(@NonNull Converter converter) {
                    assertNotNull(converter);
                    assertEquals(name, converter.getName());
                    assertTrue(converter.getUnits().size() > 2);
                    assertTrue(converter.getEnabledUnitsName().size() > 2);
                    assertNull(converter.getErrors());
                }
            });
        }
    }
}
