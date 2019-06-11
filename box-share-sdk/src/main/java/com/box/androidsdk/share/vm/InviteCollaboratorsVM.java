package com.box.androidsdk.share.vm;

import android.arch.lifecycle.LiveData;

import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees;
import com.box.androidsdk.share.sharerepo.ShareRepoInterface;

public class InviteCollaboratorsVM extends BaseVM {


    public InviteCollaboratorsVM(ShareRepoInterface shareRepo, BoxCollaborationItem shareItem) {
        super(shareRepo, shareItem);
    }

    public LiveData<DataWrapper<BoxIteratorInvitees>> getInvitees(BoxCollaborationItem boxCollaborationItem, String filter) {
        return mShareRepo.getInvitees(boxCollaborationItem, filter);
    }

    public LiveData<InviteCollaboratorsDataWrapper> addCollabs(BoxCollaborationItem boxCollaborationItem, BoxCollaboration.Role selectedRole, String[] emails) {
        return mShareRepo.addCollabs(boxCollaborationItem, selectedRole, emails);
    }
}
