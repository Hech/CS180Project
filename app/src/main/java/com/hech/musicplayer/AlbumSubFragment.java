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
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.hech.musicplayer.R.id.action_settings;

public class AlbumSubFragment extends Fragment{
    private long albumID;
    private String albumName;
    private Playlist albumSongs;
    private ListView albumView;
    private SongMapper albumMap;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    private boolean TitleAscending = false;
    private boolean ArtistAscending = false;

    public AlbumSubFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        setHasOptionsMenu(true);
        albumView = (ListView)view.findViewById(R.id.song_list);
        //Receive playlist id and title from playlist parent fragment
        Bundle bundle = this.getArguments();
        if(bundle != null){
            albumID = bundle.getLong("album_id");
            albumName = bundle.getString("album_name");
            albumSongs = new Playlist(albumID, albumName);
        }
        getAlbumSongs();
        albumMap = new SongMapper(view.getContext(), albumSongs.getSongList());
        albumView.setAdapter(albumMap);
        albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                songPicked(view);
                //Log.d("RecentlyPlayed", songList.get(position).getTitle());
                Song s = albumSongs.getSong(position);
                ((MainActivity)getActivity()).setRecentlyPlayed(s);
            }
        });
        return view;
    }
    public void getAlbumSongs() {
        ContentResolver recentResolver = getActivity().getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selectedAlbums[] = {albumName};
        Cursor recentCursor = recentResolver.query(musicUri,
                null,
                MediaStore.Audio.Media.ALBUM + "=?",
                selectedAlbums,
                null);
        if (recentCursor != null && recentCursor.moveToFirst()) {
            //get columns
            int titleColumn = recentCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = recentCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = recentCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = recentCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            do {
                long thisId = recentCursor.getLong(idColumn);
                String thisTitle = recentCursor.getString(titleColumn);
                String thisArtist = recentCursor.getString(artistColumn);
                String thisAlbum = recentCursor.getString(albumColumn);
                albumSongs.addSong(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            } while (recentCursor.moveToNext());
        }
    }
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }
    private ServiceConnection musicConnection = new ServiceConnection() {
        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setSongsList(albumSongs.getSongList());
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
    @Override
    public void onPause(){ super.onPause(); }
    @Override
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
            musicService.setNowPlaying(albumSongs.getSongList());
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
                albumSongs.pList = musicService.sortSongsByAttribute(albumSongs.getSongList(), 0, false);
            }
            else
            {
                TitleAscending = true;
                ArtistAscending = false;
                albumSongs.pList = musicService.sortSongsByAttribute(albumSongs.getSongList(), 0, true);
            }
            SongMapper songMap = new SongMapper(albumView.getContext(), albumSongs.getSongList());
            albumView.setAdapter(songMap);
        }
        if(id == R.id.action_sort_artist)
        {
            if(ArtistAscending)
            {
                TitleAscending = false;
                ArtistAscending = false;
                albumSongs.pList = musicService.sortSongsByAttribute(albumSongs.getSongList(), 1, false);
            }
            else
            {
                TitleAscending = false;
                ArtistAscending = true;
                albumSongs.pList = musicService.sortSongsByAttribute
                        (albumSongs.getSongList(), 1, true);
            }
            SongMapper songMap = new SongMapper(albumView.getContext(), albumSongs.getSongList());
            albumView.setAdapter(songMap);
        }
        if(id == R.id.action_shuffle)
        {
            albumSongs.pList = musicService.shuffle();
            SongMapper songMap = new SongMapper(albumView.getContext(), albumSongs.getSongList());
            albumView.setAdapter(songMap);
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
