package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Artyom on 14.07.2016.
 */
public interface ConvertersRepository {

    interface LoadEnabledConvertersTypesCallback {

        void onConvertersTypesLoaded(@NonNull List<String> convertersTypes, int position);
    }

    interface LoadAllConvertersTypesCallback {

        void onConvertersTypesLoaded(@NonNull List<Pair<String, Boolean>> convertersTypes);
    }

    interface GetConverterCallback {

        void onConverterLoaded(@NonNull Converter converter);
    }

    void getEnabledConvertersTypes(@NonNull LoadEnabledConvertersTypesCallback callback);

    void getAllConverterTypes(@NonNull LoadAllConvertersTypesCallback callback);

    void getConverter(@NonNull String name, @NonNull GetConverterCallback callback);
}