package com.tiomamaster.customizableconverter.settings;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.Divider;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperCallback;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 20.10.2016.
 */

public class EditFragment extends Fragment implements SettingsContract.EditView,
        RecyclerViewAdapter.ActionListener {

    private SettingsContract.UserActionListener mPresenter;

    private SettingsActivity mParentActivity;

    private RecyclerView mRecyclerView;

    private RecyclerViewAdapter mAdapter;

    private ItemTouchHelper mItemTouchHelper;

    public EditFragment() {
    }

    public static EditFragment newInstance() {
        return new EditFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new RecyclerViewAdapter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_edit, container, false);

        mRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(mAdapter));
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        // draw divider between items
        mRecyclerView.addItemDecoration(new Divider(getActivity(), 0));

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
    public void showEditor(List<Pair<String, Boolean>> data) {
        mAdapter.setDataSet(data);
        mParentActivity.showFragment(this);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onStartSwipe(final int position) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        mAdapter.confirmDismiss(position);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        mAdapter.cancelDismiss(position);
                        break;
                }
            }
        };

        new AlertDialog.Builder(mParentActivity).setMessage(
                "Are you sure want to delete this converter?")
                .setPositiveButton("OK", listener)
                .setNegativeButton("CANCEL", listener)
                .setCancelable(false).show();
    }
}