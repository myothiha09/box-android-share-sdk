package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.box.androidsdk.share.utils.InviteCollabsTransformer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class InviteCollaboratorsVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockShareItem: BoxCollaborationItem = mock()


    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockInviteCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockGetInviteeTransformedResponse: PresenterData<BoxIteratorInvitees> = mock()
    private val mockFetchRolesTransformedResponse: PresenterData<BoxCollaborationItem> = mock()
    private val mockInviteCollabsTransformedResponse: InviteCollaboratorsPresenterData = mock()

    private val mockShareRepo: ShareRepo = mock()

    private lateinit var inviteCollabVM: InviteCollaboratorsShareVM
    private val mockTransformer: InviteCollabsTransformer = mock()


    @Before
    fun setup() {
        mockShareRepo()
        inviteCollabVM = InviteCollaboratorsShareVM(mockShareRepo, mockShareItem)

        attachObservers()
        mockTransformers()

    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.fetchRoleItem).thenReturn(MutableLiveData())
        whenever(mockShareRepo.inviteCollabsBatch).thenReturn(MutableLiveData())
        whenever(mockShareRepo.invitees).thenReturn(MutableLiveData())

        whenever(mockShareRepo.fetchRolesApi(mockShareItem)).then {
            val data = mockShareRepo.fetchRoleItem as MutableLiveData
            data.postValue(mockFetchRolesResponse)
        }

        whenever(mockShareRepo.inviteCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)).then {
            val data = mockShareRepo.inviteCollabsBatch as MutableLiveData
            data.postValue(mockInviteCollabsResponse)
        }

        whenever(mockShareRepo.getInviteesApi(mockShareItem, mockFilter)).then {
            val data = mockShareRepo.invitees as MutableLiveData
            data.postValue(mockGetInviteeResponse)
        }
    }
    private fun mockTransformers() {
        whenever(mockTransformer.getFetchRolesItemPresenterData(mockFetchRolesResponse)).thenReturn(mockFetchRolesTransformedResponse)
        whenever(mockTransformer.getInviteCollabsPresenterDataFromBoxResponse(mockInviteCollabsResponse)).thenReturn(mockInviteCollabsTransformedResponse)
        whenever(mockTransformer.getInviteesPresenterData(mockGetInviteeResponse)).thenReturn(mockGetInviteeTransformedResponse)
    }

    private fun attachObservers() {
        inviteCollabVM.fetchRoleItem.observeForever(mock())
        inviteCollabVM.invitees.observeForever(mock())
        inviteCollabVM.inviteCollabs.observeForever(mock())
    }

    @Test
    fun `test get fetch role item get transformed presenter data`() {
        assertNull(inviteCollabVM.fetchRoleItem.value)
        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchRolesApi(mockShareItem)

        assertEquals(inviteCollabVM.fetchRoleItem.value, mockFetchRolesTransformedResponse)
    }

}