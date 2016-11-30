package com.tiomamaster.customizableconverter.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;

import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 11.10.2016.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SettingsContract.SettingsView {

    private SettingsContract.UserActionListener mPresenter;

    private SettingsActivity mParentActivity;

    private boolean isFirstCall = true;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // customize converters click listener
        getPreferenceScreen().getPreference(0).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mPresenter.loadEditor();
                return true;
            }
        });

        // standard form click listener
        getPreferenceScreen().getPreference(4).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mPresenter.standardOrDefaultClicked();
                return true;
            }
        });

        // default form click listener
        getPreferenceScreen().getPreference(5).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mPresenter.standardOrDefaultClicked();
                return true;
            }
        });

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (SettingsActivity) getActivity();

        if (isFirstCall) {
            isFirstCall = false;
            mPresenter.loadSettings();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mPresenter.handleHomePressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void closeSettings() {
        getActivity().finish();
    }

    @Override
    public void showSettings(String[] summaries) {
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < summaries.length; i++) {
            screen.getPreference(i + 1).setSummary(summaries[i]);
        }
        mParentActivity.showFragment(this);
    }

    @Override
    public void enableGrSizeOption(boolean enable) {
        getPreferenceScreen().getPreference(2).setEnabled(enable);
    }

    @Override
    public void enableFormattingOptions(boolean enable) {
        for (int i = 2; i < getPreferenceScreen().getPreferenceCount() - 1; i++) {
            getPreferenceScreen().getPreference(i).setEnabled(enable);
        }
    }

    @Override
    public void setPresenter(@NonNull SettingsContract.UserActionListener presenter) {
        mPresenter = checkNotNull(presenter);
    }
}