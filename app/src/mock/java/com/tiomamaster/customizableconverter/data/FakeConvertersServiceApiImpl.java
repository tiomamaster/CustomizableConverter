package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 16.07.2016.
 */
public class FakeConvertersServiceApiImpl implements ConvertersServiceApi {

    private static final Map<String, Converter> CONVERTERS = new LinkedHashMap<>();
    private static int convertersCount = 6;
    private static int unitsCount = 17;

    private static final Handler H = new Handler();

    static {
        for (int j = 0; j < convertersCount; j ++) {
            LinkedHashMap<String, Double> units = new LinkedHashMap<>();
            for (int i = 0; i < unitsCount - j; i++) {
                units.put("Unit" + i + j, ((double) i + 1 + j*5));
            }
            Converter test = new Converter("Fake converter" + j, units);
            test.setLastUnitPosition(new Random().nextInt(test.getUnits().size()));
            test.setLastQuantity("123456789.0987654321");
            CONVERTERS.put(test.getName(), test);
        }
    }

    public FakeConvertersServiceApiImpl(Context context) {

        // set listener only for one converter instance
        Repositories.getInMemoryRepoInstance(context).
                setOnSettingsChangeListener(CONVERTERS.get("Fake converter0").
                        getOnSettingsChangeListener());
    }

    @Override
    public void getAllConvertersTypes(@NonNull final ConverterServiceCallback<List<Pair<String, Boolean>>> callback) {
        final List<Pair<String, Boolean>> converters = new ArrayList<>(CONVERTERS.size());
        for (Converter next : CONVERTERS.values()) {
            converters.add(new Pair<>(next.getName(), new Random().nextBoolean()));
        }
        H.postDelayed(new Runnable() {
            @Override
            public void run() {callback.onLoaded(converters);}
        }, 1000);
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final ConverterServiceCallback<Converter> callback) {
        checkNotNull(name);
        checkNotNull(callback);

        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onLoaded(CONVERTERS.get(name));
            }
        }, 1000);
    }

    @Override
    public void getLastConverter(@NonNull ConverterServiceCallback<Converter> callback) {
        checkNotNull(callback);

        String[] names = CONVERTERS.keySet().toArray(new String[]{});

        callback.onLoaded(CONVERTERS.get(names[new Random().nextInt(convertersCount)]));
    }
}