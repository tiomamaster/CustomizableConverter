package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        for (int j = 0; j < convertersCount; j++) {
            List<Converter.Unit> units = new ArrayList<>();
            for (int i = 0; i < unitsCount - j; i++) {
                units.add(new Converter.Unit("Unit" + i + j, (double) i + 1 + j * 5, true));
            }
            Converter test = new Converter("Fake converter" + j, units);
            test.setLastUnitPosition(new Random().nextInt(test.getUnits().size()));
            CONVERTERS.put(test.getName(), test);
        }
    }

    public FakeConvertersServiceApiImpl(Context context) {
        // set listener only for one converter instance
        Repositories.getInMemoryRepoInstance(context).
                setOnSettingsChangeListener(Converter.getOnSettingsChangeListener());

    }

    @Override
    public void getAllConvertersTypes(@NonNull final LoadCallback<List<Pair<String, Boolean>>> callback) {
        final List<Pair<String, Boolean>> converters = new ArrayList<>(CONVERTERS.size());
        for (Converter next : CONVERTERS.values()) {
            converters.add(new Pair<>(next.getName(), true));
        }
        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.onLoaded(converters);
            }
        }, 1000);
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final LoadCallback<Converter> callback) {
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
    public void getLastConverter(@NonNull LoadCallback<Converter> callback) {
        checkNotNull(callback);

        String[] names = CONVERTERS.keySet().toArray(new String[]{});

        callback.onLoaded(CONVERTERS.get(names[new Random().nextInt(convertersCount)]));
    }

    @Override
    public void saveConverter(@NonNull final SaveCallback callback,
                              @NonNull final Converter converter, @NonNull final String oldName) {
        checkNotNull(callback);
        checkNotNull(converter);

        H.postDelayed(new Runnable() {
            @Override
            public void run() {
                CONVERTERS.put(converter.getName(), converter);
                callback.onSaved(true);
            }
        }, 1000);
    }

    @Override
    public void setLastConverter(@NonNull String name) {

    }

    @Override
    public void setLastUnit(@NonNull String converterName, int unitPos) {

    }

    @Override
    public void setLastQuantity(@NonNull String converterName, @NonNull String quantity) {

    }

    @Override
    public void writeConvertersOrder(@NonNull List<Pair<String, Boolean>> converters) {

    }

    @Override
    public void writeConverterState(@NonNull String name, boolean state) {

    }

    @Override
    public void deleteConverter(@NonNull String name) {

    }
}