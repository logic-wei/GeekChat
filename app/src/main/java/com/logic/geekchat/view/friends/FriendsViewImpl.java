package com.logic.geekchat.view.friends;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.logic.geekchat.LogUtil;
import com.logic.geekchat.R;
import com.logic.geekchat.model.ChatClient.Friend;
import com.logic.geekchat.presenter.friends.FriendsPresenterImpl;
import com.logic.geekchat.presenter.friends.IFriendsPresenter;
import com.logic.geekchat.view.chat.ChatViewImpl;
import com.logic.geekchat.view.chat.IChatView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FriendsViewImpl extends Fragment implements IFriendsView {
    private static String TAG = "FriendsViewImpl";

    private IFriendsPresenter mFriendsPresenter;

    @BindView(R.id.friends_list)
    ListView mFriendsList;
    FriendsListAdapter mFriendsListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFriendsPresenter = new FriendsPresenterImpl(this);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.friends, container, false);
        ButterKnife.bind(this,view);
        ArrayList<Friend> defaultFriends = new ArrayList<>();
        defaultFriends.add(new Friend("no friend"));
        mFriendsListAdapter = new FriendsListAdapter(defaultFriends);
        mFriendsList.setAdapter(mFriendsListAdapter);
        mFriendsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), ChatViewImpl.class);
                intent.putExtra(IChatView.EXTRA_CHAT_WITH, mFriendsListAdapter.getItem(position).getId());
                LogUtil.i(TAG, "chat with:"+intent.getStringExtra(IChatView.EXTRA_CHAT_WITH));
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void setFriends(List<Friend> friends) {
        if (friends == null) {
            Toast.makeText(getContext(), "sync failed", Toast.LENGTH_SHORT).show();
            return;
        }
        mFriendsListAdapter.setFriends(friends);
    }

    private class FriendsListAdapter extends ArrayAdapter<Friend> {
        private List<Friend> mFriends;

        FriendsListAdapter(List<Friend> friends) {
            super(FriendsViewImpl.this.getContext(), R.layout.friend_item);
            mFriends = friends;
        }

        @Override
        public int getCount() {
            return mFriends.size();
        }

        @Nullable
        @Override
        public Friend getItem(int position) {
            return mFriends.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_item, parent, false);
            }
            if (convertView.getTag() == null) {
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.photoView = convertView.findViewById(R.id.image_view_photo);
                viewHolder.nameView = convertView.findViewById(R.id.text_view_name);
                convertView.setTag(viewHolder);
            }
            //初始化item
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.nameView.setText(mFriends.get(position).getId());
            viewHolder.photoView.setImageResource(R.drawable.ic_launcher_background);

            return convertView;
        }

        class ViewHolder {
            ImageView photoView;
            TextView nameView;
        }

        public void setFriends(List<Friend> friends) {
            mFriends = friends;
            notifyDataSetChanged();
        }
    }

    @Override
    public void onLogout(boolean ifSucceed) {
        if (ifSucceed) {
            Toast.makeText(getContext(), "succeed to logout", Toast.LENGTH_SHORT).show();
            if (getActivity() != null)
                getActivity().onBackPressed();
        } else {
            Toast.makeText(getContext(), "failed to logout", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friends_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.item_logout:
                mFriendsPresenter.logout();
                return true;
            case R.id.item_sync:
                mFriendsPresenter.syncFriends();
                return true;
            case R.id.item_add:
                addFriend();
                return true;
        }
        return false;
    }

    @Override
    public void onAddResult(String id, boolean ifSucceed) {
        if (ifSucceed) {
            Toast.makeText(getContext(), "succeed to add "+id+" as friend", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "failed to add "+id+" as friend", Toast.LENGTH_SHORT).show();
        }
    }

    private void addFriend() {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.add_friend, null, false);
        final EditText toAddEditText = view.findViewById(R.id.edit_text_to_add);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .setTitle("add friend")
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFriendsPresenter.addFriend(toAddEditText.getText().toString());
                    }
                })
                .create();
        alertDialog.show();
    }
}
