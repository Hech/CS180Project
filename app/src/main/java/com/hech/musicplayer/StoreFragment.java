package com.hech.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class StoreFragment extends Fragment {
    private ListView storeView;
    private View StoreFragmentView;

    public StoreFragment(){}
    @Override
    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_store,
                        container, false);
        StoreFragmentView = view;
        // Get the store view
        storeView = (ListView)view.findViewById(R.id.store_list);

        setHasOptionsMenu(true);
        return view;
    }

    // Get all the songs from the database and show on the device
    public void getOnlineSongList() {

    }

    // Fetch the price of the song (needs to also work for albums)
    public void getSongPrice() {

    }

    // Deducts the song price from the account
    public void payment(String user) {
    }
}
