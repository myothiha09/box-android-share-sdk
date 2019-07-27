package com.box.androidsdk.share.usx.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.usx.adapters.CollaboratorsAdapter;
import com.box.androidsdk.share.databinding.UsxFragmentCollaborationsBinding;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.CollaborationsShareVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;

import java.util.ArrayList;

public class CollaborationsFragment extends BoxFragment implements AdapterView.OnItemClickListener {

    protected static final String TAG = CollaborationsFragment.class.getName();
    private CollaboratorsAdapter mCollaboratorsAdapter;
    private CollaborationsFragmentCallback mCallback;
    private UsxFragmentCollaborationsBinding binding;
    private CollaborationsShareVM mCollaborationsShareVM;
    private SelectRoleShareVM mSelectRoleShareVM;

    public interface CollaborationsFragmentCallback {
        void notifySwitchToAccessRoleFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitles();

        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_collaborations, container, false);
        mCollaborationsShareVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(CollaborationsShareVM.class);
        mSelectRoleShareVM = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(mCollaborationsShareVM);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.collaboratorsList.setDivider(null);
        mCollaboratorsAdapter = new CollaboratorsAdapter(getActivity(), mCollaborationsShareVM);
        binding.collaboratorsList.setAdapter(mCollaboratorsAdapter);
        binding.collaboratorsList.setOnItemClickListener(this);


        mCollaborationsShareVM.getCollaborations().observe(this, onCollaborationsChange);
        mCollaborationsShareVM.getRoleItem().observe(this, onRoleItemChange);
        mCollaborationsShareVM.getUpdateCollaboration().observe(this ,onUpdateCollaboration);
        mCollaborationsShareVM.getUpdateOwner().observe(this, onUpdateOwnerCollaboration);
        mCollaborationsShareVM.getDeleteCollaboration().observe(this, onDeleteCollaboration);

        mCollaborationsShareVM.getItemInfo().observe(this, onBoxItemComplete);
        // Get serialized roles or fetch them if they are not available
        if (getItem().getAllowedInviteeRoles() == null) {
            fetchRoles();
        }
        return view;
    }

    //fragment is recreated when user click back so this will get triggered.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mSelectRoleShareVM.isRemoveSelected()) {
            showSpinner();
            mCollaborationsShareVM.deleteCollaboration(mSelectRoleShareVM.getCollaboration());
            mSelectRoleShareVM.setRemoveSelected(false);
        } else {
            if (mSelectRoleShareVM.getSelectedRole().getValue() != null && mSelectRoleShareVM.getCollaboration() != null) {
                if (mSelectRoleShareVM.getSelectedRole().getValue() != mSelectRoleShareVM.getCollaboration().getRole()) { //this means user selected a different role.
                    if (mSelectRoleShareVM.getSelectedRole().getValue() == BoxCollaboration.Role.OWNER) {
                        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.box_sharesdk_change_owner_alert_title)
                                .setMessage(R.string.box_sharesdk_change_owner_alert_message)
                                .setPositiveButton(android.R.string.yes, (d, which) -> {
                                    showSpinner(R.string.box_sharesdk_fetching_collaborators, R.string.boxsdk_Please_wait);
                                    mCollaborationsShareVM.updateOwner(mSelectRoleShareVM.getCollaboration());
                                }).setNegativeButton(android.R.string.no, (d, which) -> {}).setIcon(android.R.drawable.ic_dialog_alert).create();
                        dialog.show();
                    } else {
                        showSpinner();
                        mCollaborationsShareVM.updateCollaboration(mSelectRoleShareVM.getCollaboration(), mSelectRoleShareVM.getSelectedRole().getValue());

                    }
                    mSelectRoleShareVM.setSelectedRole(null);
                }
            }
        }

        if (mCollaborationsShareVM.getCollaborations().getValue() == null) {
            mCollaborationsShareVM.fetchItemInfo(mCollaborationsShareVM.getShareItem());
        }
    }

    public void setCallback(CollaborationsFragmentCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addResult(Intent data) {
//        data.putExtra(CollaborationUtils.EXTRA_COLLABORATIONS, mCollaborations);
        data.putExtra(CollaborationUtils.EXTRA_OWNER_UPDATED, mCollaborationsShareVM.isOwnerUpdated());
        //I commented these out since this could be passing too much data inside an Intent.
        super.addResult(data);
    }

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_shared_with));
        actionbarTitleVM.setSubtitle(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BoxCollaboration collaboration = (BoxCollaboration) mCollaboratorsAdapter.getItem(position);
        if (collaboration != null) {
            ArrayList<BoxCollaboration.Role> rolesArr = getRoles();
            BoxCollaborator collaborator = collaboration.getAccessibleBy();
            String collabId = collaborator == null ? "" : collaborator.getId();

            if ((rolesArr == null || rolesArr.size() == 0) && !collabId.equals(mCollaborationsShareVM.getUserId())) {
                showToast(R.string.box_sharesdk_cannot_get_collaborators);
                return;
            }

            BoxCollaboration.Role role = collaboration.getRole();
            String name;
            if (collaborator == null) {
                String email = collaboration.getInviteEmail();
                name = (email != null && !email.isEmpty()) ? email :getString(R.string.box_sharesdk_another_person);
            } else {
                name = collaborator.getName();
            }
            boolean allowOwner = getItem().getOwnedBy().getId().equals(mCollaborationsShareVM.getUserId());
            if (allowOwner){
                // currently changing owner only seems to be supported for folders (does not show up as a allowed invitee role).
                allowOwner = getItem() instanceof BoxFolder;
            }
            mSelectRoleShareVM.setSelectedRole(role);
            mSelectRoleShareVM.setRoles(rolesArr);
            mSelectRoleShareVM.setName(name);
            mSelectRoleShareVM.setAllowRemove(true);
            mSelectRoleShareVM.setAllowOwnerRole(allowOwner);
            mSelectRoleShareVM.setCollaboration(collaboration);
            mCallback.notifySwitchToAccessRoleFragment();
        }
    }

    public BoxCollaborationItem getItem() {
        return (BoxCollaborationItem) mCollaborationsShareVM.getShareItem();
    }

    /**
     * Executes the request to retrieve collaborations for the item
     */
    public void fetchCollaborations() {
        if (getItem() == null || SdkUtils.isBlank(getItem().getId())) {
            showToast(R.string.box_sharesdk_cannot_view_collaborations);
            return;
        }

        showSpinner(R.string.box_sharesdk_fetching_collaborators, R.string.boxsdk_Please_wait);
        mCollaborationsShareVM.fetchCollaborations(getItem());
    }

    /**
     * Executes the request to retrieve the available roles for the item
     */
    private void fetchRoles() {
        if (getItem() == null || SdkUtils.isBlank(getItem().getId())) {
            return;
        }

        showSpinner(R.string.box_sharesdk_fetching_collaborators, R.string.boxsdk_Please_wait);
        mCollaborationsShareVM.fetchRoles(getItem());
    }

    public ArrayList<BoxCollaboration.Role> getRoles() {
        if (getItem().getAllowedInviteeRoles() != null) {
            return getItem().getAllowedInviteeRoles();
        }
        return null;
    }


    public static CollaborationsFragment newInstance(BoxCollaborationItem collaborationItem, ShareVMFactory factory) {
        Bundle args = BoxFragment.getBundle(collaborationItem);
        CollaborationsFragment fragment = new CollaborationsFragment();
        fragment.setArguments(args);
        fragment.mShareVMFactory = factory;
        return fragment;
    }
    private Observer<PresenterData<BoxCollaboration>> onUpdateCollaboration = presenterData -> {
        dismissSpinner();
        if (presenterData.isSuccess()) {
            BoxCollaborator collaborator = presenterData.getData().getAccessibleBy();
            String collabId = collaborator == null ? "" : collaborator.getId();
            if (collabId.equals(mCollaborationsShareVM.getUserId())) { //updated permission of yourself so the list look might need to be updated to match the new permission
                mCollaborationsShareVM.fetchItemInfo(mCollaborationsShareVM.getShareItem());
            }
            mCollaboratorsAdapter.update(presenterData.getData());
        } else {
            BoxLogUtils.e(com.box.androidsdk.share.fragments.CollaborationsFragment.class.getName(), "Update Collaborator request failed",
                    presenterData.getException());
            if (presenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(presenterData.getStrCode());
            }
            if (presenterData.getException() instanceof BoxException) {
                logBoxException((BoxException) presenterData.getException(), R.string.box_sharesdk_cannot_get_collaborators);
            }
        }
    };

    private void logBoxException(BoxException boxException, int res) {
        BoxLogUtils.nonFatalE("UpdateCollabError", getString(res)
                + boxException.getErrorType() + " " + boxException.getResponseCode(), boxException);
    }


    private Observer<PresenterData<BoxItem>> onBoxItemComplete = presenterData -> {
        dismissSpinner();
        if (presenterData.isSuccess()) {
            mCollaborationsShareVM.setShareItem(presenterData.getData());
            fetchCollaborations();
        } else {
            if(presenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(presenterData.getStrCode());
            }
        }
    };


    private Observer<PresenterData<BoxIteratorCollaborations>> onCollaborationsChange = presenterData -> {
        dismissSpinner();
        if (presenterData.isSuccess()) {
            mCollaboratorsAdapter.setItems(presenterData.getData());
        } else {
            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch Collaborators request failed",
                    presenterData.getException());

            if (presenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(presenterData.getStrCode());
            }
            BoxLogUtils.nonFatalE("CollaborationsError", getString(R.string.box_sharesdk_cannot_get_collaborators)
                    + presenterData.getException(), presenterData.getException());
        }
    };

    private Observer<PresenterData<BoxCollaborationItem>> onRoleItemChange = presenterData -> {
        dismissSpinner();
        if (presenterData.isSuccess()) {
            mCollaborationsShareVM.setShareItem(presenterData.getData());
        } else {
            BoxLogUtils.e(com.box.androidsdk.share.fragments.CollaborationsFragment.class.getName(), "Fetch roles request failed",
                    presenterData.getException());
            showToast(presenterData.getStrCode());
        }
    };


    private Observer<PresenterData<BoxVoid>> onUpdateOwnerCollaboration = presenterData -> {
        dismissSpinner();
        if (presenterData.isSuccess()) {
            mCollaborationsShareVM.setOwnerUpdated(true);
            getActivity().finish();
        } else {
            BoxLogUtils.e(com.box.androidsdk.share.fragments.CollaborationsFragment.class.getName(), "Update Owner request failed",
                    presenterData.getException());
            if (presenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(presenterData.getStrCode());
            }
            BoxLogUtils.nonFatalE("UpdateOwner", getString(R.string.box_sharesdk_cannot_get_collaborators)
                    , presenterData.getException());
        }
    };

    private Observer<PresenterData<BoxRequest>> onDeleteCollaboration = presenterData -> {
        if (presenterData.isSuccess()) {
            BoxRequestsShare.DeleteCollaboration req = (BoxRequestsShare.DeleteCollaboration) presenterData.getData();
            mCollaboratorsAdapter.delete(req.getId());

            if (mCollaboratorsAdapter.getCount() == 0) {
                fetchCollaborations(); //this will force the view to refresh
            }
            //fetchCollaborations(); //refresh collaborations
        } else {
            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Delete Collaborator request failed",
                    presenterData.getException());
            showToast(presenterData.getStrCode());
        }
    };


}
