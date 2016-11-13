package com.tiomamaster.customizableconverter.data;

/**
 * Created by Artyom on 05.11.2016.
 */

public interface SettingsRepository {

    interface OnSettingsChangeListener{

        void onSettingsChange(int grSize, int maxFrDigits, boolean stForm);
    }

    String[] getSummaries();

    void setNewLanguage(String newVal);

    void setNewGroupingSize(int newVal);

    void setNewPrecision(int newVal);

    void setNewResultView(boolean newVal);

    boolean getResultView();

    void setOnSettingsChangeListener(OnSettingsChangeListener listener);
}
