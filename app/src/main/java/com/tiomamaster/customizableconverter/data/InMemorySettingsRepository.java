package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.tiomamaster.customizableconverter.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 05.11.2016.
 */

class InMemorySettingsRepository implements SettingsRepository, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_LANGUAGE = "PREF_LANGUAGE";
    private static final String PREF_GROUPING_SIZE = "PREF_GROUPING_SIZE";
    private static final String PREF_PRECISION = "PREF_PRECISION";
    private static final String PREF_STANDARD_FORM = "PREF_STANDARD_FORM";
    private static final String PREF_DEFAULT_FORM = "PREF_DEFAULT_FORM";

    @NonNull private Context mContext;

    private SharedPreferences mPrefs;

    private Map<String, OnSettingsChangeListener> mChangeListeners;

    private String mLanguage;
    private int mGrSize;
    private int mPrecision;
    private boolean isStandardForm;
    private boolean isDefaultForm;

    InMemorySettingsRepository(@NonNull Context c) {
        mContext = checkNotNull(c, "need Context");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);

        if (Locale.getDefault().equals(new Locale("ru"))) mLanguage = "ru";
        else mLanguage = "en";
        mGrSize = Integer.parseInt(mPrefs.getString(PREF_GROUPING_SIZE, "3"));
        mPrecision = Integer.parseInt(mPrefs.getString(PREF_PRECISION, "5"));
        isStandardForm = mPrefs.getBoolean(PREF_STANDARD_FORM, false);
        isDefaultForm = mPrefs.getBoolean(PREF_DEFAULT_FORM, false);

        mPrefs.registerOnSharedPreferenceChangeListener(this);

        mChangeListeners = new HashMap<>(2);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_LANGUAGE:
                // TODO: handle change language
                mLanguage = sharedPreferences.getString(key, mLanguage);
                break;
            case PREF_GROUPING_SIZE:
                mGrSize = Integer.parseInt(sharedPreferences.getString(key, "3"));
                break;
            case PREF_PRECISION:
                mPrecision = Integer.parseInt(sharedPreferences.getString(key, "5"));
                break;
            case PREF_STANDARD_FORM:
                isStandardForm = sharedPreferences.getBoolean(key, false);
                break;
            case PREF_DEFAULT_FORM:
                isDefaultForm = sharedPreferences.getBoolean(key, false);
                break;
        }
        notifyListeners();
    }

    @Override
    public String[] getSummaries() {
        String[] summaries = new String[5];

        // language summary
        summaries[0] = mContext.getString(R.string.app_language);

        // grouping size summary
        summaries[1] = mGrSize + " " + mContext.getString(R.string.pref_summary_grouping_size);

        // precision summary
        summaries[2] = mPrecision + " " + mContext.getString(R.string.pref_summary_precision);

        // standard form summary
        summaries[3] = mContext.getString(R.string.pref_summary_standard_form);

        // default form summary
        summaries[4] = mContext.getString(R.string.pref_summary_default_form);

        return summaries;
    }

    @Override
    public boolean getStandardForm() {
        return isStandardForm;
    }

    @Override
    public boolean getDefaultForm() {
        return isDefaultForm;
    }


    @Override
    public void setOnSettingsChangeListener(@NonNull OnSettingsChangeListener listener) {
        checkNotNull(listener);
        mChangeListeners.put(listener.getClass().getCanonicalName(), listener);

        // to initialize settings properties in converters and summaries in fragment
        listener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm);
    }

    private void notifyListeners() {
        for (OnSettingsChangeListener listener : mChangeListeners.values()) {
            listener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm);
        }
    }
}