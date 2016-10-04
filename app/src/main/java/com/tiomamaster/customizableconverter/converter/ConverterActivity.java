package com.tiomamaster.customizableconverter.converter;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.support.v7.widget.Toolbar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.annotations.VisibleForTesting;
import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 14.07.2016.
 */
public class ConverterActivity extends AppCompatActivity {

    @VisibleForTesting
    String mConverterFragmentTag = "ConverterFragment";

    @VisibleForTesting
    Spinner mSpinConvTypes;
    private ArrayAdapter<String> mConvertersAdapter;
    private String mSpinSelKey = "spin_sel";
    private int mSpinnerSelection;

    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinConvTypes = (Spinner) findViewById(R.id.spinner_conv_types);
        mSpinConvTypes.setVisibility(View.GONE);
        mConvertersAdapter = new MySpinnerAdapter(this, Color.WHITE, Color.parseColor("#009688"),
                Color.parseColor("#673AB7"),
                getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_item_height),
                mSpinConvTypes);
        mSpinConvTypes.setAdapter(mConvertersAdapter);

        ConverterFragment converterFragment =
                (ConverterFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (converterFragment == null) {
            // Create the fragment
            converterFragment = ConverterFragment.newInstance();
            initFragment(converterFragment);
        }

        // to prevent system call when starting or screen rotation
        mSpinConvTypes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mSpinConvTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ((ConverterFragment) getSupportFragmentManager().
                                findFragmentByTag(mConverterFragmentTag)).
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
        mSpinConvTypes.setSelection(mSpinnerSelection);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(mSpinSelKey, mSpinConvTypes.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    private void initFragment(Fragment converterFragment) {
        // Add the ConverterFragment to the layout
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contentFrame, converterFragment, mConverterFragmentTag);
        transaction.commit();
    }

    public void initSpinner(@NonNull String[] convertersTypes) {
        checkNotNull(convertersTypes);

        toolbar.setTitle("");

        mSpinConvTypes.setVisibility(View.VISIBLE);

        for (String s : convertersTypes) {
            mConvertersAdapter.add(s);
        }
    }

    public void initSpinner(@NonNull String[] convertersTypes, int selection) {
        initSpinner(convertersTypes);

        mSpinConvTypes.setSelection(selection);
    }
}