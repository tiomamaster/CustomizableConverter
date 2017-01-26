package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.name;
import static android.R.attr.transitionName;
import static android.R.attr.value;
import static android.os.Build.VERSION_CODES.N;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Artyom on 23.01.2017.
 */
@RunWith(Parameterized.class)
public class EditConverterPresenterTest {

    @Mock
    private SettingsContract.EditConverterView mView;

    @Mock
    private ConvertersRepository mRepository;

    private EditConverterPresenter mPresenter;

    private String mInitialConverterName;

    private static final String NEW_CONVERTER = "";
    private static final String OLD_CONVERTER = "Test";

    private List<Converter.Unit> mUnits = new ArrayList<>(
            Arrays.asList(new Converter.Unit("Unit0", 1d, true),
                    new Converter.Unit("Unit1", 2d, true),
                    new Converter.Unit("Unit2", 3d, false),
                    new Converter.Unit("Unit3", 4d, true)));

    private Converter mCurConverter = new Converter(OLD_CONVERTER, mUnits);


    @Parameterized.Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(NEW_CONVERTER, OLD_CONVERTER);
    }

    public EditConverterPresenterTest(String initialConverterName) {
        mInitialConverterName = initialConverterName;
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // mock fields
        List<Pair<String, Boolean>> mAllConverters = new ArrayList<>(Arrays.asList(new Pair<>("one", true),
                new Pair<>("two", false), new Pair<>("three", false), new Pair<>("four", true)));
        when(mRepository.getCachedConvertersTypes()).thenReturn(mAllConverters);

        mPresenter = new EditConverterPresenter(mRepository, mView, mInitialConverterName);

        loadUnits();
    }

    @Test
    public void handleHomePressed() {
        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            // add converter name and two units to enable save
            mPresenter.setConverterName("New");

            addNewUnit("One", "1");

            addNewUnit("Two", "2");

            mPresenter.handleHomePressed();

            verify(mView).showAskDialog();
        } else {
            // no need to show dialog if no changes are done
            mPresenter.handleHomePressed();

            verify(mView).showPreviousView();

            mPresenter.enableUnit(2, true);
            mPresenter.handleHomePressed();

            verify(mView).showAskDialog();
        }
    }

    @Test
    public void handleFabPressed() {
        mPresenter.handleFabPressed();
        verify(mView).showEditUnit(null, null);
    }

    @Test
    public void getConverterName() {
        String actual = mPresenter.getConverterName();
        if (mInitialConverterName.equals(NEW_CONVERTER)) assertEquals(NEW_CONVERTER, actual);
        else assertEquals(OLD_CONVERTER, actual);
    }

    @Test
    public void setEmptyConverterName() {
        mPresenter.setConverterName("");
        verify(mView).showConverterExistError(false);
        verify(mView).enableSaveConverter(false);
    }

    @Test
    public void setConverterNameNotExist() {
        String name = "New Name";
        mPresenter.setConverterName(name);

        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            verify(mView).showConverterExistError(false);
            verify(mView).enableSaveConverter(false);
        } else {
            verify(mView).showConverterExistError(false);
            assertEquals(name, mCurConverter.getName());
            verify(mView).enableSaveConverter(true);
        }
    }

    @Test
    public void setConverterNameExist() {
        String name = "one";
        mPresenter.setConverterName(name);

        verify(mView).showConverterExistError(true);
        verify(mView).enableSaveConverter(false);
    }

    @Test
    public void moveUnit() {
        if (mInitialConverterName.equals(OLD_CONVERTER)) {
            List<Converter.Unit> beforeMove = new ArrayList<>(mUnits);

            mPresenter.moveUnit(1, 3);

            assertEquals(mUnits.get(1), beforeMove.get(2));
            assertEquals(mUnits.get(3), beforeMove.get(1));

            verify(mView).enableSaveConverter(true);
        }
    }

    @Test
    public void deleteUnit() throws Exception {
        if (mInitialConverterName.equals(OLD_CONVERTER)) {
            mPresenter.deleteUnit(1);

            verify(mView).notifyUnitRemoved(1);
            verify(mView).enableSaveConverter(true);

            assertTrue(mUnits.size() == 3);
            assertTrue(mCurConverter.getLastUnitPosition() == 0);

            mPresenter.deleteUnit(0);

            // disable save converter when not enough enabled units
            verify(mView).notifyUnitRemoved(0);
            verify(mView).enableSaveConverter(false);

            mPresenter.deleteUnit(1);

            // show warning when attempt to delete from last two units
            verify(mView).showWarning(1);
        }
    }

    @Test
    public void enableUnit() {
        if (mInitialConverterName.equals(OLD_CONVERTER)) {
            mPresenter.enableUnit(0, false);

            verify(mView).enableSaveConverter(true);

            mPresenter.enableUnit(1, false);

            // disable save converter when not enough enabled units
            verify(mView).enableSaveConverter(false);
        }
    }

    @Test
    public void editUnit() {
        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            // crete new unit
            mPresenter.handleFabPressed();

            mPresenter.setUnitName("Unit");

            verify(mView).showUnitExistError(false);
            verify(mView).enableSaveUnit(false);

            mPresenter.setUnitValue("1");

            verify(mView).enableSaveUnit(true);
        } else {
            String name = "Unit0";
            // edit existing unit
            mPresenter.editUnit(name, "1");

            verify(mView).showEditUnit(name, "1");

            mPresenter.setUnitName("Unit1");

            // unit exist, so show error
            verify(mView).showUnitExistError(true);

            mPresenter.setUnitValue("2");

            verify(mView).enableSaveUnit(false);

            // set initial name
            mPresenter.setUnitName(name);

            // so, edit existing unit
            verify(mView).showUnitExistError(false);
            verify(mView).enableSaveUnit(true);
        }
    }

    @Test
    public void saveUnit() {
        String[] names = {"Name0", "Name1"};
        String[] values = {"1", "2"};
        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            // add new unit
            addNewUnit(names[0], values[0]);

            verify(mView).onUnitEdited(0);

            // add one more unit
            addNewUnit(names[1], values[1]);

            verify(mView).onUnitEdited(1);
            verify(mView).showHint(false);
        } else {
            // edit unit name and value
            mPresenter.editUnit(mUnits.get(0).name, String.valueOf(mUnits.get(0).value));
            mPresenter.setUnitName(names[0]);
            mPresenter.setUnitValue(values[1]);
            mPresenter.saveUnit();

            assertEquals(names[0], mUnits.get(0).name);
            assertEquals(Double.parseDouble(values[1]), mUnits.get(0).value, 0);

            verify(mView).onUnitEdited(0);

            // edit unit value
            mPresenter.editUnit(mUnits.get(1).name, String.valueOf(mUnits.get(1).value));
            mPresenter.setUnitValue(values[1]);
            mPresenter.saveUnit();

            assertEquals(Double.parseDouble(values[1]), mUnits.get(1).value, 0);

            verify(mView).onUnitEdited(1);
        }
    }

    @Test
    public void saveConverter() {
        mPresenter.saveConverter(true);

        verify(mView).enableSaveConverter(false);
        verify(mView).setConverterSavingIndicator(true);

        ArgumentCaptor<Converter> converterCaptor = ArgumentCaptor.forClass(Converter.class);
        ArgumentCaptor<ConvertersRepository.SaveConverterCallback> callbackCaptor =
                ArgumentCaptor.forClass(ConvertersRepository.SaveConverterCallback.class);

        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            verify(mRepository).saveConverter(callbackCaptor.capture(), converterCaptor.capture(),
                    eq(NEW_CONVERTER));
            callbackCaptor.getValue().onConverterSaved(true);
            verify(mView).setConverterSavingIndicator(false);
            verify(mView).showPreviousView();
            assertEquals(NEW_CONVERTER, converterCaptor.getValue().getName());
        } else {
            verify(mRepository).saveConverter(callbackCaptor.capture(), converterCaptor.capture(),
                    eq(OLD_CONVERTER));
            callbackCaptor.getValue().onConverterSaved(true);
            verify(mView).setConverterSavingIndicator(false);
            verify(mView).showPreviousView();
            assertEquals(OLD_CONVERTER, converterCaptor.getValue().getName());
        }
    }

    private void loadUnits() {
        mPresenter.loadUnits();

        if (mInitialConverterName.equals(NEW_CONVERTER)) {
            verify(mView).showHint(true);
            verify(mView).showUnits(anyList());
        } else {
            ArgumentCaptor<ConvertersRepository.GetConverterCallback> captor =
                    ArgumentCaptor.forClass(ConvertersRepository.GetConverterCallback.class);
            verify(mRepository).getConverter(anyString(), captor.capture());
            captor.getValue().onConverterLoaded(mCurConverter);
        }
    }

    private void addNewUnit(String name, String value) {
        mPresenter.handleFabPressed();
        mPresenter.setUnitName(name);
        mPresenter.setUnitValue(value);
        mPresenter.saveUnit();
    }
}