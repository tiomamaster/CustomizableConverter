package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

/**
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */
public interface ConvertersServiceApi {

    interface LoadCallback<T> {
        void onLoaded(@NonNull T converters);
    }

    interface SaveCallback {
        void onSaved(boolean saved);
    }

    void getAllConvertersTypes(@NonNull LoadCallback<List<Pair<String, Boolean>>> callback);

    void getConverter(@NonNull String name, @NonNull LoadCallback<Converter> callback);

    void getLastConverter(@NonNull LoadCallback<Converter> callback);

    void saveConverter(@NonNull SaveCallback callback, @NonNull Converter converter,
                       @NonNull String oldName);
}