package com.hech.musicplayer;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlbumMapper extends BaseAdapter {

    private ArrayList<Album> albums;
    private LinkedHashMap<String, Number> albumPrices;
    private LayoutInflater songInf;

    public AlbumMapper(Context c, ArrayList<Album> Albums, LinkedHashMap<String, Number> AlbumPrices){
        albums= Albums;
        albumPrices = AlbumPrices;
        songInf=LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        if(albums == null)
        {
            Log.d("Info", "Songs null...");
            return 0;
        }
        return albums.size();
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
        LinearLayout albumLay = (LinearLayout)songInf.inflate
                (R.layout.album, parent, false);
        //get title and artist views
        TextView albumView = (TextView)albumLay.findViewById(R.id.album_title);
        TextView artistView = (TextView)albumLay.findViewById(R.id.album_artist);
        TextView priceView = (TextView)albumLay.findViewById(R.id.album_price);
        //TextView albumView = (TextView)songLay.findViewById(R.id.song_album);
        //get song using position
        Album currAlbum = albums.get(position);
        //get title and artist strings
        //songView.setText(currSong.getTitle());
        artistView.setText(currAlbum.getArtist());
        albumView.setText(currAlbum.getName());
        Log.d("ERRORCHECK", currAlbum.getName());
        Number f = albumPrices.get(currAlbum.getName());
        if(albumPrices.containsKey(currAlbum.getName()));
        {
            Log.d("ErrorCheck4", "albumprices contians " + currAlbum.getName());
        }
        if(f == null)
        {
            Log.d("ErrorCheck3", "F is null");
        }
        Log.d("ErrorCheck2", f.toString());
        priceView.setText("$" + albumPrices.get(currAlbum.getName()).toString());
        //set position as tag
        albumLay.setTag(position);
        return albumLay;
    }
}
