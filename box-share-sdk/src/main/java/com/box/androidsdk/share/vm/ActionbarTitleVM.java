package com.box.androidsdk.share.vm;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The ViewModel that will be used for dynamically changing title based on the Fragment
 */
public class ActionbarTitleVM extends ViewModel {
    MutableLiveData<String> mTitle = new MutableLiveData<>();
    MutableLiveData<String> mSubtitle = new MutableLiveData<>();

    /**
     * Returns a LiveData that holds the title for Actionbar.
     * @return a LiveData that holds the title for Actionbar
     */
    public MutableLiveData<String> getTitle() {
        return mTitle;
    }

    /**
     * Set a new value for the title.
     * @param title the new title value
     */
    public void setTitle(String title) {
        this.mTitle.postValue(title);
    }

    /**
     * Returns a LiveData that holds the subtitle for Actionbar.
     * @return a LiveData that holds the subtitle for Actionbar
     */
    public MutableLiveData<String> getSubtitle() {
        return mSubtitle;
    }

    /**
     * Set a new value for the subtitle.
     * @return subtitle the new subtitle value
     */
    public void setSubtitle(String subtitle) {
        this.mSubtitle.postValue(subtitle);
    }
}
