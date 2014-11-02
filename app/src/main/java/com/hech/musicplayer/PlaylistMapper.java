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

    private ArrayList<Playlist> playlists;
    private LayoutInflater songInf;
    private int index = 0;

    public PlaylistMapper(Context c, ArrayList<Playlist> PlayLists){
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
        LinearLayout playlistLay = (LinearLayout)songInf.inflate
                (R.layout.playlist, parent, false);
        //get title and artist views
        TextView playlistView = (TextView)playlistLay.findViewById(R.id.playlist_title);
        //get playlist using position
        Playlist currPlaylist = playlists.get(position);
        //get title and artist strings
        playlistView.setText(currPlaylist.getTitle());
        //set position as tag
        playlistLay.setTag(position);
        return playlistLay;
    }
}
