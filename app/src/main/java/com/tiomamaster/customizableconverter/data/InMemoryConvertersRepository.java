package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private List<Pair<String,Boolean>> mCachedConvertersTypes;

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

        mLastPos = 0;
        if (mCachedConvertersTypes != null) {
            callback.onConvertersTypesLoaded(getEnabledConverters(), mLastPos);
            return;
        }

        mConvertersServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.ConverterServiceCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                mCachedConvertersTypes = converters;
                callback.onConvertersTypesLoaded(getEnabledConverters(), mLastPos);
            }
        });
    }

    @NonNull
    private List<String> getEnabledConverters() {
        List<String> converters = new ArrayList<>(mCachedConvertersTypes.size());
        for (int i = 0; i < mCachedConvertersTypes.size(); i++) {
            Pair<String, Boolean> pair = mCachedConvertersTypes.get(i);
            if (pair.second) {
                converters.add(pair.first);
                if (TextUtils.equals(pair.first, mLastConverterName)) mLastPos = i;
            }
        }
        return converters;
    }

    @Override
    public void getAllConverterTypes(@NonNull final LoadAllConvertersTypesCallback callback) {
        checkNotNull(callback);

        // simply return cached converters types if they exist
        if (mCachedConvertersTypes != null) {
            callback.onConvertersTypesLoaded(mCachedConvertersTypes);
            return;
        }

        // or load they from the API, cache and return through using callback
        mConvertersServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.ConverterServiceCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                mCachedConvertersTypes = converters;
                callback.onConvertersTypesLoaded(converters);
            }
        });
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final GetConverterCallback callback) {
        checkNotNull(name);
        if (mCachedConverters != null && mCachedConverters.containsKey(name)) {
            callback.onConverterLoaded(mCachedConverters.get(name));
            mLastConverterName = mCachedConverters.get(name).getName();
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

        if (mCachedConverters == null) {
            mCachedConverters = new HashMap<>();
        }

        mCachedConverters.put(converter.getName(), converter);
    }
}