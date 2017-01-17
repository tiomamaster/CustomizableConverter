package com.tiomamaster.customizableconverter.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;

import com.tiomamaster.customizableconverter.data.Converter;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.name;
import static android.R.attr.value;
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

        void showConverterExistError(boolean visible);

        void setUnitsLoadingIndicator(boolean active);

        void notifyUnitRemoved(int position);

        /**
         * Show dialog with warning text when user want to delete unit from the converter
         * when number of it is less then 3, because need at least 2 units.
         *
         * @param position The position of unit in the list which user want to delete,
         *                 need for adapter notification.
         */
        void showWarning(int position);

        void showEditUnit(@Nullable String name, @Nullable String value);

        /**
         * Show error massage.
         * @param visible True if show error, false otherwise.
         */
        void showUnitExistError(boolean visible);

        void enableSaveUnit(boolean enable);

        void onUnitEdited(int position);

        void enableSaveConverter(boolean enable);

        void showHint(boolean visible);

        void setConverterSavingIndicator(boolean active);
    }

    interface EditConverterUal extends UserActionListener {

        @Nullable
        String getConverterName();

        void setConverterName(@NonNull String newName);

        void loadUnits();

        void moveUnit(int fromPosition, int toPosition);

        void deleteUnit(int position);

        void enableUnit(int orderPosition , boolean enable);

        void editUnit(@NonNull String name, @NonNull String value);

        void setUnitName(@NonNull String newName);

        void setUnitValue(@NonNull String newValue);

        void saveUnit();

        void saveConverter();
    }
}