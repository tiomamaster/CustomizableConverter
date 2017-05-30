package com.tiomamaster.customizableconverter.converter;

import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.TemperatureConverter;

import org.hamcrest.Matchers;
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
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.booleanThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConverterPresenterTest {

    private static final List<String> CONVERTERS_TYPES = Arrays.asList("Test0", "Test1",
            "Temperature", "Температура");

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
    public void loadConvertersTypes_ShowItInView() {
        mPresenter.loadConvertersTypes();

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getEnabledConvertersTypes(mLoadConvertersTypesCaptor.capture());
        mLoadConvertersTypesCaptor.getValue().onConvertersTypesLoaded(CONVERTERS_TYPES, anyInt());

        verify(mView).setProgressIndicator(false);

        verify(mView).showConvertersTypes(eq(CONVERTERS_TYPES), anyInt());
    }

    @Test
    public void loadConvertersTypes_ShowNothing() {
        mPresenter.loadConvertersTypes();

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getEnabledConvertersTypes(mLoadConvertersTypesCaptor.capture());
        mLoadConvertersTypesCaptor.getValue().onConvertersTypesLoaded(new ArrayList<String>(), anyInt());

        verify(mView).showNoting();
    }

    @Test
    public void loadConverter_ShowItInView() {
        String name = CONVERTERS_TYPES.get(0);
        mPresenter.loadConverter(name);

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getConverter(eq(name), anyBoolean(), mGetConverterCaptor.capture());
        Converter converter = new Converter(name, new ArrayList<Converter.Unit>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);

        verify(mView).setProgressIndicator(false);
        verify(mView).showConverter(anyList(), anyInt(), anyString(), eq(false));
        assertEquals(name, converter.getName());

        // load temperature converter in en and ru locales
        name = CONVERTERS_TYPES.get(2);
        mPresenter.loadConverter(name);
        verify(mRepository).getConverter(eq(name), anyBoolean(), mGetConverterCaptor.capture());
        converter = new TemperatureConverter(name, new ArrayList<Converter.Unit>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);

        name = CONVERTERS_TYPES.get(3);
        mPresenter.loadConverter(name);
        verify(mRepository).getConverter(eq(name), anyBoolean(), mGetConverterCaptor.capture());
        converter = new TemperatureConverter(name, new ArrayList<Converter.Unit>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);
        verify(mView, times(2)).showConverter(anyList(), anyInt(), anyString(), eq(true));
    }

    @Test
    public void callConvert_ShowResult() {
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

        mPresenter.convert(from, ".");
        verify(mCurConverter).convertAll(0, from);
    }

    @Test
    public void callConvert_ShowEmptyResult() {
        mPresenter.convert("Test", "");

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(mView).showConversionResult(listCaptor.capture());
        assertTrue(listCaptor.getValue().isEmpty());
    }

    @Test
    public void saveLastUnitPos_CallCurConverterAndRepo() {
        loadConverter();

        mPresenter.saveLastUnitPos(1);

        ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.TYPE);

        verify(mCurConverter).setLastUnitPosition(captor.capture());
        assertEquals(1, captor.getValue().intValue());

        verify(mRepository).saveLastUnit();
    }

    @Test
    public void saveLastQuantity_CallCurConverterAndRepo() {
        loadConverter();

        String quantity = "100";

        mPresenter.saveLastQuantity(quantity);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(mCurConverter).setLastQuantity(captor.capture());

        assertEquals(quantity, captor.getValue());

        verify(mRepository).saveLastQuantity();
    }

    @Test
    public void openSettings_ShowSettingsView() {
        mPresenter.openSettings();

        verify(mView).showSettingsUi();
    }

    private void loadConverter() {
        mPresenter.loadConverter(anyString());

        verify(mRepository).getConverter(anyString(), anyBoolean(), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onConverterLoaded(mCurConverter);
    }
}