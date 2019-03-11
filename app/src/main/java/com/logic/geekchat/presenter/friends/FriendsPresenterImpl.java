package com.logic.geekchat.presenter.friends;

import com.logic.geekchat.LogUtil;
import com.logic.geekchat.model.ChatClient.ChatClient;
import com.logic.geekchat.model.ChatClient.IChatClient;
import com.logic.geekchat.view.friends.IFriendsView;

public class FriendsPresenterImpl implements IFriendsPresenter {
    private static String TAG = "FriendsPresenterImpl";

    private IFriendsView mFriendsView;
    private IChatClient mChatClient;

    public FriendsPresenterImpl(IFriendsView friendsView) {
        mFriendsView = friendsView;
        mChatClient = ChatClient.getInstance();
    }

    @Override
    public void syncFriends() {
        LogUtil.i(TAG, "syncFriends");
        mChatClient.syncFriends(new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mFriendsView.setFriends(mChatClient.getFriends());
                        break;
                    case RESULT_FAILED:
                        mFriendsView.setFriends(null);
                        break;
                }
            }
        });
    }

    @Override
    public void logout() {
        LogUtil.i(TAG, "logout");
        mChatClient.logout(new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mFriendsView.onLogout(true);
                        break;
                    case RESULT_FAILED:
                        mFriendsView.onLogout(false);
                        break;
                }
            }
        });
    }

    @Override
    public void addFriend(final String id) {
        mChatClient.addFriend(id, new IChatClient.ResultListener() {
            @Override
            public void onResult(int result) {
                switch (result) {
                    case RESULT_SUCCEED:
                        mFriendsView.setFriends(mChatClient.getFriends());
                        mFriendsView.onAddResult(id, true);
                        break;
                    case RESULT_FAILED:
                        mFriendsView.onAddResult(id, false);
                        break;
                }
            }
        });
    }
}
