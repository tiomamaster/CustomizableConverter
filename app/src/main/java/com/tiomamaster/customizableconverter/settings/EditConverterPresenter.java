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

    EditConverterPresenter(@NonNull ConvertersRepository convertersRepository,
                           @NonNull SettingsContract.EditConverterView editConverterView,
                           @Nullable String initialConverterName) {
        mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        mView = checkNotNull(editConverterView, "editConverterView cannot be null");
        mCurConverterName = mInitialConverterName = initialConverterName;

        if (initialConverterName == null) isNewConverter = true;

        // create converter lower case names set
        mConverterNames = createLowerCaseConverterNames();

        mView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        mView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {
        // show dialog for creation new unit withe empty field
        mView.showEditUnit(null, null);

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

        mCurConverterName = newName;

        if (isNewConverter) {
            if (mConverterNames.contains(newName.toLowerCase())) mView.showConverterExistError(true);
            else mView.showConverterExistError(false);
        } else {
            if (!mInitialConverterName.toLowerCase().equals(newName.toLowerCase())
                    && mConverterNames.contains(newName.toLowerCase())) {
                mView.showConverterExistError(true);
            } else {
                mView.showConverterExistError(false);
            }
        }
    }

    @Override
    public void loadUnits() {
        if (isNewConverter) {
            // when create new converter, produce list with 2 empty units to show for user
            mUnits = new ArrayList<>(2);
            mUnits.add(new Converter.Unit("", 0d, true));
            mUnits.add(new Converter.Unit("", 0d, true));

            // create empty unit lower case names set
            mUnitNames = new HashSet<>();

            mView.showUnits(mUnits);
            return;
        }

        mView.setProgressIndicator(true);

        mConvertersRepo.getConverter(mInitialConverterName, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                checkNotNull(converter);

                mCurConverter = converter;

                mUnits = converter.getUnits();

                // create unit lower case names set
                mUnitNames = createLowerCaseUnitNames();

                mView.showUnits(mUnits);

                mView.setProgressIndicator(false);
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
    }

    @Override
    public void deleteUnit(int position) {
        if (mUnits.size() > 2) {
            mUnits.remove(position);
            mView.notifyUnitRemoved(position);

            // reset saved position to avoid index out of bounds
            if (mCurConverter != null) { // may by null when create new converter
                mCurConverter.setLastUnitPosition(0);
            }
        } else {
            mView.showWarning(position);
        }
    }

    @Override
    public void enableUnit(int orderPosition, boolean enable) {
        mUnits.get(orderPosition).isEnabled = enable;
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
        }
        mView.showUnitExistError(false);

        if (!mCurUnitValue.isEmpty() && !mCurUnitName.isEmpty()) mView.enableSaveUnit(true);
        else mView.enableSaveUnit(false);
    }

    @Override
    public void setUnitValue(@NonNull String newValue) {
        checkNotNull(newValue);

        mCurUnitValue = newValue;

        if (!mCurUnitValue.isEmpty() && !mCurUnitName.isEmpty()) mView.enableSaveUnit(true);
        else mView.enableSaveUnit(false);
    }

    @Override
    public void saveUnit() {
        if (isNewUnit) {
            // create new unit
            mUnits.add(new Converter.Unit(mCurUnitName, Double.valueOf(mCurUnitValue), true));

            // update view
            mView.onUnitEdited(mUnits.size() - 1);
        } else {
            if (!mInitialUnitName.equals(mCurUnitName)) {
                // update existing unit with new name and value
                int index = mUnits.indexOf(new Converter.Unit(mInitialUnitName, 0d, true));
                mUnits.get(index).name = mCurUnitName;
                mUnits.get(index).value = Double.valueOf(mCurUnitValue);

                // add new unit name to the set
                mUnitNames.add(mCurUnitName.toLowerCase());

                // update view
                mView.onUnitEdited(index);
            } else {
                // update value of existing unit with new one
                int index = mUnits.indexOf(new Converter.Unit(mCurUnitName, 0d, true));
                mUnits.get(index).value = Double.valueOf(mCurUnitValue);

                // update view
                mView.onUnitEdited(index);
            }
        }
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
}