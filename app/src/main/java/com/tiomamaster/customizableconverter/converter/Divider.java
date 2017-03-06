package com.tiomamaster.customizableconverter.converter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Divider for drawing between the recycler view items
 */

public class Divider extends RecyclerView.ItemDecoration {

    private final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDivider;

    @NonNull private Context mContext;

    private int mStartPos;

    public Divider(@NonNull Context context, int startPos) {
        mContext = checkNotNull(context, "context cannot be null");
        mStartPos = startPos;

        TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = mContext.getResources().getDimensionPixelSize(R.dimen.divider_padding);
        int right = parent.getWidth() - left;

        int childCount = parent.getChildCount();
        for (int i = mStartPos; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
