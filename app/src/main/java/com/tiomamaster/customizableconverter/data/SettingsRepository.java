package com.tiomamaster.customizableconverter.data;

/**
 * Created by Artyom on 05.11.2016.
 */

public interface SettingsRepository {

    interface OnSettingsChangeListener{

        void onSettingsChange(int grSize, int maxFrDigits, boolean stForm, boolean defForm);
    }

    String[] getSummaries();

    boolean getStandardForm();

    boolean getDefaultForm();

    void setOnSettingsChangeListener(OnSettingsChangeListener listener);
}
