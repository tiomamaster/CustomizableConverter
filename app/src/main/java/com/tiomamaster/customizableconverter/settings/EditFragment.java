package com.tiomamaster.customizableconverter.settings;


import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 20.10.2016.
 */

public class EditFragment extends Fragment implements SettingsContract.EditView{

    private SettingsContract.UserActionListener mPresenter;

    private SettingsActivity mParentActivity;

    public EditFragment() {}

    public static EditFragment newInstance() {
        return new EditFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_edit, container, false);

        RecyclerView rw = (RecyclerView) root.findViewById(R.id.recycler_view);
        rw.setLayoutManager(new LinearLayoutManager(getContext()));
        rw.setAdapter(new RecyclerViewAdapter(new String[]{"Fake1", "Fake2", "Fake3", "Fake4", "Fake5"}));

        // draw divider between items
        rw.addItemDecoration(new RecyclerView.ItemDecoration() {

            private final int[] ATTRS = new int[]{android.R.attr.listDivider};

            private Drawable mDivider;

            {
                TypedArray styledAttributes = getActivity().obtainStyledAttributes(ATTRS);
                mDivider = styledAttributes.getDrawable(0);
                styledAttributes.recycle();
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int left = getResources().getDimensionPixelSize(R.dimen.divider_padding);
                int right = parent.getWidth() - left;

                int childCount = parent.getChildCount();
                for (int i = 0; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        });

        setRetainInstance(true);
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mPresenter.handleHomePressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPresenter(@NonNull SettingsContract.UserActionListener presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (SettingsActivity) getActivity();
    }

    @Override
    public void showEditor() {
        mParentActivity.showFragment(this);
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private String[] mData;

        class ViewHolder extends RecyclerView.ViewHolder {

            private AppCompatTextView mText;
            private AppCompatCheckBox mCheck;

            ViewHolder(View itemView) {
                super(itemView);

                mText = (AppCompatTextView) itemView.findViewById(R.id.textView);
                mCheck = (AppCompatCheckBox) itemView.findViewById(R.id.checkBox);
            }
        }

        RecyclerViewAdapter(@NonNull String[] convertersNames) {
            mData = checkNotNull(convertersNames);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.settings_recycler_view_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mText.setText(mData[position]);
        }

        @Override
        public int getItemCount() {
            return mData.length;
        }
    }
}
