package com.tiomamaster.customizableconverter.settings;

import com.tiomamaster.customizableconverter.data.SettingsRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void pressHomeBtn_CloseSettings() {
        mPresenter.handleHomePressed();

        verify(mView).showPreviousView();
    }

    @Test
    public void loadSummaries_SetListener () {
        mPresenter.loadSummaries();

        verify(mRepository).setOnSettingsChangeListener(mPresenter);
    }

    @Test
    public void setDefaultForm_DisableAllFormattingOption() {
        when(mRepository.getDefaultForm()).thenReturn(true);

        mPresenter.standardOrDefaultClicked();

        verify(mView).enableFormattingOptions(false);
    }

    @Test
    public void setStandardForm_DisableGrSizeOption() {
        when(mRepository.getStandardForm()).thenReturn(true);
        when(mRepository.getDefaultForm()).thenReturn(false);

        mPresenter.standardOrDefaultClicked();

        verify(mView).enableGrSizeOption(false);
    }

    @Test
    public void onSettingsChange_UpdateSummaries() throws Exception {
        String[] summaries = {"Sum1", "Sum2", "Sum3"};
        when(mRepository.getSummaries()).thenReturn(summaries);

        mPresenter.onSettingsChange(0, 0, true, true, false);

        verify(mRepository).getSummaries();

        verify(mView).showSummaries(summaries);
    }

    @Test
    public void onSettingsChange_RestartApp() {
        mPresenter.onSettingsChange(0, 0, true, true, true);

        verify(mView).restartApp();
    }
}