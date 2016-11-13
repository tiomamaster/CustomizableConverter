package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.internal.BooleanSupplier;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.configuration.AnnotationEngine;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 13.11.2016.
 */
public class InMemorySettingsRepositoryTest {

    private SettingsRepository mSettingsRepo;

    @Mock
    private SettingsRepository.OnSettingsChangeListener mSettingsChangeListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Context c = mock(Context.class);
        SharedPreferences sp = mock(SharedPreferences.class);
        when(c.getSharedPreferences(anyString(), anyInt())).thenReturn(sp);
        when(sp.getString(anyString(), anyString())).thenReturn("1");
        mSettingsRepo = new InMemorySettingsRepository(c);
        mSettingsRepo.setOnSettingsChangeListener(mSettingsChangeListener);
    }

    @Test
    public void setNewGroupingSize() {

        int grSize = 1;

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);

        mSettingsRepo.setNewGroupingSize(grSize);

        // check that listener is called
        verify(mSettingsChangeListener, times(2)).
                onSettingsChange(captor.capture(), anyInt(), anyBoolean());

        // check argument
        assertEquals(grSize, captor.getValue().intValue());

    }

    @Test
    public void setNewPrecision() {

        int precision = 2;

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);

        mSettingsRepo.setNewPrecision(precision);

        // check that listener is called
        verify(mSettingsChangeListener, times(2)).
                onSettingsChange(anyInt(), captor.capture(), anyBoolean());

        // check argument
        assertEquals(precision, captor.getValue().intValue());

    }

    @Test
    public void setNewResultView() {

        boolean view = true;

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        mSettingsRepo.setNewResultView(view);

        // check that listener is called
        verify(mSettingsChangeListener, times(2)).
                onSettingsChange(anyInt(), anyInt(), captor.capture());

        // check argument
        assertEquals(view, captor.getValue().booleanValue());

    }

    @Test
    public void setOnSettingsChangeListener() {

        // check that listener is called
        verify(mSettingsChangeListener).onSettingsChange(anyInt(), anyInt(), anyBoolean());
    }

}