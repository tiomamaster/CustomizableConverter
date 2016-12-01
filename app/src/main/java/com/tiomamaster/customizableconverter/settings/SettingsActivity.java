package com.tiomamaster.customizableconverter.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.Repositories;

/**
 * Created by Artyom on 11.10.2016.
 */

public class SettingsActivity extends AppCompatActivity {

    private static final String TITLE_KEY = "TitleKey";

    private static final String SETTINGS_FRAGMENT_TAG = "SETTINGS_FRAGMENT_TAG";
    private static final String EDIT_FRAGMENT_TAG = "EDIT_FRAGMENT_TAG";

    private SettingsContract.UserActionListener mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // find or create fragments and add they to the activity
        SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS_FRAGMENT_TAG);
        EditFragment editFragment = (EditFragment) getSupportFragmentManager().findFragmentByTag(EDIT_FRAGMENT_TAG);
        if (settingsFragment == null) {
            settingsFragment = SettingsFragment.newInstance();
            editFragment = EditFragment.newInstance();
            initFragments(settingsFragment, editFragment);
        }

        // Create the presenter
        mPresenter = (SettingsContract.UserActionListener) getLastCustomNonConfigurationInstance();
        if (mPresenter == null) {
            mPresenter = new SettingsPresenter(
                    Injection.provideConvertersRepository(getApplicationContext()),
                    Repositories.getInMemoryRepoInstance(getApplicationContext()),
                    settingsFragment, editFragment);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPresenter;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(TITLE_KEY, (String) getSupportActionBar().getTitle());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        getSupportActionBar().setTitle(savedInstanceState.getString(TITLE_KEY));
    }

    @Override
    public void onBackPressed() {
        mPresenter.handleHomePressed();
    }

    private void initFragments(Fragment fragment1, Fragment fragment2) {
        // Add the Fragments to the fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment1, SETTINGS_FRAGMENT_TAG);
        transaction.add(R.id.contentFrame, fragment2, EDIT_FRAGMENT_TAG);
        transaction.commit();
    }

    void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment instanceof SettingsFragment) {
            transaction.hide(fragmentManager.findFragmentByTag(EDIT_FRAGMENT_TAG));
            getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        }
        else {
            transaction.hide(fragmentManager.findFragmentByTag(SETTINGS_FRAGMENT_TAG));
            getSupportActionBar().setTitle(R.string.pref_title_customize);
        }
        transaction.show(fragment);
        transaction.commit();
    }
}