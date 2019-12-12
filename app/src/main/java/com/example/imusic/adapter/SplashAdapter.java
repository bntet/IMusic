package com.example.imusic.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.List;

public class SplashAdapter extends FragmentStatePagerAdapter{
    private List<Fragment> fragmentList;
    public SplashAdapter(FragmentManager fm,List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }
    public Fragment getItem(int position){
        return fragmentList.get(position);
    }
    public int getCount(){
        return fragmentList.size();
    }
}
