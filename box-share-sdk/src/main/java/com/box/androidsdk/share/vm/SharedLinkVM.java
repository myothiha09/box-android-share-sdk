package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxSharedLink;
import com.box.androidsdk.share.internal.models.BoxFeatures;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

import java.sql.Date;

public class SharedLinkVM extends BaseShareVM {

    private final LiveData<PresenterData<BoxItem>> mShareLinkedItem;



    public SharedLinkVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mShareLinkedItem = Transformations.map(shareRepo.getShareLinkedItem(),
                response -> transformer.getSharedLinkItemPresenterData(response, getShareItem()));
    }

    public void createDefaultSharedLink(BoxCollaborationItem item) {
        mShareRepo.createDefaultSharedLink(item);
    }

    public void disableSharedLink(BoxCollaborationItem item) {
        mShareRepo.disableSharedLink(item);
    }


    public LiveData<PresenterData<BoxItem>> getSharedLinkedItem() {
        return mShareLinkedItem;
    }

}
