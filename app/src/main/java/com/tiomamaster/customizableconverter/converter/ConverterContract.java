package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import java.util.List;

interface ConverterContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showConvertersTypes(@NonNull List<String> converters, int selection);

        void showConverter(@NonNull List<String> units, int lastUnitPos,
                           @NonNull String lastQuantity, boolean signedQuantity);

        void showConversionResult(@NonNull List<Pair<String, String>> result);

        void showSettingsUi();

        void showNoting();

        void showError(int messageResId);

        void enableSwipeToRefresh(boolean isEnabled);

        void showSnackBar(int messageResId);

        void hideSnackBar();
    }

    interface UserActionListener {

        void loadConvertersTypes();

        void loadConverter(@NonNull String name);

        void convert(@NonNull String from, @NonNull String quantity);

        void saveLastUnitPos(int pos);

        void saveLastQuantity(@NonNull String quantity);

        void openSettings();

        void updateCourses();
    }
}