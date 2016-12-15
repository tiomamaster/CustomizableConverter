package com.tiomamaster.customizableconverter.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 07.12.2016.
 */
class EditConverterPresenter implements SettingsContract.EditConverterUal {

    @NonNull private ConvertersRepository mConvertersRepo;

    @NonNull private SettingsContract.EditConverterView mView;

    @Nullable private String mConverterName;

    EditConverterPresenter(@NonNull ConvertersRepository convertersRepository,
                           @NonNull SettingsContract.EditConverterView editConverterView,
                           @Nullable String initialConverterName) {
        this.mConvertersRepo = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        this.mView = checkNotNull(editConverterView, "editConverterView cannot be null");
        this.mConverterName = initialConverterName;

        mView.setPresenter(this);
    }

    @Override
    public void handleHomePressed() {
        mView.showPreviousView();
    }

    @Override
    public void handleFabPressed() {

    }

    @Nullable
    @Override
    public String getConverterName() {
        return mConverterName;
    }

    @Override
    public void loadUnits() {
        if (mConverterName == null) {
            List<Converter.Unit> emptyUnits = new ArrayList<>(2);
            emptyUnits.add(new Converter.Unit("", 0d, true));
            emptyUnits.add(new Converter.Unit("", 0d, true));
            mView.showUnits(emptyUnits);
            return;
        }

        mConvertersRepo.getConverter(mConverterName, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                mView.showUnits(converter.getUnits());
            }
        });
    }
}
