package com.tiomamaster.customizableconverter;

import android.content.Context;

import com.tiomamaster.customizableconverter.data.Repositories;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.FakeConvertersServiceApiImpl;

/**
 * Created by Artyom on 16.07.2016.
 */
public class Injection {

    public static ConvertersRepository provideConvertersRepository(Context c) {
        return Repositories.getInMemoryRepoInstance(new FakeConvertersServiceApiImpl());
    }
}
