package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load converters from the a data source.
 */
class InMemoryConvertersRepository implements ConvertersRepository {

    private final ConvertersServiceApi mConvertersServiceApi;

    private int mLastPos;

    private String mLastConverterName;

    @VisibleForTesting
    String[] mCachedConvertersTypes;

    @VisibleForTesting
    Map<String, Converter> mCachedConverters;

    private boolean mFirstCall = true;

    InMemoryConvertersRepository(@NonNull ConvertersServiceApi mConvertersServiceApi) {
        this.mConvertersServiceApi = checkNotNull(mConvertersServiceApi);
    }

    @Override
    public void getConvertersTypes(@NonNull final LoadConvertersTypesCallback callback) {
        if (mFirstCall) {
            mConvertersServiceApi.getLastConverter(new ConvertersServiceApi.ConverterServiceCallback<Converter>() {
                @Override
                public void onLoaded(@NonNull Converter converter) {
                    cacheConverter(converter);
                }
            });
            mFirstCall = false;
        }

        if (mCachedConvertersTypes != null) {
            callback.onConvertersTypesLoaded(mCachedConvertersTypes, mLastPos);
            return;
        }

        mConvertersServiceApi.getAllConvertersTypes(new ConvertersServiceApi.ConverterServiceCallback<String[]>() {
            @Override
            public void onLoaded(@NonNull String[] converters) {
                checkNotNull(converters);

                mCachedConvertersTypes = converters;

                callback.onConvertersTypesLoaded(converters, mLastPos);
            }
        });
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final GetConverterCallback callback) {
        checkNotNull(name);
        if (mCachedConverters != null && mCachedConverters.containsKey(name)) {
            callback.onConverterLoaded(mCachedConverters.get(name));
            return;
        }

        checkNotNull(callback);
        mConvertersServiceApi.getConverter(name, new ConvertersServiceApi.ConverterServiceCallback<Converter>() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                cacheConverter(converter);

                callback.onConverterLoaded(converter);
            }
        });
    }

    private void cacheConverter(@NonNull Converter converter) {
        checkNotNull(converter);

        mLastConverterName = converter.getName();

        mLastPos = converter.getOrderPosition();

        if (mCachedConverters == null) {
            mCachedConverters = new HashMap<>();
        }

        mCachedConverters.put(converter.getName(), converter);
    }
}