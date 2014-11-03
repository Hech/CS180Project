package com.hech.musicplayer;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static com.hech.musicplayer.R.id.action_continuousPlay;
import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.action_stopPlay;

public class PlaylistFragment extends Fragment{
    private ArrayList<Playlist> playlists;
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
        setHasOptionsMenu(true);
        // Get the playlist view
        playlistView = (ListView)view.findViewById(R.id.play_list);
        // Create empty playlist library
        playlists = new ArrayList<Playlist>();
        // Scan device and populate playlist library
        getplaylistList();
        //Map the song list to the song viewer
        PlaylistMapper playlistMap = new PlaylistMapper(view.getContext(), playlists);
        playlistView.setAdapter(playlistMap);
        //Fragments need Click Listeners
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                playlistPicked(view, position);
            }

        });
        return view;
    }
    //If the user selects a playlist from the list, display its songs
    public void playlistPicked(View view, int position){
        /*Toast.makeText(getActivity().getApplicationContext(),
                text,
                Toast.LENGTH_LONG).show();*/
        fillPlaylist(view, position);
    }
    public void fillPlaylist(View view, int position){/*
        Cursor playlistCursor = getActivity().getContentResolver().query();
        if(playlistCursor != null && playlistCursor.moveToFirst()){

        }*/
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
            int idColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists._ID);
            int titleColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.NAME);
            //fill playlist List
            do {
                long thisId = playlistCursor.getLong(idColumn);
                String thisTitle = playlistCursor.getString(titleColumn);
                playList.add(new Playlist(thisId, thisTitle));
            } while (playlistCursor.moveToNext());
        }
    }
    public void onDestroy()
    {
       /* getActivity().stopService(playIntent);
        musicService = null;*/
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == action_settings) {
            return true;
        }
        if (id == action_continuousPlay)
        {
            musicService.setContinuousPlayMode(true);
            musicService.playSong();
        }
        if(id == action_stopPlay)
        {
            musicService.setContinuousPlayMode(false);
            musicService.stopPlay();
        }
        if(id == R.id.action_end)
        {
            getActivity().stopService(playIntent);
            musicService = null;
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}
