package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

/**
 * The ViewModel that is responsible for holding and retrieving data for CollaboratorsInitialsView
 */
public class CollaboratorsInitialsVM extends BaseShareVM {

    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;

    public CollaboratorsInitialsVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mCollaborations = Transformations.map(shareRepo.getCollaborations(), transformer::getIntialsViewCollabsPresenterData);
    }

    /**
     * Make a backend call through share repo to fetch collaborations for a shared item.
     * @param item the shared item
     */
    public void fetchCollaborations(BoxCollaborationItem item) {
        mShareRepo.fetchCollaborations(item);
    }

    /**
     * Returns a LiveData that holds information about the collaborations of an item.
     * @return a LiveData that holds information about the collaborations of an item
     */
    public LiveData<PresenterData<BoxIteratorCollaborations>> getCollaborations() {
        return mCollaborations;
    }
}
