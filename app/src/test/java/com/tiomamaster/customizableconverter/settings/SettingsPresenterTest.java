package com.tiomamaster.customizableconverter.settings;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 31.10.2016.
 */
public class SettingsPresenterTest {

    @Mock
    private ConvertersRepository mConvertersRepo;

    @Mock
    private SettingsRepository mSettingsRepo;

    @Mock
    private SettingsContract.SettingsView mSettingsView;

    @Mock
    private SettingsContract.EditView mEditView;

    private SettingsContract.UserActionListener mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new SettingsPresenter(mConvertersRepo, mSettingsRepo, mSettingsView, mEditView);
    }

    @Test
    public void handleHomePressed() {

        // load settings first
        mPresenter.loadSettings();

        mPresenter.handleHomePressed();

        // check the settings closed
        verify(mSettingsView).closeSettings();

        // load editor for converters
        mPresenter.loadEditor();

        mPresenter.handleHomePressed();

        // check the settings shown 2 times because first call
        verify(mSettingsView, times(2)).showSettings(null);
    }

    @Test
    public void loadSettings() {

        when(mSettingsRepo.getSummaries()).thenReturn(new String[]{""});

        mPresenter.loadSettings();

        verify(mSettingsRepo).getSummaries();

        verify(mSettingsView).showSettings(new String[]{""});

        verify(mSettingsRepo).getResultView();

        verify(mSettingsView).enableGrSizeOption(anyBoolean());
    }

    @Test
    public void allSettingsChange() {

        // language changed
        mPresenter.handleLanguageChanged(anyString());
        verify(mSettingsRepo).setNewLanguage(anyString());

        // grouping size
        mPresenter.handleGroupingSizeChanged("5");
        verify(mSettingsRepo).setNewGroupingSize(anyInt());

        // precision
        mPresenter.handlePrecisionChanged("5");
        verify(mSettingsRepo).setNewPrecision(anyInt());

        // standard form or not changed
        mPresenter.handleResultViewChanged(anyBoolean());
        verify(mSettingsView).enableGrSizeOption(anyBoolean());
        verify(mSettingsRepo).setNewResultView(anyBoolean());
    }
}