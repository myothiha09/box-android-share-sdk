package com.box.androidsdk.share.usx.fragments;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentCollaborationRolesBinding;
import com.box.androidsdk.share.utils.FragmentCallback;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.SelectRoleShareVM;


public class CollaboratorsRolesFragment extends Fragment {


    public interface RoleUpdateNotifier {
        void setRole(BoxCollaboration.Role role);
    }

    public static final String TAG = CollaboratorsRolesFragment.class.getName();
    SelectRoleShareVM vm;
    private FragmentCallback mFragmentCallback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        UsxFragmentCollaborationRolesBinding binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_collaboration_roles, container, false);
        View view = binding.getRoot();

        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_title_access_level));
        actionbarTitleVM.setSubtitle(null);

        vm = ViewModelProviders.of(getActivity()).get(SelectRoleShareVM.class);
        binding.setViewModel(vm);
        binding.setRoleUpdateNotifier(vm::setSelectedRole);
        return view;
    }


    public static CollaboratorsRolesFragment newInstance() {
        return new CollaboratorsRolesFragment();
    }

    public void setFragmentCallback(FragmentCallback callback) {
        this.mFragmentCallback = callback;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFragmentCallback != null) {
            mFragmentCallback.callBack();
        }
    }
}
