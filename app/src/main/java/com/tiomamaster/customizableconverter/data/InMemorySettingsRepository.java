package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.tiomamaster.customizableconverter.R;

/**
 * Created by Artyom on 05.11.2016.
 */

public class InMemorySettingsRepository implements SettingsRepository {

    private Context mContext;

    private SharedPreferences mPrefs;

    public InMemorySettingsRepository(Context c) {
        mContext = c;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(c);
    }

    @Override
    public String[] getSummaries() {
        String[] summaries = new String[3];

        // language summary
        summaries[0] = mContext.getString(R.string.language);

        // grouping size summary
        summaries[1] = mPrefs.getString("pref_grouping_size", "") + " " +
                mContext.getString(R.string.pref_summary_grouping_size);

        // precision summary
        summaries[2] = mPrefs.getString("pref_precision", "") + " " +
                mContext.getString(R.string.pref_summary_precision);

        return summaries;
    }
}