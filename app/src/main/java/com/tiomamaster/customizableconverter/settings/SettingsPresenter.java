package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.SettingsRepository;

import java.util.List;

import static android.R.attr.icon;
import static android.R.attr.id;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 27.10.2016.
 */

class SettingsPresenter implements SettingsContract.SettingsUal, SettingsRepository.OnSettingsChangeListener {

    @NonNull
    private SettingsRepository mSettingsRepo;

    @NonNull
    private SettingsContract.SettingsView mSettingsView;

    SettingsPresenter(@NonNull SettingsRepository settingsRepository,
                      @NonNull SettingsContract.SettingsView settingsView) {
        mSettingsRepo = checkNotNull(settingsRepository, "settingsRepository cannot be null");
        mSettingsView = checkNotNull(settingsView, "settingsView cannot be null");

        mSettingsView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        mSettingsView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {
        //there is no action button here need to handle
    }

    @Override
    public void loadSummaries() {
        mSettingsRepo.setOnSettingsChangeListener(this);
    }

    @Override
    public void standardOrDefaultClicked() {
        if (mSettingsRepo.getDefaultForm()) {
            mSettingsView.enableFormattingOptions(false);
            return;
        } else {
            mSettingsView.enableFormattingOptions(true);
        }

        mSettingsView.enableGrSizeOption(!mSettingsRepo.getStandardForm());
    }

    @Override
    public void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm) {
        showSummaries();
    }

    private void showSummaries() {
        mSettingsView.showSummaries(mSettingsRepo.getSummaries());
    }
}
