package com.hech.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Store_RecommendFragment extends Fragment{
    private int totalDownloads = 0;
    private ArrayList<String> ownedSongs = new ArrayList<String>();
    private HashMap<String, Integer> genrePref = new HashMap<String, Integer>();
    private ArrayList<Song> unownedSongs = new ArrayList<Song>();
    private View StoreFragmentView;
    private GridView storeView;
    private Context context;
    private Fragment currentFrag;
    private ArrayList<Song> storeList = new ArrayList<Song>();
    private HashMap<String, Number> songPrices = new HashMap<String, Number>();

    public Store_RecommendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        setHasOptionsMenu(true);
        try {
            getOwnedSongs(getCurrentUser());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            getPreferences(ownedSongs);
//            for(Map.Entry<String, Integer> entry : genrePref.entrySet()){
//                Toast.makeText(getActivity().getApplicationContext(), entry.getKey()+" : "+entry.getValue(), Toast.LENGTH_SHORT).show();
//            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        Log.d("Recommend", "Owned List Size: " + totalDownloads);
        View view = inflater.inflate(R.layout.fragment_store,
                container, false);
        StoreFragmentView = view;
        // Get the store view
        context = getActivity().getApplicationContext();
        storeView = (GridView)view.findViewById(R.id.store_list);
        context = getActivity().getApplicationContext();
        getUnownedSongs();
        //sortByPref();
        StoreMapper songMap = new StoreMapper(view.getContext(), unownedSongs, songPrices, currentFrag);
        storeView.setAdapter(songMap);
        return view;
    }

    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return ((MainActivity)getActivity()).getUserLoggedin();
    }

    public ArrayList<String> getOwnedSongs(String user)
        throws ParseException {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
            query.whereEqualTo("Login", getCurrentUser());
            List<ParseObject> parseObjects = query.find();
            totalDownloads = parseObjects.size();
            for (int i = 0; i < totalDownloads; ++i) {
                ownedSongs.add(parseObjects.get(i).getString("song_Id"));
            }
        Log.d("Recommend", "Owned Songs List Retrieved");
        return ownedSongs;
    }

    public HashMap<String, Integer> getPreferences(ArrayList<String> songs)
        throws ParseException {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
            for (int i = 0; i < songs.size(); ++i) {
                query.whereEqualTo("Name", songs.get(i));
                List<ParseObject> parseObjects = query.find();
                String genre = parseObjects.get(0).getString("Genre");
                Integer count = genrePref.get(genre);
                if(count == null){
                    genrePref.put(genre, 1);
                }
                else{
                    genrePref.put(genre, count+1);
                }
            }
            return genrePref;
    }
    public void getUnownedSongs(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereLessThan("Price", 10);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseObjects, ParseException e) {
                for (int i = 0; i < parseObjects.size(); ++i) {
                    String name = parseObjects.get(i).getString("Name");
                    if(!ownedSongs.contains(name)) {
                        String artist = parseObjects.get(i).getString("Artist");
                        String album = parseObjects.get(i).getString("Album");
                        String genre = parseObjects.get(i).getString("Genre");
                        Number price = parseObjects.get(i).getNumber("Price");
                        Song s = new Song(0, name, artist, album);
                        unownedSongs.add(s);
                        //Log.d("Recommend", name+" Added");
                        songPrices.put(name, price);
                    }
                }
                StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), unownedSongs, songPrices, currentFrag);
                storeView.setAdapter(songMap);
            }
        });
    }
    public void sortByPref(){

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        currentFrag = this;
        super.onStart();
    }

}
