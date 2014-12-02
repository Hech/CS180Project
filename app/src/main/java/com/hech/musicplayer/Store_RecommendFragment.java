package com.hech.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class Store_RecommendFragment extends Fragment{
    public Store_RecommendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
        query.whereEqualTo("Login", getCurrentUser());
        setHasOptionsMenu(true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    Log.d("Recommend", "Songs List Retrieved");
                }else{
                    Log.d("Recommend", "No Songs Downloaded");
                }
            }
        });
        View view = inflater.inflate(R.layout.fragment_storerecommend,
                container, false);
        return view;
    }

    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return ((MainActivity)getActivity()).getUserLoggedin();
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
