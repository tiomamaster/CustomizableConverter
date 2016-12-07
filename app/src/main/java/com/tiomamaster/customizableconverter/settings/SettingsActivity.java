package com.tiomamaster.customizableconverter.settings;

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
import com.tiomamaster.customizableconverter.data.Repositories;

/**
 * Created by Artyom on 11.10.2016.
 */

public class SettingsActivity extends AppCompatActivity {

    private static final String TITLE_KEY = "TITLE_KEY";

    private SettingsContract.UserActionListener mPresenter;

    private FloatingActionButton mFab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFab = (FloatingActionButton) findViewById(R.id.fab_add);
        mFab.setVisibility(View.GONE);

        mPresenter = (SettingsContract.UserActionListener) getLastCustomNonConfigurationInstance();

        // find fragment in the layout or create new settings fragment with it presenter
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            fragment = SettingsFragment.newInstance();
            initFragment(fragment);

            mPresenter = new SettingsPresenter(
                    Repositories.getInMemoryRepoInstance(getApplicationContext()),
                    ((SettingsContract.SettingsView) fragment));
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

    private void initFragment(Fragment fragment) {
        // add the fragment to the fragment manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment);
        transaction.commit();
    }

    void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.contentFrame, fragment);
        transaction.commit();
    }

    void setUserActionListener(SettingsContract.UserActionListener userActionListener) {
        mPresenter = userActionListener;
    }

    void setFabVisibility(boolean visible) {
        if (visible) mFab.setVisibility(View.VISIBLE);
        else mFab.setVisibility(View.GONE);
    }
}