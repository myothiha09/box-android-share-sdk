package com.box.androidsdk.share.utils;

import android.databinding.BindingAdapter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InviteCollaboratorsBindingAdapters {

    @BindingAdapter(value = {"personalMessageTextView", "addPersonalMessageButton", "bottomDivider"})
    public static void onEmptyAndUnfocused(EditText view, View v1, View v2, View v3) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && ((EditText)v).getText().toString().isEmpty()) {
                    v1.setVisibility(View.GONE);
                    v2.setVisibility(View.VISIBLE);
                    v3.setVisibility(View.GONE);
                    view.setVisibility(View.GONE);
                }
            }
        });
    }
    @BindingAdapter(value = {"personalMessageEditText", "personalMessageTextView", "bottomDivider"})
    public static void onAddPersonalMessageBottom(Button view, View v1, View v2, View v3) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v1.setVisibility(View.VISIBLE);
                v2.setVisibility(View.VISIBLE);
                v3.setVisibility(View.VISIBLE);
                view.setVisibility(View.GONE);
                v1.requestFocus();
            }
        });
    }
}
