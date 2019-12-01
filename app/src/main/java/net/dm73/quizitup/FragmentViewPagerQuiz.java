package net.dm73.quizitup;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class FragmentViewPagerQuiz extends FragmentPagerAdapter {

    private List<Fragment> fragments;

    public FragmentViewPagerQuiz(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return (fragments == null) ? 0 : fragments.size();
    }


}
