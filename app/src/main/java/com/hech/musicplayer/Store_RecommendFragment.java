package com.hech.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Map;

public class Store_RecommendFragment extends Fragment{
    ArrayList<String> ownedSongs = new ArrayList<String>();
    HashMap<String, Integer> genrePref = new HashMap<String, Integer>();
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
            for(Map.Entry<String, Integer> entry : genrePref.entrySet()){
                Toast.makeText(getActivity().getApplicationContext(), entry.getKey()+" : "+entry.getValue(), Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        Log.d("Recommend", "List Complete Size: " + ownedSongs.size());
        //getPreference(ownedSongs);
        View view = inflater.inflate(R.layout.fragment_storerecommend,
                container, false);
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
            for (int i = 0; i < parseObjects.size(); ++i) {
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
        super.onStart();
    }

}
