package com.tiomamaster.customizableconverter.data;

import android.support.annotation.NonNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConverterRepositories {

    private ConverterRepositories() {}

    private static ConvertersRepository repository = null;

    public synchronized static ConvertersRepository getInMemoryRepoInstance(
            @NonNull ConvertersServiceApi convertersServiceApi) {
        checkNotNull(convertersServiceApi);
        if (repository == null) {
            repository = new InMemoryConvertersRepository(convertersServiceApi);
        }
        return repository;
    }
}
