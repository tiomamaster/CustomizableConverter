package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;

public interface SettingsRepository {

    interface OnSettingsChangeListener{

        void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm,
                              boolean langChanged);
    }

    String[] getSummaries();

    boolean getStandardForm();

    boolean getDefaultForm();

    void setOnSettingsChangeListener(@NonNull OnSettingsChangeListener listener);
}
