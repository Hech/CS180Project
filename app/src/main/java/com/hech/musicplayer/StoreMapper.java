package com.hech.musicplayer;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StoreMapper extends BaseAdapter {

    private ArrayList<Song> songs;
    private HashMap<String, Number> prices;
    private LayoutInflater storeInf;

    public StoreMapper(Context c, ArrayList<Song> Songs, HashMap<String, Number> Prices){
        songs=Songs;
        prices = Prices;
        storeInf=LayoutInflater.from(c);
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
        LinearLayout storeLay = (LinearLayout)storeInf.inflate
                (R.layout.song, parent, false);
        //get title and artist views
        TextView songView = (TextView)storeLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)storeLay.findViewById(R.id.song_artist);
        TextView albumView = (TextView)storeLay.findViewById(R.id.song_album);
        TextView priceView = (TextView)storeLay.findViewById(R.id.song_price);
        //get song using position
        Song currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        albumView.setText(currSong.getAlbum());
        priceView.setText("$"+prices.get(currSong.getTitle()).toString());

        Log.d("Info", currSong.getTitle() + currSong.getAlbum() + currSong.getArtist());
        //set position as tag
        storeLay.setTag(position);
        return storeLay;
    }
}
