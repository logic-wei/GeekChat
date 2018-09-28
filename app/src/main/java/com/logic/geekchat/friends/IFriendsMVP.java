package com.logic.geekchat.friends;

import java.util.List;

public interface IFriendsMVP {
    interface IModel {

    }

    interface IView {
        void setFriends(List<Friend> friends);
    }

    interface IPresenter {
        void syncFriends();
    }
}
