package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 07.12.2016.
 */
class EditConverterPresenter implements SettingsContract.EditConverterUal {

    @NonNull
    private ConvertersRepository mConvertersRepo;

    @NonNull
    private SettingsContract.EditConverterView mView;

    private String mInitialConverterName;

    private String mCurConverterName;

    private boolean isNew;

    EditConverterPresenter(@NonNull ConvertersRepository convertersRepository,
                           @NonNull SettingsContract.EditConverterView editConverterView,
                           @Nullable String initialConverterName) {
        mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        mView = checkNotNull(editConverterView, "editConverterView cannot be null");
        mCurConverterName = mInitialConverterName = initialConverterName;

        if (initialConverterName == null) isNew = true;

        mView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        mView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {

    }

    @Nullable
    @Override
    public String getConverterName() {
        return mCurConverterName;
    }

    @Override
    public void setConverterName(@NonNull String newName) {
        checkNotNull(newName);

        mCurConverterName = newName;

        if (isNew) {
            if (mConvertersRepo.getCachedConvertersTypes().contains(new Pair<>(newName, true)) ||
                    mConvertersRepo.getCachedConvertersTypes().contains(new Pair<>(newName, false))) {
                mView.error(true);
            } else mView.error(false);
        } else {
            if (!mInitialConverterName.equals(newName) &&
                    mConvertersRepo.getCachedConvertersTypes().contains(new Pair<>(newName, true)) ||
                    mConvertersRepo.getCachedConvertersTypes().contains(new Pair<>(newName, false))) {
                mView.error(true);
            } else {
                mView.error(false);
            }
        }
    }

    @Override
    public void loadUnits() {
        if (isNew) {
            List<Converter.Unit> emptyUnits = new ArrayList<>(2);
            emptyUnits.add(new Converter.Unit("", 0d, true));
            emptyUnits.add(new Converter.Unit("", 0d, true));
            mView.showUnits(emptyUnits);
            return;
        }

        mView.setProgressIndicator(true);

        mConvertersRepo.getConverter(mInitialConverterName, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                checkNotNull(converter);

                mView.setProgressIndicator(false);

                mView.showUnits(converter.getUnits());
            }
        });
    }
}