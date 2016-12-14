package com.tiomamaster.customizableconverter.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.Divider;
import com.tiomamaster.customizableconverter.data.Converter;

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
        ConvertersEditFragment view = ConvertersEditFragment.newInstance();
        SettingsContract.UserActionListener presenter = new ConvertersEditPresenter(
                Injection.provideConvertersRepository(getContext()), view);
        mParentActivity.showFragment(view);
        mParentActivity.setUserActionListener(presenter);
    }

    @Override
    public void showUnits(@Nullable List<Converter.Unit> units) {
        mAdapter.setUnits(units);
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
                return new VHHeader(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.rw_edit_converter_header, parent, false));
            }
            throw new RuntimeException("there is no type that matches the type "
                    + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof VHItem) {
                int pos = position - 1;
                ((VHItem) holder).mUnitName.setText(mUnits.get(pos).name);
                ((VHItem) holder).mUnitValue.setText(String.valueOf(mUnits.get(pos).value));
                ((VHItem) holder).mCheck.setChecked(mUnits.get(pos).isEnabled);
            } else if (holder instanceof VHHeader) {
                ((VHHeader) holder).mEditName.setText(mActionListener.getConverterName());
            }
        }

        @Override
        public int getItemCount() {
            if (mUnits == null) return 1;
            return mUnits.size() + 1;
        }

        void setUnits(@Nullable List<Converter.Unit> newData) {
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

        void clearDataSet() {
            if (mUnits != null) {
                notifyItemRangeRemoved(1, mUnits.size());
                mUnits = null;
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

            private EditText mEditName;

            VHHeader(View itemView) {
                super(itemView);

                mEditName = (EditText) itemView.findViewById(R.id.edit_text_name);
            }
        }
    }
}
