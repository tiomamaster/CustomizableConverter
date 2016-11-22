package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Artyom on 27.10.2016.
 */

interface SettingsContract {

    interface View {

        void setPresenter(@NonNull UserActionListener presenter);
    }

    interface SettingsView extends View {

        void closeSettings();

        void showSettings(String[] summaries);

        void enableGrSizeOption(boolean enable);
    }

    interface EditView extends View {

        void showEditor(List<Pair<String, Boolean>> data);
    }

    interface UserActionListener {

        void handleHomePressed();

        void loadSettings();

        void loadEditor();

        void handleLanguageChanged(String newVal);

        void handleGroupingSizeChanged(String newVal);

        void handlePrecisionChanged(String newVal);

        void handleResultViewChanged(boolean newVal);
    }
}