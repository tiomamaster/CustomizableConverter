package com.tiomamaster.customizableconverter.converter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.annotations.VisibleForTesting;
import com.tiomamaster.customizableconverter.Injection;
import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.data.Converter;
import com.tiomamaster.customizableconverter.settings.SettingsActivity;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 14.07.2016.
 */
public class ConverterFragment extends Fragment implements ConverterContract.View {

    private ConverterContract.UserActionListener mActionsListener;

    @VisibleForTesting
    Converter mCurConverter;

    @VisibleForTesting
    InputMethodManager mImm;

    @VisibleForTesting
    Spinner mSpinnerUnits;
    private ArrayAdapter<String> mUnitsAdapter;

    private EditText mQuantity;

    private TextView mResultText;

    private ConverterActivity mParentActivity;

    @VisibleForTesting
    RecyclerView mConversionResult;
    private ConversionResultAdapterWithHeader mResultAdapter;

    private View mRecyclerViewHeader;

    public ConverterFragment() {}

    public static ConverterFragment newInstance() {
        return new ConverterFragment();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mParentActivity = (ConverterActivity) getActivity();

        mActionsListener = new ConverterPresenter(Injection.
                provideConvertersRepository(mParentActivity.getApplicationContext()), this);

        mImm = (InputMethodManager) mParentActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        // create adapter here because need context
        mUnitsAdapter = new MySpinnerAdapter(mParentActivity, Color.BLACK,
                Color.parseColor("#009688"), -1,
                getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_item_height),
                mSpinnerUnits);
        mSpinnerUnits.setAdapter(mUnitsAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                mActionsListener.openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionsListener.loadConvertersTypes();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_converter, container, false);

        mConversionResult = (RecyclerView) root.findViewById(R.id.conversion_result);
        mConversionResult.setLayoutManager(new LinearLayoutManager(getContext()));
        mResultAdapter = new ConversionResultAdapterWithHeader();
        mConversionResult.setAdapter(mResultAdapter);

        // clear edit text focus
        mConversionResult.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mQuantity.clearFocus();
                mQuantity.setFocusableInTouchMode(false);

                // hide soft keyboard when scroll
                hideSoftInput();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        // draw divider between items
        mConversionResult.addItemDecoration(new RecyclerView.ItemDecoration() {

            private final int[] ATTRS = new int[]{android.R.attr.listDivider};

            private Drawable mDivider;

            {
                TypedArray styledAttributes = getActivity().obtainStyledAttributes(ATTRS);
                mDivider = styledAttributes.getDrawable(0);
                styledAttributes.recycle();
            }

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                int left = getResources().getDimensionPixelSize(R.dimen.divider_padding);
                int right = parent.getWidth() - left;

                int childCount = parent.getChildCount();
                for (int i = 1; i < childCount - 1; i++) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin;
                    int bottom = top + mDivider.getIntrinsicHeight();

                    mDivider.setBounds(left, top, right, bottom);
                    mDivider.draw(c);
                }
            }
        });

        mConversionResult.setVisibility(View.INVISIBLE);

        SwipeRefreshLayout swipeRefreshLayout =
                (SwipeRefreshLayout) root.findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.colorPrimary),
                ContextCompat.getColor(getActivity(), R.color.accent),
                ContextCompat.getColor(getActivity(), R.color.primary_dark));

        mRecyclerViewHeader = inflater.inflate(R.layout.recycler_view_header, container, false);

        mSpinnerUnits = (Spinner) mRecyclerViewHeader.findViewById(R.id.spinner_units);
        // to prevent system calls
        mSpinnerUnits.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mSpinnerUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // save last selected unit position in converter
                            mCurConverter.setLastUnitPosition(position);

                            convert(((CheckedTextView) view).getText().toString(), mQuantity.getText().toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
                return false;
            }
        });

        mQuantity = (EditText) mRecyclerViewHeader.findViewById(R.id.quantity);

        // to prevent focused when app start and give watcher when user touch
        mQuantity.setFocusableInTouchMode(false);
        mQuantity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mQuantity.setFocusableInTouchMode(true);
                    mQuantity.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (s.length() != 0 && mSpinnerUnits.getSelectedItem() != null) {
                                // save last entered quantity in converter
                                mCurConverter.setLastQuantity(s.toString());

                                convert(mSpinnerUnits.getSelectedItem().toString(), s.toString());
                            }
                        }
                    });
                }
                return false;
            }
        });

        mResultText = (TextView) mRecyclerViewHeader.findViewById(R.id.resultText);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        return root;
    }

     void hideSoftInput() {
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = mParentActivity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(mParentActivity);
        }
        mImm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // Convert from selected in Spinner unit and quantity in EditText
    // to all possible units in this type of converter and set they to RecyclerView
    private void convert(String from, String quantity) {
        if (TextUtils.isEmpty(quantity)) return;
        mResultAdapter.setDataSet(mCurConverter.convertAllExtFormatted(Double.parseDouble(quantity), from));
        mResultText.setVisibility(View.VISIBLE);
    }

    @Override
    public void setProgressIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        final SwipeRefreshLayout srl =
                (SwipeRefreshLayout) getView().findViewById(R.id.refresh_layout);

        // Make sure setRefreshing() is called after the layout is done with everything else.
        srl.post(new Runnable() {
            @Override
            public void run() {
                srl.setRefreshing(active);
            }
        });
    }

    @Override
    public void showConvertersTypes(@NonNull String[] converters, int selection) {
        checkNotNull(converters);

        // inflate spinner using converters types
        mParentActivity.initSpinner(converters, selection);

        // show first converter,
        // further must be last converter with which user worked before close the application
        mActionsListener.loadConverter(mParentActivity.mSpinConvTypes.getSelectedItem().toString());
    }

    @Override
    public void showConverter(@NonNull Converter converter) {
        checkNotNull(converter);
        mCurConverter = converter;

        // clear spinner units and set new data
        mUnitsAdapter.clear();
        for (String s : converter.getAllUnitsName()) {
            mUnitsAdapter.add(s);
        }
        // set last selection
        mSpinnerUnits.setSelection(converter.getLastUnitPosition());

        // set last quantity text
        if (mQuantity != null) {
            mQuantity.setText(converter.getLastQuantity());
            mQuantity.setFocusableInTouchMode(false);
            mQuantity.clearFocus();
        }

        // clear recycler view conversion result
        mResultAdapter.clearDataSet();

        // hide text Result:
        if (mResultText != null) {
            mResultText.setVisibility(View.INVISIBLE);
        }

        // show conversion result
        convert(mSpinnerUnits.getSelectedItem().toString(), converter.getLastQuantity());

        mConversionResult.setVisibility(View.VISIBLE);
    }

    @Override
    public void showSettingsUi() {
        startActivity(new Intent(getContext(), SettingsActivity.class));
    }

    /**
     * Handle selection of spinner converters types
     */
    void spinnerSelected(String selection) {
        mConversionResult.setVisibility(View.GONE);
        mActionsListener.loadConverter(selection);
    }

    private class ConversionResultAdapterWithHeader extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_ITEM = 0;
        private final static int TYPE_HEADER = 1;

        private String[][] mDataSet;

        private class VHItem extends RecyclerView.ViewHolder {

            private TextView mUnitName;
            private TextView mResult;

            VHItem(View itemView) {
                super(itemView);
                mUnitName = (TextView) itemView.findViewById(R.id.unit_name);
                mResult = (TextView) itemView.findViewById(R.id.result);
            }
        }

        private class VHHeader extends RecyclerView.ViewHolder {

            VHHeader(View itemView) {
                super(itemView);
            }
        }

        void setDataSet(@NonNull String[][] newData) {
            checkNotNull(newData);
            if (mDataSet == null) {
                mDataSet = newData;
                notifyItemRangeInserted(1, mDataSet.length);
            } else {
                mDataSet = newData;
                // because notifyItemInserted() cause bug inside recycler view
                notifyDataSetChanged();
            }
        }

        void clearDataSet() {
            if (mDataSet != null) {
                notifyItemRangeRemoved(1, mDataSet.length);
                mDataSet = null;
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) return TYPE_HEADER;
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                return new VHItem(LayoutInflater.from(parent.getContext()).
                        inflate(R.layout.recycler_view_item, parent, false));
            } else if (viewType == TYPE_HEADER) {
                return new VHHeader(mRecyclerViewHeader);
            }
            throw new RuntimeException("there is no type that matches the type "
                    + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof VHItem) {
                if (mDataSet != null) {
                    ((VHItem) holder).mUnitName.setText(mDataSet[position - 1][0]);

                    if (mDataSet[position - 1][1].contains("×")) {
                        // format text to show power
                        String result = mDataSet[position - 1][1];
                        int start = result.indexOf('×') + 3;
                        int end = result.length();
                        SpannableStringBuilder ssb = new SpannableStringBuilder(result);
                        ssb.setSpan(new SuperscriptSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ssb.setSpan(new RelativeSizeSpan(0.75f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        ((VHItem) holder).mResult.setText(ssb);
                    } else
                        ((VHItem) holder).mResult.setText(mDataSet[position - 1][1]);
                }
            } else if (holder instanceof VHHeader) {
                mQuantity.requestFocus();
            }
        }

        @Override
        public int getItemCount() {
            if (mDataSet == null) return 1;
            return mDataSet.length + 1;
        }
    }
}