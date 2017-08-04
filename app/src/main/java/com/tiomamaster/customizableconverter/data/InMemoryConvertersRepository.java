package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation to load converters from the data source.
 */
class InMemoryConvertersRepository implements ConvertersRepository {

    private final ConvertersServiceApi mServiceApi;

    private int mLastPos;

    @VisibleForTesting String mLastConverterName;

    @VisibleForTesting List<Pair<String, Boolean>> mCachedConvertersTypes;

    @VisibleForTesting Map<String, Converter> mCachedConverters;

    InMemoryConvertersRepository(@NonNull ConvertersServiceApi serviceApi) {
        this.mServiceApi = checkNotNull(serviceApi);
    }

    @Override
    public void getEnabledConvertersTypes(@NonNull final LoadEnabledConvertersTypesCallback callback) {
        checkNotNull(callback);

        if (mCachedConvertersTypes != null) {
            callback.onConvertersTypesLoaded(getEnabledConverters(), mLastPos);
            return;
        }

        mServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.LoadConvertersCallback() {
                    @Override
                    public void onLoaded(@NonNull List<Pair<String, Boolean>> converters,
                                         @Nullable String lastSelConverter) {
                        mLastConverterName = lastSelConverter;
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

        // or load they from the API, cache and return using callback
        mServiceApi.getAllConvertersTypes(
                new ConvertersServiceApi.LoadConvertersCallback() {
                    @Override
                    public void onLoaded(@NonNull List<Pair<String, Boolean>> converters,
                                         @Nullable String lastSelConverter) {
                        mCachedConvertersTypes = converters;
                        callback.onConvertersTypesLoaded(converters);
                    }
                });
    }

    @Override
    public void getConverter(@NonNull final String name, final boolean clone,
                             @NonNull final GetConverterCallback callback) {
        checkNotNull(name);
        checkNotNull(callback);

        mServiceApi.cancelUpdateRequest();

        // save last selected converter
        if ((mLastConverterName != null) && !mLastConverterName.equals(name) && !clone) {
            mServiceApi.setLastConverter(name);
            mLastConverterName = name;
        }

        if (mCachedConverters != null && mCachedConverters.containsKey(name)) {
            if (clone) callback.onConverterLoaded((Converter) mCachedConverters.get(name).clone());
            else callback.onConverterLoaded(mCachedConverters.get(name));
            return;
        }

        mServiceApi.getConverter(name, new ConvertersServiceApi.LoadConverterCallback() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                cacheConverter(converter);

                if (clone) callback.onConverterLoaded((Converter) converter.clone());
                else callback.onConverterLoaded(converter);
            }

            @Override
            public void onError(@Nullable String message) {
                callback.reportError(message);
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
                    if (index == -1)
                        index = mCachedConvertersTypes.indexOf(new Pair<>(oldName, false));
                    mCachedConvertersTypes.add(index,
                            new Pair<>(converter.getName(), mCachedConvertersTypes.remove(index).second));
                }

                callback.onConverterSaved(saved);
            }
        }, converter, oldName);
    }

    @Override
    public void saveLastUnit() {
        mServiceApi.setLastUnit(mLastConverterName,
                mCachedConverters.get(mLastConverterName).getLastUnitPosition());
    }

    @Override
    public void saveLastQuantity() {
        mServiceApi.setLastQuantity(mLastConverterName,
                mCachedConverters.get(mLastConverterName).getLastQuantity());
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

    @Override
    public void updateCourses(@NonNull final GetConverterCallback callback) {
        Converter currencyConverter = null;
        if (mCachedConverters != null) {
            Converter lastConverter = mCachedConverters.get(mLastConverterName);
            if (lastConverter instanceof CurrencyConverter) currencyConverter = lastConverter;
        }

        mServiceApi.updateCourses(new ConvertersServiceApi.LoadConverterCallback() {
            @Override
            public void onLoaded(@NonNull Converter converter) {
                cacheConverter(converter);

                callback.onConverterLoaded(converter);
            }

            @Override
            public void onError(@Nullable String message) {
                callback.reportError(message);
            }
        }, currencyConverter);
    }

    private void cacheConverter(@NonNull Converter converter) {
        checkNotNull(converter);

        if (mCachedConverters == null) mCachedConverters = new HashMap<>();

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