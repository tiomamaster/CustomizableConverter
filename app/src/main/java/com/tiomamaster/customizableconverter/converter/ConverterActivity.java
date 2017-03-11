package com.tiomamaster.customizableconverter.converter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.InMemorySettingsRepository;
import com.tiomamaster.customizableconverter.data.Repositories;

import java.util.List;

public class ConverterActivity extends AppCompatActivity {

    public static final String CONVERTER_FRAGMENT_TAG = "ConverterFragment";

    public final static int REQUEST_CODE_SETTINGS_ACTIVITY = 101;
    public final static int RESULT_CODE_RESTART_APP = 102;

    @VisibleForTesting Spinner mSpinConverterTypes;
    private ArrayAdapter<String> mConvertersAdapter;
    private String mSpinSelKey = "spin_sel";
    private int mSpinnerSelection;

    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();

        mSpinConverterTypes = (Spinner) findViewById(R.id.spinner_converter_types);
        mSpinConverterTypes.setVisibility(View.GONE);
        mConvertersAdapter = new MySpinnerAdapter(this, Color.WHITE, Color.parseColor("#009688"),
                Color.parseColor("#673AB7"),
                getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_item_height),
                mSpinConverterTypes);
        mSpinConverterTypes.setAdapter(mConvertersAdapter);

        ConverterFragment converterFragment =
                (ConverterFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (converterFragment == null) {
            // Create the fragment
            converterFragment = ConverterFragment.newInstance();
            initFragment(converterFragment);
        }

        // to prevent system call when starting or screen rotation
        mSpinConverterTypes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSpinConverterTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ((ConverterFragment) getSupportFragmentManager().
                                findFragmentByTag(CONVERTER_FRAGMENT_TAG)).hideSoftInput();

                        ((ConverterFragment) getSupportFragmentManager().
                                findFragmentByTag(CONVERTER_FRAGMENT_TAG)).
                                spinnerSelected(((TextView) view).getText().toString());

                        mSpinnerSelection = position;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
                return false;
            }
        });

        if (savedInstanceState != null) {
            mSpinnerSelection = savedInstanceState.getInt(mSpinSelKey);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSpinConverterTypes.setSelection(mSpinnerSelection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(mSpinSelKey, mSpinConverterTypes.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(((InMemorySettingsRepository)
                Repositories.getInMemoryRepoInstance(newBase)).updateLocale());
    }

    public void initSpinner(@NonNull List<String> convertersTypes, int selection) {
        showSpinner(true);

        // clear adapter and inflate it
        mConvertersAdapter.clear();
        for (String s : convertersTypes) {
            mConvertersAdapter.add(s);
        }

        mSpinConverterTypes.setSelection(selection);
    }

    void showSpinner(boolean visible) {
        if (visible) {
            actionBar.setTitle("");

            mSpinConverterTypes.setVisibility(View.VISIBLE);
        } else {
            actionBar.setTitle(getString(getApplicationInfo().labelRes));

            mSpinConverterTypes.setVisibility(View.GONE);
        }
    }

    private void initFragment(Fragment fragment) {
        // Add the ConverterFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, fragment, CONVERTER_FRAGMENT_TAG);
        transaction.commit();
    }
}