package com.box.androidsdk.share.usx.fragments;

import androidx.annotation.StringRes;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentCollaborationRolesBinding;
import com.box.androidsdk.share.fragments.CollaborationsFragment;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.CollaborationsShareVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SelectRoleShareVM;
import com.box.androidsdk.share.vm.ShareVMFactory;


public class CollaboratorsRolesFragment extends BoxFragment {

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_title_access_level));
        actionbarTitleVM.setSubtitle(null);
    }

    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

    public static final String TAG = CollaboratorsRolesFragment.class.getName();
    SelectRoleShareVM vm;
    private ViewModelProvider.Factory mShareVMFactory;
    CollaborationsShareVM mCollaborationsShareVM;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UsxFragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_collaboration_roles, container, false);
        View view = binding.getRoot();


        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(role -> {
            vm.setSelectedRole(role);
        });

        if (mShareVMFactory != null) {
            mCollaborationsShareVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(CollaborationsShareVM.class);
            vm.getSelectedRole().observe(this, role -> {
                if (role == vm.getCollaboration().getRole()) {
                    return; //do nothing if the updated role is the current role
                } else if (role == BoxCollaboration.Role.OWNER) {
                    showSpinner();
                    Toast.makeText(getContext(), role.toString(), Toast.LENGTH_LONG).show();
                } else {
                    showSpinner();
                    mCollaborationsShareVM.updateCollaboration(vm.getCollaboration(), role);
                    mCollaborationsShareVM.getUpdateCollaboration().observe(this, onUpdateCollaboration); //don't start observing until the first change
                }
            });


        }
        return view;
    }

    private Observer<PresenterData<BoxCollaboration>> onUpdateCollaboration = data -> {
        dismissSpinner();
        if (data.isSuccess()) {
            vm.setCollaboration(data.getData());
        } else {
            BoxLogUtils.e(CollaborationsFragment.class.getName(), "Update Collaborator request failed",
                    data.getException());
            if (data.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(data.getStrCode());
            }
            if (data.getException() instanceof BoxException) {
                logBoxException((BoxException) data.getException(), R.string.box_sharesdk_cannot_get_collaborators);
            }
        }
    };

    private void logBoxException(BoxException boxException, int res) {
        BoxLogUtils.nonFatalE("UpdateCollabError", getString(res)
                + boxException.getErrorType() + " " + boxException.getResponseCode(), boxException);
    }



    public void setShareVMFactory(ShareVMFactory shareVMFactory) {
        this.mShareVMFactory = shareVMFactory;
    }


    public static CollaboratorsRolesFragment newInstance(BoxItem item) {
        Bundle args = getBundle(item);
        CollaboratorsRolesFragment fragment =  new CollaboratorsRolesFragment();
        fragment.setArguments(args);

        return fragment;
    }
}
