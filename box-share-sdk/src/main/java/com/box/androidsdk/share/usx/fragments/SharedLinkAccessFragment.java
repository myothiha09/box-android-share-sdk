package com.box.androidsdk.share.usx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkAccessBinding;
import com.box.androidsdk.share.utils.FragmentCallback;
import com.box.androidsdk.share.vm.AccessLevelShareVM;
import com.box.androidsdk.share.vm.ActionbarTitleVM;

import java.util.HashSet;

import static com.box.androidsdk.content.models.BoxSharedLink.Access.COLLABORATORS;
import static com.box.androidsdk.content.models.BoxSharedLink.Access.COMPANY;
import static com.box.androidsdk.content.models.BoxSharedLink.Access.OPEN;

public class SharedLinkAccessFragment extends BoxFragment {

    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";


    private boolean mPasswordProtectedLinksSupported = false;
    AccessLevelShareVM mAccessLevelShareVM;
    UsxFragmentSharedLinkAccessBinding binding;

    @Override
    protected void setTitles() {
        ActionbarTitleVM actionbarTitleVM = ViewModelProviders.of(getActivity()).get(ActionbarTitleVM.class);
        actionbarTitleVM.setTitle(getString(R.string.box_sharesdk_title_link_access));
        actionbarTitleVM.setSubtitle(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link_access, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        View view = binding.getRoot();

        setTitles();

        mAccessLevelShareVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(AccessLevelShareVM.class);
        setupUi();

        setupListeners();
        return view;
    }

    private void setupListeners() {

    }

    private void setupUi() {
        binding.accessRadioGroup.setViewModel(mAccessLevelShareVM);
        if (mAccessLevelShareVM.getSelectedAccess().getValue() == null) mAccessLevelShareVM.setSelectedAccess(mShareItem.getSharedLink().getEffectiveAccess());
        if (mAccessLevelShareVM.getActiveRadioButtons().isEmpty()) mAccessLevelShareVM.setActiveRadioButtons(mAccessLevelShareVM.generateActiveButtons());
    }





    public static SharedLinkAccessFragment newInstance(BoxItem boxItem) {
        Bundle args = BoxFragment.getBundle(boxItem);
        SharedLinkAccessFragment fragment = new SharedLinkAccessFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Callback for updating shared link access
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFragmentCallback != null) mFragmentCallback.callBack();
    }
}
