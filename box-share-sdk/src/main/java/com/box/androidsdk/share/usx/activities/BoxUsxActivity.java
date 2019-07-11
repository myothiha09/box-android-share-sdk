package com.box.androidsdk.share.usx.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.BoxShareController;
import com.box.androidsdk.share.usx.fragments.SharedLinkAccessFragment;
import com.box.androidsdk.share.usx.fragments.SharedLinkFragment;

/**
 * Activity used to share/unshare an item from Box. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxUsxActivity extends BoxActivity {

    private static final int REQUEST_SHARED_LINK_ACCESS = 100;
    private static int REQUEST_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usx_activity_usx);
        secondaryBar();
        initToolbar();
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) instanceof SharedLinkAccessFragment) {
            changeTitleBar();
        }

    }

    @Override
    protected void initializeUi() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment instanceof SharedLinkAccessFragment) {
            setupSwitchLinkedAccessFragment((SharedLinkAccessFragment) fragment);
        } else {
            if (fragment == null) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.setTransition(FragmentTransaction.TRANSIT_NONE);
                mFragment = SharedLinkFragment.newInstance(baseShareVM.getShareItem());
                ft.add(R.id.fragmentContainer, mFragment);
                ft.commit();
            } else {
                mFragment = (SharedLinkFragment)fragment;
            }
            mFragment.setController(new BoxShareController(mSession));

            ((SharedLinkFragment)mFragment).setOnEditLinkAccessButtonClickListener(v -> switchToShareAccessFragment());
            ((SharedLinkFragment)mFragment).setOnInviteCollabsClickListener( v ->
                    startActivityForResult(BoxInviteCollaboratorsActivity.getLaunchIntent(BoxUsxActivity.this,
                            (BoxCollaborationItem) baseShareVM.getShareItem(), mSession), REQUEST_COLLABORATORS));
            ((SharedLinkFragment)mFragment).setOnCollabsListener( v ->
                    startActivityForResult(BoxCollaborationsActivity.getLaunchIntent(BoxUsxActivity.this,
                            (BoxCollaborationItem) baseShareVM.getShareItem(), mSession), REQUEST_COLLABORATORS));
        }
    }

    private void switchToShareAccessFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        SharedLinkAccessFragment fragment = SharedLinkAccessFragment.newInstance(baseShareVM.getShareItem());
        setupSwitchLinkedAccessFragment(fragment);
        changeTitleBar();
        ft.replace(R.id.fragmentContainer, fragment).addToBackStack(null);
        ft.commit();
    }

    private void setupSwitchLinkedAccessFragment(SharedLinkAccessFragment fragment) {
        fragment.setFragmentCallBack(() -> {
            resetTitleBar();
            showToast("SharedLinkAccessFragment callback.");
        });
    }

    private void changeTitleBar() {
        findViewById(R.id.no_subtitle_action_bar).setVisibility(View.VISIBLE);
        findViewById(R.id.box_action_bar).setVisibility(View.GONE);
    }

    private void resetTitleBar() {
        findViewById(R.id.no_subtitle_action_bar).setVisibility(View.GONE);
        findViewById(R.id.box_action_bar).setVisibility(View.VISIBLE);
    }

    private void secondaryBar() {
        Toolbar actionBar = findViewById(R.id.no_subtitle_action_bar);
        actionBar.setTitle(getString(R.string.box_sharesdk_title_link_access));
        actionBar.setNavigationIcon(R.drawable.ic_box_sharesdk_arrow_back_black_24dp);
        actionBar.setNavigationOnClickListener(v -> onBackPressed());
    }

    //
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_COLLABORATORS){
            ((SharedLinkFragment)mFragment).refreshInitialsViews();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

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
