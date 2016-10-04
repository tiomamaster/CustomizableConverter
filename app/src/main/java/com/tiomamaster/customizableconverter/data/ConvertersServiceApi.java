package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Defines an interface to the service API that is used by this application. All data request should
 * be piped through this interface.
 */
public interface ConvertersServiceApi {

    interface ConverterServiceCallback<T> {
        void onLoaded(@NonNull T converters);
    }

    void getAllConvertersTypes(@NonNull ConverterServiceCallback<String[]> callback);

    void getConverter(@NonNull String name, @NonNull ConverterServiceCallback<Converter> callback);

    void getLastConverter(@NonNull ConverterServiceCallback<Converter> callback);
}
