package com.hech.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.String;
import java.lang.Number;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Susie on 11/28/14.
 */
public class SongReviews extends Fragment{
    private HashMap<String, Bundle> info;
    private ArrayList<String> order;
    private GridView reviewView;
    private View ReviewFragmentView;
    private Context context;
    private Fragment currentFrag = this;
    private HashMap<String, Number> songRev = new HashMap<String, Number>();
    private HashMap<String, Number> albumRev = new HashMap<String, Number>();
    private ArrayList<String> re = new ArrayList<String>();
    private ArrayList<String> lo = new ArrayList<String>();
    private ArrayList<Number> ra;
    //    private HashMap<String, Number> ra = new HashMap<String, Number>();
    private ArrayList<HashMap<String,String>> reviews = new ArrayList<HashMap<String,String>>();

    public SongReviews(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(!getArguments().getBoolean("is_album"))
            querySongReviews();
        else
            queryAlbumReviews();

        View view = inflater.inflate(R.layout.fragment_review, container, false);
        ReviewFragmentView = view;
        // Get the review view
        reviewView = (GridView)view.findViewById(R.id.review_list);
        context = getActivity().getApplicationContext();
        ReviewMapper reviewMap = new ReviewMapper(view.getContext(), info, order, currentFrag);
        reviewView.setAdapter(reviewMap);
        reviewView.setFocusableInTouchMode(true);
        reviewView.requestFocus();
        reviewView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    //Switch to subplaylist song view
                    Fragment subFragment = new Store_ViewPager();
                    FragmentManager fragmentManager = getFragmentManager();
                    if (subFragment != null) {
                        Log.d("ViewPager", "Switch: StoreView");
                        fragmentManager.beginTransaction().replace(R.id.frame_container,
                                subFragment).commit();
                    }
                    return true;
                }
                return false;
            }
        });

        return view;
    }



    public void querySongRatings()
    {

        Log.d("QuerySongRatings", " This should print 1");

        final String song = getArguments().getString("song_title");
        //Query the Song Ratings class to obtain all the
        ParseQuery<ParseObject>query= ParseQuery.getQuery("Ratings");
        //Query where the "Map_Songs_to_Reviews" has been initialized
        query.whereEqualTo("SongId", song);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> ratings, ParseException e) {
                if(!ratings.isEmpty()) {
                   // ra = new ArrayList<Number>(songRev.size());
                    Log.d("SongRatings: ", "IS NOT EMPTY");
                    Log.d("SongRatings: ", "Size is " + songRev.size());

                    for (int k = 0; k < ratings.size() && k < 20; k++) {
                        String check_login = ratings.get(k).getString("Login");
                        Log.d("Checklogin", check_login);
                        if (info.containsKey(check_login)) {

                            //Log.d("Song Rating Number: ", ((Integer) k).toString());
                           // Log.d("iNDEX: ", songRev.get(check_login).toString());
                           // Log.d("value: ", ratings.get(k).getNumber("Reviews").toString());
                            //ra.add(songRev.get(check_login).intValue(), ratings.get(k).getNumber("Reviews"));
                            Log.d("Rating", ratings.get(k).getNumber("Reviews").toString() );
                            info.get(check_login).putInt("Rating", ratings.get(k).getNumber("Reviews").intValue());
                        } else {
                            Bundle b = new Bundle();
                            b.putString("Review", "");
                            b.putInt("Rating", ratings.get(k).getNumber("Reviews").intValue());
                            info.put(check_login, b);
                            order.add(check_login);
                        }
                    }
                }
                else {
                    Log.d("SongRatings: ", "IS EMPTY");
                }
                ReviewMapper reviewMap = new ReviewMapper(getActivity().getApplicationContext(), info, order, currentFrag);
                reviewView.setAdapter(reviewMap);
            }
        });
    }


    public void queryAlbumRatings()
    {

        Log.d("QuerySongRatings", " This should print 1");

        final String album = getArguments().getString("album_name");
        //Query the Song Ratings class to obtain all the
        ParseQuery<ParseObject>query= ParseQuery.getQuery("Album_Ratings");
        //Query where the "Map_Songs_to_Reviews" has been initialized
        query.whereEqualTo("AlbumId", album);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> ratings, ParseException e) {
                if(!ratings.isEmpty()) {
                    //ra = new ArrayList<Number>(songRev.size());
                   // Log.d("SongRatings: ", "IS NOT EMPTY");
                   // Log.d("SongRatings: ", "Size is " + songRev.size());

                    for (int k = 0; k < ratings.size(); k++) {
                        String check_login = ratings.get(k).getString("Login");
                        if (info.containsKey(check_login)) {
                            info.get(check_login).putInt("Rating", ratings.get(k).getNumber("Reviews").intValue());
                            //Log.d("Song Rating Number: ", "K");
                            //ra.add(songRev.get(check_login).intValue(), ratings.get(k).getNumber("Reviews"));
                        } else {
                            Bundle b = new Bundle();
                            b.putString("Review", "");
                            b.putInt("Rating", ratings.get(k).getNumber("Reviews").intValue());
                            info.put(check_login, b);
                            order.add(check_login);
                        }
                    }
                }
                ReviewMapper reviewMap = new ReviewMapper(getActivity().getApplicationContext(), info, order, currentFrag);
                reviewView.setAdapter(reviewMap);
            }
        });
    }


    public void querySongReviews(){

        info = new HashMap<String, Bundle>();
        order = new ArrayList<String>();
        //TODO get the song name from the click!
        //Get the name of the song from when the user clicks
        final String song = getArguments().getString("song_title");
        int pos = 0;
        //Query the Users class to obtain all the
        //Sets up a query to DB to look at Users class to obtain the song reviews
        final ParseQuery<ParseObject>query = ParseQuery.getQuery("Users");
        //Query where the "Map_Songs_to_Reviews" has been initialized
        query.whereExists("Map_Songs_to_Reviews");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> songR, ParseException e) {

                for(int k = 0; k < songR.size(); k++){
                    HashMap<String,String> mapReviews = (HashMap <String,String>)songR.get(k).get("Map_Songs_to_Reviews");
                    if(mapReviews.containsKey(song)){
                        final String login = songR.get(k).getString("Login");
                        //Number pos = k;
                        Bundle b = new Bundle();
                        b.putString("Review", mapReviews.get(song));
                        b.putInt("Rating", 0);
                        //songRev.put(login, pos);

                        info.put(login, b);
                        //re.add(mapReviews.get(song));
                        //lo.add(login);
                        order.add(login);
                        Log.d("querySongReviews", mapReviews.get(song));
                    }
                }
                querySongRatings();

            }
        });
    }


    public void queryAlbumReviews(){

        info = new HashMap<String, Bundle>();
        order = new ArrayList<String>();
        //TODO get the album name from the click!
        //Get the name of the album from when the user clicks
        final String album = getArguments().getString("album_name");
        //Query the Users class to obtain all the
        //Sets up a query to DB to look at Users class to obtain the song reviews
        ParseQuery<ParseObject>query= ParseQuery.getQuery("Users");
        //Query where the "Map_Songs_to_Reviews" has been initialized
        query.whereExists("Map_Albums_to_Reviews");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> songR, ParseException e) {
                Log.d("AlbumReviews", "doneparseing");
                for(int k = 0; k < songR.size(); k++){
                    HashMap<String,String> mapReviews = (HashMap <String,String>)songR.get(k).get("Map_Albums_to_Reviews");
                    Log.d("AlbumReviews SongR size: " , ((Integer)songR.size()).toString());
                    Log.d("AlbumReviews mapReviews size: " , ((Integer)mapReviews.size()).toString());
                    Log.d("AlbumReviews checking: " , album);
                    if(mapReviews.containsKey(album)){
                        Log.d("AlbumReviews Contians", album);
                        final String login = songR.get(k).getString("Login");
                        Bundle b = new Bundle();
                        b.putString("Review", mapReviews.get(album));
                        b.putInt("Rating", 0);
                        info.put(login, b);
                        order.add(login);

                    }
                }
                queryAlbumRatings();
            }
        });
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
