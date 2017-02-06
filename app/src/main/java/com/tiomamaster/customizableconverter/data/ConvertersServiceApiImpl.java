package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConvertersServiceApiImpl implements ConvertersServiceApi {

    private ConvertersDatabaseHelper mDbHelper;

    public ConvertersServiceApiImpl(Context c) {
        mDbHelper = new ConvertersDatabaseHelper(c);
    }

    @Override
    public void getAllConvertersTypes(@NonNull LoadCallback<List<Pair<String, Boolean>>> callback) {
        checkNotNull(callback);
    }

    @Override
    public void getConverter(@NonNull String name, @NonNull LoadCallback<Converter> callback) {
        checkNotNull(name);
        checkNotNull(callback);
    }

    @Override
    public void getLastConverter(@NonNull LoadCallback<Converter> callback) {
        checkNotNull(callback);
    }

    @Override
    public void saveConverter(@NonNull SaveCallback callback, @NonNull Converter converter,
                              @NonNull String oldName) {
        checkNotNull(callback);
        checkNotNull(converter);
        checkNotNull(oldName);
    }

    @Override
    public void writeConvertersOrder(@NonNull List<Pair<String, Boolean>> converters) {
        checkNotNull(converters);
    }

    @Override
    public void writeConverterState(@NonNull String name, boolean state) {
        checkNotNull(name);
    }

    @Override
    public void deleteConverter(@NonNull String name) {
        checkNotNull(name);
    }
}