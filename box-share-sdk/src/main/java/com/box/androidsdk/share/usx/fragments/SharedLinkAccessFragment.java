package com.box.androidsdk.share.usx.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.databinding.UsxFragmentSharedLinkAccessBinding;

public class SharedLinkAccessFragment extends BoxFragment {

    private static final String DATE_FRAGMENT_TAG = "datePicker";
    private static final String PASSWORD_FRAGMENT_TAG = "passwordFrag";
    private static final String ACCESS_RADIAL_FRAGMENT_TAG = "accessFrag";

    private boolean mPasswordProtectedLinksSupported = false;
    UsxFragmentSharedLinkAccessBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.usx_fragment_shared_link_access, container, false);
        View view = binding.getRoot();
        return view;
    }
    
    public static SharedLinkAccessFragment newInstance(BoxItem boxItem) {
        Bundle args = BoxFragment.getBundle(boxItem);
        SharedLinkAccessFragment fragment = new SharedLinkAccessFragment();
        fragment.setArguments(args);
        return fragment;
    }


}
