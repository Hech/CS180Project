package com.hech.musicplayer;


import android.app.Fragment;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.play_pause_toggle;

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
    View view;
    private SeekBar seekBar;
    private Runnable runnable;
    private final Handler handler = new Handler();

    public AlbumSubFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song, container, false);
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
        //Show/Hide the Controller View
        String currSong = ((MainActivity) getActivity()).getCurrentSongName();
        if (((MainActivity) getActivity()).getMusicService().playing ||
                ((MainActivity) getActivity()).getMusicService().paused) {
            showController();
            setControllerSong(currSong);
            //If paused, toggle the controller correctly
            if (((MainActivity) getActivity()).getMusicService().paused) {
                ToggleButton toggle = (ToggleButton) view.findViewById(play_pause_toggle);
                toggle.setChecked(true);

            }
            //Track the song's progress
            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (getActivity() != null) {
                        int currentPosition = ((MainActivity) getActivity())
                                .getMusicService().getPlayer().getCurrentPosition();
                        int duration = ((MainActivity) getActivity())
                                .getMusicService().getPlayer().getDuration();
                        int progress = (currentPosition * 100) / duration;
                        String currentTime = "";
                        currentTime = String.format("%01d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                                .toMinutes(currentPosition))
                        );

                        String endTime = "";
                        endTime = String.format("%01d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration),
                                TimeUnit.MILLISECONDS.toSeconds(duration) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                                .toMinutes(duration))
                        );

                        TextView currentSong = (TextView) view
                                .findViewById(R.id.seek_bar_curr);
                        currentSong.setText(currentTime);

                        TextView currentEnd = (TextView) view
                                .findViewById(R.id.seek_bar_max);
                        currentEnd.setText(endTime);

                        seekBar.setProgress(progress);
                        handler.postDelayed(this, 1000);
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
        }
            albumMap = new SongMapper(view.getContext(), albumSongs.getSongList());
            albumView.setAdapter(albumMap);
            albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, final View v,
                                        int position, long id) {
                    songPicked(v);
                    //Log.d("RecentlyPlayed", songList.get(position).getTitle());
                    Song s = albumSongs.getSong(position);
                    ((MainActivity) getActivity()).setRecentlyPlayed(s);
                    //Force pause option in controller
                    ToggleButton toggle = (ToggleButton) view
                            .findViewById(R.id.play_pause_toggle);
                    toggle.setChecked(false);
                    seekBar = (SeekBar)view.findViewById(R.id.seek_bar);
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() != null) {
                                int currentPosition = ((MainActivity) getActivity())
                                        .getMusicService().getPlayer().getCurrentPosition();
                                int duration = ((MainActivity) getActivity())
                                        .getMusicService().getPlayer().getDuration();
                                int progress = (currentPosition * 100) / duration;
                                String currentTime = "";
                                currentTime = String.format("%01d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                                        TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                                        .toMinutes(currentPosition))
                                );

                                String endTime = "";
                                endTime = String.format("%01d:%02d",
                                        TimeUnit.MILLISECONDS.toMinutes(duration),
                                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                                        .toMinutes(duration))
                                );

                                TextView currentSong = (TextView)view
                                        .findViewById(R.id.seek_bar_curr);
                                currentSong.setText(currentTime);

                                TextView currentEnd = (TextView)view
                                        .findViewById(R.id.seek_bar_max);
                                currentEnd.setText(endTime);

                                seekBar.setProgress(progress);
                                handler.postDelayed(this, 1000);
                            }
                        }
                    };
                    handler.postDelayed(runnable, 1000);
                }
            });
        //Click Listener for Play/Pause
        view.findViewById(R.id.play_pause_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).getMusicService().playing){
                    ((MainActivity)getActivity()).getMusicService().pausePlay();
                }
                else{
                    ((MainActivity)getActivity()).getMusicService().resumePlay();
                }
            }
        });
        //Listener for MediaPlayer Song Complete
        ((MainActivity)getActivity()).getMusicService()
                .getPlayer().setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("MediaPlayerListener", "Song Complete");
                //If there isn't more to play
                if(!((MainActivity)getActivity()).getMusicService().getContinuousPlayMode()) {
                    ((MainActivity) getActivity()).getMusicService()
                            .stoppedState();
                    //Force play option in controller
                    ToggleButton toggle = (ToggleButton) view
                            .findViewById(R.id.play_pause_toggle);
                    toggle.setChecked(true);
                }
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
            musicService.setCurrUser(((MainActivity) getActivity()).getUserLoggedin());
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
        menu.clear();
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
    public void setControllerSong(String songName) {
        TextView currentSong = (TextView) view
                .findViewById(R.id.music_current_song);
        currentSong.setText(songName);

    }

    public void showController() {
        LinearLayout current = (LinearLayout) view
                .findViewById(R.id.music_current);
        current.setVisibility(View.VISIBLE);
        RelativeLayout controller = (RelativeLayout) view.findViewById(R.id.music_controller);
        controller.setVisibility(View.VISIBLE);
    }

    public void hideController() {
        LinearLayout current = (LinearLayout) view
                .findViewById(R.id.music_current);
        current.setVisibility(View.GONE);
        RelativeLayout controller = (RelativeLayout) view
                .findViewById(R.id.music_controller);
        controller.setVisibility(View.GONE);

    }
}
