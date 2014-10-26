package com.hech.musicplayer;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;
    private ArrayList<Song> songs;
    private Map<String, ArrayList<Song> > playlists;
    private ArrayList<Song> nowPlaying;
    private int position;
    private final IBinder musicBind = new MusicBinder();
    private boolean continuousPlayMode = false;


    public void onCreate(){
        super.onCreate();
        position = 0;
        songs = null;
        nowPlaying = null;
        playlists = null;
        player = new MediaPlayer();
        initPlayer();
    }

    public void initPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    public void setPlaylists(Map<String, ArrayList<Song> > lists)
    {
        playlists = lists;
    }

    public void pickPlaylist(String name)
    {
        nowPlaying = playlists.get(name);
    }
    public void setSongsList(ArrayList<Song> list)
    {
        songs = list;
        nowPlaying = songs;
    }

    public void setContinuousPlayMode(boolean mode)
    {
        continuousPlayMode = mode;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }


    public void playSong(){
        player.reset();
        Song playSong = nowPlaying.get(position);
        long currSong = playSong.getID();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e)
        {
            Log.e("Music Service", "Error Setting Data Source", e);
        }
        player.prepareAsync();
    }
    public void stopPlay(){
        player.stop();
    }
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public boolean onError(MediaPlayer mp, int x, int y) {
        mp.stop();
        return false;
    }

    public void onCompletion(MediaPlayer mp) {
        if(position >= nowPlaying.size())
        {
            mp.stop();
            return;
        }
        setSong(position + 1);
        if(continuousPlayMode)
        {
            playSong();
        }
    }


    public void setSong(int pos)
    {
        position = pos;
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }
}
