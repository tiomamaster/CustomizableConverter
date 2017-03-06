package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

public class Repositories {

    private static ConvertersRepository convertersRepo = null;

    private static SettingsRepository settingsRepo = null;

    private Repositories() {}

    public synchronized static ConvertersRepository getInMemoryRepoInstance(
            @NonNull ConvertersServiceApi convertersServiceApi) {
        checkNotNull(convertersServiceApi);
        if (convertersRepo == null) {
            convertersRepo = new InMemoryConvertersRepository(convertersServiceApi);
        }
        return convertersRepo;
    }

    public synchronized static SettingsRepository getInMemoryRepoInstance(Context c) {
        if (settingsRepo == null) {
            settingsRepo = new InMemorySettingsRepository(c);
        }
        return settingsRepo;
    }
}