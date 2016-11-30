package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

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

    @Mock private SettingsRepository.OnSettingsChangeListener mSettingsChangeListener;

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
    public void setOnSettingsChangeListenerShouldCallIt() {
        // check that listener was called
        verify(mSettingsChangeListener).onSettingsChange(anyInt(), anyInt(), anyBoolean(), anyBoolean());
    }
}