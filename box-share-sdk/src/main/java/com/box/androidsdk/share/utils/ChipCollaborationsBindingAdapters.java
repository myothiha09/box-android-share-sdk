package com.box.androidsdk.share.utils;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.widget.MultiAutoCompleteTextView;

import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.views.ChipCollaborationView;

public class ChipCollaborationsBindingAdapters {
    static InviteeAdapter adapter;
    @BindingAdapter("app:adapter")
    public static void setAdapter(ChipCollaborationView chipCollaborationView, boolean value) {
        InviteeAdapter adapter = createAdapter(chipCollaborationView.getContext(), value);
        adapter.setInviteeAdapterListener(new InviteeAdapter.InviteeAdapterListener() {
            @Override
            public void onFilterTermChanged(CharSequence constraint) {
                if (constraint.length() >= 3) {
                    String firstThreeChars = constraint.subSequence(0, 3).toString();
                    if (!firstThreeChars.equals(mFilterTerm)) {
                        mFilterTerm = firstThreeChars;
                        fetchInvitees();
                    }
                }
            }
        });
        chipCollaborationView.setAdapter(adapter);
    }

    @BindingAdapter("app:tokenizer")
    public static void setTokenizer(ChipCollaborationView chipCollaborationView, MultiAutoCompleteTextView.CommaTokenizer tokenizer) {
        chipCollaborationView.setTokenizer(tokenizer);
    }
    public static InviteeAdapter createAdapter(Context context, boolean value) {
        adapter = adapter != null ? adapter: new InviteeAdapter(context) {
            @Override
            protected boolean isReadContactsPermissionAvailable() {
                return value && super.isReadContactsPermissionAvailable();
            }
        };
        return adapter;
    }
}
