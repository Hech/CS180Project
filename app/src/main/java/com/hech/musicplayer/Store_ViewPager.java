package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/*Layout Pager for Fragments*/
public class Store_ViewPager extends Fragment {
    ViewPager viewPager = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("StoreViewPager", "created");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.viewpager_store,
                container, false);
        viewPager = (ViewPager) view.findViewById(R.id.pager_store);
        FragmentManager fragmentManager = getFragmentManager();
        if(viewPager == null){
            Toast.makeText(getActivity().getApplication(), "Null Pager", Toast.LENGTH_LONG).show();
        }
        viewPager.setAdapter(new pagerAdapter(fragmentManager));
        Log.d("StoreViewPager", "adapter set");
        return view;
    }
}

class pagerAdapter extends FragmentStatePagerAdapter {
    public pagerAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }
    @Override
    public Fragment getItem(int i) {
        android.app.Fragment choice = null;
        switch (i) {
            case 0:
                choice = new StoreFragment();
                break;
            case 1:
                choice = new Store_RecommendFragment();
                break;
            default:
                break;
        }
        return choice;
    }
    @Override
    /*The total number of tabs*/
    public int getCount(){
        return 2;
    }
    @Override
    public CharSequence getPageTitle(int position){
       if(position == 0){
        return "Hot List";
       }
       if(position == 1){
        return "Recommended Songs";
       }
        return "";
    }
}