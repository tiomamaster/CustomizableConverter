package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Concrete implementation of ConvertersServiceApi interface.
 */
public class ConvertersServiceApiImpl implements ConvertersServiceApi {

    /**
     * All requests to the database should be piped through this executor.
     */
    private static final ExecutorService sSingleExecutor = Executors.newSingleThreadExecutor();

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
                List<Pair<String, Boolean>> result = null;
                try {
                    result = sSingleExecutor.submit(new Callable<List<Pair<String, Boolean>>>() {
                        @Override
                        public List<Pair<String, Boolean>> call() throws Exception {
                            return mDbHelper.getAllConverters();
                        }
                    }).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
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
                Converter result = null;
                try {
                    result = sSingleExecutor.submit(new Callable<Converter>() {
                        @Override
                        public Converter call() throws Exception {
                            return mDbHelper.createConverter(name);
                        }
                    }).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
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
                Converter result = null;
                try {
                    result = sSingleExecutor.submit(new Callable<Converter>() {
                        @Override
                        public Converter call() throws Exception {
                            return mDbHelper.createLastConverter();
                        }
                    }).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Repositories.getInMemoryRepoInstance(mContext).setOnSettingsChangeListener(
                        Converter.getOnSettingsChangeListener());
                return result;
            }

            @Override
            protected void onPostExecute(Converter converter) {
                callback.onLoaded(converter);
            }
        }.execute();
    }

    @Override
    public void setLastConverter(@NonNull final String name) {
        checkNotNull(name);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveLastConverter(name);
            }
        });
    }

    @Override
    public void setLastUnit(@NonNull final String converterName, final int unitPos) {
        checkNotNull(converterName);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveLastUnit(converterName, unitPos);
            }
        });
    }

    @Override
    public void setLastQuantity(@NonNull final String converterName, @NonNull final String quantity) {
        checkNotNull(converterName);
        checkNotNull(quantity);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveLastQuantity(converterName, quantity);
            }
        });
    }

    @Override
    public void saveConverter(@NonNull final SaveCallback callback, @NonNull final Converter converter,
                              @NonNull final String oldName) {
        checkNotNull(callback);
        checkNotNull(converter);
        checkNotNull(oldName);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean result = false;
                try {
                    result = sSingleExecutor.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return mDbHelper.save(converter, oldName);
                        }
                    }).get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(Boolean saved) {
                callback.onSaved(saved);
            }
        }.execute();
    }

    @Override
    public void writeConvertersOrder(@NonNull final List<Pair<String, Boolean>> converters) {
        checkNotNull(converters);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveOrder(converters);
            }
        });
    }

    @Override
    public void writeConverterState(@NonNull final String name, final boolean state) {
        checkNotNull(name);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveState(name, state);
            }
        });
    }

    @Override
    public void deleteConverter(@NonNull final String name) {
        checkNotNull(name);

        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.delete(name);
            }
        });
    }
}