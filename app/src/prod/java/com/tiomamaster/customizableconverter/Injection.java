package com.tiomamaster.customizableconverter;

import android.content.Context;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.ConvertersServiceApiImpl;
import com.tiomamaster.customizableconverter.data.Repositories;

/**
 * Created by Artyom on 16.07.2016.
 */
public class Injection {

    public static ConvertersRepository provideConvertersRepository(Context appContext) {
        return Repositories.getInMemoryRepoInstance(new ConvertersServiceApiImpl(appContext));
    }
}
