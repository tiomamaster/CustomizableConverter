package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;

import com.tiomamaster.customizableconverter.data.Converter;

import java.util.List;

import static android.R.attr.name;

/**
 * Created by Artyom on 14.07.2016.
 */
interface ConverterContract {

    interface View {

        void setProgressIndicator(boolean active);

        void showConvertersTypes(@NonNull List<String> converters, int selection);

        void showConverter(@NonNull Converter converter);

        void showSettingsUi();

        void showNoting();
    }

    interface UserActionListener {

        void loadConvertersTypes();

        void loadConverter(@NonNull String name);

        void openSettings();
    }
}