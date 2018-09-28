package com.logic.geekchat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.logic.geekchat.friends.FriendsViewImpl;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.view_pager_main)
    ViewPager mMainViewPager;
    MainPagerAdapter mMainPagerAdapter;
    List<Fragment> mFragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFragments.add(new FriendsViewImpl());
        mFragments.add(new FriendsViewImpl());
        mMainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), mFragments);
        mMainViewPager.setAdapter(mMainPagerAdapter);
    }

    private class MainPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> mFragments;

        MainPagerAdapter(FragmentManager manager, List<Fragment> fragments) {
            super(manager);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "message";
                case 1:
                    return "test";
            }
            return "hello";
        }
    }

}
