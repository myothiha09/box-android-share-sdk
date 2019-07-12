package com.box.androidsdk.share.usx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkBinding;

/**
 * Created by varungupta on 3/5/2016.
 */
public class UsxFragment extends BoxFragment {

    private static final String UNSHARE_WARNING_TAG = "com.box.sharesdk.unshare_warning";

    private View.OnClickListener mOnEditAccessClickListener;
    private View.OnClickListener mOnInviteCollabsClickListener;
    private View.OnClickListener mOnCollabsClickListener;
    UsxFragmentSharedLinkBinding binding;

    /**
     * A speical interface used by UsxFragment only to give non string resources title and subtitle with theme changes.
     */
    public interface SpecialToolbar {
        void specialToolbar();
    }
    SpecialToolbar mSpecialToolbar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link, container, false);
        binding.setOnInviteCollabsClickListener(mOnInviteCollabsClickListener);
        binding.setOnEditAccessClickListener(mOnEditAccessClickListener);
        binding.initialViews.setArguments((BoxCollaborationItem) mShareItem, mController);
        binding.setOnCollabsListener(mOnCollabsClickListener);
        View view = binding.getRoot();
        mSpecialToolbar.specialToolbar();
        return view;
    }

    public void setOnEditLinkAccessButtonClickListener(View.OnClickListener onEditLinkAccessButtonClickListener) {
        this.mOnEditAccessClickListener = onEditLinkAccessButtonClickListener;
    }

    public void setOnInviteCollabsClickListener(View.OnClickListener onInviteCollabsClickListener) {
        this.mOnInviteCollabsClickListener = onInviteCollabsClickListener;
    }

    public void setOnCollabsListener(View.OnClickListener onInviteCollabsClickListener) {
        this.mOnCollabsClickListener = onInviteCollabsClickListener;
    }

    public void refreshInitialsViews() {
        if (binding !=  null && binding.initialViews != null) {
            binding.initialViews.refreshView();
        }

    }

    public static UsxFragment newInstance(BoxItem item) {
        Bundle args = BoxFragment.getBundle(item);
        UsxFragment fragment = new UsxFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setSpecialToolbar(SpecialToolbar toolbar) {
        this.mSpecialToolbar = toolbar;
    }


    @Override
    public int getFragmentTitle() {
        return R.string.box_sharesdk_title_access_level;
    }

    @Override
    public int getFragmentSubtitle() {
        return -1;
    }
}
