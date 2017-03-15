package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InMemorySettingsRepositoryTest {

    private SettingsRepository mRepository;

    @Mock private SettingsRepository.OnSettingsChangeListener mSettingsChangeListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Context c = mock(Context.class);
        SharedPreferences sp = mock(SharedPreferences.class);
        when(c.getSharedPreferences(anyString(), anyInt())).thenReturn(sp);
        when(sp.getString(anyString(), anyString())).thenReturn("1");
        mRepository = new InMemorySettingsRepository(c);
        mRepository.setOnSettingsChangeListener(mSettingsChangeListener);
        mRepository.setOnSettingsChangeListener(mSettingsChangeListener);
    }

    @Test
    public void setOnSettingsChangeListenerShouldCallAll() {
        // check that all listener was called
        verify(mSettingsChangeListener, times(2))
                .onSettingsChange(anyInt(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean());
    }
}