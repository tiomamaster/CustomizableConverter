package com.tiomamaster.customizableconverter;

import android.content.Context;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.ConvertersServiceApiImpl;
import com.tiomamaster.customizableconverter.data.Repositories;

public class Injection {

    public static ConvertersRepository provideConvertersRepository(Context appContext) {
        return Repositories.getInMemoryRepoInstance(new ConvertersServiceApiImpl(appContext));
    }
}