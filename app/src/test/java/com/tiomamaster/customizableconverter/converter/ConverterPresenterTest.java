package com.tiomamaster.customizableconverter.converter;

import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConverterPresenterTest {

    private static final List<String> CONVERTERS_TYPES = Arrays.asList("Test0", "Test1");

    @Mock private ConverterContract.View mView;

    @Mock private ConvertersRepository mRepository;

    @Mock private Converter mCurConverter;

    private ConverterPresenter mPresenter;

    @Captor private ArgumentCaptor<ConvertersRepository.LoadEnabledConvertersTypesCallback> mLoadConvertersTypesCaptor;

    @Captor private ArgumentCaptor<ConvertersRepository.GetConverterCallback> mGetConverterCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new ConverterPresenter(mRepository, mView);
    }

    @Test
    public void loadConvertersTypesShowItInView() {
        mPresenter.loadConvertersTypes();

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getEnabledConvertersTypes(mLoadConvertersTypesCaptor.capture());
        mLoadConvertersTypesCaptor.getValue().onConvertersTypesLoaded(CONVERTERS_TYPES, anyInt());

        verify(mView).setProgressIndicator(false);

        verify(mView).showConvertersTypes(eq(CONVERTERS_TYPES), anyInt());
    }

    @Test
    public void loadConvertersTypesShowNothing() {
        mPresenter.loadConvertersTypes();

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getEnabledConvertersTypes(mLoadConvertersTypesCaptor.capture());
        mLoadConvertersTypesCaptor.getValue().onConvertersTypesLoaded(new ArrayList<String>(), anyInt());

        verify(mView).showNoting();
    }

    @Test
    public void loadConverterShowItInView() {
        String name = CONVERTERS_TYPES.get(0);
        mPresenter.loadConverter(name);

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getConverter(eq(name), mGetConverterCaptor.capture());
        Converter converter = new Converter(name, new ArrayList<Converter.Unit>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);

        verify(mView).setProgressIndicator(false);

        verify(mView).showConverter(anyList(), anyInt(), anyString());

        assertEquals(name, converter.getName());
    }

    @Test
    public void callConvertShowResult() {
        String from = "Test";
        double quantity = 100;

        when(mCurConverter.convertAll(quantity, from)).thenReturn(new ArrayList<Pair<String, String>>());

        loadConverter();

        mPresenter.convert(from, String.valueOf(quantity));

        ArgumentCaptor<Double> doubleCaptor = ArgumentCaptor.forClass(Double.TYPE);
        ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

        verify(mCurConverter).convertAll(doubleCaptor.capture(), stringCaptor.capture());

        assertEquals(from, stringCaptor.getValue());
        assertEquals(quantity, doubleCaptor.getValue());

        verify(mView).showConversionResult(anyList());
    }

    @Test
    public void saveLastUnitPosCallCurConverter() {
        loadConverter();

        mPresenter.saveLastUnitPos(1);

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.TYPE);

        verify(mCurConverter).setLastUnitPosition(captor.capture());
        assertEquals(1, captor.getValue().intValue());
    }

    @Test
    public void saveLastQuantityCallCurConverter() {
        loadConverter();

        String quantity = "100";

        mPresenter.saveLastQuantity(quantity);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(mCurConverter).setLastQuantity(captor.capture());

        assertEquals(quantity, captor.getValue());
    }

    @Test
    public void openSettingsShowSettingsView() {
        mPresenter.openSettings();

        verify(mView).showSettingsUi();
    }

    private void loadConverter() {
        mPresenter.loadConverter(anyString());

        verify(mRepository).getConverter(anyString(), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onConverterLoaded(mCurConverter);
    }
}