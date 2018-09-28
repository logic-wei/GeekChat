package com.logic.geekchat.friends;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.logic.geekchat.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FriendsViewImpl extends Fragment implements IFriendsMVP.IView {

    @BindView(R.id.recycler_view_friends)
    RecyclerView mFriendsRecyclerView;

    private IFriendsMVP.IPresenter mPresenter;

    @Override
    public void setFriends(List<Friend> friends) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.friends, container, false);
        ButterKnife.bind(this, view);
        initFriendsRecyclerView();
        return view;
    }

    void initFriendsRecyclerView() {
        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mFriendsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFriendsRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        mFriendsRecyclerView.setAdapter(new FriendsRVAdapter());
    }

    class FriendsRVAdapter extends RecyclerView.Adapter<FriendsRVAdapter.ViewHolder> {

        List<Friend> mFriends;

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView mNameTextView;
            ImageView mPhotoImageView;
            ViewHolder(View itemView) {
                super(itemView);
                mNameTextView = itemView.findViewById(R.id.text_view_name);
                mPhotoImageView = itemView.findViewById(R.id.image_view_photo);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.friend_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.mNameTextView.setText("hello"+i);
            viewHolder.mPhotoImageView.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_launcher_background));
        }

        @Override
        public int getItemCount() {
            return 99;
        }
    }
}
