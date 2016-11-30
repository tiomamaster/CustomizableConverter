package com.tiomamaster.customizableconverter.converter;

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
import java.util.LinkedHashMap;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by Artyom on 16.07.2016.
 */
public class ConverterPresenterTest {

    private static final List<String> CONVERTERS_TYPES = Arrays.asList("Test0", "Test1");

    @Mock private ConverterContract.View mView;

    @Mock private ConvertersRepository mRepository;

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
    public void loadConverterShowItView() {
        String name = CONVERTERS_TYPES.get(0);
        mPresenter.loadConverter(name);

        verify(mView).setProgressIndicator(true);

        verify(mRepository).getConverter(eq(name), mGetConverterCaptor.capture());
        Converter converter = new Converter(name, new ArrayList<Converter.Unit>());
        mGetConverterCaptor.getValue().onConverterLoaded(converter);

        verify(mView).setProgressIndicator(false);

        verify(mView).showConverter(converter);

        assertEquals(name, converter.getName());
    }

    @Test
    public void openSettingsShowSettingsView() {
        mPresenter.openSettings();

        verify(mView).showSettingsUi();
    }
}