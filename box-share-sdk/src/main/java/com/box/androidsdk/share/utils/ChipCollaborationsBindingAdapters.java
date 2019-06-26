package com.box.androidsdk.share.utils;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.views.ChipCollaborationView;

public class ChipCollaborationsBindingAdapters {
    @BindingAdapter(value = "adapter")
    public static void setAdapter(ChipCollaborationView chipCollaborationView, InviteeAdapter adapter) {
        chipCollaborationView.setAdapter(adapter);
    }

    @BindingAdapter(value = "tokenizer")
    public static void setTokenizer(ChipCollaborationView chipCollaborationView, MultiAutoCompleteTextView.CommaTokenizer tokenizer) {
        chipCollaborationView.setTokenizer(tokenizer);
    }

}
