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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InMemoryConvertersRepositoryTest {

    private InMemoryConvertersRepository mRepository;

    @Mock private ConvertersServiceApi mServiceApi;

    @Mock private Converter mConverter;

    @Mock private CurrencyConverter mCurrencyCon;

    @Mock private ConvertersRepository.LoadEnabledConvertersTypesCallback mLoadEnabledCallback;

    @Mock private ConvertersRepository.LoadAllConvertersTypesCallback mLoadAllCallback;

    @Mock private ConvertersRepository.GetConverterCallback mGetConverterCallback;

    @Captor private ArgumentCaptor<ConvertersServiceApi.LoadConvertersCallback> mGetConvertersCaptor;

    @Captor private ArgumentCaptor<ConvertersServiceApi.LoadConverterCallback> mGetConverterCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mRepository = new InMemoryConvertersRepository(mServiceApi);
    }

    @Test
    public void getEnabledConvertersTypes_RepositoryCachesAfterFirstApiCall() {
        // first call to the repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getAllConvertersTypes(mGetConvertersCaptor.capture());
        mGetConvertersCaptor.getValue().onLoaded(new ArrayList<Pair<String, Boolean>>(), "");

        // second call to repository
        mRepository.getEnabledConvertersTypes(mLoadEnabledCallback);

        // check Api was called once
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.LoadConvertersCallback.class));
    }

    @Test
    public void getAllConverterTypes_RepositoryCachesAfterFirstApiCall() {
        // first call to the repository
        mRepository.getAllConverterTypes(mLoadAllCallback);

        // verify that service api was called enables repository caching
        verify(mServiceApi).getAllConvertersTypes(mGetConvertersCaptor.capture());
        mGetConvertersCaptor.getValue().onLoaded(new ArrayList<Pair<String, Boolean>>(), "");

        // second call to repository
        mRepository.getAllConverterTypes(mLoadAllCallback);

        // check Api was called once
        verify(mServiceApi).getAllConvertersTypes(any(ConvertersServiceApi.LoadConvertersCallback.class));
    }

    @Test
    public void getConverter_RepositoryCachesAfterFirstApiCall() {
        String name = "Test";

        //first call to the repository
        mRepository.mLastConverterName = name;
        mRepository.getConverter(name, false, mGetConverterCallback);

        verify(mServiceApi).cancelUpdateRequest();

        // verify that service api was called enables repository caching
        verify(mServiceApi).getConverter(eq(name), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onLoaded(new Converter(name, new ArrayList<Converter.Unit>()));

        // second call to repository
        mRepository.getConverter(name, false, mGetConverterCallback);

        // check Api was called once
        verify(mServiceApi).getConverter(eq(name), any(ConvertersServiceApi.LoadConverterCallback.class));
    }

    @Test
    public void getConverter_NameChanging() {
        String[] names = {"Test0", "Test1"};

        // retrieve first converter
        mRepository.mLastConverterName = names[0];
        mRepository.getConverter(names[0], false, mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(names[0]), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onLoaded(new Converter(names[0], new ArrayList<Converter.Unit>()));

        String startName = mRepository.mLastConverterName;

        // retrieve second converter
        mRepository.getConverter(names[1], false, mGetConverterCallback);
        verify(mServiceApi).getConverter(eq(names[1]), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onLoaded(new Converter(names[1], new ArrayList<Converter.Unit>()));

        assertNotEquals(startName, mRepository.mLastConverterName);

        startName = mRepository.mLastConverterName;

        // both converters already caching

        // retrieve first converter again
        mRepository.getConverter(names[0], false, mGetConverterCallback);

        assertNotEquals(startName, mRepository.mLastConverterName);
    }

    @Test
    public void getConverter_CallSetLastConverter() {
        mRepository.mLastConverterName = "Test";

        mRepository.getConverter("Test1", false, mGetConverterCallback);

        // set last converter if last converter name and new name are not equals
        // and not need clone of converter, so in this case a user select the converter
        verify(mServiceApi).setLastConverter("Test1");

        assertEquals("Test1", mRepository.mLastConverterName);
    }

    @Test
    public void getEnabledConvertersTypes_ReturnOnlyEnabled() {
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

        verify(mServiceApi).getAllConvertersTypes(mGetConvertersCaptor.capture());
        mGetConvertersCaptor.getValue().onLoaded(allConverters, "Third");

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

        ArgumentCaptor<ConvertersServiceApi.SaveCallback> saveCaptor =
                ArgumentCaptor.forClass(ConvertersServiceApi.SaveCallback.class);
        verify(mServiceApi).saveConverter(saveCaptor.capture(), eq(mConverter), eq(""));
        saveCaptor.getValue().onSaved(true);

        // check cache contains new converter
        assertEquals(mRepository.mCachedConvertersTypes.get(2), new Pair<>(converterName, true));
        assertEquals(mRepository.mCachedConverters.get(converterName).getName(), mConverter.getName());

        verify(saveConverterCallback).onConverterSaved(true);

        // edit existing converter
        converterName = "Edited name";
        when(mConverter.getName()).thenReturn(converterName);
        mRepository.saveConverter(saveConverterCallback, mConverter, "One"); // edit converter One

        verify(mServiceApi).saveConverter(saveCaptor.capture(), eq(mConverter), eq("One"));
        saveCaptor.getValue().onSaved(true);

        // check cache
        assertEquals(new Pair<>(converterName, true), mRepository.mCachedConvertersTypes.get(0));
        assertEquals(mConverter.getName(), mRepository.mCachedConverters.get(converterName).getName());
    }

    @Test
    public void saveLastUnit() {
        mRepository.mLastConverterName = "Test";
        mRepository.mCachedConverters = new HashMap<>();
        mRepository.mCachedConverters.put(mRepository.mLastConverterName, mConverter);
        when(mConverter.getLastUnitPosition()).thenReturn(0);

        mRepository.saveLastUnit();

        verify(mServiceApi).setLastUnit(mRepository.mLastConverterName, 0);
    }

    @Test
    public void saveLastQuantity() {
        mRepository.mLastConverterName = "Test";
        mRepository.mCachedConverters = new HashMap<>();
        mRepository.mCachedConverters.put(mRepository.mLastConverterName, mConverter);
        when(mConverter.getLastQuantity()).thenReturn("1");

        mRepository.saveLastQuantity();

        verify(mServiceApi).setLastQuantity(mRepository.mLastConverterName, "1");
    }

    @Test
    public void saveConvertersOrder_CallServiceApi() {
        List<Pair<String, Boolean>> types = cacheTypes();

        mRepository.saveConvertersOrder();

        verify(mServiceApi).writeConvertersOrder(types);
    }

    @Test
    public void saveConverterState_CallServiceApi() {
        List<Pair<String, Boolean>> types = cacheTypes();

        int index = 1;

        mRepository.saveConverterState(index);

        verify(mServiceApi).writeConverterState(types.get(index).first, types.get(index).second);
    }

    @Test
    public void saveConverterDeletion_CallServiceApi() {
        List<Pair<String, Boolean>> types = cacheTypes();

        int index = 0;
        mRepository.saveConverterDeletion(0);

        verify(mServiceApi).deleteConverter(types.get(index).first);
    }

    @Test
    public void getConverterReceiveError_CallReportError() {
        // should be initialized at this point
        mRepository.mLastConverterName = "Test";

        mRepository.getConverter("Test1", false, mGetConverterCallback);

        verify(mServiceApi).getConverter(anyString(), mGetConverterCaptor.capture());
        mGetConverterCaptor.getValue().onError("error message");

        verify(mGetConverterCallback).reportError("error message");
    }

    @Test
    public void updateCoursesSuccess_CallOnConverterLoaded() {
        mRepository.mLastConverterName = "Test";
        mRepository.mCachedConverters = new HashMap<>();
        mRepository.mCachedConverters.put(mRepository.mLastConverterName, mCurrencyCon);

        mRepository.updateCourses(mGetConverterCallback);

        verify(mServiceApi).updateCourses(mGetConverterCaptor.capture(), any(CurrencyConverter.class));
        mGetConverterCaptor.getValue().onLoaded(mCurrencyCon);

        verify(mGetConverterCallback).onConverterLoaded(mCurrencyCon);
    }

    @Test
    public void updateCoursesSuccess_CacheCurrencyConverter() {
        // first call
        mRepository.updateCourses(mGetConverterCallback);

        CurrencyConverter nullCon = null;
        verify(mServiceApi).updateCourses(mGetConverterCaptor.capture(), eq(nullCon));
        // caching happens at this point
        mGetConverterCaptor.getValue().onLoaded(mCurrencyCon);

        // second call
        mRepository.updateCourses(mGetConverterCallback);

        // give already created currency converter to the service api for courses update
        verify(mServiceApi).updateCourses(mGetConverterCaptor.capture(), eq(mCurrencyCon));
        mGetConverterCaptor.getValue().onLoaded(mCurrencyCon);
    }

    @Test
    public void updateCoursesError_CallReportError() {
        mRepository.mLastConverterName = "Test";
        mRepository.updateCourses(mGetConverterCallback);

        // no currency converter in cache so simply pass null
        CurrencyConverter nullCurCon = null;
        verify(mServiceApi).updateCourses(mGetConverterCaptor.capture(), eq(nullCurCon));
        mGetConverterCaptor.getValue().onError("Error message");

        verify(mGetConverterCallback).reportError("Error message");
    }

    private List<Pair<String, Boolean>> cacheTypes() {
        mRepository.getAllConverterTypes(mLoadAllCallback);
        List<Pair<String, Boolean>> types = Arrays.asList(new Pair<>("One", true), new Pair<>("Two", false));
        verify(mServiceApi).getAllConvertersTypes(mGetConvertersCaptor.capture());
        mGetConvertersCaptor.getValue().onLoaded(types, "");
        return types;
    }
}