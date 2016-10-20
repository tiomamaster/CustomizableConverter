package com.tiomamaster.customizableconverter.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.ConverterFragment;

/**
 * Created by Artyom on 11.10.2016.
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SettingsFragment settingsFragment =
                (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (settingsFragment == null) {
            // Create the fragment
            settingsFragment = new SettingsFragment();
            initFragment(settingsFragment);
        }
//        EditFragment editFragment =
//                (EditFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
//        if (editFragment == null) {
//            // Create the fragment
//            editFragment = EditFragment.newInstance();
//            initFragment(editFragment);
//        }
    }

    private void initFragment(Fragment fragment) {
        // Add the ConverterFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
