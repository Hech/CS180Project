package com.hech.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Susie on 11/29/14.
 */
public class ReviewMapper extends BaseAdapter {

    //private ArrayList<String> reviews;
   // private ArrayList<String> logins;
   // private ArrayList<Number> ratings;
    //    private HashMap<String, Number> ratings;
    HashMap<String, Bundle> info;
    ArrayList<String> order;
    private LayoutInflater revInf;
    private Fragment f;

    public ReviewMapper(Context c, HashMap<String, Bundle> Info, ArrayList<String> Order, Fragment fragment){
        //reviews = re;
        //logins = lo;
        //ratings = ra;
        info = Info;
        order = Order;
        revInf=LayoutInflater.from(c);
        f = fragment;
    }
    @Override
    public int getCount() {
        if(info.size() ==  0)
        {
            Log.d("ReviewMapper", "There are no reviews for this song");
            return 0;
        }
        return info.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //map review to the layout
        RelativeLayout reviewLay = (RelativeLayout)revInf.inflate
                (R.layout.reviews_list, parent, false);


        //get login and review views
        //if(ratings == null)
       //     rate = 1;
       // else
       //     rate = ratings.get(position).floatValue();
        Log.d("Rating", ((Integer) info.get(order.get(position)).getInt("Rating")).toString());
        final float rate = info.get(order.get(position)).getInt("Rating");
        RatingBar ratingBar = (RatingBar)reviewLay.findViewById(R.id.ratingBarS);
        ratingBar.setFocusableInTouchMode(false);
        ratingBar.setFocusable(false);
        ratingBar.setOnTouchListener(null);
        ratingBar.setOnClickListener(null);
        Log.d("Rating Bar: ", "Stars should be: " + rate);
        ratingBar.setRating(rate);
        final TextView revView = (TextView)reviewLay.findViewById(R.id.review);
        TextView loginView = (TextView)reviewLay.findViewById(R.id.login);
        //get title and artist strings
        revView.setText(info.get(order.get(position)).getString("Review"));
        loginView.setText(order.get(position));


        //Log.d("Review: ", logins.get(position) + ": " + reviews.get(position));
        //set position as tag
        reviewLay.setTag(position);
        return reviewLay;
    }

}
