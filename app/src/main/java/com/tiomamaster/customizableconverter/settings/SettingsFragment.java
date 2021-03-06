package com.tiomamaster.customizableconverter.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.MenuItem;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

public class SettingsFragment extends PreferenceFragmentCompat implements SettingsContract.SettingsView {

    private SettingsContract.SettingsUal mActionListener;

    private SettingsActivity mParentActivity;

    public SettingsFragment() {
    }

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // customize converters click listener
        getPreferenceScreen().getPreference(0).setOnPreferenceClickListener(preference -> {
            ConvertersEditFragment view = ConvertersEditFragment.newInstance();
            SettingsContract.UserActionListener presenter = new ConvertersEditPresenter(
                    Injection.provideConvertersRepository(getContext()), view);
            mParentActivity.showFragment(view);
            mParentActivity.setActionListener(presenter);
            return true;
        });

        // standard form click listener
        getPreferenceScreen().getPreference(4).setOnPreferenceClickListener(preference -> {
            mActionListener.standardOrDefaultClicked();
            return true;
        });

        // default form click listener
        getPreferenceScreen().getPreference(5).setOnPreferenceClickListener(preference -> {
            mActionListener.standardOrDefaultClicked();
            return true;
        });

        // enable or disable options at startup according to the preferences
        mActionListener.standardOrDefaultClicked();

        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (SettingsActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        mActionListener.loadSummaries();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mActionListener.handleHomePressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSummaries(String[] summaries) {
        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 0; i < summaries.length; i++) {
            screen.getPreference(i + 1).setSummary(summaries[i]);
        }
    }

    @Override
    public void showDialog() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.msg_restart)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> System.exit(0))
                .setNegativeButton(R.string.later, (dialog, which) -> dialog.dismiss())
                .show();
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
        mActionListener = (SettingsContract.SettingsUal) checkNotNull(presenter);
    }

    @Override
    public void showPreviousView() {
        getActivity().finish();
    }
}