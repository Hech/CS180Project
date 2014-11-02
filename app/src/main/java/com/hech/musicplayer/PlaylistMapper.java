package com.hech.musicplayer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlaylistMapper extends BaseAdapter {

    private ArrayList<ArrayList<Song>> playlists;
    private LayoutInflater songInf;
    private int index = 0;

    public PlaylistMapper(Context c, ArrayList<ArrayList<Song>> PlayLists){
        playlists=PlayLists;
        songInf=LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return playlists.size();
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
        TextView songView = (TextView)songLay.findViewById(R.id.playlist_title);
        //get song using position
        Song currSong = playlists.get(position).get(index);
        //get title and artist strings
        songView.setText(currSong.getTitle());
        //set position as tag
        songLay.setTag(position);
        return songLay;
    }
}
