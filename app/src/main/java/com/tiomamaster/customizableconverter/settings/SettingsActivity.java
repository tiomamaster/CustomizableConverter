package com.tiomamaster.customizableconverter.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.InMemorySettingsRepository;
import com.tiomamaster.customizableconverter.data.Repositories;

public class SettingsActivity extends AppCompatActivity
        implements EditUnitDialogFragment.EditUnitDialogListener {

    private static final String TITLE_KEY = "TITLE_KEY";

    private SettingsContract.UserActionListener mActionListener;

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.title_fragment_settings));

        mFab = (FloatingActionButton) findViewById(R.id.fab_add);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionListener.handleFabPressed();
            }
        });

        mActionListener = (SettingsContract.UserActionListener) getLastCustomNonConfigurationInstance();

        // find fragment in the layout or create new settings fragment with it presenter
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            fragment = SettingsFragment.newInstance();
            initFragment(fragment);

            mActionListener = new SettingsPresenter(
                    Repositories.getInMemoryRepoInstance(getApplicationContext()),
                    ((SettingsContract.SettingsView) fragment));
        }

        if (mActionListener instanceof SettingsContract.SettingsUal) {
            mFab.setVisibility(View.GONE);
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mActionListener;
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(((InMemorySettingsRepository)
                Repositories.getInMemoryRepoInstance(newBase)).updateLocale());
    }

    @Override
    public void onBackPressed() {
        mActionListener.handleHomePressed();
    }

    @Override
    public void onDialogPositiveClick() {
        ((SettingsContract.EditConverterUal) mActionListener).saveUnit();
    }

    @Override
    public void onDialogNegativeClick() {
        ((EditConverterFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame))
                .clearEditText();
    }

    @Override
    public void onUnitNameChanged(String newName) {
        ((SettingsContract.EditConverterUal) mActionListener).setUnitName(newName);
    }

    @Override
    public void onUnitValueChanged(String newValue) {
        ((SettingsContract.EditConverterUal) mActionListener).setUnitValue(newValue);
    }

    void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFrame, fragment);
        transaction.commit();

        // set appropriate toolbar title and handle fab
        if (fragment instanceof SettingsFragment) {
            getSupportActionBar().setTitle(getString(R.string.title_fragment_settings));
            mFab.hide();
        } else {
            getSupportActionBar().setTitle(getString(R.string.title_fragment_edit));
            // do not show fab instantly for EditConverterFragment
            if (fragment instanceof ConvertersEditFragment) mFab.show();
        }
    }

    void setActionListener(SettingsContract.UserActionListener mActionListener) {
        this.mActionListener = mActionListener;
    }

    void showFab(boolean visible) {
        if (visible) mFab.show();
        else if (mFab.isShown()) mFab.hide();
        else mFab.setVisibility(View.GONE);
    }

    private void initFragment(Fragment fragment) {
        // add the fragment to the fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment);
        transaction.commit();
    }
}