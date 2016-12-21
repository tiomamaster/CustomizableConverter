package com.tiomamaster.customizableconverter.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;

import com.tiomamaster.customizableconverter.data.Converter;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.N;

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

        void handleFabPressed();
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

        void showAskDialog(@NonNull String name, @NonNull DialogInterface.OnClickListener listener);

        void notifyConverterRemoved(int position);

        void notifyConverterCancelRemove(int position);

        void showEditConverter(@Nullable String name);
    }

    interface ConvertersEditUal extends UserActionListener {

        List<Pair<String, Boolean>> loadConverters();

        void moveConverter(int fromPosition, int toPosition);

        void deleteConverter(int position);

        void enableConverter(int orderPosition , boolean enable);
    }

    interface EditConverterView extends View {

        void showUnits(@NonNull List<Converter.Unit> units);

        void error(boolean visible);

        void setProgressIndicator(boolean active);
    }

    interface EditConverterUal extends UserActionListener {

        @Nullable
        String getConverterName();

        void setConverterName(@NonNull String newName);

        void loadUnits();
    }
}