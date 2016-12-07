package com.tiomamaster.customizableconverter.settings;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 31.10.2016.
 */
public class SettingsPresenterTest {

    @Mock private ConvertersRepository mConvertersRepo;

    @Mock private SettingsRepository mSettingsRepo;

    @Mock private SettingsContract.SettingsView mSettingsView;

    @Mock private SettingsContract.EditView mEditView;

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
        verify(mSettingsView, times(2)).showSummaries(null);
    }

    @Test
    public void loadSettings() {
        when(mSettingsRepo.getSummaries()).thenReturn(new String[]{""});

        mPresenter.loadSettings();

        verify(mSettingsRepo).getSummaries();

        verify(mSettingsView).showSummaries(new String[]{""});

        verify(mSettingsRepo).getStandardForm();

        verify(mSettingsView).enableGrSizeOption(anyBoolean());
    }

    @Test
    public void loadEditor() {
        mPresenter.loadEditor();

        ArgumentCaptor<ConvertersRepository.LoadAllConvertersTypesCallback> captor =
                ArgumentCaptor.forClass(ConvertersRepository.LoadAllConvertersTypesCallback.class);
        verify(mConvertersRepo).getAllConverterTypes(captor.capture());
        captor.getValue().onConvertersTypesLoaded(any(List.class));

        verify(mEditView).showEditor(any(List.class));
    }

    @Test
    public void standardOrDefaultClickedWithFalse() {
        when(mSettingsRepo.getDefaultForm()).thenReturn(false);
        when(mSettingsRepo.getStandardForm()).thenReturn(true);
        mPresenter.standardOrDefaultClicked();

        verify(mSettingsRepo).getDefaultForm();

        ArgumentCaptor<Boolean> booleanCaptor = ArgumentCaptor.forClass(Boolean.TYPE);

        verify(mSettingsView).enableFormattingOptions(booleanCaptor.capture());
        assertTrue(booleanCaptor.getValue());

        verify(mSettingsRepo).getStandardForm();

        verify(mSettingsView).enableGrSizeOption(booleanCaptor.capture());
        assertFalse(booleanCaptor.getValue());
    }

    @Test
    public void standardOrDefaultClickedWithTrue() {
        when(mSettingsRepo.getDefaultForm()).thenReturn(true);
        mPresenter.standardOrDefaultClicked();

        verify(mSettingsRepo).getDefaultForm();

        ArgumentCaptor<Boolean> booleanCaptor = ArgumentCaptor.forClass(Boolean.TYPE);

        verify(mSettingsView).enableFormattingOptions(booleanCaptor.capture());
        assertFalse(booleanCaptor.getValue());
    }
}