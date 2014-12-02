package com.hech.musicplayer;

import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import javax.xml.parsers.FactoryConfigurationError;

public class StoreRecommendMapper extends BaseAdapter {

    private ArrayList<Song> songs;
    private HashMap<String, Number> prices;
    private LayoutInflater storeInf;
    private Fragment f;

    public StoreRecommendMapper(Context c, ArrayList<Song> Songs, HashMap<String, Number> Prices, Fragment fragment){
        songs=Songs;
        prices = Prices;
        storeInf=LayoutInflater.from(c);
        f = fragment;
    }
    @Override
    public int getCount() {
        if(songs == null)
        {
            Log.d("Info", "Songs null...");
            return 0;
        }
        return songs.size();
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
        //map to song layout
        RelativeLayout storeLay = (RelativeLayout)storeInf.inflate
                (R.layout.song_review, parent, false);
        //get title and artist views
        final TextView songView = (TextView)storeLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)storeLay.findViewById(R.id.song_artist);
        TextView albumView = (TextView)storeLay.findViewById(R.id.song_album);
        TextView priceView = (TextView)storeLay.findViewById(R.id.song_price);
        //get song using position
        final Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        albumView.setText(currSong.getAlbum());
        priceView.setText("$"+prices.get(currSong.getTitle()).toString());
        Button button = (Button)storeLay.findViewById(R.id.revbutton);

        // Set listener for Download button belonging to EVERY song element in the list
        Log.d("Info", currSong.getTitle() + currSong.getAlbum() + currSong.getArtist());
        //set position as tag
        storeLay.setTag(position);
        return storeLay;
    }
}
