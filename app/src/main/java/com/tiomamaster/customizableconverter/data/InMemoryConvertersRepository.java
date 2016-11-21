package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load converters from the a data source.
 */
class InMemoryConvertersRepository implements ConvertersRepository {

    private final ConvertersServiceApi mConvertersServiceApi;

    @VisibleForTesting
    int mLastPos;

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
    public void getEnabledConvertersTypes(@NonNull final LoadEnabledConvertersTypesCallback callback) {
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

        mConvertersServiceApi.getEnabledConvertersTypes(new ConvertersServiceApi.ConverterServiceCallback<String[]>() {
            @Override
            public void onLoaded(@NonNull String[] converters) {
                checkNotNull(converters);

                mCachedConvertersTypes = converters;

                callback.onConvertersTypesLoaded(converters, mLastPos);
            }
        });
    }

    // TODO
    @Override
    public void getAllConverterTypes(@NonNull LoadAllConvertersTypesCallback callback) {

    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final GetConverterCallback callback) {
        checkNotNull(name);
        if (mCachedConverters != null && mCachedConverters.containsKey(name)) {
            callback.onConverterLoaded(mCachedConverters.get(name));
            mLastPos = mCachedConverters.get(name).getOrderPosition();
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