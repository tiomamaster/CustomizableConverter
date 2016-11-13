package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;

import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 05.11.2016.
 */

public class InMemorySettingsRepository implements SettingsRepository {

    private byte mSettingsItemCount = 3;

    @NonNull
    private Context mContext;

    private SharedPreferences mPrefs;

    @NonNull
    private OnSettingsChangeListener mChangeListener;

    private String mLanguage;

    private int mGrSize;

    private int mPrecision;

    private boolean isStandardForm;

    public InMemorySettingsRepository(@NonNull Context c) {
        mContext = checkNotNull(c, "need Context");
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);

        mLanguage = mPrefs.getString("pref_language", "");
        mGrSize = Integer.parseInt(mPrefs.getString("pref_grouping_size", ""));
        mPrecision = Integer.parseInt(mPrefs.getString("pref_precision", ""));
        isStandardForm = mPrefs.getBoolean("pref_standard_form", false);
    }

    @Override
    public String[] getSummaries() {
        String[] summaries = new String[mSettingsItemCount];

        // language summary
        summaries[0] = mContext.getString(R.string.language);

        // grouping size summary
        summaries[1] = mGrSize + " " +
                mContext.getString(R.string.pref_summary_grouping_size);

        // precision summary
        summaries[2] = mPrecision + " " +
                mContext.getString(R.string.pref_summary_precision);

        return summaries;
    }

    @Override
    public void setNewLanguage(String newVal) {
        mLanguage = newVal;
    }

    @Override
    public void setNewGroupingSize(int newVal) {
        mGrSize = newVal;
        mChangeListener.onSettingsChange(mGrSize, mPrecision, isStandardForm);
    }

    @Override
    public void setNewPrecision(int newVal) {
        mPrecision = newVal;
        mChangeListener.onSettingsChange(mGrSize, mPrecision, isStandardForm);
    }

    @Override
    public void setNewResultView(boolean newVal) {
        isStandardForm = newVal;
        mChangeListener.onSettingsChange(mGrSize, mPrecision, isStandardForm);
    }

    @Override
    public boolean getResultView() {
        return isStandardForm;
    }

    @Override
    public void setOnSettingsChangeListener(@NonNull OnSettingsChangeListener listener) {
        mChangeListener = checkNotNull(listener);

        // to initialize settings properties in converters
        listener.onSettingsChange(mGrSize, mPrecision, isStandardForm);
    }
}