package com.tiomamaster.customizableconverter.converter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tiomamaster.customizableconverter.R;

class MySpinnerAdapter extends ArrayAdapter<String> {

    private LayoutInflater mInflater;

    private int mColorPrimary;
    private int mColorAccent;
    private int mBackground;

    private int mHeight;

    private Spinner mSpinner;

    private Context mContext;

    MySpinnerAdapter(Context ctx, int colorPrimary, int colorAccent, int background,
                            int height, Spinner spinner) {
        super(ctx, R.layout.item_spin_converter_types);
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mColorPrimary = colorPrimary;
        mColorAccent =colorAccent;
        mBackground = background;
        mHeight = height;
        mSpinner = spinner;
        mContext = ctx;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_spin_converter_types, null);
            ((AppCompatTextView)convertView).setTextColor(mColorPrimary);
            ((AppCompatTextView)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            if (mBackground != -1) {
                convertView.setBackgroundColor(mBackground);
            }
            ((AppCompatTextView)convertView).setHeight(mHeight);
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_spin_converter_types, null);
            ((AppCompatTextView)convertView).setHeight(mHeight);
            ((AppCompatTextView)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            int paddingLeftRight = mContext.getResources().
                    getDimensionPixelSize(R.dimen.spinner_item_left_right_padding);
            convertView.setPadding(paddingLeftRight, 0, paddingLeftRight, 0);
            if (mBackground != -1) {
                convertView.setBackgroundColor(mBackground);
            }
        }
        if (mSpinner.getSelectedItemPosition() == position)
            ((AppCompatTextView) convertView).setTextColor(mColorAccent);
        else
            ((AppCompatTextView) convertView).setTextColor(mColorPrimary);
        return super.getDropDownView(position, convertView, parent);
    }
}