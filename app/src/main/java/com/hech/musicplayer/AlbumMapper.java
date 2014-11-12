package com.hech.musicplayer;

import android.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AlbumMapper extends BaseAdapter {

    private ArrayList<Album> albums;
    private LinkedHashMap<String, Number> albumPrices;
    private LayoutInflater songInf;
    private Fragment f = null;

    public AlbumMapper(Context c, ArrayList<Album> Albums){
        albums=Albums;
        songInf=LayoutInflater.from(c);
    }

    public AlbumMapper(Context c, ArrayList<Album> Albums, LinkedHashMap<String, Number> AlbumPrices, Fragment fragment){
        albums= Albums;
        albumPrices = AlbumPrices;
        songInf=LayoutInflater.from(c);
        f = fragment;
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
        //Check if price view shouldn't be used
        if(f == null){
            Log.d("AlbumMap", "Don't Display Price");
            View albumLay = getListView(position, convertView, parent);
            return albumLay;
        }
        //map to song layout
        RelativeLayout albumLay = (RelativeLayout)songInf.inflate
                (R.layout.album, parent, false);
        //get title and artist views
        TextView albumView = (TextView)albumLay.findViewById(R.id.album_title);
        TextView artistView = (TextView)albumLay.findViewById(R.id.album_artist);
        //TextView genreView = (TextView)albumLay.findViewById(R.id.album_genre);
        TextView priceView = (TextView)albumLay.findViewById(R.id.album_price);
        //TextView albumView = (TextView)songLay.findViewById(R.id.song_album);
        //get song using position
        final Album currAlbum = albums.get(position);
        //get title and artist strings
        //songView.setText(currSong.getTitle());
        artistView.setText(currAlbum.getArtist());
        albumView.setText(currAlbum.getName());
        //genreView.setText(currAlbum.getGenre());
        Log.d("ERRORCHECK", currAlbum.getName());

        priceView.setText("$" + albumPrices.get(currAlbum.getName()).toString());

        Button button = (Button)albumLay.findViewById(R.id.album_revbutton);

        ImageButton button2 = (ImageButton)albumLay.findViewById(R.id.album_dlButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((StoreFragment) f).reviewSong(currAlbum.getName());
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((StoreFragment) f).albumPicked(currAlbum.getName());
            }
        });
        //set position as tag
        albumLay.setTag(position);
        return albumLay;
    }
    public View getListView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout = (LinearLayout)songInf.inflate
                (R.layout.song, parent, false);
        //get title and artist views
        TextView albumView = (TextView)layout.findViewById(R.id.song_title);
        TextView artistView = (TextView)layout.findViewById(R.id.song_artist);
        TextView dateView = (TextView)layout.findViewById(R.id.song_album);
        //get song using position
        Album currAlbum = albums.get(position);
        //get title and artist strings
        albumView.setText(currAlbum.getName());
        artistView.setText(currAlbum.getArtist());
        dateView.setText(currAlbum.getName());
        //set position as tag
        layout.setTag(position);
        return layout;
    }
}
