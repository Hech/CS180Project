package com.hech.musicplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;


public class SubMapper extends BaseAdapter {

    private ArrayList<StreamSong> songs;
    private LayoutInflater songInf;

    public SubMapper(Context c, ArrayList<StreamSong> Songs){
        songs=Songs;
        songInf=LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
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
        LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song, parent, false);
        //get title and artist views
        TextView songView = (TextView)songLay.findViewById(R.id.song_title);
        TextView artistView = (TextView)songLay.findViewById(R.id.song_artist);
        TextView albumView = (TextView)songLay.findViewById(R.id.song_album);
       //TextView genreView = (TextView)songLay.findViewById(R.id.song_genre);
        //get song using position
        StreamSong currSong = songs.get(position);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        artistView.setText(currSong.getArtist());
        albumView.setText(currSong.getAlbum());
        //genreView.setText(currSong.getGenre());
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }
}
