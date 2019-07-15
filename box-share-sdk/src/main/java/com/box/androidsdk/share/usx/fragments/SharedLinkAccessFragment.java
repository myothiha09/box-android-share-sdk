package com.box.androidsdk.share.usx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.models.BoxBookmark;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxFolder;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkAccessBinding;
import com.box.androidsdk.share.vm.ActionbarTitleVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.SharedLinkVM;


public class SharedLinkAccessFragment extends BoxFragment {

    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";

    SharedLinkVM mShareLinkVM;
    UsxFragmentSharedLinkAccessBinding binding;

    public interface SharedLinkAccessNotifiers {
        void notifyAccessLevelChange(BoxSharedLink.Access access);
        void notifyDownloadChange(boolean download);
    }


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

        View view = binding.getRoot();

        setTitles();

        mShareLinkVM = ViewModelProviders.of(getActivity(), mShareVMFactory).get(SharedLinkVM.class);
        setShareItem(mShareLinkVM.getShareItem());

        mShareLinkVM.getSharedLinkedItem().observe(this, onBoxItemComplete);
        mShareLinkVM.getItemInfo().observe(this, onBoxItemComplete);



        setupUi();
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return view;
    }


    private void setupUi() {
        binding.setViewModel(mShareLinkVM);
        binding.accessRadioGroup.setViewModel(mShareLinkVM);
        binding.accessRadioGroup.setShareItem(mShareLinkVM.getShareItem());
        if (mShareLinkVM.getActiveRadioButtons().isEmpty()) mShareLinkVM.setActiveRadioButtons(mShareLinkVM.generateActiveButtons());

        binding.setSharedLinkAccessNotifier(notifiers);
        binding.accessRadioGroup.setSharedLinkAccessNotifier(notifiers);

    }

    private SharedLinkAccessNotifiers notifiers = new SharedLinkAccessNotifiers() {
        @Override
        public void notifyAccessLevelChange(BoxSharedLink.Access access) {
            if (access != null && access != mShareLinkVM.getShareItem().getSharedLink().getEffectiveAccess()) {
                changeAccess(access);
            }

        }

        @Override
        public void notifyDownloadChange(boolean download) {
            changeDownloadPermission(download);
        }
    };

    /**
     * Modifies the share link access
     *
     * @param access the share link access level
     */
    private void changeAccess(final BoxSharedLink.Access access){
        if (access == null){
            // Should not be possible to get here.
            return;
        }

        showSpinner(R.string.box_sharesdk_updating_link_access, R.string.boxsdk_Please_wait);
        mShareLinkVM.changeAccessLevel((BoxCollaborationItem) mShareLinkVM.getShareItem(), access);
    }

    /**
     * Modifies the download permssion of the share item
     *
     * @param canDownload whether or not the item can be downloaded
     */
    private void changeDownloadPermission(boolean canDownload){
        try {
            mShareLinkVM.changeDownloadPermission((BoxCollaborationItem) mShareLinkVM.getShareItem(), canDownload);
        } catch (Exception e){
            showToast("Bookmarks do not have a permission that can be changed.");
        }
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

    private Observer<PresenterData<BoxItem>> onBoxItemComplete = boxItemPresenterData -> {
        dismissSpinner();
        if (boxItemPresenterData.isSuccess() && boxItemPresenterData.getData() != null) {
            //data might still be null if the original request was not BoxRequestItem
            setShareItem(boxItemPresenterData.getData());
        } else {
            if(boxItemPresenterData.getStrCode() != PresenterData.NO_MESSAGE) {
                showToast(boxItemPresenterData.getStrCode());
            }
        }
    };

    public void setShareItem(BoxItem item) {
        mShareLinkVM.setShareItem(item);
        binding.setShareItem(mShareLinkVM.getShareItem());
        binding.accessRadioGroup.setShareItem(mShareLinkVM.getShareItem());
    }
}
