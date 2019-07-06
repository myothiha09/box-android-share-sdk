package com.box.androidsdk.share.utils;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.box.androidsdk.share.usx.activities.BoxInviteCollaboratorsActivity;

public class SharedLinkBindingAdapters {

    public static void onLinkClick(boolean checked, TextView textView) {
        if (checked) Toast.makeText(textView.getContext(), "Link Copied (P.S Just toasting)", Toast.LENGTH_SHORT).show();
    }

}
