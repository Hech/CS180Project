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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Store_RecommendFragment extends Fragment{
    private int totalDownloads = 0;
    private ArrayList<String> ownedSongs = new ArrayList<String>();
    private HashMap<String, Integer> genrePref = new HashMap<String, Integer>();
    private ArrayList<Song> unownedSongs = new ArrayList<Song>();

//    private View StoreFragmentView;
    private GridView storeView;
//    private Context context;
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

        Log.d("Recommend", "Owned List Size: " + totalDownloads);
        View view = inflater.inflate(R.layout.fragment_store,
                container, false);
        // Get the store view
        storeView = (GridView)view.findViewById(R.id.store_list);
        getUnownedSongs();
        //Will load an empty list until the Parse background job is done
        StoreMapper songMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
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

    public void getUnownedSongs(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereLessThan("Price", 10);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<String> genreIndex = new ArrayList<String>();
                for (int i = 0; i < parseObjects.size(); ++i) {
                    String name = parseObjects.get(i).getString("Name");
                    if(!ownedSongs.contains(name)) {
                        String artist = parseObjects.get(i).getString("Artist");
                        String album = parseObjects.get(i).getString("Album");
                        Number price = parseObjects.get(i).getNumber("Price");
                        String genre = parseObjects.get(i).getString("Genre");

                        genreIndex.add(genre);
                        Song s = new Song(0, name, artist, album);
                        unownedSongs.add(s);
                        //Log.d("Recommend", name+" Added");
                        songPrices.put(name, price);
                    }
                    else{
                        String genre = parseObjects.get(i).getString("Genre");
                        Integer count = genrePref.get(genre);
                        if(count == null){
                            genrePref.put(genre, 1);
                        }
                        else{
                            genrePref.put(genre, count+1);
                        }
                        parseObjects.remove(i);
                        --i;
                    }
                }
                genrePref = sortByValue(genrePref);
                sortByPref(genreIndex);
                StoreMapper songMap = new StoreMapper(getActivity().getApplicationContext(),
                                                        storeList, songPrices, currentFrag);
                storeView.setAdapter(songMap);
            }
        });
    }
    public void sortByPref(ArrayList<String> genre){
        HashMap<String, Integer> sortMap = new HashMap<String, Integer>();
        for(int i = 0; i < unownedSongs.size(); ++i){
            Integer count = genrePref.get(genre.get(i));
            if(count == null){
                count = new Integer(0);
            }
            sortMap.put(unownedSongs.get(i).getTitle(), count);
        }
        sortMap = sortByValue(sortMap);
        ArrayList<String> sortedList = new ArrayList<String>(sortMap.keySet());
        while(!sortedList.isEmpty()){
            for(int i = 0; i < unownedSongs.size(); ++i){
                if(storeList.size() >= 10){
                    Log.d("Recommend", "Unowned List Weighted");
                    return;
                }
                if((unownedSongs.get(i).getTitle()).equals(sortedList.get(0))){
                    storeList.add(unownedSongs.get(i));
                    sortedList.remove(0);
                    Log.d("Recommend Weights: ", unownedSongs.get(i).getTitle() + " " + sortMap.get(unownedSongs.get(i).getTitle()));
                    break;
                }
            }
        }
        Log.d("Recommend", "Unowned List Weighted");

    }
    public HashMap sortByValue(HashMap genremap){
        List list = new LinkedList(genremap.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((Comparable) ((Map.Entry) (rhs)).getValue())
                        .compareTo(((Map.Entry) (lhs)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for(Iterator it = list.iterator(); it.hasNext();){
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
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
