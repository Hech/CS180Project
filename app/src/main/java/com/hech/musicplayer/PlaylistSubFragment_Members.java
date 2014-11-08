package com.hech.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import static com.hech.musicplayer.R.id.action_settings;


public class PlaylistSubFragment_Members extends Fragment {
    Playlist playlist;
    ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean TitleAscending = false;
    private boolean ArtistAscending = false;

    public PlaylistSubFragment_Members() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_song, container, false);
        //Tell existance of personal option list
        setHasOptionsMenu(true);
        // Get the song view
        songView = (ListView)view.findViewById(R.id.song_list);
       //Receive playlist id and title from playlist parent fragment
        Bundle bundle = this.getArguments();
        if(bundle != null){
            playlist = new Playlist(bundle.getLong("playlist_id"),
                                    bundle.getString("playlist_name"));
            fillPlaylist(playlist);
        }
        SongMapper songMap = new SongMapper(view.getContext(), playlist.getSongList());
        songView.setAdapter(songMap);
        //Click Listener for song list
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                songPicked(view);
            }

        });

        return view;
    }
    // Create the connection to the music service
    private ServiceConnection musicConnection = new ServiceConnection() {

        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setSongsList(playlist.getSongList());
            musicBound = true;
        }
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    public void fillPlaylist(Playlist pList){
        String [] projection = {
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ALBUM
        };
        Cursor playlistCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external",
                        pList.getID()),
                projection,
                MediaStore.Audio.Media.IS_MUSIC+" != 0",
                null,
                null);
        if(playlistCursor != null && playlistCursor.moveToFirst()){
            //get columns
            int idColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.AUDIO_ID);
            int titleColumn = playlistCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = playlistCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do{
                long thisId = playlistCursor.getLong(idColumn);
                String thisTitle = playlistCursor.getString(titleColumn);
                String thisArtist = playlistCursor.getString(artistColumn);
                String thisAlbum = playlistCursor.getString(albumColumn);
                pList.addSong(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                }while(playlistCursor.moveToNext());
        }
    }
    //If the user selects a song from the list, play it
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }
    // Connects MainActivity to the music service on startup, also starts the music service
    public void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }
    public void onDestroy(){
        getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.song, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        if (id == R.id.action_continuousPlay)
        {
            musicService.setContinuousPlayMode(true);
            musicService.setNowPlaying(playlist.getSongList());
            musicService.playSong();
            Log.d("SongFragment", "MusicPlayCalled");
        }
        if(id == R.id.action_stopPlay)
        {
            musicService.setContinuousPlayMode(false);
            Log.d("SongFragment", "MusicStopCalled");
            musicService.stopPlay();
        }
        if(id == R.id.action_sort_title)
        {
            if(TitleAscending)
            {
                TitleAscending = false;
                ArtistAscending = false;
                playlist.pList = musicService.sortSongsByAttribute(playlist.getSongList(), 0, false);
            }
            else
            {
                TitleAscending = true;
                ArtistAscending = false;
                playlist.pList = musicService.sortSongsByAttribute(playlist.getSongList(), 0, true);
            }

            SongMapper songMap = new SongMapper(songView.getContext(), playlist.getSongList());
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_sort_artist)
        {
            if(ArtistAscending)
            {
                TitleAscending = false;
                ArtistAscending = false;
                playlist.pList = musicService.sortSongsByAttribute(playlist.getSongList(), 1, false);
            }
            else
            {
                TitleAscending = false;
                ArtistAscending = true;
                playlist.pList = musicService.sortSongsByAttribute(playlist.getSongList(), 1, true);
            }
            SongMapper songMap = new SongMapper(songView.getContext(), playlist.getSongList());
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_shuffle)
        {
            playlist.pList = musicService.shuffle();
            SongMapper songMap = new SongMapper(songView.getContext(), playlist.getSongList());
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_end)
        {
            getActivity().stopService(playIntent);
            musicService = null;
            Log.d("SongFragment", "AppCloseCalled");
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}
