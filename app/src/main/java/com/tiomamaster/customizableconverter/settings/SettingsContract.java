package com.tiomamaster.customizableconverter.settings;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;

import java.util.List;

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

        void showDialog();

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
         * @param position The position of unit in the list which user want to delete,
         *                 need for adapter notification.
         */
        void showWarning(int position);

        /**
         * Show dialog to edit existing or create new unit.
         * @param name The name of the editing unit. Pass null if create new unit.
         * @param value The value of the editing unit. Pass null if unit is uneditable,
         *              or empty string if create new unit.
         */
        void showUnitEditor(@Nullable String name, @Nullable String value);

        void showUnitEditor(@NonNull String name);

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

        void showAskDialog();

        void showUnitsLoadingError(int messageResId);
    }

    interface EditConverterUal extends UserActionListener {

        @Nullable
        String getConverterName();

        void setConverterName(@NonNull String newName);

        void loadUnits();

        void moveUnit(int fromPosition, int toPosition);

        void deleteUnit(int position);

        void enableUnit(int orderPosition , boolean enable);

        void editUnit(@NonNull String name, @Nullable String value);

        void setUnitName(@NonNull String newName);

        void setUnitValue(@NonNull String newValue);

        void saveUnit();

        void saveConverter(boolean closeEditor);

        /**
         * @return true if units value editable for current converter, false otherwise.
         */
        boolean isUnitsValueEditable();

        void updateUnits();
    }
}