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

public class SettingsFragment extends PreferenceFragmentCompat implements
        SettingsContract.SettingsView, Preference.OnPreferenceChangeListener {

    private SettingsContract.UserActionListener mPresenter;

    private SettingsActivity mParentActivity;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // set preference change listener for all preferences which started at position 1
        for (int i = 1; i < getPreferenceScreen().getPreferenceCount(); i++) {
            getPreferenceScreen().getPreference(i).setOnPreferenceChangeListener(this);
        }

        getPreferenceScreen().getPreference(0)
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        mPresenter.loadEditor();
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

        mPresenter.loadSettings();
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getOrder()) {
            // language changed
            case 1:
                mPresenter.handleLanguageChanged((String) newValue);

                preference.setSummary(R.string.language);
                break;

            // grouping size changed
            case 2:
                preference.setSummary(newValue + " " + getResources().
                        getString(R.string.pref_summary_grouping_size));

                mPresenter.handleGroupingSizeChanged((String) newValue);
                break;

            // precision changed
            case 3:
                preference.setSummary(newValue + " " + getResources().
                        getString(R.string.pref_summary_precision));

                mPresenter.handlePrecisionChanged((String) newValue);
                break;

            // standard form changed
            case 4:
                mPresenter.handleResultViewChanged((Boolean) newValue);
                break;
        }
        return true;
    }

        @Override
        public void closeSettings () {
            getActivity().finish();
        }

        @Override
        public void showSettings(String[] summaries) {
            PreferenceScreen screen = getPreferenceScreen();
            screen.getPreference(1).setSummary(summaries[0]);
            screen.getPreference(2).setSummary(summaries[1]);
            screen.getPreference(3).setSummary(summaries[2]);
            mParentActivity.showFragment(this);
        }

    @Override
    public void enableGrSizeOption(boolean enable) {
        getPreferenceScreen().getPreference(2).setEnabled(enable);
    }

    @Override
        public void setPresenter (@NonNull SettingsContract.UserActionListener presenter){
            mPresenter = checkNotNull(presenter);
        }
    }