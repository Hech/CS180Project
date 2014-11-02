package com.hech.musicplayer;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.hech.musicplayer.R.id.action_continuousPlay;
import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.action_stopPlay;

public class SongFragment extends Fragment {
    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    public SongFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_song,
                        container, false);
        // Get the song view
        songView = (ListView)view.findViewById(R.id.song_list);
        // Create empty song library
        songList = new ArrayList<Song>();
        // Scan device and populate song library
        Log.d("SongFragment", "Get Songs");
        getSongList();

        //Map the song list to the song viewer
        SongMapper songMap = new SongMapper(view.getContext(), songList);
        songView.setAdapter(songMap);
        //Fragments need Click Listeners
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                musicService.setSong(Integer.parseInt(view.getTag().toString()));
                musicService.playSong();
            }

        });
        return view;
    }

    public void getSongList() {
        //retrieve song info

        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }
    // Create the connection to the music service
    private ServiceConnection musicConnection = new ServiceConnection() {

        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setSongsList(songList);
            //TODO set playlists here when we have them available in a file on the device
            musicBound = true;

        }

        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };
    // Connects MainActivity to the music service on startup, also starts the music service
    public void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    //If the user selects a song from the list, play it
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }
    public void onDestroy()
    {
        getActivity().stopService(playIntent);
        musicService = null;
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
