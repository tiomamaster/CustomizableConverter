package com.tiomamaster.customizableconverter.data;

import android.content.Context;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by Artyom on 16.07.2016.
 */
public class InMemoryConvertersRepositoryTest {

    private static final String[] CONVERTERS_TYPES = {"Test0", "Test1"};

    private InMemoryConvertersRepository mRepository;

    @Mock
    private ConvertersServiceApi mServiceApi;

    @Mock
    private Converter mConverter;

    @Mock
    private ConvertersRepository.LoadConvertersTypesCallback mLoadConvertersTypesCallback;

    @Mock
    private ConvertersRepository.GetConverterCallback mGetConverterCallback;

    @Captor
    private ArgumentCaptor<ConvertersServiceApi.ConverterServiceCallback> mCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mRepository = new InMemoryConvertersRepository(mServiceApi);
    }

    @Test
    public void getConvertersTypes_repositoryCachesAfterFirstApiCall() {
        // first call to the repository
        mRepository.getConvertersTypes(mLoadConvertersTypesCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getAllConvertersTypes(mCaptor.capture());
        mCaptor.getValue().onLoaded(CONVERTERS_TYPES);

        // second call to repository
        mRepository.getConvertersTypes(mLoadConvertersTypesCallback);

        // check Api was called once
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getConverter_repositoryCachesAfterFirstApiCall() {
        //first call to the
        mRepository.getConverter(CONVERTERS_TYPES[0], mGetConverterCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getConverter(eq(CONVERTERS_TYPES[0]), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(CONVERTERS_TYPES[0], new LinkedHashMap<String, Double>()));

        // second call to repository
        mRepository.getConverter(CONVERTERS_TYPES[0], mGetConverterCallback);

        // check Api was called once
        verify(mServiceApi).getConverter(eq(CONVERTERS_TYPES[0]), any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getLastConverter_AlwaysCallOnce() {
        // several calls
        mRepository.getConvertersTypes(mLoadConvertersTypesCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getLastConverter(mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(CONVERTERS_TYPES[0], new LinkedHashMap<String, Double>()));

        // verify that converter cached after first call
        assertNotNull(mRepository.mCachedConverters);
        assertNotNull(mRepository.mCachedConverters.get(CONVERTERS_TYPES[0]));

        mRepository.getConvertersTypes(mLoadConvertersTypesCallback);
        mRepository.getConvertersTypes(mLoadConvertersTypesCallback);

        // check Api was called once
        verify(mServiceApi).getLastConverter(any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void lastPositionChanging() {
        // retrieve first converter
        mRepository.getConverter(CONVERTERS_TYPES[0], mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(CONVERTERS_TYPES[0]), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(CONVERTERS_TYPES[0], new LinkedHashMap<String, Double>()));

        int startPos = mRepository.mLastPos;

        // retrieve first converter
        mRepository.getConverter(CONVERTERS_TYPES[1], mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(CONVERTERS_TYPES[1]), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(CONVERTERS_TYPES[1], new LinkedHashMap<String, Double>()));

        assertNotEquals(startPos, mRepository.mLastPos);

        startPos = mRepository.mLastPos;

        // both converter already caching

        // retrieve first converter again
        mRepository.getConverter(CONVERTERS_TYPES[0], mGetConverterCallback);

        assertNotEquals(startPos, mRepository.mLastPos);
    }
}
