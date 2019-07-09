package com.box.androidsdk.share.usx.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.usx.fragments.SharedLinkFragment;

/**
 * Activity used to share/unshare an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxSharedLinkActivity extends BoxActivity {

    private static final int REQUEST_SHARED_LINK_ACCESS = 100;
    private static int REQUEST_SHOW_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_shared_link);
        initToolbar();
    }

    @Override
    protected void initializeUi() {
        mFragment = (SharedLinkFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = SharedLinkFragment.newInstance(baseShareVM.getShareItem());
            ft.add(R.id.fragmentContainer, mFragment);
            ft.commit();
        }
        mFragment.setController(new BoxShareController(mSession));

//        ((SharedLinkFragment)mFragment).setOnEditLinkAccessButtonClickListener(v ->
//                startActivityForResult(BoxSharedLinkAccessActivity.getLaunchIntent(BoxSharedLinkActivity.this,
//                        mShareItem, mSession), REQUEST_SHARED_LINK_ACCESS));
        ((SharedLinkFragment)mFragment).setOnInviteCollabsClickListener( v ->
                startActivity(BoxInviteCollaboratorsActivity.getLaunchIntent(BoxSharedLinkActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession)));
        ((SharedLinkFragment)mFragment).setOnCollabsListener( v ->
                startActivity(BoxCollaborationsActivity.getLaunchIntent(BoxSharedLinkActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession)));
    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_SHARED_LINK_ACCESS){
//            ((SharedLinkFragment)mFragment).refreshShareItemInfo();
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param item item to view share link information for
     * @param session the session to view the share link information with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxItem item, BoxSession session) {
        if (session == null || session.getUser() == null)
            throw new IllegalArgumentException("Invalid user associated with Box session.");

        Intent intent = new Intent(context, BoxSharedLinkActivity.class);
        intent.putExtra(CollaborationUtils.EXTRA_ITEM, item);
        intent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }

}
