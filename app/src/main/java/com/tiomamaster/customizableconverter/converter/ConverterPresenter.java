package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;
import android.support.annotation.StringDef;

import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 14.07.2016.
 */
class ConverterPresenter implements ConverterContract.UserActionListener {

    private ConvertersRepository mConvertersRepository;
    private ConverterContract.View mConverterView;

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

        mConvertersRepository.getConverter(name, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                checkNotNull(converter);

                mConverterView.setProgressIndicator(false);

                mConverterView.showConverter(converter);
            }
        });
    }

    @Override
    public void openSettings() {
        mConverterView.showSettingsUi();
    }
}