package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;

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
    public void loadConverters() {
        mConvertersRepo.getAllConverterTypes(new ConvertersRepository.LoadAllConvertersTypesCallback() {
            @Override
            public void onConvertersTypesLoaded(@NonNull List<Pair<String, Boolean>> convertersTypes) {
                mView.showConverters(convertersTypes);
            }
        });
    }
}
