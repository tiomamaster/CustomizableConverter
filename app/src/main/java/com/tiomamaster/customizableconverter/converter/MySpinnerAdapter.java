package com.tiomamaster.customizableconverter.converter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckedTextView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tiomamaster.customizableconverter.R;

/**
 * Created by Artyom on 29.07.2016.
 */
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
        super(ctx, R.layout.item_spinner_conv_types);
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
            convertView = mInflater.inflate(R.layout.item_spinner_conv_types, null);
            ((AppCompatCheckedTextView)convertView).setTextColor(mColorPrimary);
            ((AppCompatCheckedTextView)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            if (mBackground != -1) {
                convertView.setBackgroundColor(mBackground);
            }
            ((AppCompatCheckedTextView)convertView).setHeight(mHeight);
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_spinner_conv_types, null);
            ((AppCompatCheckedTextView)convertView).setHeight(mHeight);
            ((AppCompatCheckedTextView)convertView).setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            int paddingLeftRight = mContext.getResources().
                    getDimensionPixelSize(R.dimen.spinner_item_left_right_padding);
            convertView.setPadding(paddingLeftRight, 0, paddingLeftRight, 0);
            if (mBackground != -1) {
                convertView.setBackgroundColor(mBackground);
            }
        }
        if (mSpinner.getSelectedItemPosition() == position)
            ((AppCompatCheckedTextView) convertView).setTextColor(mColorAccent);
        else
            ((AppCompatCheckedTextView) convertView).setTextColor(mColorPrimary);
        return super.getDropDownView(position, convertView, parent);
    }
}