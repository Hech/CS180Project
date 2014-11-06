package com.hech.musicplayer;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StoreFragment extends Fragment {
    private ListView storeView;
    private View StoreFragmentView;
    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;

    private ArrayList<Song> storeList;
    private HashMap<String, Float> songPrices;
    private HashMap<String, Float> albumPrices;

    private ServiceConnection musicConnection = new ServiceConnection() {

        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicBound = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };



    public StoreFragment(){}
    @Override

    public void onStart(){
        super.onStart();

        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void songPicked(View view){
        confirmPayment(view.getTag().toString());

    }

    public void confirmPayment(String songName)
    {
        Float price = songPrices.get(songName);


        boolean confirmation = displayAndWaitForConfirm(songName, price);
        if(confirmation)
        {
            payment(getCurrentUser(), price);
        }
    }

    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState){
        getOnlineSongList();
        View view = inflater.inflate(R.layout.fragment_store,
                        container, false);
        StoreFragmentView = view;
        // Get the store view
        storeView = (ListView)view.findViewById(R.id.store_list);
        setHasOptionsMenu(true);

        SongMapper songMap = new SongMapper(view.getContext(), storeList);
        storeView.setAdapter(songMap);
        //Fragments need Click Listeners
        storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                songPicked(view);
            }

        });

        return view;
    }

    public void onDestroy(){
        getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.song, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Get all the songs from the database and show on the device
    public void getOnlineSongList() {
         storeList = null;
         songPrices = null;
         albumPrices = null;
    }
    //Display Price and get confirmation
    public boolean displayAndWaitForConfirm(String songName, float price)
    {
        return false;
    }

    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return "";
    }
    // Fetch the price of the song (needs to also work for albums)
    //public Map<String, Float> getSongPrice(String songId) {
    //}

    // Deducts the song price from the account balance
    public void payment(String user, Float songPrice) {
    }

    // Rate the song (5-star scale)
    public void rateSong() {
    }

    // Review the song (text review)
    public void reviewSong(String songId, String review) {
    }
}
