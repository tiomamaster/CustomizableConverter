package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConvertersServiceApiImpl implements ConvertersServiceApi {

    private static ExecutorService sSingleExecutor = Executors.newSingleThreadExecutor();

    private ConvertersDbHelper mDbHelper;

    private Context mContext;

    public ConvertersServiceApiImpl(Context c) {
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) {
            mDbHelper = new ConvertersDbHelper(c, "ru");
        } else {
            mDbHelper = new ConvertersDbHelper(c, "en");
        }

        mContext = c;
    }

    @Override
    public void getAllConvertersTypes(@NonNull final LoadCallback<List<Pair<String, Boolean>>> callback) {
        checkNotNull(callback);

        new AsyncTask<Void, Void, List<Pair<String, Boolean>>>() {
            @Override
            protected List<Pair<String, Boolean>> doInBackground(Void... params) {
                return mDbHelper.getAllConverters();
            }

            @Override
            protected void onPostExecute(List<Pair<String, Boolean>> converters) {
                callback.onLoaded(converters);
            }
        }.execute();
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final LoadCallback<Converter> callback) {
        checkNotNull(name);
        checkNotNull(callback);

        new AsyncTask<Void, Void, Converter>() {
            @Override
            protected Converter doInBackground(Void... params) {
                return mDbHelper.createConverter(name);
            }

            @Override
            protected void onPostExecute(Converter converter) {
                callback.onLoaded(converter);
            }
        }.execute();
    }

    @Override
    public void getLastConverter(@NonNull final LoadCallback<Converter> callback) {
        checkNotNull(callback);

        new AsyncTask<Void, Void, Converter>() {
            @Override
            protected Converter doInBackground(Void... params) {
                Converter converter = mDbHelper.createLastConverter();
                Repositories.getInMemoryRepoInstance(mContext).setOnSettingsChangeListener(
                        converter.getOnSettingsChangeListener());
                return converter;
            }

            @Override
            protected void onPostExecute(Converter converter) {
                callback.onLoaded(converter);
            }
        }.execute();
    }

    @Override
    public void setLastConverter(@NonNull String name) {
        checkNotNull(name);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void saveConverter(@NonNull SaveCallback callback, @NonNull Converter converter,
                              @NonNull String oldName) {
        checkNotNull(callback);
        checkNotNull(converter);
        checkNotNull(oldName);
    }

    @Override
    public void writeConvertersOrder(@NonNull List<Pair<String, Boolean>> converters) {
        checkNotNull(converters);
    }

    @Override
    public void writeConverterState(@NonNull String name, boolean state) {
        checkNotNull(name);
    }

    @Override
    public void deleteConverter(@NonNull String name) {
        checkNotNull(name);
    }
}