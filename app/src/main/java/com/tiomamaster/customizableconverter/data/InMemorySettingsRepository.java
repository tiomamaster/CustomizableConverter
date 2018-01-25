package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.tiomamaster.customizableconverter.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class InMemorySettingsRepository implements SettingsRepository, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_LANGUAGE = "PREF_LANGUAGE";
    private static final String PREF_GROUPING_SIZE = "PREF_GROUPING_SIZE";
    private static final String PREF_PRECISION = "PREF_PRECISION";
    private static final String PREF_STANDARD_FORM = "PREF_STANDARD_FORM";
    private static final String PREF_DEFAULT_FORM = "PREF_DEFAULT_FORM";

    @NonNull private Context mContext;

    private Map<String, OnSettingsChangeListener> mChangeListeners;

    private String mLanguage;
    private boolean isLangChanged;
    private int mGrSize;
    private int mPrecision;
    private boolean isStandardForm;
    private boolean isDefaultForm;

    InMemorySettingsRepository(@NonNull Context c) {
        mContext = checkNotNull(c, "need Context");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        String startLang;
        if (Locale.getDefault().getLanguage().equals(new Locale("ru").getLanguage())) startLang = "ru";
        else startLang = "en";
        mLanguage = prefs.getString(PREF_LANGUAGE, startLang);
        mGrSize = Integer.parseInt(prefs.getString(PREF_GROUPING_SIZE, "3"));
        mPrecision = Integer.parseInt(prefs.getString(PREF_PRECISION, "5"));
        isStandardForm = prefs.getBoolean(PREF_STANDARD_FORM, false);
        isDefaultForm = prefs.getBoolean(PREF_DEFAULT_FORM, false);

        prefs.registerOnSharedPreferenceChangeListener(this);

        mChangeListeners = new HashMap<>(2);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PREF_LANGUAGE:
                isLangChanged = true;
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
        mChangeListeners.put(listener.getClass().getCanonicalName(), listener);

        // to initialize settings properties in converters and summaries in fragment
        listener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm, false);
    }

    public Context updateLocale() {
        Locale locale = new Locale(mLanguage);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            mContext = mContext.createConfigurationContext(config);
        } else {
            config.locale = locale;
            mContext.getResources().updateConfiguration(config, mContext.getResources().getDisplayMetrics());
        }
        return mContext;
    }

    private void notifyListeners() {
        for (OnSettingsChangeListener listener : mChangeListeners.values()) {
            listener.onSettingsChange(mGrSize, mPrecision, isStandardForm, isDefaultForm, isLangChanged);
        }
    }
}