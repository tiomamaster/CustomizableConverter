package com.tiomamaster.customizableconverter.converter;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConverterPresenterTest {

    private static final String[] CONVERTERS_TYPES = {"Test0", "Test1"};

    @Mock
    private ConverterContract.View mView;

    @Mock
    private ConvertersRepository mRepository;

    private ConverterPresenter mPresenter;

    @Captor
    private ArgumentCaptor<ConvertersRepository.LoadConvertersTypesCallback> mLoadConvertersTypesCaptor;

    @Captor
    private ArgumentCaptor<ConvertersRepository.GetConverterCallback> mGetConverterCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mPresenter = new ConverterPresenter(mRepository, mView);
    }

    @Test
    public void loadConvertersTypesAndShowInView() {
        mPresenter.loadConvertersTypes();

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getConvertersTypes(mLoadConvertersTypesCaptor.capture());
        mLoadConvertersTypesCaptor.getValue().onConvertersTypesLoaded(CONVERTERS_TYPES, anyInt());

        verify(mView).setProgressIndicator(false);

        verify(mView).showConvertersTypes(eq(CONVERTERS_TYPES), anyInt());
    }

    @Test
    public void loadConverterAndShowInView() {
        mPresenter.loadConverter(CONVERTERS_TYPES[0]);

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getConverter(eq(CONVERTERS_TYPES[0]), mGetConverterCaptor.capture());
        Converter converter = new Converter(CONVERTERS_TYPES[0], new LinkedHashMap<String, Double>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);

        verify(mView).setProgressIndicator(false);

        verify(mView).showConverter(converter);
    }

    @Test
    public void openSettingsTest() {
        mPresenter.openSettings();

        verify(mView).showSettingsUi();
    }
}