package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 07.12.2016.
 */
class EditConverterPresenter implements SettingsContract.EditConverterUal {

    @NonNull
    private ConvertersRepository mConvertersRepo;

    @NonNull
    private SettingsContract.EditConverterView mView;

    private String mInitialConverterName;

    private String mCurConverterName;

    private Converter mCurConverter;

    private List<Converter.Unit> mUnits;

    private Set<String> mUnitNames;

    private Set<String> mConverterNames;

    private boolean isNewConverter;

    private boolean isNewUnit;

    private String mInitialUnitName;

    private String mCurUnitName;

    private String mCurUnitValue;

    private int mEnabledUnits;

    private boolean mGoodConverterName;

    private boolean mHaveUnsavedChanges;

    EditConverterPresenter(@NonNull ConvertersRepository convertersRepository,
                           @NonNull SettingsContract.EditConverterView editConverterView,
                           @Nullable String initialConverterName) {
        mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        mView = checkNotNull(editConverterView, "editConverterView cannot be null");
        mCurConverterName = mInitialConverterName = initialConverterName;

        if (initialConverterName == null || initialConverterName.isEmpty()) isNewConverter = true;
        else mGoodConverterName = true;

        // create converter lower case names set
        mConverterNames = createLowerCaseConverterNames();

        mView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        if (mHaveUnsavedChanges) mView.showAskDialog();
        else mView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {
        // show dialog for creation new unit with empty field
        mView.showEditUnit(null, null);

        mInitialUnitName = mCurUnitName = mCurUnitValue = "";

        isNewUnit = true;
    }

    @Nullable
    @Override
    public String getConverterName() {
        return mCurConverterName;
    }

    @Override
    public void setConverterName(@NonNull String newName) {
        checkNotNull(newName);

        if (newName.isEmpty()) {
            mView.showConverterExistError(false);
            mView.enableSaveConverter(false);
            mGoodConverterName = false;
            checkCanSave();
            return;
        }

        mCurConverterName = newName;

        if (isNewConverter) {
            if (mConverterNames.contains(newName.toLowerCase())) {
                converterExist();
            } else {
                mView.showConverterExistError(false);
                mGoodConverterName = true;
                checkCanSave();
            }
        } else {
            if (!mInitialConverterName.toLowerCase().equals(newName.toLowerCase())
                    && mConverterNames.contains(newName.toLowerCase())) {
                converterExist();
            } else {
                mView.showConverterExistError(false);

                // update existing converter name
                mCurConverter.setName(mCurConverterName);

                mGoodConverterName = true;
                checkCanSave();
            }
        }
    }

    @Override
    public void loadUnits() {
        if (isNewConverter) {
            if (mUnits == null) {
                // when create new converter, produce empty units list
                mUnits = new ArrayList<>();

                // create empty unit lower case names set
                mUnitNames = new HashSet<>();
            }

            if (mUnits.size() < 2) {
                mView.showHint(true);
            }
            mView.showUnits(mUnits);
            return;
        }

        mView.setUnitsLoadingIndicator(true);

        mConvertersRepo.getConverter(mInitialConverterName, true, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                checkNotNull(converter);

                mCurConverter = converter;

                mUnits = mCurConverter.getUnits();

                // create unit lower case names set
                mUnitNames = createLowerCaseUnitNames();

                mEnabledUnits = mCurConverter.getEnabledUnitsName().size();

                mView.showUnits(mUnits);
                mView.setUnitsLoadingIndicator(false);
            }
        });
    }

    @Override
    public void moveUnit(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mUnits, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mUnits, i, i - 1);
            }
        }
        if (mCurConverter != null) mCurConverter.setLastUnitPosition(0);
        checkCanSave();
    }

    @Override
    public void deleteUnit(int position) {
        if (mUnits.size() > 2) {
            mUnits.remove(position);
            mView.notifyUnitRemoved(position);

            // reset saved position to avoid index out of bounds
            // may by null when create new converter
            if (mCurConverter != null) mCurConverter.setLastUnitPosition(0);
            mEnabledUnits--;
        } else {
            mView.showWarning(position);
        }
        checkCanSave();
    }

    @Override
    public void enableUnit(int orderPosition, boolean enable) {
        if (mUnits.get(orderPosition).isEnabled != enable) {
            mUnits.get(orderPosition).isEnabled = enable;
            if (enable) mEnabledUnits++;
            else mEnabledUnits--;

            if (mCurConverter != null) mCurConverter.setLastUnitPosition(0);
            checkCanSave();
        }
    }

    @Override
    public void editUnit(@NonNull String name, @NonNull String value) {
        checkNotNull(name);
        checkNotNull(value);

        mView.showEditUnit(name, value);

        mInitialUnitName = mCurUnitName = name;

        mCurUnitValue = value;

        isNewUnit = false;
    }

    @Override
    public void setUnitName(@NonNull String newName) {
        checkNotNull(newName);

        mCurUnitName = newName;

        if (!mInitialUnitName.toLowerCase().equals(newName.toLowerCase())
                && mUnitNames.contains(newName.toLowerCase())) {
            mView.showUnitExistError(true);
            return;
        } else mView.showUnitExistError(false);

        if (!mCurUnitValue.isEmpty() && !mCurUnitName.isEmpty()) mView.enableSaveUnit(true);
        else mView.enableSaveUnit(false);
    }

    @Override
    public void setUnitValue(@NonNull String newValue) {
        checkNotNull(newValue);

        mCurUnitValue = newValue;

        if (!mCurUnitValue.isEmpty() && !mUnitNames.contains(mCurUnitName.toLowerCase()) ||
                mInitialUnitName.toLowerCase().equals(mCurUnitName.toLowerCase()))
            mView.enableSaveUnit(true);
        else mView.enableSaveUnit(false);
    }

    @Override
    public void saveUnit() {
        if (isNewUnit) {
            // create new unit and add it to the units list
            mUnits.add(new Converter.Unit(mCurUnitName, Double.valueOf(mCurUnitValue), true));

            mUnitNames.add(mCurUnitName);

            mView.onUnitEdited(mUnits.size() - 1);
            if (mUnits.size() > 1) {
                mView.showHint(false);
            }

            mEnabledUnits++;
            checkCanSave();
        } else {
            if (!mInitialUnitName.equals(mCurUnitName)) {
                // update existing unit with new name and value
                int index = mUnits.indexOf(new Converter.Unit(mInitialUnitName, 0d, true));
                mUnits.get(index).name = mCurUnitName;
                mUnits.get(index).value = Double.valueOf(mCurUnitValue);

                // add new unit name to the set and remove old name
                mUnitNames.add(mCurUnitName.toLowerCase());
                mUnitNames.remove(mInitialUnitName);

                mView.onUnitEdited(index);

                checkCanSave();
            } else {
                // update value of existing unit with new one
                int index = mUnits.indexOf(new Converter.Unit(mCurUnitName, 0d, true));
                mUnits.get(index).value = Double.valueOf(mCurUnitValue);

                mView.onUnitEdited(index);

                checkCanSave();
            }
        }
    }

    @Override
    public void saveConverter(final boolean closeEditor) {
        mView.enableSaveConverter(false);
        mView.setConverterSavingIndicator(true);

        if (isNewConverter) mCurConverter = new Converter(mCurConverterName, mUnits);

        mConvertersRepo.saveConverter(new ConvertersRepository.SaveConverterCallback() {
            @Override
            public void onConverterSaved(boolean saved) {
                mView.setConverterSavingIndicator(false);
                if (closeEditor) mView.showPreviousView();
                mInitialConverterName = mCurConverterName;
                mHaveUnsavedChanges = false;
            }
        }, mCurConverter, mInitialConverterName);
    }

    private Set<String> createLowerCaseUnitNames() {
        Set<String> names = new HashSet<>();
        for (Converter.Unit mUnit : mUnits) {
            names.add(mUnit.name.toLowerCase());
        }
        return names;
    }

    private Set<String> createLowerCaseConverterNames() {
        Set<String> names = new HashSet<>();
        for (Pair<String, Boolean> pair : mConvertersRepo.getCachedConvertersTypes()) {
            names.add(pair.first.toLowerCase());
        }
        return names;
    }

    private void checkCanSave() {
        if (mEnabledUnits > 1 && mGoodConverterName) {
            mView.enableSaveConverter(true);

            mHaveUnsavedChanges = true;
        } else {
            mView.enableSaveConverter(false);

            mHaveUnsavedChanges = false;
        }
    }

    private void converterExist() {
        mView.showConverterExistError(true);
        mView.enableSaveConverter(false);
        mGoodConverterName = false;
    }
}