package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Susie on 11/29/14.
 */
public class Song_ViewPager extends Fragment {
    private ViewPager viewPager = null;


    class pagerAdapt extends FragmentStatePagerAdapter {
        private Bundle args;
        private FragmentManager fragManage = null;

        public pagerAdapt(FragmentManager fragmentManager, Bundle Args) {
            super(fragmentManager);
            fragManage = fragmentManager;
            args = Args;
        }

        @Override
        public Fragment getItem(int i) {
            android.app.Fragment choice = null;
            switch (i) {
                /*case 0:
                    choice = new BackFragment();
                    Bundle b = new Bundle();
                    b.putString("Frag", "store");
                    choice.setArguments(b);
                    break;*/
                case 0:
                    //TODO PRISCILLA PASS THE BUNDLE FROM THE STOREFRAGMENT TO STOREINFO
                    choice = new StoreInfo();
                    choice.setArguments(args);
                    break;
                case 1:
                    choice = new SongReviews();
                    choice.setArguments(args);
                    break;
            }
            return choice;
        }


        @Override
        /*The total number of tabs*/
        //TODO make 3 when hot list fragment is made
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            //if (position == 0)
              //  return "GoBack";
            if (position == 0) {
                return "Songs";
            }
            if (position == 1) {
                return "Reviews";
            }
            if (position == 2) {
                return "Other";
            }
            return "";
        }
    }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            View view = inflater.inflate(R.layout.viewpager_store,
                    container, false);
            viewPager = (ViewPager) view.findViewById(R.id.pager_store);
            //mAdapter = new CursorPagerAdapt er(getFragmentManager(), getClass(), null, null);
            FragmentManager fragmentManager = getFragmentManager();
            if (viewPager == null) {
                Toast.makeText(getActivity().getApplication(), "Null Pager", Toast.LENGTH_LONG).show();
            }
            viewPager.setCurrentItem(1);
            viewPager.setAdapter(new pagerAdapt(fragmentManager, getArguments()));
            return view;
        }

}
