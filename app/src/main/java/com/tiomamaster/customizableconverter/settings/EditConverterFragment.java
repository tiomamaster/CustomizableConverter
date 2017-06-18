package com.tiomamaster.customizableconverter.settings;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.Divider;
import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperAdapter;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperCallback;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperViewHolder;

import java.util.List;

import static android.R.attr.name;
import static com.google.common.base.Preconditions.checkNotNull;

public class EditConverterFragment extends Fragment implements SettingsContract.EditConverterView {

    private SettingsContract.EditConverterUal mActionListener;

    private UnitsAdapterWithHeader mAdapter;

    private SettingsActivity mParentActivity;

    private ItemTouchHelper mItemTouchHelper;

    private View mRecyclerViewHeader;

    private EditText mEditName;

    private TextView mTextError;

    private InputMethodManager mImm;

    private TextView mTextHint;

    private ProgressBar mProgress;
    private TextView mTextLoading;

    private MenuItem mBtnSave;
    private boolean mItemSaveVisible;

    private AlertDialog mSavingDialog;

    private boolean isConverterExist;

    private Button mBtnUpdate;
    private TextView mTextLoadingError;

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

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                clearEditText();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        // draw divider between items
        recyclerView.addItemDecoration(new Divider(getActivity(), 1));

        mRecyclerViewHeader = inflater.inflate(R.layout.rw_edit_converter_header, container, false);

        mEditName = (EditText) mRecyclerViewHeader.findViewById(R.id.edit_text_name);

        // to prevent focused when fragment start and give watcher when user touch
        mEditName.setFocusableInTouchMode(false);
        mEditName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditName.setFocusableInTouchMode(true);
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
                return false;
            }
        });

        mTextError = (TextView) mRecyclerViewHeader.findViewById(R.id.text_msg_error);

        mTextHint = (TextView) mRecyclerViewHeader.findViewById(R.id.text_hint);

        mProgress = (ProgressBar) mRecyclerViewHeader.findViewById(R.id.progress);
        mTextLoading = (TextView) mRecyclerViewHeader.findViewById(R.id.text_loading);

        mBtnUpdate = (Button) mRecyclerViewHeader.findViewById(R.id.btn_update);
        mTextLoadingError = (TextView) mRecyclerViewHeader.findViewById(R.id.text_msg_loading_error);

        setRetainInstance(true);
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        mActionListener.loadUnits();

        if (isConverterExist) {
            showConverterExistError(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_edit_converter, menu);

        // we can save a new converter or changes in the existing
        // only if some conditions are done, so by default disable this menu item
        mBtnSave = menu.findItem(R.id.save).setVisible(mItemSaveVisible);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mActionListener.handleHomePressed();
                return true;
            case R.id.save:
                mActionListener.saveConverter(false);
                return true;
            default: return super.onOptionsItemSelected(item);
        }
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
    public void showConverterExistError(boolean visible) {
        isConverterExist = visible;
        if (visible) {
            mEditName.getBackground().mutate().setColorFilter(Color.parseColor("#d50000"),
                    PorterDuff.Mode.SRC_ATOP);
            mTextError.setVisibility(View.VISIBLE);
        } else {
            mEditName.getBackground().clearColorFilter();
            mTextError.setVisibility(View.GONE);
        }
    }

    @Override
    public void setUnitsLoadingIndicator(boolean active) {
        if (active) {
            mTextLoading.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.VISIBLE);
        }
        else {
            mTextLoading.setVisibility(View.GONE);
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void notifyUnitRemoved(int position) {
        mAdapter.notifyItemRemoved(position + 1);
    }

    @Override
    public void showWarning(int position) {
        new AlertDialog.Builder(mParentActivity).setMessage(
                getString(R.string.msg_delete_all_units))
                .setPositiveButton(R.string.ok, null)
                .setCancelable(false).show();

        mAdapter.notifyItemChanged(position + 1);
    }

    @Override
    public void showUnitEditor(@Nullable String name, @Nullable String value) {
        DialogFragment editDialog = EditUnitDialogFragment.newInstance(name, value);
        editDialog.show(mParentActivity.getSupportFragmentManager(),
                EditUnitDialogFragment.EDIT_UNIT_DIALOG_TAG);
    }

    @Override
    public void showUnitEditor(@NonNull String name) {
        DialogFragment editDialog = EditUnitDialogFragment.newInstance(name);
        editDialog.show(mParentActivity.getSupportFragmentManager(),
                EditUnitDialogFragment.EDIT_UNIT_DIALOG_TAG);
    }

    @Override
    public void showUnitExistError(boolean visible) {
        ((EditUnitDialogFragment)mParentActivity.getSupportFragmentManager().
                findFragmentByTag(EditUnitDialogFragment.EDIT_UNIT_DIALOG_TAG)).showError(visible);
    }

    @Override
    public void enableSaveUnit(boolean enable) {
        ((EditUnitDialogFragment)mParentActivity.getSupportFragmentManager().
                findFragmentByTag(EditUnitDialogFragment.EDIT_UNIT_DIALOG_TAG)).enableSaveBtn(enable);
    }

    @Override
    public void onUnitEdited(int position) {
        mAdapter.notifyItemChanged(position + 1);
        clearEditText();
    }

    @Override
    public void enableSaveConverter(boolean enable) {
        mItemSaveVisible = enable;
        mBtnSave.setVisible(enable);
    }

    @Override
    public void showHint(boolean visible) {
        mTextHint.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setConverterSavingIndicator(boolean active) {
        if (mSavingDialog == null) {
            mSavingDialog = new AlertDialog.Builder(mParentActivity)
                    .setView(R.layout.dialog_progress)
                    .setCancelable(false).create();
        }

        if (active) mSavingDialog.show();
        else mSavingDialog.dismiss();
    }

    @Override
    public void showAskDialog() {
        new AlertDialog.Builder(mParentActivity).setMessage(
                getString(R.string.msg_save_changes))
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mActionListener.saveConverter(true);
                    }
                })
                .setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showPreviousView();
                    }
                })
                .setCancelable(false).show();
    }

    @Override
    public void showUnitsLoadingError(@NonNull String message) {
        mBtnUpdate.setVisibility(View.VISIBLE);
        mTextLoadingError.setText(message);
        mTextLoadingError.setVisibility(View.VISIBLE);
    }

    void clearEditText() {
        mEditName.clearFocus();
        mEditName.setFocusableInTouchMode(false);
        hideSoftInput();
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

    private class UnitsAdapterWithHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements ItemTouchHelperAdapter {

        private final static int TYPE_ITEM = 0;
        private final static int TYPE_HEADER = 1;

        private boolean editable;

        private List<Converter.Unit> mUnits;

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                if (editable) {
                    return new VHItem(LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.rw_edit_converter_item, parent, false));
                } else {
                    return new VHItem(LayoutInflater.from(parent.getContext()).
                            inflate(R.layout.rw_converters_edit_item, parent, false));
                }
            } else if (viewType == TYPE_HEADER) {
                return new VHHeader(mRecyclerViewHeader);
            }
            throw new RuntimeException("there is no type that matches the type "
                    + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof VHItem) {
                int pos = position - 1;
                ((VHItem) holder).mUnitName.setText(mUnits.get(pos).name);

                if (editable) {
                    double value = mUnits.get(pos).value;
                    if (value != 0d) {
                        ((VHItem) holder).mUnitValue.setText(String.valueOf(value));
                    }
                }

                ((VHItem) holder).mCheck.setChecked(mUnits.get(pos).isEnabled);
                ((VHItem) holder).mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mActionListener.enableUnit(holder.getAdapterPosition() - 1, isChecked);
                    }
                });

                ((VHItem) holder).mHandleReorder.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEventCompat.getActionMasked(event) ==
                                MotionEvent.ACTION_DOWN) {
                            mItemTouchHelper.startDrag(holder);
                        }
                        return false;
                    }
                });
            } else if (holder instanceof VHHeader) {
                mEditName.setText(mActionListener.getConverterName());
            }
        }

        @Override
        public int getItemCount() {
            if (mUnits == null) return 1;
            return mUnits.size() + 1;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (toPosition == 0) return false;

            mActionListener.moveUnit(fromPosition - 1, toPosition - 1);
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            mActionListener.deleteUnit(position - 1);
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

            editable = mActionListener.isUnitsValueEditable();
            if (editable) mParentActivity.showFab(true);
            else mParentActivity.showFab(false);
        }

        private class VHItem extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            private TextView mUnitName;

            private TextView mUnitValue;

            private CheckBox mCheck;

            private ImageView mHandleReorder;

            VHItem(View itemView) {
                super(itemView);

                mUnitName = (TextView) itemView.findViewById(R.id.text_view_name);
                mCheck = (CheckBox) itemView.findViewById(R.id.check_box_enable);
                mHandleReorder = (ImageView) itemView.findViewById(R.id.image_view_handle);

                if (editable) {
                    mUnitValue = (TextView) itemView.findViewById(R.id.text_view_value);

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mActionListener.editUnit(mUnitName.getText().toString(),
                                    mUnitValue.getText().toString());
                        }
                    });
                } else {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mActionListener.editUnit(mUnitName.getText().toString(), null);
                        }
                    });
                }
            }

            @Override
            public void onItemSelected() {
                itemView.setBackgroundColor(Color.LTGRAY);
            }

            @Override
            public void onItemClear() {
                itemView.setBackgroundColor(0);
            }
        }

        private class VHHeader extends RecyclerView.ViewHolder {

            VHHeader(View itemView) {
                super(itemView);
            }
        }
    }
}
