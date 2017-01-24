package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

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

    @VisibleForTesting int mLastPos;

    @VisibleForTesting String mLastConverterName;

    private List<Pair<String,Boolean>> mCachedConvertersTypes;

    private Map<String, Converter> mCachedConverters;

    private boolean mFirstCall = true;

    InMemoryConvertersRepository(@NonNull ConvertersServiceApi mConvertersServiceApi) {
        this.mConvertersServiceApi = checkNotNull(mConvertersServiceApi);
    }

    @Override
    public void getEnabledConvertersTypes(@NonNull final LoadEnabledConvertersTypesCallback callback) {
        if (mFirstCall) {
            mConvertersServiceApi.getLastConverter(new ConvertersServiceApi.LoadCallback<Converter>() {
                @Override
                public void onLoaded(@NonNull Converter converter) {
                    cacheConverter(converter);
                }
            });
            mFirstCall = false;
        }

        if (mCachedConvertersTypes != null) {
            callback.onConvertersTypesLoaded(getEnabledConverters(), mLastPos);
            return;
        }

        mConvertersServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.LoadCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                mCachedConvertersTypes = converters;
                callback.onConvertersTypesLoaded(getEnabledConverters(), mLastPos);
            }
        });
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
                new ConvertersServiceApi.LoadCallback<List<Pair<String, Boolean>>>() {
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
            callback.onConverterLoaded((Converter) mCachedConverters.get(name).clone());
            mLastConverterName = mCachedConverters.get(name).getName();
            return;
        }

        checkNotNull(callback);
        mConvertersServiceApi.getConverter(name, new ConvertersServiceApi.LoadCallback<Converter>() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                cacheConverter(converter);

                callback.onConverterLoaded((Converter) converter.clone());
            }
        });
    }

    @Override
    public List<Pair<String, Boolean>> getCachedConvertersTypes() {
        return mCachedConvertersTypes;
    }

    @Override
    public void saveConverter(@NonNull final SaveConverterCallback callback, @NonNull Converter converter) {
        checkNotNull(callback);
        checkNotNull(converter);

        if (!mCachedConvertersTypes.contains(new Pair<>(converter.getName(), true))
                && !mCachedConvertersTypes.contains(new Pair<>(converter.getName(), false))) {
            mCachedConvertersTypes.add(new Pair<>(converter.getName(), true));
        }

        mConvertersServiceApi.saveConverter(new ConvertersServiceApi.SaveCallback() {
            @Override
            public void onSaved(boolean saved) {
                callback.onConverterSaved(saved);
            }
        }, converter);
    }

    private void cacheConverter(@NonNull Converter converter) {
        checkNotNull(converter);

        mLastConverterName = converter.getName();

        if (mCachedConverters == null) {
            mCachedConverters = new HashMap<>();
        }

        mCachedConverters.put(converter.getName(), converter);
    }

    @NonNull
    private List<String> getEnabledConverters() {
        mLastPos = 0;
        List<String> converters = new ArrayList<>(mCachedConvertersTypes.size());
        for (int i = 0; i < mCachedConvertersTypes.size(); i++) {
            Pair<String, Boolean> pair = mCachedConvertersTypes.get(i);
            if (pair.second && !converters.contains(pair.first)) {
                converters.add(pair.first);
                if (pair.first.equals(mLastConverterName)) mLastPos = converters.size() - 1;
            }
        }
        return converters;
    }
}