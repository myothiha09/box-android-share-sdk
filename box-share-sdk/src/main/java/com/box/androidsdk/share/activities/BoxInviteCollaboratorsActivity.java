package com.box.androidsdk.share.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.fragments.CollaboratorsRolesFragment;
import com.box.androidsdk.share.fragments.InviteCollaboratorsFragment;

import java.util.ArrayList;

/**
 * Activity used to allow users to invite additional collaborators to the folder. Email addresses will auto complete from the phones address book
 * as well as Box's internal invitee endpoint. The intent to launch this activity can be retrieved via the static getLaunchIntent method
 */
public class BoxInviteCollaboratorsActivity extends BoxActivity implements InviteCollaboratorsFragment.InviteCollaboratorsListener, View.OnClickListener{

    private boolean mSendEnabled;
    private static int REQUEST_SHOW_COLLABORATORS = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_collaborators);
        initToolbar();

    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);//to get blackText on status bar. will be moved to BoxActivity after all screens are updated.
    }

    @Override
    protected void initializeUi() {
        mFragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (mFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
            mFragment = InviteCollaboratorsFragment.newInstance((BoxCollaborationItem) mShareItem);
            ft.add(R.id.fragmentContainer, mFragment, InviteCollaboratorsFragment.TAG);
            ft.commit();
        }
        mFragment.setController(mController);
        ((InviteCollaboratorsFragment)mFragment).setInviteCollaboratorsListener(this);
        ((InviteCollaboratorsFragment)mFragment).setOnEditAccessListener(this);
        mSendEnabled = ((InviteCollaboratorsFragment)mFragment).areCollaboratorsPresent();
    }

    @Override
    public void onClick(View v) {
        Bundle data = ((InviteCollaboratorsFragment)mFragment).getData();
        String collaboratorName = data.getString(CollaboratorsRolesFragment.ARGS_NAME);
        ArrayList<BoxCollaboration.Role> roles = (ArrayList<BoxCollaboration.Role>) data.getSerializable(CollaboratorsRolesFragment.ARGS_ROLES);
        BoxCollaboration.Role selectedRole = (BoxCollaboration.Role) data.getSerializable(CollaboratorsRolesFragment.ARGS_SELECTED_ROLE);
        boolean allowRemove = data.getBoolean(CollaboratorsRolesFragment.ARGS_ALLOW_REMOVE);
        boolean allowOwnerRole = data.getBoolean(CollaboratorsRolesFragment.ARGS_ALLOW_OWNER_ROLE);
        BoxCollaboration collaboration = (BoxCollaboration)data.getSerializable(CollaboratorsRolesFragment.ARGS_SERIALIZABLE_EXTRA);
        if (roles == null || roles.size() == 0) {
            SdkUtils.toastSafely(getApplicationContext(), R.string.box_sharesdk_cannot_get_collaborators, Toast.LENGTH_SHORT);
           return;
        }
        Intent intent = BoxCollaborationRolesActivity.getLaunchIntent(this, mShareItem, mSession, roles, selectedRole, collaboratorName,allowRemove, allowOwnerRole, collaboration);
        startActivityForResult(intent, 11);
    }

    @Override
    public void onShowCollaborators(BoxIteratorCollaborations collaborations) {
        Intent collabsIntent = BoxCollaborationsActivity.getLaunchIntent(this, (BoxCollaborationItem)mShareItem, mSession, collaborations);
        startActivityForResult(collabsIntent, REQUEST_SHOW_COLLABORATORS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SHOW_COLLABORATORS && resultCode == RESULT_OK) {
            InviteCollaboratorsFragment fragment = (InviteCollaboratorsFragment) getSupportFragmentManager().findFragmentByTag(InviteCollaboratorsFragment.TAG);
            fragment.refreshUi();
        } else if (requestCode == 11 && resultCode == RESULT_OK) {
            String str = data.getStringExtra("Sel");
            Toast.makeText(getApplicationContext(), "S", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCollaboratorsPresent() {
        if (!mSendEnabled) {
            mSendEnabled = true;
            invalidateOptionsMenu();
        }
    }

    @Override
    public void onCollaboratorsAbsent() {
        if (mSendEnabled) {
            mSendEnabled = false;
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.box_sharesdk_action_send) {
            ((InviteCollaboratorsFragment)mFragment).addCollaborations();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets a fully formed intent that can be used to start the activity with
     *
     * @param context context to launch the intent in
     * @param collaborationItem item to add collaborators to
     * @param session the session to add collaborators with
     * @return the intent to launch the activity
     */
    public static Intent getLaunchIntent(Context context, BoxCollaborationItem collaborationItem, BoxSession session) {
        if (collaborationItem == null || SdkUtils.isBlank(collaborationItem.getId()) || SdkUtils.isBlank(collaborationItem.getType()))
            throw new IllegalArgumentException("A valid collaboration item must be provided for retrieving collaborations");
        if (session == null || session.getUser() == null ||  SdkUtils.isBlank(session.getUser().getId()))
            throw new IllegalArgumentException("A valid user must be provided for retrieving collaborations");

        Intent inviteIntent = new Intent(context, BoxInviteCollaboratorsActivity.class);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_ITEM, collaborationItem);
        inviteIntent.putExtra(CollaborationUtils.EXTRA_USER_ID, session.getUser().getId());
        return inviteIntent;
    }
}
