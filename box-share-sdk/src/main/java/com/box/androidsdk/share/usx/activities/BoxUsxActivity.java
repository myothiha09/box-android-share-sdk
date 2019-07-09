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
public class BoxUsxActivity extends BoxActivity {

    private static final int REQUEST_SHARED_LINK_ACCESS = 100;
    private static int REQUEST_SHOW_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_usx);
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
//                startActivityForResult(BoxSharedLinkAccessActivity.getLaunchIntent(BoxUsxActivity.this,
//                        mShareItem, mSession), REQUEST_SHARED_LINK_ACCESS));
        ((SharedLinkFragment)mFragment).setOnInviteCollabsClickListener( v ->
                startActivity(BoxInviteCollaboratorsActivity.getLaunchIntent(BoxUsxActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession)));
        ((SharedLinkFragment)mFragment).setOnCollabsListener( v ->
                startActivity(BoxCollaborationsActivity.getLaunchIntent(BoxUsxActivity.this,
                        (BoxCollaborationItem) baseShareVM.getShareItem(), mSession)));
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        getSupportActionBar().setTitle(baseShareVM.getShareItem().getName());
        getSupportActionBar().setSubtitle(capitalizeFirstLetter(baseShareVM.getShareItem().getType()));
    }

    private String capitalizeFirstLetter(String str) {
        StringBuilder sb = new StringBuilder();
        for(String curr: str.split(" ")) {
            sb.append(Character.toUpperCase(curr.charAt(0)) + curr.substring(1) + " ");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
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

        Intent intent = new Intent(context, BoxUsxActivity.class);
        intent.putExtra(CollaborationUtils.EXTRA_ITEM, item);
        intent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return intent;
    }

}
