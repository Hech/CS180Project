package com.hech.musicplayer;

import android.app.Activity;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;

import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.drawer_layout;
import static com.hech.musicplayer.R.id.play_pause_toggle;

public class SongFragment extends Fragment {
    private ArrayList<Song> songList = null;
    private ArrayList<Song> songViewList = null;
    private ListView songView;
    //private MusicService musicService;
    //private Intent playIntent;
    //private boolean musicBound = false;
    private View SongFragmentView;
    private boolean TitleAscending = false;
    private boolean ArtistAscending = false;
    private View view;


    public SongFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_song,
                        container, false);
        //Get recently played from main activity
        //recentlyPlayed = ((MainActivity)getActivity()).recentlyPlayed;

        SongFragmentView = view;
        // Get the song view
        songView = (ListView)view.findViewById(R.id.song_list);
        setHasOptionsMenu(true);
        //Show/Hide the Controller View
        String currSong = ((MainActivity)getActivity()).getCurrentSongName();
        if(((MainActivity)getActivity()).getMusicService().playing ||
                ((MainActivity)getActivity()).getMusicService().paused) {
            showController();
            setControllerSong(currSong);
            //If paused, toggle the controller correctly
            if(((MainActivity)getActivity()).getMusicService().paused){
                ToggleButton toggle = (ToggleButton)view.findViewById(play_pause_toggle);
                toggle.setChecked(true);
            }
        }
        else{
            hideController();
        }

        // Scan device and populate song library
        Log.d("SongFragment", "Get Songs");
        if(songList == null || ((MainActivity)getActivity()).getNewSongsAvailable()) {
            songList = new ArrayList<Song>();
            getSongList();
            ((MainActivity)getActivity()).setNewSongsAvail(false);
            //musicService.setSongsList(songList);
            ((MainActivity)getActivity()).getMusicService()
                    .setSongsList(songList);
        }
        if(songViewList == null) {
            songViewList = new ArrayList<Song>(songList);
        }
        //Map the song list to the song viewer
        final SongMapper songMap = new SongMapper(view.getContext(), songViewList);
        songView.setAdapter(songMap);
        //Fragments need Click Listeners
        songView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                songPicked(view);
                Song s = new Song(songList.get(position).getID(),
                                songList.get(position).getTitle(),
                                songList.get(position).getArtist(),
                                songList.get(position).getAlbum());
                //Update controller's song name
                setControllerSong(s.getTitle());
                //Update current song in MainActivity
                ((MainActivity)getActivity()).setCurrentSongName(s.getTitle());
                //Force pause option in controller
                ToggleButton toggle = (ToggleButton)SongFragmentView
                        .findViewById(R.id.play_pause_toggle);
                toggle.setChecked(false);
                ((MainActivity)getActivity()).setRecentlyPlayed(s);
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
        return view;
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,
                null,
                MediaStore.Audio.Media.IS_MUSIC+" != 0",
                null,
                null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            }
            while (musicCursor.moveToNext());
        }
    }
    // Create the connection to the music service
    //private ServiceConnection musicConnection = new ServiceConnection() {
        //Initialize the music service once a connection is established
    //    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    //        MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
    //        musicService = binder.getService();
    //        musicService.setCurrUser(((MainActivity) getActivity()).getUserLoggedin());
    //        musicService.setSongsList(songList);
    //        musicBound = true;
    //    }
    //    public void onServiceDisconnected(ComponentName componentName) {
    //        musicBound = false;
    //    }
    //};
    // Connects MainActivity to the music service on startup, also starts the music service
    @Override
    public void onStart(){
        super.onStart();
     //   if(playIntent == null){
     //       playIntent = new Intent(getActivity(), MusicService.class);
     //       getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
     //      getActivity().startService(playIntent);
     //   }
    }
    //If the user selects a song from the list, play it
    public void songPicked(View view){
        //musicService.setSong(Integer.parseInt(view.getTag().toString()));
        //musicService.playSong();
        ((MainActivity)getActivity()).getMusicService()
                .setSong(Integer.parseInt(view.getTag().toString()));
        ((MainActivity)getActivity()).getMusicService().playSong();
        showController();
    }
    @Override
    public void onDestroy(){
    //    getActivity().stopService(playIntent);
    //    getActivity().unbindService(musicConnection);
    //    musicService = null;
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
       // if (id == R.id.action_continuousPlay)
       // {
       //     musicService.setContinuousPlayMode(true);
       //     musicService.setNowPlaying(songViewList);
       //     musicService.playSong();
       //     Log.d("SongFragment", "MusicPlayCalled");
       // }
        if(id == R.id.action_stopPlay)
        {
        //    musicService.setContinuousPlayMode(false);
            Log.d("SongFragment", "MusicStopCalled");
        //    musicService.stopPlay();
            ((MainActivity)getActivity()).getMusicService().setContinuousPlayMode(false);
            ((MainActivity)getActivity()).getMusicService().stopPlay();
            hideController();
        }
        if(id == R.id.action_sort_title)
        {
            if(TitleAscending)
            {
                TitleAscending = false;
                ArtistAscending = false;
                //songViewList = musicService.sortSongsByAttribute(songList, 0, false);
                ((MainActivity)getActivity()).getMusicService()
                        .sortSongsByAttribute(songList, 0, false);
            }
            else
            {
                TitleAscending = true;
                ArtistAscending = false;
                //songViewList = musicService.sortSongsByAttribute(songList, 0, true);
                ((MainActivity)getActivity()).getMusicService()
                        .sortSongsByAttribute(songList, 0, true);
            }
            SongMapper songMap = new SongMapper(SongFragmentView.getContext(), songViewList);
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_sort_artist)
        {
            if(ArtistAscending)
            {
                TitleAscending = false;
                ArtistAscending = false;
                //songViewList = musicService.sortSongsByAttribute(songList, 1, false);
                songViewList = ((MainActivity)getActivity()).getMusicService()
                        .sortSongsByAttribute(songList, 1, false);
            }
            else
            {
                TitleAscending = false;
                ArtistAscending = true;
                //songViewList = musicService.sortSongsByAttribute(songList, 1, true);
                songViewList = ((MainActivity)getActivity()).getMusicService()
                        .sortSongsByAttribute(songList, 1, false);
            }
            SongMapper songMap = new SongMapper(SongFragmentView.getContext(), songViewList);
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_shuffle)
        {
            songViewList = ((MainActivity)getActivity()).getMusicService().shuffle();
            SongMapper songMap = new SongMapper(SongFragmentView.getContext(), songViewList);
            songView.setAdapter(songMap);
        }
        if(id == R.id.action_end)
        {
            ((MainActivity)getActivity()).getMusicService()
                    .stopService(((MainActivity)getActivity()).getPlayIntent());
            //getActivity().stopService(playIntent);
            ((MainActivity)getActivity()).setMusicServiceNull();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
    public void setControllerSong(String songName){
        TextView currentSong = (TextView)view
                                .findViewById(R.id.music_current_song);
        currentSong.setText(songName);

    }
    public void showController(){
        LinearLayout controller = (LinearLayout)view
                                  .findViewById(R.id.music_current);
        controller.setVisibility(View.VISIBLE);
        controller = (LinearLayout)view.findViewById(R.id.music_controller);
        controller.setVisibility(View.VISIBLE);
    }
    public void hideController(){
        LinearLayout controller = (LinearLayout) view
                    .findViewById(R.id.music_current);
        controller.setVisibility(View.GONE);
        controller = (LinearLayout) view
                    .findViewById(R.id.music_controller);
        controller.setVisibility(View.GONE);
    }
    @Override
    public void onPause(){ super.onPause(); }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}