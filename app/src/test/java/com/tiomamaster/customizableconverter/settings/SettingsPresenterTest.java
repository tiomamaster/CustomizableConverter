package com.tiomamaster.customizableconverter.settings;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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

    @Mock private SettingsContract.SettingsView mView;

    @Mock private SettingsRepository mRepository;

    private SettingsPresenter mPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new SettingsPresenter(mRepository, mView);
    }

    @Test
    public void pressHomeBtnCloseSettings() {
        mPresenter.handleHomePressed();

        verify(mView).showPreviousView();
    }

    @Test
    public void loadAndShowSummaries () {
        String[] summaries = {"Sum1", "Sum2", "Sum3"};
        when(mRepository.getSummaries()).thenReturn(summaries);

        mPresenter.loadSummaries();

        verify(mRepository).getSummaries();

        verify(mView).showSummaries(summaries);
    }

    @Test
    public void setDefaultFormDisableAllFormattingOption() {
        when(mRepository.getDefaultForm()).thenReturn(true);

        mPresenter.standardOrDefaultClicked();

        verify(mView).enableFormattingOptions(false);
    }

    @Test
    public void setStandardFormDisableGrSizeOption() {
        when(mRepository.getStandardForm()).thenReturn(true);
        when(mRepository.getDefaultForm()).thenReturn(false);

        mPresenter.standardOrDefaultClicked();

        verify(mView).enableGrSizeOption(false);
    }
}