package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;

import java.util.List;

import static android.R.attr.name;
import static com.tiomamaster.customizableconverter.R.id.quantity;

/**
 * Created by Artyom on 14.07.2016.
 */
interface ConverterContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showConvertersTypes(@NonNull List<String> converters, int selection);

        void showConverter(@NonNull List<String> units, int lastUnitPos, @NonNull String lastQuantity);

        void showConversionResult(@NonNull List<Pair<String, String>> result);

        void showSettingsUi();

        void showNoting();
    }

    interface UserActionListener {

        void loadConvertersTypes();

        void loadConverter(@NonNull String name);

        void convert(@NonNull String from, @NonNull String quantity);

        void saveLastUnitPos(int pos);

        void saveLastQuantity(@NonNull String quantity);

        void openSettings();
    }
}