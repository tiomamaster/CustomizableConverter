package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Concrete implementation of ConvertersServiceApi interface.
 */
public class ConvertersServiceApiImpl implements ConvertersServiceApi {

    /**
     * All requests to the database should be piped through this executor.
     */
    private static final ExecutorService sSingleExecutor = Executors.newSingleThreadExecutor();

    private final Handler mHandler;

    private ConvertersDbHelper mDbHelper;

    private CurrencyLoader mCurrencyLoader;

    private Context mContext;

    public ConvertersServiceApiImpl(Context c) {
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) {
            mDbHelper = new ConvertersDbHelper(c, "ru");
            mCurrencyLoader = new CurrencyLoader(c, "ru");
        } else {
            mDbHelper = new ConvertersDbHelper(c, "en");
            mCurrencyLoader = new CurrencyLoader(c, "en");
        }

        mContext = c;

        mHandler = new Handler(c.getApplicationContext().getMainLooper());
    }

    @Override
    public void getAllConvertersTypes(@NonNull final LoadConvertersCallback callback) {
        try {
            final Pair<String, List<Pair<String, Boolean>>> result =
                    sSingleExecutor.submit(new Callable<Pair<String, List<Pair<String, Boolean>>>>() {
                        @Override
                        public Pair<String, List<Pair<String, Boolean>>> call() throws Exception {
                            return mDbHelper.getAllConverters();
                        }
                    }).get();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onLoaded(result.second, result.first);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getConverter(@NonNull final String name, @NonNull final LoadConverterCallback callback) {
        try {
            final Converter result = sSingleExecutor.submit(new Callable<Converter>() {
                @Override
                public Converter call() throws Exception {
                    return mDbHelper.create(name);
                }
            }).get();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (result == null) {
                        callback.onError("There are no converters with given name - " + name);
                        return;
                    }

                    Repositories.getInMemoryRepoInstance(mContext).setOnSettingsChangeListener(
                            Converter.getOnSettingsChangeListener());

                    if (result.getUnits().isEmpty() && result instanceof CurrencyConverter) {
                        updateCourses(callback, result);
                    } else callback.onLoaded(result);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLastConverter(@NonNull final String name) {
        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveLastConverter(name);
            }
        });
    }

    @Override
    public void setLastUnit(@NonNull final String converterName, final int unitPos) {
        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveLastUnit(converterName, unitPos);
            }
        });
    }

    @Override
    public void setLastQuantity(@NonNull final String converterName, @NonNull final String quantity) {
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
        try {
            final boolean result = sSingleExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return mDbHelper.save(converter, oldName);
                }
            }).get();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onSaved(result);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeConvertersOrder(@NonNull final List<Pair<String, Boolean>> converters) {
        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveOrder(converters);
            }
        });
    }

    @Override
    public void writeConverterState(@NonNull final String name, final boolean state) {
        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.saveState(name, state);
            }
        });
    }

    @Override
    public void deleteConverter(@NonNull final String name) {
        sSingleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mDbHelper.delete(name);
            }
        });
    }

    @Override
    public void updateCourses(@NonNull final LoadConverterCallback callback,
                              @Nullable final Converter converter) {
        mCurrencyLoader.getFreshCourses(new Response.Listener<List<CurrencyConverter.CurrencyUnit>>() {
            @Override
            public void onResponse(final List<CurrencyConverter.CurrencyUnit> response) {
                sSingleExecutor.execute(new Runnable() {
                    @Override
                    public void run() {

                        // update or insert currency units values and get they from the database
                        final List<Converter.Unit> result = mDbHelper.updateOrInsertCourses(response);

                        final CurrencyConverter[] newConverter = new CurrencyConverter[1];

                        if (converter != null) {
                            ((CurrencyConverter) converter).setLastUpdateTime(System.currentTimeMillis());
                            // update converter units
                            converter.getUnits().clear();
                            converter.getUnits().addAll(result);
                        } else {
                            newConverter[0] = mDbHelper.create();
                        }

                        // report result
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (converter != null) callback.onLoaded(converter);
                                else callback.onLoaded(newConverter[0]);
                            }
                        });
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    @Override
    public void cancelUpdateRequest() {
        mCurrencyLoader.cancelAllRequests();
    }
}