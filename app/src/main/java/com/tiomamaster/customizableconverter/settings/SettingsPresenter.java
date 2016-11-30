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

class SettingsPresenter implements SettingsContract.UserActionListener {

    private enum State {
        SETTINGS, CONVERTERS_EDITOR, UNITS_EDITOR
    }

    private ConvertersRepository mConvertersRepo;
    private SettingsRepository mSettingsRepo;
    private SettingsContract.SettingsView mSettingsView;
    private SettingsContract.EditView mEditView;

    private State state = State.SETTINGS;

    SettingsPresenter(@NonNull ConvertersRepository convertersRepository,
                      SettingsRepository settingsRepository, @NonNull SettingsContract.SettingsView settingsView,
                      @NonNull SettingsContract.EditView editView) {
        mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        mSettingsRepo = checkNotNull(settingsRepository, "settingsRepository cannot be null");
        mSettingsView = checkNotNull(settingsView, "settingsView cannot be null");
        mEditView = checkNotNull(editView, "editView cannot be null");

        mSettingsView.setPresenter(this);
        mEditView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        switch (state) {
            case SETTINGS:
                mSettingsView.closeSettings();
                break;
            case CONVERTERS_EDITOR:
                loadSettings();
                break;
            case UNITS_EDITOR:
                break;
        }
    }

    @Override
    public void loadSettings() {
        state = State.SETTINGS;

        mSettingsView.showSettings(mSettingsRepo.getSummaries());

        mSettingsView.enableGrSizeOption(!mSettingsRepo.getStandardForm());
    }

    @Override
    public void loadEditor() {
        state = State.CONVERTERS_EDITOR;
        mConvertersRepo.getAllConverterTypes(new ConvertersRepository.LoadAllConvertersTypesCallback() {
            @Override
            public void onConvertersTypesLoaded(@NonNull List<Pair<String, Boolean>> convertersTypes) {
                mEditView.showEditor(convertersTypes);
            }
        });
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
}
