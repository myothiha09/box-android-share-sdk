package com.box.androidsdk.share.fragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.CollaborationUtils;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.FragmentCollaborationRolesBinding;
import com.box.androidsdk.share.vm.SelectRoleVM;
import com.box.androidsdk.share.vm.SelectRoleVMFactory;

import java.util.ArrayList;
import java.util.List;


public class CollaboratorsRolesFragment extends BoxFragment {

    public static final String ARGS_ROLES = "argsRoles";
    public static final String ARGS_SELECTED_ROLE = "argsSelectedRole";
    public static final String ARGS_NAME = "argsName";
    public static final String ARGS_ALLOW_REMOVE = "argsAllowRemove";
    public static final String ARGS_ALLOW_OWNER_ROLE = "argsAllowOwnerRole";
    public static final String ARGS_SERIALIZABLE_EXTRA = "argsTargetId";

    SelectRoleVM vm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_collaboration_roles, container, false);
        List<BoxCollaboration.Role> mRoles = (ArrayList<BoxCollaboration.Role>) getArguments().getSerializable(ARGS_ROLES);
        BoxCollaboration.Role mSelectedRole = (BoxCollaboration.Role) getArguments().getSerializable(ARGS_SELECTED_ROLE);
        boolean mAllowRemove = getArguments().getBoolean(ARGS_ALLOW_REMOVE);
        boolean mAllowOwnerRole = getArguments().getBoolean(ARGS_ALLOW_OWNER_ROLE);
        BoxCollaboration mCollaboration = (BoxCollaboration) getArguments().getSerializable(ARGS_SERIALIZABLE_EXTRA);

        View view = binding.getRoot();

        SelectRoleVMFactory factory = new SelectRoleVMFactory(mRoles, mAllowOwnerRole, mSelectedRole, mAllowRemove, mCollaboration);
        vm = ViewModelProviders.of(getActivity(), factory).get(SelectRoleVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(vm::setSelectedRole);
        return view;
    }

    public static CollaboratorsRolesFragment newInstance(BoxCollaborationItem item, ArrayList<BoxCollaboration.Role> roles, BoxCollaboration.Role selectedRole, String name, boolean allowRemove, boolean allowOwnerRole, BoxCollaboration collaboration) {
        CollaboratorsRolesFragment fragment = new CollaboratorsRolesFragment();

        Bundle b = getBundle(item);
        b.putSerializable(ARGS_ROLES, roles);
        b.putSerializable(ARGS_SELECTED_ROLE, selectedRole);
        b.putString(ARGS_NAME, name);
        b.putBoolean(ARGS_ALLOW_REMOVE, allowRemove);
        b.putBoolean(ARGS_ALLOW_OWNER_ROLE, allowOwnerRole);
        b.putSerializable(ARGS_SERIALIZABLE_EXTRA, collaboration);
        fragment.setArguments(b);

        return fragment;
    }
    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

    @Override
    public void addResult(Intent data) {
        super.addResult(data);
        data.putExtra(ARGS_ROLES, vm.getSelectedRole());
    }
}
