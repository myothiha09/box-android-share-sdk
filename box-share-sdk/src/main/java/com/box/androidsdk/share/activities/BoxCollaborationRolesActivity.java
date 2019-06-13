package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.CollaboratorsRolesFragment;
import com.box.androidsdk.share.fragments.SharedLinkFragment;

import java.util.ArrayList;

public class BoxCollaborationRolesActivity extends BoxActivity {

    public ArrayList<BoxCollaboration.Role> mRoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collaboration_roles);
        initToolbar();
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); //to get blackText on status bar. will be moved to BoxActivity after all screens are updated.
    }

    @Override
    protected void initializeUi() {
        mFragment = (SharedLinkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mRoles = (ArrayList<BoxCollaboration.Role>) getIntent().getSerializableExtra(CollaborationUtils.EXTRA_COLLABORATIONS_ROLES);
            mFragment = CollaboratorsRolesFragment.newInstance((BoxCollaborationItem) mShareItem, mRoles);
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.setController(mController);
    }

    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session) {
        if (session == null || session.getUser() == null)
            throw new IllegalArgumentException("Invalid user associated with Box session.");

        Intent intent = new Intent(context, BoxCollaborationRolesActivity.class);
        intent.putExtra(CollaborationUtils.EXTRA_ITEM, item);
        intent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }
    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param item item to view share link information for
     * @param session the session to view the share link information with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session, ArrayList<BoxCollaboration.Role> roles) {
        Intent intent = getLaunchIntent(context, item, session);
        intent.putExtra(CollaborationUtils.EXTRA_COLLABORATIONS_ROLES, roles);
        return intent;
    }
}
