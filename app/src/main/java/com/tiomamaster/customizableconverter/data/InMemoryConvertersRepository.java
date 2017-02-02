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

    private final ConvertersServiceApi mServiceApi;

    private int mLastPos;

    @VisibleForTesting String mLastConverterName;

    @VisibleForTesting List<Pair<String,Boolean>> mCachedConvertersTypes;

    @VisibleForTesting Map<String, Converter> mCachedConverters;

    private boolean mFirstCall = true;

    InMemoryConvertersRepository(@NonNull ConvertersServiceApi serviceApi) {
        this.mServiceApi = checkNotNull(serviceApi);
    }

    @Override
    public void getEnabledConvertersTypes(@NonNull final LoadEnabledConvertersTypesCallback callback) {
        if (mFirstCall) {
            mServiceApi.getLastConverter(new ConvertersServiceApi.LoadCallback<Converter>() {
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

        mServiceApi.getAllConvertersTypes(
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
        mServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.LoadCallback<List<Pair<String, Boolean>>>() {
            @Override
            public void onLoaded(@NonNull List<Pair<String, Boolean>> converters) {
                mCachedConvertersTypes = converters;
                callback.onConvertersTypesLoaded(converters);
            }
        });
    }

    @Override
    public void getConverter(@NonNull final String name, final boolean clone,
                             @NonNull final GetConverterCallback callback) {
        checkNotNull(name);
        if (mCachedConverters != null && mCachedConverters.containsKey(name)) {
            if (clone) callback.onConverterLoaded((Converter) mCachedConverters.get(name).clone());
            else callback.onConverterLoaded(mCachedConverters.get(name));

            mLastConverterName = mCachedConverters.get(name).getName();
            return;
        }

        checkNotNull(callback);
        mServiceApi.getConverter(name, new ConvertersServiceApi.LoadCallback<Converter>() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                cacheConverter(converter);

                if (clone) callback.onConverterLoaded((Converter) mCachedConverters.get(name).clone());
                else callback.onConverterLoaded(mCachedConverters.get(name));
            }
        });
    }

    @Override
    public List<Pair<String, Boolean>> getCachedConvertersTypes() {
        return mCachedConvertersTypes;
    }

    @Override
    public void saveConverter(@NonNull final SaveConverterCallback callback,
                              @NonNull final Converter converter, @NonNull final String oldName) {
        checkNotNull(callback);
        checkNotNull(converter);
        checkNotNull(oldName);

        mServiceApi.saveConverter(new ConvertersServiceApi.SaveCallback() {
            @Override
            public void onSaved(boolean saved) {
                if (oldName.isEmpty()) {
                    // in this case create new converter
                    mCachedConvertersTypes.add(new Pair<>(converter.getName(), true));
                    mCachedConverters.put(converter.getName(), converter);
                } else {
                    // edit existing
                    mCachedConverters.put(converter.getName(), converter);

                    int index = mCachedConvertersTypes.indexOf(new Pair<>(oldName, true));
                    if (index == -1) index = mCachedConvertersTypes.indexOf(new Pair<>(oldName, false));
                    mCachedConvertersTypes.add(index,
                            new Pair<>(converter.getName(), mCachedConvertersTypes.remove(index).second));
                }

                callback.onConverterSaved(saved);
            }
        }, converter, oldName);
    }

    @Override
    public void saveConvertersOrder() {
        mServiceApi.writeConvertersOrder(mCachedConvertersTypes);
    }

    @Override
    public void saveConverterState(int position) {
        Pair<String, Boolean> pair = mCachedConvertersTypes.get(position);
        mServiceApi.writeConverterState(pair.first, pair.second);
    }

    @Override
    public void saveConverterDeletion(int position) {
        mServiceApi.deleteConverter(mCachedConvertersTypes.get(position).first);
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