package com.twt.service.wenjin.ui.main;

import com.twt.service.wenjin.support.LogHelper;

/**
 * Created by M on 2015/3/20.
 */
public class MainPresenterImpl implements MainPresenter {

    private MainView mMainView;

    public MainPresenterImpl(MainView MainView) {
        this.mMainView = MainView;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
            mMainView.replaceFragment(position);
            mMainView.setMainTitle(position);
    }

    @Override
    public void selectItem(int position) {
        if (position < 20) {
//            mMainView.sendDrawerItemClickedEvent(position);
        } else if (position == 21) {
            // start SettingsActivity
            mMainView.startSettingsActivity();
        } else if (position == 22) {
            // start FeedbackActivity
            mMainView.startFeedbackActivity();
        } else if (position == 23) {
            mMainView.startLoginActivity();
        }
    }
}
