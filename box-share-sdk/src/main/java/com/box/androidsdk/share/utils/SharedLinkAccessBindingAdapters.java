package com.box.androidsdk.share.utils;

import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.usx.fragments.SharedLinkAccessFragment;

public class SharedLinkAccessBindingAdapters {

    public static void onAccessLevelCheckChanged(boolean checked, BoxSharedLink.Access access, SharedLinkAccessFragment.SharedLinkAccessNotifiers notifiers) {
        if (checked) {
            notifiers.notifyAccessLevelChange(access);
        }
    }
}
