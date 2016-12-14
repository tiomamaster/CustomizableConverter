package com.tiomamaster.customizableconverter.settings;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.Divider;
import com.tiomamaster.customizableconverter.data.Repositories;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperAdapter;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperCallback;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperViewHolder;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 20.10.2016.
 */
public class ConvertersEditFragment extends Fragment implements SettingsContract.ConvertersEditView {

    private SettingsContract.ConvertersEditUal mActionListener;

    private SettingsActivity mParentActivity;

    private ConvertersAdapter mAdapter;

    private ItemTouchHelper mItemTouchHelper;

    public ConvertersEditFragment() {
    }

    public static ConvertersEditFragment newInstance() {
        return new ConvertersEditFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new ConvertersAdapter(mActionListener.loadConverters());
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

        // draw divider between items
        recyclerView.addItemDecoration(new Divider(getActivity(), 0));

        setRetainInstance(true);
        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (SettingsActivity) getActivity();
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
        mActionListener = (SettingsContract.ConvertersEditUal) checkNotNull(presenter);
    }

    @Override
    public void showPreviousView() {
        SettingsFragment view = SettingsFragment.newInstance();
        SettingsContract.UserActionListener presenter = new SettingsPresenter(
                Repositories.getInMemoryRepoInstance(getContext()), view);
        mParentActivity.showFragment(view);
        mParentActivity.setUserActionListener(presenter);
        mParentActivity.setFabVisibility(false);
    }

    private class ConvertersAdapter extends RecyclerView.Adapter<ConvertersAdapter.ViewHolder>
            implements ItemTouchHelperAdapter {

        private List<Pair<String, Boolean>> mConverters;

        ConvertersAdapter(List<Pair<String, Boolean>> converters) {
            this.mConverters = converters;
            notifyDataSetChanged();
        }

        @Override
        public ConvertersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ConvertersAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.item_converters_edit, parent, false));
        }

        @Override
        public void onBindViewHolder(final ConvertersAdapter.ViewHolder holder, int position) {
            holder.mName.setText(mConverters.get(position).first);

            holder.mHandleReorder.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) ==
                            MotionEvent.ACTION_DOWN) {
                        mItemTouchHelper.startDrag(holder);
                    }
                    return false;
                }
            });

            holder.mCheck.setChecked(mConverters.get(position).second);
            holder.mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mConverters.add(holder.getAdapterPosition(),
                            new Pair<>(mConverters.remove(holder.getAdapterPosition()).first, isChecked));
                }
            });
        }

        @Override
        public int getItemCount() {
            if (mConverters != null)
                return mConverters.size();
            return 0;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mConverters, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mConverters, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(final int position) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case AlertDialog.BUTTON_POSITIVE:
                            mConverters.remove(position);
                            break;
                        case AlertDialog.BUTTON_NEGATIVE:
                            notifyItemChanged(position);
                            break;
                    }
                }
            };

            new AlertDialog.Builder(mParentActivity).setMessage(
                    getString(R.string.msg_delete_converter))
                    .setPositiveButton(android.R.string.ok, listener)
                    .setNegativeButton(android.R.string.cancel, listener)
                    .setCancelable(false).show();
        }

        class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

            private AppCompatTextView mName;

            private ImageView mHandleReorder;

            private AppCompatCheckBox mCheck;

            ViewHolder(View itemView) {
                super(itemView);

                mName = (AppCompatTextView) itemView.findViewById(R.id.text_view_name);
                mName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditConverterFragment view = EditConverterFragment.newInstance();
                        SettingsContract.UserActionListener presenter = new EditConverterPresenter(
                                Injection.provideConvertersRepository(getContext()), view,
                                mName.getText().toString());
                        mParentActivity.showFragment(view);
                        mParentActivity.setUserActionListener(presenter);
                    }
                });

                mHandleReorder = (ImageView) itemView.findViewById(R.id.image_view_handle);
                mCheck = (AppCompatCheckBox) itemView.findViewById(R.id.check_box_enable);
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
    }
}