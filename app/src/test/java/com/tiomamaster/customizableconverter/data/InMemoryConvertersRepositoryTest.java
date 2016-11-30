package com.tiomamaster.customizableconverter.data;

import android.support.v4.util.Pair;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by Artyom on 16.07.2016.
 */
public class InMemoryConvertersRepositoryTest {

    private InMemoryConvertersRepository mRepository;

    @Mock private ConvertersServiceApi mServiceApi;

    @Mock private Converter mConverter;

    @Mock private ConvertersRepository.LoadEnabledConvertersTypesCallback mLoadEnabledCallback;

    @Mock private ConvertersRepository.LoadAllConvertersTypesCallback mLoadAllCallback;

    @Mock private ConvertersRepository.GetConverterCallback mGetConverterCallback;

    @Captor private ArgumentCaptor<ConvertersServiceApi.ConverterServiceCallback> mCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mRepository = new InMemoryConvertersRepository(mServiceApi);
    }

    @Test
    public void getEnabledConvertersTypesRepositoryCachesAfterFirstApiCall() {
        // first call to the repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getAllConvertersTypes(mCaptor.capture());
        mCaptor.getValue().onLoaded(new ArrayList<Pair<String, Boolean>>());

        // second call to repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // check Api was called once
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getAllConverterTypesRepositoryCachesAfterFirstApiCall() {
        // first call to the repository
        mRepository.getAllConverterTypes(mLoadAllCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getAllConvertersTypes(mCaptor.capture());
        mCaptor.getValue().onLoaded(new ArrayList<Pair<String, Boolean>>());

        // second call to repository
        mRepository.getAllConverterTypes(mLoadAllCallback);

        // check Api was called once
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getConverterRepositoryCachesAfterFirstApiCall() {
        String name = "Test";

        //first call to the repository
        mRepository.getConverter(name, mGetConverterCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getConverter(eq(name), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(name, new ArrayList<Converter.Unit>()));

        // second call to repository
        mRepository.getConverter(name, mGetConverterCallback);

        // check Api was called once
        verify(mServiceApi).getConverter(eq(name), any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getLastConverterAlwaysCallOnce() {
        String name = "Test";

        //first call to the repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getLastConverter(mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(name, new ArrayList<Converter.Unit>()));

        // several calls to the repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // check Api was called once
        verify(mServiceApi).getLastConverter(any(ConvertersServiceApi.ConverterServiceCallback.class));
    }

    @Test
    public void getConverterNameChanging() {
        String[] names = {"Test0", "Test1"};

        // retrieve first converter
        mRepository.getConverter(names[0], mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(names[0]), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(names[0], new ArrayList<Converter.Unit>()));

        String startName = mRepository.mLastConverterName;

        // retrieve second converter
        mRepository.getConverter(names[1], mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(names[1]), mCaptor.capture());
        mCaptor.getValue().onLoaded(new Converter(names[1], new ArrayList<Converter.Unit>()));

        assertNotEquals(startName, mRepository.mLastConverterName);

        startName = mRepository.mLastConverterName;

        // both converter already caching

        // retrieve first converter again
        mRepository.getConverter(names[0], mGetConverterCallback);

        assertNotEquals(startName, mRepository.mLastConverterName);
    }

    @Test
    public void getEnabledConvertersTypesReturnOnlyEnabled() {
        // simulate last converter name
        mRepository.mLastConverterName = "Third";

        // create list of all converters
        List<Pair<String, Boolean>> allConverters = new ArrayList<>(4);
        Pair<String, Boolean> first = new Pair<>("First", true);
        Pair<String, Boolean> second = new Pair<>("Second", true);
        Pair<String, Boolean> third = new Pair<>("Third", true);
        Pair<String, Boolean> fourth = new Pair<>("Fourth", false);
        allConverters.add(first);
        allConverters.add(second);
        allConverters.add(third);
        allConverters.add(fourth);
        allConverters.add(third);
        allConverters.add(first);
        allConverters.add(fourth);

        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        verify(mServiceApi).getAllConvertersTypes(mCaptor.capture());
        mCaptor.getValue().onLoaded(allConverters);

        ArgumentCaptor<List> listCaptor = ArgumentCaptor.forClass(List.class);

        ArgumentCaptor<Integer> intCaptor = ArgumentCaptor.forClass(Integer.TYPE);

        verify(mLoadEnabledCallback).onConvertersTypesLoaded(listCaptor.capture(), intCaptor.capture());
        List<String> listResult = listCaptor.getValue();
        int intResult = intCaptor.getValue();

        // verify the result list of enabled converters
        assertEquals(3, listResult.size());
        assertEquals(listResult.get(0), first.first);
        assertEquals(listResult.get(1), second.first);
        assertEquals(listResult.get(2), third.first);
        assertEquals(2, intResult);
    }
}