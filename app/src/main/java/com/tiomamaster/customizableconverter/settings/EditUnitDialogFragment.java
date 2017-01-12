package com.tiomamaster.customizableconverter.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.R;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Artyom on 05.01.2017.
 */
public class EditUnitDialogFragment extends DialogFragment {

    static final String EDIT_UNIT_DIALOG_TAG = "EDIT_UNIT_DIALOG_TAG";

    private static final String ARGUMENT_UNIT_NAME = "ARGUMENT_UNIT_NAME";

    private static final String ARGUMENT_UNIT_VALUE = "ARGUMENT_UNIT_VALUE";

    private AlertDialog mDialog;

    private EditUnitDialogListener mListener;

    private TextView mTextError;

    public static EditUnitDialogFragment newInstance(String name, String value) {
        EditUnitDialogFragment f = new EditUnitDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_UNIT_NAME, name);
        args.putString(ARGUMENT_UNIT_VALUE, value);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mListener = (EditUnitDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_edit_unit, null);

        EditText name = (EditText) dialogView.findViewById(R.id.edit_text_name);

        name.setText(getArguments().getString(ARGUMENT_UNIT_NAME));

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mListener.onUnitTextChanged(s.toString());
            }
        });

        mTextError = (TextView) dialogView.findViewById(R.id.text_msg_error);

        final EditText value = (EditText) dialogView.findViewById(R.id.edit_text_value);

        value.setText(getArguments().getString(ARGUMENT_UNIT_VALUE));

        InputFilter valueFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if ((!source.toString().equals(".") && dest.toString().equals("0") && dstart == 1) ||
                        dstart == 0 && !dest.toString().equals("") && source.toString().matches("0|[.]") ||
                        dest.toString().matches("0.\\d+") && (dstart == 0 || dstart == 1) && source.toString().equals("0") ||
                        dest.toString().matches("0.\\d+") && dstart == 1 && source.toString().matches("\\d"))
                    return "";
                return null;
            }
        };
        value.setFilters(new InputFilter[]{valueFilter});

        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(value.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditUnitDialogFragment.this.getDialog().cancel();

                        mListener.onDialogNegativeClick();
                    }
                });

        mDialog = builder.create();

        return mDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        mListener.onDialogNegativeClick();
    }

    void showError(boolean visible){
        mDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(!visible);
        mTextError.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public interface EditUnitDialogListener {
        public void onDialogPositiveClick(@NonNull String value);
        public void onDialogNegativeClick();
        public void onUnitTextChanged(String newName);
    }
}