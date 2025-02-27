package com.box.androidsdk.share.usx.views;


import android.app.Activity;
import android.content.Context;
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

import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.models.BoxCollaboration;
import com.box.androidsdk.content.models.BoxCollaborationItem;
import com.box.androidsdk.content.models.BoxCollaborator;
import com.box.androidsdk.content.models.BoxIteratorCollaborations;
import com.box.androidsdk.content.models.BoxUser;
import com.box.androidsdk.content.utils.SdkUtils;
import com.box.androidsdk.content.views.BoxAvatarView;
import com.box.androidsdk.share.R;
import com.box.androidsdk.share.vm.CollaboratorsInitialsVM;
import com.box.androidsdk.share.vm.PresenterData;
import com.eclipsesource.json.JsonObject;


import java.util.ArrayList;
import java.util.List;

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
    private TextView mCollabsCount;
    private int mViewCount = -1;

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

    public void setArguments(CollaboratorsInitialsVM vm, int viewCount) {
        mCollaboratorsInitialsVM = vm;
        mCollaboratorsInitialsVM.getCollaborations().observe((LifecycleOwner) getContext(), onCollaborationsChange);
        mViewCount = viewCount;
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
        mCollabsCount.setVisibility(GONE);
        mInitialsListView.setVisibility(GONE);
        mCollaboratorsInitialsVM.fetchCollaborations(getCollaborationItem());
    }

    public void refreshView() {
        fetchCollaborations();
    }
    private Observer<PresenterData<BoxIteratorCollaborations>> onCollaborationsChange = presenterData -> {
        if (!presenterData.isHandled()) {
            if (presenterData.isSuccess()) {
                updateView(presenterData.getData());
            } else {
                showToast(getContext(), getString(presenterData.getStrCode()));
                if (presenterData.getException() instanceof BoxException) {
                    if (((BoxException)presenterData.getException()).getResponseCode() == HTTP_NOT_FOUND) {
                        ((Activity)getContext()).finish();
                    }
                }
            }
        } else {
            PresenterData<BoxIteratorCollaborations> boxCollaborations = mCollaboratorsInitialsVM.getCollaborations().getValue();
            if (boxCollaborations != null) {
                updateView(boxCollaborations.getData());
            }
        }

    };

    private void updateViewVisibilityForNoCollaborators() {
        mInitialsListView.setVisibility(GONE);
        mCollabsCount.setVisibility(VISIBLE);
        mCollabsCount.setText(R.string.box_sharesdk_no_collaborators);
    }

    private void updateViewVisibilityIfCollaboratorsFound() {
        mInitialsListView.setVisibility(VISIBLE);
        mCollabsCount.setVisibility(VISIBLE);
    }

    private void updateView(BoxIteratorCollaborations boxIteratorCollaborations) {
        mProgressBar.setVisibility(GONE);
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
        final ArrayList<BoxCollaboration> collaborations = mCollaborations.getEntries();


        clearInitialsView();
        if (mViewCount == -1) { //use the default logic (for BoxItemInfoFragment in the future)
            final int remainingWidth = mInitialsListView.getWidth();
            System.out.println(remainingWidth);
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
                                int viewWidth = initialsView.getWidth();
                                int viewsCount = remainingWidth / viewWidth;
                                int addedViewCount = 0;

                                clearInitialsView(); //resetting the initialsView since width is calculated already and the first collab might not be a valid collab to be shown in the initials

                                View viewAdded = null;
                                for (int i = 0; addedViewCount < viewsCount && i < collaborations.size(); i++) {
                                    BoxCollaborator collaborator = collaborations.get(i).getAccessibleBy();
                                    if (collaborator != null) {
                                        viewAdded = addInitialsToList(collaborator);
                                        addedViewCount++;
                                    }
                                }
                                if (addedViewCount < totalCollaborators) {
                                    int remaining = totalCollaborators - addedViewCount;
                                    if (addedViewCount < mViewCount || viewAdded == null) {
                                        viewAdded = addInitialsToList(null); //create a view to not replace the current collab.
                                    } else {
                                        remaining++; //a known collab will be replaced so count him as remaining
                                    }
                                    BoxAvatarView initials = (BoxAvatarView) viewAdded.findViewById(R.id.collaborator_initials);
                                    JsonObject jsonObject = new JsonObject();
                                    jsonObject.set(BoxCollaborator.FIELD_NAME, Integer.toString(remaining));
                                    jsonObject.set(BoxCollaboration.FIELD_ID, "collab_initials_number_user");
                                    BoxUser numberUser = new BoxUser(jsonObject);
                                    initials.loadUser(numberUser, mCollaboratorsInitialsVM.getAvatarController());
                                }
                            }
                        }
                    });
                }
            });
        } else {
            int addedViewCount = 0;
            View viewAdded = null;
            for (int i = 0; i < collaborations.size() && addedViewCount < mViewCount; i++) {
                BoxCollaborator collaborator = collaborations.get(i).getAccessibleBy();
                if (collaborator != null) {
                    viewAdded = addInitialsToList(collaborator);
                    addOffset(viewAdded);
                    addedViewCount++;
                }
            }
            if (addedViewCount < totalCollaborators) {
                int remaining = totalCollaborators - addedViewCount;
                if (addedViewCount < mViewCount || viewAdded == null) { //viewAdded == null should never be true but added that just in case
                    viewAdded = addInitialsToList(null); //create a view to not replace the current collab.
                    addOffset(viewAdded);
                } else {
                    remaining++; //a known collab will be replaced so count him as remaining
                }
                BoxAvatarView initials = (BoxAvatarView) viewAdded.findViewById(R.id.collaborator_initials);
                JsonObject jsonObject = new JsonObject();
                jsonObject.set(BoxCollaborator.FIELD_NAME, Integer.toString(remaining));
                jsonObject.set(BoxCollaboration.FIELD_ID, "collab_initials_number_user");
                BoxUser numberUser = new BoxUser(jsonObject);
                initials.loadUser(numberUser, mCollaboratorsInitialsVM.getAvatarController());
            }
        }

        mCollabsCount.setText(getResources().getQuantityString(R.plurals.box_sharesdk_collaborators_count_plurals, totalCollaborators, totalCollaborators));
    }

    private void addOffset(View view) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) getResources().getDimension(R.dimen.box_sharesdk_initials_offset);
        view.setLayoutParams(layoutParams);
    }

    private void clearInitialsView() {
        mInitialsListView.removeAllViewsInLayout();
    }

    private View addInitialsToList(BoxCollaborator collaborator) {
        View layoutContainer =  LayoutInflater.from((Activity)getContext()).inflate(R.layout.usx_view_initials, null);
        BoxAvatarView initialsView = (BoxAvatarView) layoutContainer.findViewById(R.id.collaborator_initials);
        // initialsView.setBackground(getResources().getDrawable(R.drawable.ic_box_sharesdk_circle_bg));
        if (collaborator == null) {
            initialsView.loadUser(mUnknownCollaborator, mCollaboratorsInitialsVM.getAvatarController());
        } else {
            initialsView.loadUser(collaborator, mCollaboratorsInitialsVM.getAvatarController());
        }
        mInitialsListView.addView(layoutContainer);
        return layoutContainer;
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mCollaboratorsInitialsVM != null && mCollaboratorsInitialsVM.getCollaborations().getValue() == null) {
            this.fetchCollaborations();
        }

    }

    private void showToast(Context context, String mssg) {
        Toast.makeText(context, mssg, Toast.LENGTH_SHORT).show();
    }
}
