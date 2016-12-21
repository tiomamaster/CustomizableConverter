package com.tiomamaster.customizableconverter.settings;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.Divider;
import com.tiomamaster.customizableconverter.data.Converter;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 07.12.2016.
 */
public class EditConverterFragment extends Fragment implements SettingsContract.EditConverterView {

    private SettingsContract.EditConverterUal mActionListener;

    private UnitsAdapterWithHeader mAdapter;

    private SettingsActivity mParentActivity;

    private ItemTouchHelper mItemTouchHelper;

    private View mRecyclerViewHeader;

    private EditText mEditName;

    private TextView mTextError;

    private InputMethodManager mImm;

    private TextView mTextLoading;

    public static EditConverterFragment newInstance() {
        return new EditConverterFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new UnitsAdapterWithHeader();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (SettingsActivity) getActivity();

        mImm = (InputMethodManager) mParentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_with_rw, container, false);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);

        // draw divider between items
        recyclerView.addItemDecoration(new Divider(getActivity(), 1));

        mRecyclerViewHeader = inflater.inflate(R.layout.rw_edit_converter_header, container, false);

        mEditName = (EditText) mRecyclerViewHeader.findViewById(R.id.edit_text_name);
        mEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mActionListener.setConverterName(s.toString());
            }
        });

        mTextError = (TextView) mRecyclerViewHeader.findViewById(R.id.text_msg_error);

        mTextLoading = (TextView) mRecyclerViewHeader.findViewById(R.id.text_loading);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mActionListener.loadUnits();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActionListener.handleHomePressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setPresenter(@NonNull SettingsContract.UserActionListener presenter) {
        mActionListener = (SettingsContract.EditConverterUal) presenter;
    }

    @Override
    public void showPreviousView() {
        hideSoftInput();
        ConvertersEditFragment view = ConvertersEditFragment.newInstance();
        SettingsContract.UserActionListener presenter = new ConvertersEditPresenter(
                Injection.provideConvertersRepository(getContext()), view);
        mParentActivity.showFragment(view);
        mParentActivity.setActionListener(presenter);
    }

    @Override
    public void showUnits(@NonNull List<Converter.Unit> units) {
        checkNotNull(units);
        mAdapter.setUnits(units);
    }

    @Override
    public void error(boolean visible) {
        if (visible) {
            mEditName.getBackground().mutate().setColorFilter(Color.parseColor("#d50000"),
                    PorterDuff.Mode.SRC_ATOP);
            mTextError.setVisibility(View.VISIBLE);
        } else {
            mEditName.getBackground().mutate().setColorFilter(Color.parseColor("#009688"),
                    PorterDuff.Mode.SRC_ATOP);
            mTextError.setVisibility(View.GONE);
        }
    }

    @Override
    public void setProgressIndicator(boolean active) {
        if (active) mTextLoading.setVisibility(View.VISIBLE);
        else mTextLoading.setVisibility(View.GONE);
    }

    private void hideSoftInput() {
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mParentActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mParentActivity);
        }
        mImm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private class UnitsAdapterWithHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_ITEM = 0;
        private final static int TYPE_HEADER = 1;

        private List<Converter.Unit> mUnits;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                return new VHItem(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.rw_edit_converter_item, parent, false));
            } else if (viewType == TYPE_HEADER) {
                return new VHHeader(mRecyclerViewHeader);
            }
            throw new RuntimeException("there is no type that matches the type "
                    + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof VHItem) {
                int pos = position - 1;
                ((VHItem) holder).mUnitName.setText(mUnits.get(pos).name);

                double value = mUnits.get(pos).value;
                if (value != 0d) {
                    ((VHItem) holder).mUnitValue.setText(String.valueOf(value));
                }

                ((VHItem) holder).mCheck.setChecked(mUnits.get(pos).isEnabled);
            } else if (holder instanceof VHHeader) {
                mEditName.setText(mActionListener.getConverterName());
            }
        }

        @Override
        public int getItemCount() {
            if (mUnits == null) return 1;
            return mUnits.size() + 1;
        }

        void setUnits(@NonNull List<Converter.Unit> newData) {
            checkNotNull(newData);
            if (mUnits == null) {
                mUnits = newData;
                notifyItemRangeInserted(1, mUnits.size());
            } else {
                mUnits = newData;
                // because notifyItemChanged() cause bug inside recycler view
                notifyDataSetChanged();
            }
        }

        private class VHItem extends RecyclerView.ViewHolder {

            private TextView mUnitName;

            private TextView mUnitValue;

            private CheckBox mCheck;

            private ImageView mHandleReorder;

            VHItem(View itemView) {
                super(itemView);

                mUnitName = (TextView) itemView.findViewById(R.id.text_unit_name);
                mUnitValue = (TextView) itemView.findViewById(R.id.text_unit_value);
                mCheck = (CheckBox) itemView.findViewById(R.id.check_box_enable);
                mHandleReorder = (ImageView) itemView.findViewById(R.id.image_view_handle);
            }
        }

        private class VHHeader extends RecyclerView.ViewHolder {

            VHHeader(View itemView) {
                super(itemView);
            }
        }
    }
}
