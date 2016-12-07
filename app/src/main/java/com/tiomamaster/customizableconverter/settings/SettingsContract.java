package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

/**
 * Created by Artyom on 27.10.2016.
 */
interface SettingsContract {

    interface View {

        void setPresenter(@NonNull UserActionListener presenter);

        void showPreviousView();
    }

    interface UserActionListener {

        void handleHomePressed();
    }

    interface SettingsView extends View {

        void showSummaries(String[] summaries);

        void enableGrSizeOption(boolean enable);

        void enableFormattingOptions(boolean enable);
    }

    interface SettingsUal extends UserActionListener {

        void loadSummaries();

        void standardOrDefaultClicked();
    }

    interface ConvertersEditView extends View {

        void showConverters(List<Pair<String, Boolean>> data);
    }

    interface ConvertersEditUal extends UserActionListener {

        void loadConverters();
    }

    interface ConverterEditView extends View {

    }

    interface ConverterEditUal extends UserActionListener {

    }
}