package com.logic.geekchat.view.friends;

import com.logic.geekchat.model.ChatClient.Friend;

import java.util.List;

public interface IFriendsView {
    void setFriends(List<Friend> friends);
    void onLogout(boolean ifSucceed);
    void onAddResult(String id, boolean ifSucceed);
}
