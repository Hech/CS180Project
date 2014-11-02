package com.hech.musicplayer;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment{
    private ArrayList<String> playList;
    private ListView playlistView;
    private MusicService musicService;
    private Intent playIntent;

    public PlaylistFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_playlist,
                        container, false);
        // Get the playlist view
        playlistView = (ListView)view.findViewById(R.id.song_list);
        // Create empty playlist library
        playList = new ArrayList<String>();
        // Scan device and populate playlist library
        getplaylistList();
        Log.d("PlaylistFragment", "Get Playlists");


        return view;
    }
    public void getplaylistList() {
        Cursor playlistCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            //get columns
            int titleColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.NAME);
            //fill playlist List
            do {
                String thisTitle = playlistCursor.getString(titleColumn);
                playList.add(new String(thisTitle));
            } while (playlistCursor.moveToNext());
        }
    }
}
