package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class ConverterPresenter implements ConverterContract.UserActionListener {

    @NonNull private ConvertersRepository mConvertersRepository;
    @NonNull private ConverterContract.View mConverterView;

    @VisibleForTesting Converter mCurConverter;

    ConverterPresenter(@NonNull ConvertersRepository convertersRepository,
                       @NonNull ConverterContract.View converterView) {
        mConvertersRepository = checkNotNull(convertersRepository, "convertersRepository cannot be null");
        mConverterView = checkNotNull(converterView, "converterView cannot be null");
    }

    @Override
    public void loadConvertersTypes() {
        mConverterView.setProgressIndicator(true);

        mConvertersRepository.getEnabledConvertersTypes(new ConvertersRepository.LoadEnabledConvertersTypesCallback() {
            @Override
            public void onConvertersTypesLoaded(@NonNull List<String> convertersTypes, int position) {
                checkNotNull(convertersTypes);

                mConverterView.setProgressIndicator(false);

                if (convertersTypes.size() == 0) {
                    mConverterView.showNoting();
                    return;
                }

                mConverterView.showConvertersTypes(convertersTypes, position);
            }
        });
    }

    @Override
    public void loadConverter(@NonNull String name) {
        checkNotNull(name);

        mConverterView.setProgressIndicator(true);

        mConvertersRepository.getConverter(name, false, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                checkNotNull(converter);

                mConverterView.setProgressIndicator(false);

                mCurConverter = converter;

                mConverterView.showConverter(converter.getEnabledUnitsName(),
                        converter.getLastUnitPosition(),
                        converter.getLastQuantity());
            }
        });
    }

    @Override
    public void convert(@NonNull String from, @NonNull String quantity) {
        if (quantity.isEmpty()) {
            mConverterView.showConversionResult(new ArrayList<Pair<String, String>>());
            return;
        } else if (quantity.equals(".")) {
            // convert using zero quantity if user starts input with dot
            quantity = "0";
        }

        mConverterView.showConversionResult(mCurConverter.convertAll(Double.valueOf(quantity), from));
    }

    @Override
    public void saveLastUnitPos(int pos) {
        mCurConverter.setLastUnitPosition(pos);
        mConvertersRepository.saveLastUnit();
    }

    @Override
    public void saveLastQuantity(@NonNull String quantity) {
        mCurConverter.setLastQuantity(quantity);
        mConvertersRepository.saveLastQuantity();
    }

    @Override
    public void openSettings() {
        mConverterView.showSettingsUi();
    }
}