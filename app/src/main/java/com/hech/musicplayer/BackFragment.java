package com.hech.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Zach on 12/1/2014.
 */
public class BackFragment extends Fragment {
    String nextFrag;
    public void onStart(){
        super.onStart();
        nextFrag = getArguments().getString("Frag");
        if(nextFrag.equals("store")){
            getFragmentManager().beginTransaction().replace(R.id.frame_container,
                    new Store_ViewPager()).addToBackStack(null).commit();
        }

    }
}
