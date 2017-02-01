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

    interface SaveConverterCallback {
        void onConverterSaved(boolean saved);
    }

    void getEnabledConvertersTypes(@NonNull LoadEnabledConvertersTypesCallback callback);

    List<Pair<String, Boolean>> getCachedConvertersTypes();

    void getAllConverterTypes(@NonNull LoadAllConvertersTypesCallback callback);

    void getConverter(@NonNull String name, boolean clone, @NonNull GetConverterCallback callback);

    void saveConverter(@NonNull SaveConverterCallback callback, @NonNull Converter converter,
                       @NonNull String oldName);

    /**
     * Save order of converters to the persistent storage.
     */
    void saveConvertersOrder();

    /**
     * Save that converter is enable or disable to the persistent storage.
     * @param position The order position of converter.
     */
    void saveConverterState(int position);

    /**
     * Delete the converter from the persistent storage.
     * @param position The order position of converter.
     */
    void saveConverterDeletion(int position);
}