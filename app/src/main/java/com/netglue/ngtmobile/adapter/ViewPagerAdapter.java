package com.netglue.ngtmobile.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ??? on 2018-01-21.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<FragmentInfo> mFragmentList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position).fragment;
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(int iconResId, String title, Fragment fragment) {
        FragmentInfo info = new FragmentInfo(iconResId, title, fragment);
        mFragmentList.add(info);
    }

    public void addFragment(String title, Fragment fragment) {
        addFragment(0, title, fragment);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentList.get(position).getTitle();
    }

    private static class FragmentInfo {
        private int iconResId;
        private String title;
        private Fragment fragment;

        public FragmentInfo(int iconResId, String title, Fragment fragment) {
            this.iconResId = iconResId;
            this.title = title;
            this.fragment = fragment;
        }

        public int getIconResId() {
            return iconResId;
        }

        public String getTitle() {
            return title;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }

}
