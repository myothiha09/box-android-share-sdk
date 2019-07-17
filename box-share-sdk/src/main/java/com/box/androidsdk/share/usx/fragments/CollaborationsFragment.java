package com.box.androidsdk.share.usx.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequestsShare;
import com.box.androidsdk.content.requests.BoxResponse;
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

import java.net.HttpURLConnection;
import java.util.ArrayList;

public class CollaborationsFragment extends BoxFragment implements AdapterView.OnItemClickListener {

    protected static final String TAG = CollaborationsFragment.class.getName();
    protected CollaboratorsAdapter mCollaboratorsAdapter;
    protected BoxIteratorCollaborations mCollaborations;


    private boolean mOwnerUpdated = false;
    private CollaborationsFragmentCallback mCallback;
    UsxFragmentCollaborationsBinding binding;
    CollaborationsShareVM mCollaborationsShareVM;

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
        binding.setViewModel(mCollaborationsShareVM);
        binding.setLifecycleOwner(this);
        View view = binding.getRoot();
        binding.collaboratorsList.setDivider(null);
        mCollaboratorsAdapter = new CollaboratorsAdapter(getActivity(), getItem(), mCollaborationsShareVM);
        binding.collaboratorsList.setAdapter(mCollaboratorsAdapter);
        binding.collaboratorsList.setOnItemClickListener(this);


        mCollaborationsShareVM.getCollaborations().observe(this, onCollaborationsChange);
        mCollaborationsShareVM.getRoleItem().observe(this, onRoleItemChange);

        if (getArguments() != null){
            Bundle args = getArguments();
            mCollaborations = (BoxIteratorCollaborations)args.getSerializable(CollaborationUtils.EXTRA_COLLABORATIONS);
        }

        // Get serialized roles or fetch them if they are not available
        if (getItem().getAllowedInviteeRoles() == null) {
            fetchRoles();
        }
        return view;
    }

    private Observer<PresenterData<BoxIteratorCollaborations>> onCollaborationsChange = data -> {
            dismissSpinner();
            if (data.isSuccess()) {
                mCollaboratorsAdapter.setItems(data.getData());
            } else {
                BoxLogUtils.e(CollaborationsFragment.class.getName(), "Fetch Collaborators request failed",
                        data.getException());

                if (data.getStrCode() != PresenterData.NO_MESSAGE) {
                    showToast(data.getStrCode());
                }
                BoxLogUtils.nonFatalE("CollaborationsError", getString(R.string.box_sharesdk_cannot_get_collaborators)
                        + data.getException(), data.getException());
            }
    };

    private Observer<PresenterData<BoxCollaborationItem>> onRoleItemChange = data -> {
        dismissSpinner();
        if (data.isSuccess()) {
            mCollaborationsShareVM.setShareItem(data.getData());
        } else {
            BoxLogUtils.e(com.box.androidsdk.share.fragments.CollaborationsFragment.class.getName(), "Fetch roles request failed",
                    data.getException());
            showToast(data.getStrCode());
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        fetchCollaborations();
    }

    public void setCallback(CollaborationsFragmentCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void addResult(Intent data) {
        data.putExtra(CollaborationUtils.EXTRA_COLLABORATIONS, mCollaborations);
        data.putExtra(CollaborationUtils.EXTRA_OWNER_UPDATED, mOwnerUpdated);
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
        BoxCollaboration collaboration = (BoxCollaboration) view.getTag();
        if (collaboration != null) {
            ArrayList<BoxCollaboration.Role> rolesArr = getRoles();

            if (rolesArr == null || rolesArr.size() == 0) {
                SdkUtils.toastSafely(getContext(), R.string.box_sharesdk_cannot_get_collaborators, Toast.LENGTH_SHORT);
                return;
            }
            BoxCollaborator collaborator = collaboration.getAccessibleBy();
            BoxCollaboration.Role role = collaboration.getRole();
            //String name = collaborator == null ? getString(R.string.box_sharesdk_another_person) : collaborator.getName();
            boolean allowOwner = getItem().getOwnedBy().getId().equals(mCollaborationsShareVM.getUserId());
            if (allowOwner){
                // currently changing owner only seems to be supported for folders (does not show up as a allowed invitee role).
                allowOwner = getItem() instanceof BoxFolder;
            }
            SelectRoleShareVM selectRoleShareVM = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
            selectRoleShareVM.setSelectedRole(role);
            selectRoleShareVM.setRoles(rolesArr);
            selectRoleShareVM.setAllowRemove(true);
            selectRoleShareVM.setAllowOwnerRole(allowOwner);
            selectRoleShareVM.setCollaboration(collaboration);
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


    public static CollaborationsFragment newInstance(BoxCollaborationItem collaborationItem, BoxIteratorCollaborations collaborations) {
        Bundle args = BoxFragment.getBundle(collaborationItem);
        args.putSerializable(CollaborationUtils.EXTRA_COLLABORATIONS, collaborations);
        CollaborationsFragment fragment = new CollaborationsFragment();
        fragment.setArguments(args);
        return fragment;
    }

}
