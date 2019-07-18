package com.box.androidsdk.share.vm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxVoid;
import com.box.androidsdk.content.requests.BoxRequest;
import com.box.androidsdk.share.sharerepo.ShareRepo;
import com.box.androidsdk.share.utils.ShareSDKTransformer;

/**
 * The ViewModel that is responsible for holding/retrieving/modifying data for Collaborations screen
 */
public class CollaborationsShareVM extends BaseShareVM{
    private final LiveData<PresenterData<BoxRequest>> mDeleteCollaboration;
    private final LiveData<PresenterData<BoxVoid>> mUpdateOwner;
    private final LiveData<PresenterData<BoxCollaboration>> mUpdateCollaboration;
    private final LiveData<PresenterData<BoxCollaborationItem>> mRoleItem;
    private final LiveData<PresenterData<BoxIteratorCollaborations>> mCollaborations;
    private boolean mOwnerUpdated;

    public CollaborationsShareVM(ShareRepo shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
        ShareSDKTransformer transformer = new ShareSDKTransformer();
        mCollaborations = Transformations.map(shareRepo.getCollaborations(), transformer::getCollaborationsPresenterData);
        mDeleteCollaboration = Transformations.map(shareRepo.getDeleteCollaboration(), transformer::getDeleteCollaborationPresenterData);
        mUpdateOwner = Transformations.map(shareRepo.getUpdateOwner(), transformer::getUpdateOwnerPresenterData);
        mUpdateCollaboration = Transformations.map(shareRepo.getUpdateCollaboration(), transformer::getUpdateCollaborationPresenterData);
        mRoleItem = Transformations.map(shareRepo.getRoleItem(),  transformer::getFetchRolesItemPresenterData);
        mOwnerUpdated = false;
    }

    /**
     * Make a backend call through share repo to delete a collaboration.
     * @param collaboration the collaboration that will be deleted
     */
    public void deleteCollaboration(BoxCollaboration collaboration) {
        mShareRepo.deleteCollaboration(collaboration);
    }

    /**
     * Make a backend call through share repo to update a collaboration's role.
     * @param collaboration the collaboration that will be updated
     * @param role the new role for the collaboration
     */
    public void updateCollaboration(BoxCollaboration collaboration, BoxCollaboration.Role role) {
        mShareRepo.updateCollaboration(collaboration, role);
    }

    /**
     * Make a backend call through share repo to update the owner of a collaboration. This will change the owner of the collaboration item associated with the collaboration.
     * @param collaboration the collaboration that will be updated
     */
    public void updateOwner(BoxCollaboration collaboration) {
        mShareRepo.updateOwner(collaboration);
    }

    /**
     * Make a backend call through share repo to fetch collaborations for a shared item.
     * @param item the shared item
     */
    public void fetchCollaborations(BoxCollaborationItem item) {
        mShareRepo.fetchCollaborations(item);
    }

    /**
     * Returns a LiveData that holds information about the request made for deleted collaboration.
     * @return a LiveData that holds information about the request made for deleted collaboration
     */
    public LiveData<PresenterData<BoxRequest>> getDeleteCollaboration() {
        return mDeleteCollaboration;
    }

    /**
     * Returns a LiveData that holds a dummy object for notifying ownership transfer.
     * @return a LiveData that holds a dummy object for notifying ownership transfer
     */
    public LiveData<PresenterData<BoxVoid>> getUpdateOwner() {
        return mUpdateOwner;
    }

    /**
     * Returns a LiveData that holds information about the updated collaboration.
     * @return a LiveData that holds information about the updated collaboration
     */
    public LiveData<PresenterData<BoxCollaboration>> getUpdateCollaboration() {
        return mUpdateCollaboration;
    }

    /**
     * Returns a LiveData that holds information about the collaborations of an item.
     * @return a LiveData that holds information about the collaborations of an item
     */
    public LiveData<PresenterData<BoxIteratorCollaborations>> getCollaborations() {
        return mCollaborations;
    }

    /**
     * Returns true if owner was updated.
     * @return true if owner was updated
     */
    public boolean isOwnerUpdated() {
        return mOwnerUpdated;
    }

    /**
     * Set a new value for whether owner was updated or not.
     * @param ownerUpdated the new value for ownerUpdated status
     */
    public void setOwnerUpdated(boolean ownerUpdated) {
        this.mOwnerUpdated = ownerUpdated;
    }

    /**
     * Makes a backend call through share repo for fetching roles.
     * @param item the item to fetch roles on
     */
    public void fetchRoles(BoxCollaborationItem item) {
        mShareRepo.fetchRolesFromRemote(item);
    }

    /**
     * Returns a LiveData which holds a data wrapper that contains a box item that has allowed roles for invitees and a string resource code.
     * @return a LiveData which holds a data wrapper that contains box item that has allowed roles for invitees and a string resource code
     */
    public LiveData<PresenterData<BoxCollaborationItem>> getRoleItem() {
        return mRoleItem;
    }



}
