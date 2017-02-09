package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.filters.MediumTest;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tiomamaster.customizableconverter.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Artyom on 27.08.2016.
 */
@RunWith(Parameterized.class)
@LargeTest
public class ConvertersServiceApiImplTest {

    private Context mContext;

    private ConvertersServiceApiImpl mApi;

    @Parameterized.Parameter public String language;

    @Parameterized.Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList("ru", "en");
    }

    @Before
    public void setUp() {
        mContext = InstrumentationRegistry.getTargetContext();

        Locale l = new Locale(language);
        Locale.setDefault(l);

        mApi = new ConvertersServiceApiImpl(mContext);
    }

    @Test
    public void getAllConvertersTypes() throws Exception {
        final String[] expected = getConvertersNames(language);

        mApi.getAllConvertersTypes(new ConvertersServiceApi.LoadCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                assertEquals(expected.length, converters.size());
                for (int i = 0; i < expected.length; i++) {
                    assertEquals(expected[i], converters.get(i).first);
                }
            }
        });

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void getConverter() throws Exception {
        String[] expected = getConvertersNames(language);

        for (final String name : expected) {
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

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    public void setGetLastConverter() throws Exception {
        final String[] expected = getConvertersNames(language);

        mApi.setLastConverter(expected[expected.length - 1]);

        mApi.getLastConverter(new ConvertersServiceApi.LoadCallback<Converter>() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                assertEquals(expected[expected.length - 1], converter.getName());
            }
        });

        TimeUnit.SECONDS.sleep(5);
    }

    private String[] getConvertersNames(String language) throws IOException {
        if (TextUtils.equals(language, "ru")) {
            return mContext.getResources().getStringArray(R.array.translation_for_files_ru);
        }
        else return mContext.getAssets().list(language);
    }
}
