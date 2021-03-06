package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;

import com.tiomamaster.customizableconverter.data.SettingsRepository;

import static com.google.common.base.Preconditions.checkNotNull;

class SettingsPresenter implements SettingsContract.SettingsUal, SettingsRepository.OnSettingsChangeListener {

    @NonNull private final SettingsRepository mSettingsRepo;

    @NonNull private final SettingsContract.SettingsView mSettingsView;

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
    public void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm,
                                 boolean langChanged) {
        if (langChanged) mSettingsView.showDialog();
        mSettingsView.showSummaries(mSettingsRepo.getSummaries());
    }
}
