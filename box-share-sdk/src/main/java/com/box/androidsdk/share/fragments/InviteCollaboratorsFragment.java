package com.box.androidsdk.share.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.box.androidsdk.share.databinding.FragmentInviteCollaboratorsBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MultiAutoCompleteTextView;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.requests.BoxResponseBatch;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.adapters.InviteeAdapter;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Fragment to let users invite collaborators on an item.
 *
 * There are two listeners used here:
 * 1. InviteCollaboratorsListener is used to set up a listener by the parent Activity or Fragment on this Fragment.
 * 2. ShowCollaboratorsListener is used to set up a listener by this fragment on the child custom view called CollaboratorsInitialsView.
 */

public class InviteCollaboratorsFragment extends BoxFragment {


    private static final Integer MY_PERMISSIONS_REQUEST_READ_CONTACTS = 32;
    public static final String TAG = InviteCollaboratorsFragment.class.getName();
    public static final String EXTRA_USE_CONTACTS_PROVIDER = "InviteCollaboratorsFragment.ExtraUseContactsProvider";
    public static final String EXTRA_COLLAB_SELECTED_ROLE = "collabSelectedRole";

    private InviteeAdapter mAdapter;
    private BoxCollaboration.Role mSelectedRole;
    private ArrayList<BoxCollaboration.Role> mRoles;
    private String mFilterTerm;
    private boolean mInvitationFailed = false;

    private View.OnClickListener mOnEditAccessListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentInviteCollaboratorsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_invite_collaborators, container,false);
        MultiAutoCompleteTextView.CommaTokenizer tokenizer = new MultiAutoCompleteTextView.CommaTokenizer();

        mAdapter = createInviteeAdapter(getActivity());
        mAdapter.setInviteeAdapterListener(createInviteeAdapterListener());
        binding.setAdapter(mAdapter);
        binding.setTokenizer(tokenizer);
        binding.setOnRoleClickListener(mOnEditAccessListener);
        View view = binding.getRoot();

        mFilterTerm = "";

        if (savedInstanceState != null) {
            String selected_role_enum = savedInstanceState.getString(EXTRA_COLLAB_SELECTED_ROLE);
            if (selected_role_enum != null){
                mSelectedRole = BoxCollaboration.Role.fromString(selected_role_enum);
            }
        }

        // Get serialized roles or fetch them if they are not available
        if (getCollaborationItem() != null && getCollaborationItem().getAllowedInviteeRoles() != null) {
            if(getCollaborationItem().getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
                mRoles = getCollaborationItem().getAllowedInviteeRoles();
                if (mSelectedRole == null) {
                    BoxCollaboration.Role defaultRole = getBestDefaultRole(getCollaborationItem().getDefaultInviteeRole(), mRoles);
                    setSelectedRole(defaultRole);
                } else {
                    setSelectedRole(mSelectedRole);
                }
            } else {
                showNoPermissionToast();
                getActivity().finish();
            }
        } else {
            fetchRoles();
        }

        fetchInvitees();
        if (getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER)){
            requestPermissionsIfNecessary();
        }
        return view;
    }

    private BoxCollaboration.Role getBestDefaultRole(String roleName, List<BoxCollaboration.Role> roles){
        try {
            return BoxCollaboration.Role.fromString(roleName);
        } catch (IllegalArgumentException e){
            BoxLogUtils.e("invalid role name " + roleName, e);
            return roles.get(0);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSelectedRole != null) {
            outState.putString(EXTRA_COLLAB_SELECTED_ROLE, mSelectedRole.toString());
        }
        super.onSaveInstanceState(outState);
    }


    protected InviteeAdapter createInviteeAdapter(final Context context){
        return new InviteeAdapter(context) {
            @Override
            protected boolean isReadContactsPermissionAvailable() {
                return getArguments().getBoolean(EXTRA_USE_CONTACTS_PROVIDER, true) && super.isReadContactsPermissionAvailable();
            }
        };
    }
    public InviteeAdapter.InviteeAdapterListener createInviteeAdapterListener() {
        return new InviteeAdapter.InviteeAdapterListener() {
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
        };
    }


    @Override
    public int getActivityResultCode() {
        if (mInvitationFailed) {
            return Activity.RESULT_CANCELED;
        }

        return Activity.RESULT_OK;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Attach the listener to view once createView is complete
    }


    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }
    }

    public void setOnEditAccessListener(View.OnClickListener listener) {
        mOnEditAccessListener = listener;
    }
    public Bundle getData() {
        Bundle b = new Bundle();
        b.putSerializable(CollaboratorsRolesFragment.ARGS_ROLES, mRoles);
        b.putSerializable(CollaboratorsRolesFragment.ARGS_SELECTED_ROLE, mSelectedRole);
        b.putSerializable(CollaboratorsRolesFragment.ARGS_ALLOW_OWNER_ROLE, false);
        b.putSerializable(CollaboratorsRolesFragment.ARGS_ALLOW_REMOVE, false);
        b.putSerializable(CollaboratorsRolesFragment.ARGS_NAME, "");
        b.putSerializable(CollaboratorsRolesFragment.ARGS_SERIALIZABLE_EXTRA, null);
        return b;
    }

    /**
     * Executes the request to retrieve the available roles for the item
     */
    private void fetchRoles() {
        if (getCollaborationItem() == null || SdkUtils.isBlank(getCollaborationItem().getId())) {
            return;
        }

        showSpinner(R.string.box_sharesdk_fetching_collaborators, R.string.boxsdk_Please_wait);
        mController.fetchRoles(getCollaborationItem()).addOnCompletedListener(mRolesListener);
    }

    private BoxFutureTask.OnCompletedListener<BoxCollaborationItem> mRolesListener =
            new BoxFutureTask.OnCompletedListener<BoxCollaborationItem>() {
                @Override
                public void onCompleted(final BoxResponse<BoxCollaborationItem> response) {
                    dismissSpinner();
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess() && getCollaborationItem() != null) {
                                if(getCollaborationItem().getPermissions().contains(BoxItem.Permission.CAN_INVITE_COLLABORATOR)) {
                                    BoxCollaborationItem collaborationItem = response.getResult();
                                    mRoles = collaborationItem.getAllowedInviteeRoles();
                                    if (mSelectedRole != null) {
                                        setSelectedRole(mSelectedRole);
                                    } else {
                                        BoxCollaboration.Role selectedRole = mRoles != null && mRoles.size() > 0 ? getBestDefaultRole(collaborationItem.getDefaultInviteeRole(), mRoles) : null;
                                        setSelectedRole(selectedRole);
                                    }
                                    mShareItem = collaborationItem;
                                } else {
                                    showNoPermissionToast();
                                    getActivity().finish();
                                }
                            } else {
                                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch roles request failed",
                                        response.getException());
                                mController.showToast(getActivity(), getString(R.string.box_sharesdk_network_error));
                            }
                        }
                    });
                }
            };

    /**
     * Executes the request to retrieve the invitees that can be auto-completed
     */
    private void fetchInvitees() {
        if (getCollaborationItem() instanceof BoxFolder) {
            // Currently this request is only supported for folders.
            mController.getInvitees(getCollaborationItem(), mFilterTerm).addOnCompletedListener(mGetInviteesListener);
        }
    }

//    /**
//     * Executes the request to add collaborations to the item
//     */
//    public void addCollaborations() {
//        List<BoxInvitee> invitees = mAutoComplete.getObjects();
//        String[] emailParts = new String[invitees.size()];
//        for (int i = 0; i < invitees.size(); i++) {
//            emailParts[i] = invitees.get(i).getEmail();
//        }
//
//        showSpinner(R.string.box_sharesdk_adding_collaborators, R.string.boxsdk_Please_wait);
//        mController.addCollaborations(getCollaborationItem(), mSelectedRole, emailParts).addOnCompletedListener(mAddCollaborationsListener);
//    }

    private BoxFutureTask.OnCompletedListener<BoxIteratorInvitees> mGetInviteesListener =
            new BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>() {
                @Override
                public void onCompleted(final BoxResponse<BoxIteratorInvitees> response) {
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccess()) {
                                final BoxIteratorInvitees invitees = response.getResult();
                                mAdapter.setInvitees(invitees);
                            } else {
                                BoxLogUtils.e(InviteCollaboratorsFragment.class.getName(), "get invitees request failed",
                                        response.getException());

                                if (response.getException() instanceof BoxException) {
                                    BoxException boxException = (BoxException) response.getException();
                                    int responseCode = boxException.getResponseCode();

                                    if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                                        mController.showToast(getActivity(), R.string.box_sharesdk_insufficient_permissions);
                                        return;
                                    } else if (boxException.getErrorType() == BoxException.ErrorType.NETWORK_ERROR) {
                                        mController.showToast(getActivity(), getString(R.string.box_sharesdk_network_error) + responseCode);

                                    }

                                }


                            }
                        }
                    });
                }
            };

    private BoxFutureTask.OnCompletedListener<BoxResponseBatch> mAddCollaborationsListener =
            new BoxFutureTask.OnCompletedListener<BoxResponseBatch>() {
                @Override
                public void onCompleted(final BoxResponse<BoxResponseBatch> response) {
                    dismissSpinner();
                    final Activity activity = getActivity();
                    if (activity == null) {
                        return;
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleCollaboratorsInvited(response.getResult());
                        }
                    });
                }
            };

    /**
     * Handles the batch response of adding collaborations to the item by showing error messages when needed
     * and finishing the activity afterwards
     *
     * @param responses the add collaborations batch response
     */
    private void handleCollaboratorsInvited(BoxResponseBatch responses) {
        int alreadyAddedCount = 0;
        boolean didRequestFail = false;
        String name = "";
        List<String> failedCollaboratorsList = new ArrayList<String>();
        for (BoxResponse<BoxCollaboration> r : responses.getResponses()) {
            if (!r.isSuccess()) {
                HashSet<Integer> failureCodes = new HashSet<Integer>();
                failureCodes.add(HttpURLConnection.HTTP_BAD_REQUEST );
                failureCodes.add(HttpURLConnection.HTTP_FORBIDDEN);
                if (r.getException() instanceof BoxException && failureCodes.contains(((BoxException) r.getException()).getResponseCode())) {
                    String code = ((BoxException) r.getException()).getAsBoxError().getCode();
                    BoxUser user = (BoxUser) ((BoxRequestsShare.AddCollaboration) r.getRequest()).getAccessibleBy();
                    if (!SdkUtils.isBlank(code) && code.equals(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)) {
                        alreadyAddedCount++;
                        name = user == null ? "" : user.getLogin();
                    } else {
                        failedCollaboratorsList.add(user == null ? "" : user.getLogin());
                    }
                }
                didRequestFail = true;
            }
        }

        String msg;
        if (didRequestFail) {
            if (!failedCollaboratorsList.isEmpty()) {
                StringBuilder collaborators = new StringBuilder();
                for (int i = 0; i < failedCollaboratorsList.size(); i++) {
                    collaborators.append(failedCollaboratorsList.get(i));
                    if (i < failedCollaboratorsList.size() - 1) {
                        collaborators.append(' ');
                    }
                }

                BoxItem boxItem = (BoxItem) getArguments().getSerializable(CollaborationUtils.EXTRA_ITEM);
                String itemType = getItemType(boxItem);

                msg = String.format(getString(R.string.box_sharesdk_following_collaborators_error), collaborators.toString());

            } else if (alreadyAddedCount == 1) {
                msg = String.format(getString(R.string.box_sharesdk_has_already_been_invited), name);
            } else if (alreadyAddedCount > 1) {
                msg = String.format(getString(R.string.box_sharesdk_num_has_already_been_invited), Integer.toString(alreadyAddedCount));
            } else {
                msg = getString(R.string.box_sharesdk_unable_to_invite);
            }
        } else {
            if (responses.getResponses().size() == 1) {
                BoxCollaboration collaboration = (BoxCollaboration) responses.getResponses().get(0).getResult();
                if (collaboration.getAccessibleBy() == null) {
                    msg = getString(R.string.box_sharesdk_collaborators_invited);
                } else {
                    String login = ((BoxUser)(collaboration).getAccessibleBy()).getLogin();
                    msg = String.format(getString(R.string.box_sharesdk_collaborator_invited), login);
                }

            } else {
                msg = getString(R.string.box_sharesdk_collaborators_invited);
            }
        }

        mInvitationFailed = (didRequestFail && !failedCollaboratorsList.isEmpty());

        if (mInvitationFailed) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_INDEFINITE).show();
        } else {
            mController.showToast(getActivity(), msg);
            getActivity().finish();
        }
    }

    @NonNull
    private String getItemType(BoxItem boxItem) {
        if (boxItem instanceof BoxFolder) {
            return getString(com.box.sdk.android.R.string.boxsdk_folder);
        } else if (boxItem instanceof BoxFile) {
            return getString(com.box.sdk.android.R.string.boxsdk_file);
        } else {
            //default return folder as the type
            return getString(com.box.sdk.android.R.string.boxsdk_folder);
        }
    }

    private void showNoPermissionToast() {
        mController.showToast(getActivity(), R.string.box_sharesdk_insufficient_permissions);
    }

    /**
     * Sets the selected role in the UI
     *
     * @param role the collaboration role to select
     */
    private void setSelectedRole(BoxCollaboration.Role role) {
        mSelectedRole = role;
    }

    protected BoxCollaborationItem getCollaborationItem() {
        return (BoxCollaborationItem)mShareItem;
    }

    public static InviteCollaboratorsFragment newInstance(BoxCollaborationItem collaborationItem) {
        return newInstance(collaborationItem, true);
    }

    public static InviteCollaboratorsFragment newInstance(BoxCollaborationItem collaborationItem, boolean useContactsProvider) {
        Bundle args = BoxFragment.getBundle(collaborationItem);
        InviteCollaboratorsFragment fragment = new InviteCollaboratorsFragment();
        args.putBoolean(EXTRA_USE_CONTACTS_PROVIDER, useContactsProvider);
        fragment.setArguments(args);
        return fragment;
    }
}
