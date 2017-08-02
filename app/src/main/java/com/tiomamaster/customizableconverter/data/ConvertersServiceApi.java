package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.util.List;

/**
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */
interface ConvertersServiceApi {

    interface LoadConvertersCallback {

        void onLoaded(@NonNull List<Pair<String, Boolean>> converters, @Nullable String lastSelConverter);
    }

    interface LoadConverterCallback {

        void onLoaded(@NonNull Converter converter);

        void onError(@Nullable String message);
    }

    interface SaveCallback {
        void onSaved(boolean saved);
    }

    void getAllConvertersTypes(@NonNull LoadConvertersCallback callback);

    void getConverter(@NonNull String name, @NonNull LoadConverterCallback callback);

    void setLastConverter(@NonNull String name);

    void setLastUnit(@NonNull String converterName, int unitPos);

    void setLastQuantity(@NonNull String converterName, @NonNull String quantity);

    void saveConverter(@NonNull SaveCallback callback, @NonNull Converter converter,
                       @NonNull String oldName);

    void writeConvertersOrder(@NonNull List<Pair<String, Boolean>> converters);

    void writeConverterState(@NonNull String name, boolean state);

    void deleteConverter(@NonNull String name);

    void updateCourses(@NonNull LoadConverterCallback callback, @Nullable Converter converter);
}