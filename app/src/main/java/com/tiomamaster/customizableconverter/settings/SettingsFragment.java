package com.tiomamaster.customizableconverter.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.tiomamaster.customizableconverter.R;

/**
 * Created by Artyom on 11.10.2016.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        getPreferenceScreen().getPreference(0).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(SettingsFragment.this.getContext(), "fire in the hole", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }
}