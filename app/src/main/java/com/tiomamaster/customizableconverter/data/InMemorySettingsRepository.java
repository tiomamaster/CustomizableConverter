package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.tiomamaster.customizableconverter.R;

import java.util.Locale;

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

    private OnSettingsChangeListener mChangeListener;

    private String mLanguage;
    private int mGrSize = 3;
    private int mPrecision = 5;
    private boolean isStandardForm = false;
    private boolean isDefaultForm = false;

    InMemorySettingsRepository(@NonNull Context c) {
        mContext = checkNotNull(c, "need Context");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);

        if (Locale.getDefault().equals(new Locale("ru"))) mLanguage = "ru";
        else mLanguage = "en";

        mPrefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PREF_LANGUAGE)) {
            // TODO: handle change language here
            mLanguage = sharedPreferences.getString(key, mLanguage);
        } else if (key.equals(PREF_GROUPING_SIZE)) {
            mGrSize = Integer.parseInt(sharedPreferences.getString(key, "3"));
        } else if (key.equals(PREF_PRECISION)) {
            mPrecision = Integer.parseInt(sharedPreferences.getString(key, "5"));
        } else if (key.equals(PREF_STANDARD_FORM)) {
            isStandardForm = sharedPreferences.getBoolean(key, false);
        } else if (key.equals(PREF_DEFAULT_FORM)) {
            isDefaultForm = sharedPreferences.getBoolean(key, false);
        }

        mChangeListener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm);
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
        mChangeListener = checkNotNull(listener);

        // to initialize settings properties in converters
        listener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm);
    }
}