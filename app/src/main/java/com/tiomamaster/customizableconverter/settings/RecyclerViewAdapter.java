package com.tiomamaster.customizableconverter.settings;

import android.graphics.Color;
import android.support.v4.util.Pair;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperAdapter;
import com.tiomamaster.customizableconverter.settings.helper.ItemTouchHelperViewHolder;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 14.11.2016.
 */

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
        implements ItemTouchHelperAdapter {

    private List<Pair<String, Boolean>> mDataSet;

    private AdapterActionListener mAdapterActionListener;

    RecyclerViewAdapter(AdapterActionListener listener) {
        mAdapterActionListener = listener;
    }

    @Override
    public RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.edit_recycler_view_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mText.setText(mDataSet.get(position).first);

        holder.mHandle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mAdapterActionListener.onStartDrag(holder);
                }
                return false;
            }
        });

        holder.mCheck.setChecked(mDataSet.get(position).second);
        holder.mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDataSet.add(holder.getAdapterPosition(),
                new Pair<>(mDataSet.remove(holder.getAdapterPosition()).first, isChecked));
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mDataSet != null)
            return mDataSet.size();
        return 0;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataSet, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataSet, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        mAdapterActionListener.onStartSwipe(position);
    }

    void cancelDismiss(int position) {
        notifyItemChanged(position);
    }

    void confirmDismiss(int position) {
        mDataSet.remove(position);
        notifyItemRemoved(position);
    }

    void setDataSet(List<Pair<String, Boolean>> newData) {
        mDataSet = newData;
        notifyDataSetChanged();
    }

    interface AdapterActionListener {

        void onStartDrag(RecyclerView.ViewHolder viewHolder);

        void onStartSwipe(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private AppCompatTextView mText;

        private ImageView mHandle;

        private AppCompatCheckBox mCheck;

        ViewHolder(View itemView) {
            super(itemView);

            mText = (AppCompatTextView) itemView.findViewById(R.id.textView);
            mHandle = (ImageView) itemView.findViewById(R.id.handle);
            mCheck = (AppCompatCheckBox) itemView.findViewById(R.id.checkBox);
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