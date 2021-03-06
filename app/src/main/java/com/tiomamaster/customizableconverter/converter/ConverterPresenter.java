package com.tiomamaster.customizableconverter.converter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.data.ConvertersRepository;
import com.tiomamaster.customizableconverter.data.CurrencyConverter;
import com.tiomamaster.customizableconverter.data.TemperatureConverter;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

class ConverterPresenter implements ConverterContract.UserActionListener {

    @NonNull private ConvertersRepository mConvertersRepository;
    @NonNull private ConverterContract.View mConverterView;

    @VisibleForTesting Converter mCurConverter;

    private static final long ONE_DAY_IN_MILLS = 86400000;

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
    public void loadConverter(@NonNull final String name) {
        mConverterView.setProgressIndicator(true);

        mConvertersRepository.getConverter(name, false, new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                mConverterView.setProgressIndicator(false);

                mCurConverter = converter;

                if (mCurConverter instanceof CurrencyConverter) {
                    mConverterView.enableSwipeToRefresh(true);

                    if (System.currentTimeMillis() -
                            ((CurrencyConverter) mCurConverter).getLastUpdateTime()
                            > ONE_DAY_IN_MILLS) {
                        mConverterView.showSnackBar(R.string.msg_old_data);
                    }
                }
                else {
                    mConverterView.enableSwipeToRefresh(false);
                }

                if (mCurConverter instanceof TemperatureConverter) {
                    mConverterView.showConverter(converter.getEnabledUnitsName(),
                            converter.getLastUnitPosition(),
                            converter.getLastQuantity(), true);
                } else {
                    mConverterView.showConverter(converter.getEnabledUnitsName(),
                            converter.getLastUnitPosition(),
                            converter.getLastQuantity(), false);
                }
            }

            @Override
            public void reportError(@Nullable String message) {
                mConverterView.setProgressIndicator(false);
                mConverterView.showError(R.string.msg_internet_error);
            }
        });
    }

    @Override
    public void convert(@NonNull String from, @NonNull String quantity) {
        if (quantity.isEmpty() || quantity.equals("-")) {
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

    @Override
    public void updateCourses() {
        mConverterView.setProgressIndicator(true);

        mConvertersRepository.updateCourses(new ConvertersRepository.GetConverterCallback() {
            @Override
            public void onConverterLoaded(@NonNull Converter converter) {
                mConverterView.setProgressIndicator(false);
                mConverterView.hideSnackBar();

                mCurConverter = converter;

                mConverterView.showConverter(converter.getEnabledUnitsName(),
                        converter.getLastUnitPosition(),
                        converter.getLastQuantity(), false);

                mConverterView.enableSwipeToRefresh(true);
            }

            @Override
            public void reportError(@Nullable String message) {
                if (mCurConverter instanceof CurrencyConverter) {
                    mConverterView.showSnackBar(R.string.msg_internet_error);
                } else {
                    mConverterView.showError(R.string.msg_internet_error);
                }
                mConverterView.setProgressIndicator(false);
            }
        });
    }
}