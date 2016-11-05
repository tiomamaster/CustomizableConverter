package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;

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
    }

    interface EditView extends View {

        void showEditor();
    }

    interface UserActionListener {

        void handleHomePressed();

        void loadSettings();

        void loadEditor();

        void handleLanguageChanged(String newVal);

        void handleGroupingSizeChanged(String newVal);

        void handlePrecisionChanged(String newVal);

        void handleStandardFormChanged(boolean newVal);
    }
}