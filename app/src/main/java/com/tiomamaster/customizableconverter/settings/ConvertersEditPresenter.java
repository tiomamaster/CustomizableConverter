package com.tiomamaster.customizableconverter.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 06.12.2016.
 */
class ConvertersEditPresenter implements SettingsContract.ConvertersEditUal {

    @NonNull
    private ConvertersRepository mConvertersRepo;

    @NonNull
    private SettingsContract.ConvertersEditView mView;

    private List<Pair<String, Boolean>> mAllConverters;

    ConvertersEditPresenter(@NonNull ConvertersRepository convertersRepository,
                            @NonNull SettingsContract.ConvertersEditView convertersEditView) {
        this.mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        this.mView = checkNotNull(convertersEditView, "convertersEditView cannot be null");

        mView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        mView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {
        mView.showEditConverter(null);
    }

    @Override
    public List<Pair<String, Boolean>> loadConverters() {
        return mAllConverters = mConvertersRepo.getCachedConvertersTypes();
    }

    @Override
    public void moveConverter(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mAllConverters, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mAllConverters, i, i - 1);
            }
        }
    }

    @Override
    public void deleteConverter(final int position) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AlertDialog.BUTTON_POSITIVE:
                        mAllConverters.remove(position);
                        mView.notifyConverterRemoved(position);
                        break;
                    case AlertDialog.BUTTON_NEGATIVE:
                        mView.notifyConverterCancelRemove(position);
                        break;
                }
            }
        };

        mView.showAskDialog(mAllConverters.get(position).first, listener);
    }

    @Override
    public void enableConverter(int orderPosition, boolean enable) {
        mAllConverters.add(orderPosition,
                new Pair<>(mAllConverters.remove(orderPosition).first, enable));
    }
}
