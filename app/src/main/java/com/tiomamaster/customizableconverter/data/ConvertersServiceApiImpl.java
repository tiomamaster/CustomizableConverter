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

    public ConvertersServiceApiImpl(Context c) {
        ConverterServiceApiEndpoint.initialize(c);
    }


    @Override
    public void getAllConvertersTypes(@NonNull LoadCallback<List<Pair<String, Boolean>>> callback) {

    }

    @Override
    public void getConverter(@NonNull String name, @NonNull LoadCallback<Converter> callback) {
        checkNotNull(name);
        checkNotNull(callback);

        callback.onLoaded(ConverterServiceApiEndpoint.createConverter(name));
    }

    @Override
    public void getLastConverter(@NonNull LoadCallback<Converter> callback) {

    }

    @Override
    public void saveConverter(@NonNull SaveCallback callback, @NonNull Converter converter) {

    }
}