package com.tiomamaster.customizableconverter.data;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.LinkedHashMap;
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

    private final static Handler H = new Handler();

    static {
        for (int j = 0; j < convertersCount; j ++) {
            LinkedHashMap<String, Double> units = new LinkedHashMap<>();
            for (int i = 0; i < unitsCount - j; i++) {
                units.put("Unit" + i + j, ((double) i + 1 + j*5));
            }
            Converter test = new Converter("Fake converter" + j, units);
            test.setLastUnitPosition(new Random().nextInt(test.getUnits().size()));
            CONVERTERS.put(test.getName(), test);
        }
    }

    @Override
    public void getAllConvertersTypes(@NonNull final ConverterServiceCallback<String[]> callback) {
        checkNotNull(callback);

        H.postDelayed(new Runnable() {
            @Override
            public void run() {callback.onLoaded(CONVERTERS.keySet().toArray(new String[0]));}
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