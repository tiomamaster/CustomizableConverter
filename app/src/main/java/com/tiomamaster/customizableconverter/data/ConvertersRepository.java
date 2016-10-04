package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;

import static android.R.attr.name;

/**
 * Created by Artyom on 14.07.2016.
 */
public interface ConvertersRepository {

    interface LoadConvertersTypesCallback {

        void onConvertersTypesLoaded(@NonNull String[] convertersTypes, int position);
    }

    interface GetConverterCallback {

        void onConverterLoaded(@NonNull Converter converter);
    }

    void getConvertersTypes(@NonNull LoadConvertersTypesCallback callback);

    void getConverter(@NonNull String name, @NonNull GetConverterCallback callback);
}
