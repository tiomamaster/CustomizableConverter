package com.tiomamaster.customizableconverter.data;

import android.support.v4.util.Pair;

import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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

    @Captor private ArgumentCaptor<ConvertersServiceApi.LoadCallback> mCaptor;

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
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.LoadCallback.class));
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
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.LoadCallback.class));
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
        verify(mServiceApi).getConverter(eq(name), any(ConvertersServiceApi.LoadCallback.class));
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
        verify(mServiceApi).getLastConverter(any(ConvertersServiceApi.LoadCallback.class));
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

        // both converters already caching

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

    @Test
    public void saveConverter() {
        String[] names = {"One", "Two"};

        mRepository.mCachedConvertersTypes = new ArrayList<>(2);
        mRepository.mCachedConvertersTypes.addAll(Arrays.asList(new Pair<>(names[0], true),
                new Pair<>(names[1], true)));

        mRepository.mCachedConverters = new HashMap<>(2);
        mRepository.mCachedConverters.put(names[0],
                new Converter(names[0], new ArrayList<Converter.Unit>()));
        mRepository.mCachedConverters.put(names[1],
                new Converter(names[1], new ArrayList<Converter.Unit>()));

        ConvertersRepository.SaveConverterCallback saveConverterCallback =
                Mockito.mock(ConvertersRepository.SaveConverterCallback.class);
        // crete new converter
        String converterName = "Three";
        when(mConverter.getName()).thenReturn(converterName);
        mRepository.saveConverter(saveConverterCallback, mConverter, "");

        // check cache contains new converter
        assertEquals(mRepository.mCachedConvertersTypes.get(2), new Pair<>(converterName, true));
        assertEquals(mRepository.mCachedConverters.get(converterName).getName(), mConverter.getName());

        ArgumentCaptor<ConvertersServiceApi.SaveCallback> saveCaptor =
                ArgumentCaptor.forClass(ConvertersServiceApi.SaveCallback.class);
        verify(mServiceApi).saveConverter(saveCaptor.capture(), eq(mConverter), eq(""));

        saveCaptor.getValue().onSaved(true);

        verify(saveConverterCallback).onConverterSaved(true);

        // edit existing converter
        converterName = "Edited name";
        when(mConverter.getName()).thenReturn(converterName);
        mRepository.saveConverter(saveConverterCallback, mConverter, "One"); // edit converter One

        // check cache
        assertEquals(mRepository.mCachedConvertersTypes.get(0), new Pair<>(converterName, true));
        assertEquals(mRepository.mCachedConverters.get(converterName).getName(), mConverter.getName());
    }
}