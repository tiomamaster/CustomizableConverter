package com.tiomamaster.customizableconverter.settings;

import android.content.DialogInterface;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;

import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 18.01.2017.
 */
public class ConvertersEditPresenterTest {

    @Mock private SettingsContract.ConvertersEditView mView;

    @Mock private ConvertersRepository mRepository;

    private ConvertersEditPresenter mPresenter;

    private List<Pair<String, Boolean>> mAllConverters;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPresenter = new ConvertersEditPresenter(mRepository, mView);

        // mock field
        mAllConverters = new ArrayList<>(Arrays.asList(new Pair<>("one", true),
                new Pair<>("two", false), new Pair<>("three", false), new Pair<>("four", true)));
        when(mRepository.getCachedConvertersTypes()).thenReturn(mAllConverters);
        mPresenter.loadConverters();
    }

    @Test
    public void pressHomeBtnShowSettings() {
        mPresenter.handleHomePressed();

        verify(mView).showPreviousView();
    }

    @Test
    public void handleFabShowEditConverter() {
        mPresenter.handleFabPressed();

        mView.showEditConverter(null);
    }

    @Test
    public void loadConvertersReturnAll() {
        List<Pair<String, Boolean>> actual = mPresenter.loadConverters();

        verify(mRepository, times(2)).getCachedConvertersTypes();

        assertEquals(mAllConverters, actual);
    }

    @Test
    public void moveConverter() {
        List<Pair<String, Boolean>> beforeMove = new ArrayList<>(mAllConverters);

        // move down
        mPresenter.moveConverter(0, 3);

        // check result of moving
        assertEquals(mAllConverters.get(0), beforeMove.get(1));
        assertEquals(mAllConverters.get(3), beforeMove.get(0));

        beforeMove = new ArrayList<>(mAllConverters);

        // move up
        mPresenter.moveConverter(2, 1);

        // check result of moving
        assertEquals(mAllConverters.get(1), beforeMove.get(2));
        assertEquals(mAllConverters.get(2), beforeMove.get(1));
    }

    @Test
    public void deleteConverter() {
        mPresenter.deleteConverter(0);

        ArgumentCaptor<DialogInterface.OnClickListener> onClickListenerArgumentCaptor =
                ArgumentCaptor.forClass(DialogInterface.OnClickListener.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        verify(mView).showAskDialog(stringArgumentCaptor.capture(), onClickListenerArgumentCaptor.capture());
        assertEquals(mAllConverters.get(0).first, stringArgumentCaptor.getValue());

        List<Pair<String, Boolean>> beforeDelete = new ArrayList<>(mAllConverters);

        // click delete btn
        onClickListenerArgumentCaptor.getValue().onClick(any(DialogInterface.class), AlertDialog.BUTTON_POSITIVE);

        verify(mView).notifyConverterRemoved(0);

        // check result
        assertTrue(mAllConverters.size() == 3);
        assertEquals(mAllConverters.get(0), beforeDelete.get(1));

        mPresenter.deleteConverter(0);

        verify(mView, times(2)).showAskDialog(stringArgumentCaptor.capture(), onClickListenerArgumentCaptor.capture());

        // click cancel btn
        onClickListenerArgumentCaptor.getValue().onClick(any(DialogInterface.class), AlertDialog.BUTTON_NEGATIVE);

        verify(mView).notifyConverterCancelRemove(0);

        assertTrue(mAllConverters.size() == 3);
    }

    @Test
    public void enableConverter() {
        mPresenter.enableConverter(2, true);
        assertTrue(mAllConverters.get(2).second);

        mPresenter.enableConverter(2, false);
        assertFalse(mAllConverters.get(2).second);
    }

}