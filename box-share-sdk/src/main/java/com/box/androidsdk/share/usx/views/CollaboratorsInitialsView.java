package com.box.androidsdk.share.usx.views;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.BoxFutureTask;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.requests.BoxResponse;
import com.box.androidsdk.content.utils.BoxLogUtils;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.api.ShareController;
import com.box.androidsdk.share.usx.fragments.CollaborationsFragment;
import com.box.androidsdk.share.vm.CollaboratorsInitialsVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.box.androidsdk.share.vm.ShareVMFactory;
import com.eclipsesource.json.JsonObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * This is a custom view designed primarily like a container of other views to show the names of collaborators
 * with proper styling.
 */
public class CollaboratorsInitialsView extends LinearLayout {

    private LinearLayout mInitialsListView;
    protected BoxIteratorCollaborations mCollaborations;
    private BoxCollaborator mUnknownCollaborator;
    public static final String EXTRA_COLLABORATORS = "CollaboratorsInitialsView.ExtraCollaborators";
    public static final String EXTRA_SAVED_STATE = "CollaboratorsInitialsView.ExtraSaveState";

    private ProgressBar mProgressBar;
    private BoxResponse mBoxResponse;
    private TextView mCollabsCount;

    private CollaboratorsInitialsVM mCollaboratorsInitialsVM;

    public CollaboratorsInitialsView(Context context) {
        this(context, null);
    }

    public CollaboratorsInitialsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollaboratorsInitialsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setArguments(CollaboratorsInitialsVM vm) {
        mCollaboratorsInitialsVM = vm;
        mCollaboratorsInitialsVM.getCollaborations().observe((LifecycleOwner) getContext(), onCollaborationsChange);
    }

    /**
     * Sets up the child views
     */
    private void init() {
        inflate(getContext(), R.layout.usx_view_collaborators_initial, this);
        mInitialsListView = (LinearLayout) findViewById(R.id.invite_collaborator_initials_list);
        mProgressBar = findViewById(R.id.box_sharesdk_activity_progress_bar);
        mCollabsCount = findViewById(R.id.collabsCount);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add(BoxCollaborator.FIELD_NAME, "");
        mUnknownCollaborator = new BoxUser(jsonObject);

    }

    protected BoxCollaborationItem getCollaborationItem() {
        return (BoxCollaborationItem) mCollaboratorsInitialsVM.getShareItem();
    }

    public final String getString(@StringRes int resId) {
        return getResources().getString(resId);
    }

    /**
     * Executes the request to retrieve collaborations for the folder
     */
    public void fetchCollaborations() {
        if (mCollaboratorsInitialsVM == null) {
            return;
        }

        if (getCollaborationItem() == null || SdkUtils.isBlank(getCollaborationItem().getId())) {
            showToast(getContext(), getString(R.string.box_sharesdk_cannot_view_collaborations));
            return;
        }

        // Show spinner
        mProgressBar.setVisibility(VISIBLE);
        updateViewVisibilityForNoCollaborators();
        if (mBoxResponse == null) {
            // Execute request to fetch collaborators
            mCollaboratorsInitialsVM.fetchCollaborations(getCollaborationItem());
        }
    }

    public void refreshView() {
        mBoxResponse = null;
        fetchCollaborations();
    }
    private Observer<PresenterData<BoxIteratorCollaborations>> onCollaborationsChange = data -> {
        mProgressBar.setVisibility(GONE);
        if (data.isSuccess()) {
            updateView(data.getData());
        } else {
            showToast(getContext(), getString(data.getStrCode()));
            if (data.getException() instanceof BoxException) {
                if (((BoxException)data.getException()).getResponseCode() == HTTP_NOT_FOUND) {
                    ((Activity)getContext()).finish();
                }
            }
        }
    };

    private void updateViewVisibilityForNoCollaborators() {
        mInitialsListView.setVisibility(GONE);
        mCollabsCount.setVisibility(GONE);
    }

    private void updateViewVisibilityIfCollaboratorsFound() {
        mInitialsListView.setVisibility(VISIBLE);
        mCollabsCount.setVisibility(VISIBLE);
    }

    private void updateView(BoxIteratorCollaborations boxIteratorCollaborations) {
        mCollaborations = boxIteratorCollaborations;
        if (mCollaborations == null || mCollaborations.size() == 0) {
            // There are no collaborators for mShareitem
            updateViewVisibilityForNoCollaborators();
            return;
        }

        updateViewVisibilityIfCollaboratorsFound();
        int bestKnownCollabsSize = mCollaborations.size();
        if (mCollaborations.fullSize() != null) {
            bestKnownCollabsSize = mCollaborations.fullSize().intValue();
        }
        final int totalCollaborators = bestKnownCollabsSize;
        final int remainingWidth = mInitialsListView.getWidth();
        final ArrayList<BoxCollaboration> collaborations = mCollaborations.getEntries();

        clearInitialsView();
        mInitialsListView.post(new Runnable() {
            @Override
            public void run() {
                //Add the first item to calculate the width
                final View initialsView = addInitialsToList(collaborations.get(0).getAccessibleBy());
                initialsView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    private boolean initialsAdded = false;
                    @Override
                    public void onGlobalLayout() {
                        if (initialsView.isShown() && !initialsAdded) {
                            initialsAdded = true;
                            int viewsCount = 6;
                            for (int i = 1; i < viewsCount && i < totalCollaborators; i++) {
                                View viewAdded = addInitialsToList(collaborations.get(i).getAccessibleBy());
                                if (i == viewsCount - 1) {
                                    // This is the last one, display count if needed
                                    int remaining = totalCollaborators - viewsCount;
                                    if (remaining > 0) {
                                        BoxAvatarView initials = (BoxAvatarView) viewAdded.findViewById(R.id.collaborator_initials);
                                        JsonObject jsonObject = new JsonObject();
                                        jsonObject.set(BoxCollaborator.FIELD_NAME, Integer.toString(remaining + 1));
                                        jsonObject.set(BoxCollaboration.FIELD_ID, "collab_initials_number_user");
                                        BoxUser numberUser = new BoxUser(jsonObject);
                                        initials.loadUser(numberUser, mCollaboratorsInitialsVM.getAvatarController());
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
        mCollabsCount.setText(getResources().getQuantityString(R.plurals.box_sharesdk_collaborators_count_plurals, totalCollaborators, totalCollaborators));
    }


    private void clearInitialsView() {
        mInitialsListView.removeAllViewsInLayout();
    }

    private View addInitialsToList(BoxCollaborator collaborator) {
        View layoutContainer =  LayoutInflater.from((Activity)getContext()).inflate(R.layout.usx_view_initials, null);
        BoxAvatarView initialsView = (BoxAvatarView) layoutContainer.findViewById(R.id.collaborator_initials);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.box_sharesdk_initials_offset);
        layoutContainer.setLayoutParams(layoutParams);
        if (collaborator == null) {
            initialsView.loadUser(mUnknownCollaborator, mCollaboratorsInitialsVM.getAvatarController());
        } else {
            initialsView.loadUser(collaborator, mCollaboratorsInitialsVM.getAvatarController());
        }
        mInitialsListView.addView(layoutContainer);
        return layoutContainer;
    }

    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable savedState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_COLLABORATORS, mBoxResponse);
        bundle.putParcelable(EXTRA_SAVED_STATE, savedState);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            mBoxResponse = (BoxResponse) bundle.getSerializable(EXTRA_COLLABORATORS);
            Parcelable savedState =  bundle.getParcelable(EXTRA_SAVED_STATE);
            super.onRestoreInstanceState(savedState);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.fetchCollaborations();
    }

    private void showToast(Context context, String mssg) {
        Toast.makeText(context, mssg, Toast.LENGTH_SHORT).show();
    }
}
