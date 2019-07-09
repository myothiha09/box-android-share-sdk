package com.box.androidsdk.share.utils;

import android.widget.TextView;
import android.widget.Toast;

public class SharedLinkBindingAdapters {

    public static void onLinkClick(boolean checked, TextView textView) {
        if (checked) Toast.makeText(textView.getContext(), "Link Copied (P.S Just toasting)", Toast.LENGTH_SHORT).show();
    }

}
