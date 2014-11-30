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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

// Class that defines a background service that serves at the music player
// Serves as a listener for the media player it contains so it can perform appropriate
// actions when a song finishes etc.
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    // The media player that actually processes/plays audio
    private MediaPlayer player;
    //List of all songs
    private ArrayList<Song> songs;
    //Dictionary of playlists accessible by a string that would represent the playlist name or id
    private Map<String, ArrayList<Song> > playlists;
    private String currUser;

    //The playlist that is currently playing
    //TODO this list can be used to generate the nowPlaying list and highlight the current song
    private ArrayList<Song> nowPlaying;
    //The index into nowPlaying where the current song to be played is located
    private int position;
    //Intent Binder
    private final IBinder musicBind = new MusicBinder();
    //A toggle for continuous playback or one-at-a-time play
    private boolean continuousPlayMode = false;

    // Initializer for the service
    public void onCreate(){
        super.onCreate();
        position = 0;
        songs = null;
        nowPlaying = null;
        playlists = null;
        player = new MediaPlayer();
        initPlayer();
    }

    // Sets up the player Music streaming, keeps the phone from going to sleep,
    // and creates the communication channel for this service to the media player
    public void initPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    public ArrayList<Song> sortSongsByAttribute(ArrayList<Song> list, final int num, final boolean ascending)
    {
        ArrayList<Song> songsViewList = new ArrayList<Song>(list);
        Collections.sort(songsViewList, new Comparator<Song>() {
            public int compare(Song s1, Song s2) {
                if (num == 0)
                {
                    return ascending?s1.getTitle().compareToIgnoreCase(s2.getTitle()):
                            s1.getTitle().compareToIgnoreCase(s2.getTitle()) * -1;
                }
                else if (num == 1)
                {
                    return ascending?s1.getArtist().compareToIgnoreCase(s2.getArtist()):
                            s1.getArtist().compareToIgnoreCase(s2.getArtist()) * -1;
                }
                else if(num == 2)
                {
                    return ascending?s1.getAlbum().compareToIgnoreCase(s2.getAlbum()):
                            s1.getAlbum().compareToIgnoreCase(s2.getAlbum()) * -1;
                }
                else if (s1.getID() < s2.getID())
                    return ascending?1:-1;
                else if (s1.getID() > s2.getID())
                    return ascending?-1:1;
                else if (s1.getID() == s2.getID())
                    return 0;
                return 0;

            }
        });
        return songsViewList;
    }

    MediaPlayer getPlayer() {return player;}
    //initializes the playlist variable with a pre-genterated playlist structure
    public void setPlaylists(Map<String, ArrayList<Song> > lists)
    {
        playlists = lists;
    }

    // Selects a playlist to play by name or id
    public void pickPlaylist(String name)
    {
        nowPlaying = playlists.get(name);
    }

    // initializes the all songs list with a pre-generated list
    public void setSongsList(ArrayList<Song> list)
    {
        songs = list;
        nowPlaying = songs;
    }

    // Sets songs to play continuously or not
    public void setContinuousPlayMode(boolean mode)
    {
        continuousPlayMode = mode;
    }

    public void setNowPlaying(ArrayList<Song> list) {
        player.reset();
        position = 0;
        nowPlaying = list;

    }
    //
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    // starts playback of the next song to be played
    // this song can be set either by the user touching
    // the screen (see MainActivity.songPicked()) or by
    // continuous playback when a currently playing song finishes
    // (see MusicService.OnCompletion())
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


    public ArrayList<Song> shuffle() {
        Collections.shuffle(nowPlaying, new Random());
        return nowPlaying;
    }
    //Stops playback
    public void stopPlay(){
        player.stop();
    }

    //Starts the player when the Media Player is ready
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    // if there is an error, stop the media player
    public boolean onError(MediaPlayer mp, int x, int y) {
        mp.stop();
        return false;
    }

    // On completion of playback we increment the song position
    // if we have not finished the playlist, and then we also play
    // if we are set to continuous play
    public void onCompletion(MediaPlayer mp) {

        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Song_Bank");
        query2.whereEqualTo("Name", nowPlaying.get(position).getTitle()).whereEqualTo("Album", nowPlaying.get(position).getAlbum());
        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null)
                {
                    parseObject.put("Plays", parseObject.getNumber("Plays").intValue() + 1);
                    parseObject.saveInBackground();
                }
                else
                {
                    Log.d("MusicService", "Song completed is not one that was downloaded by app");
                }
            }
        });
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
        query.whereEqualTo("Login", currUser).whereEqualTo("song_Id", nowPlaying.get(position).getTitle());
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                    if(e == null)
                    {
                        parseObject.put("Plays", parseObject.getNumber("Plays").intValue() + 1);
                        parseObject.saveInBackground();


                    }
                    else
                    {
                        Log.d("MusicService", "Song completed is not one that was downloaded by app");
                    }
            }
        });

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

    // Sets the next song to be played
    public void setSong(int pos)
    {
        position = pos;
    }

    public void setCurrUser(String user) {currUser = user;}
    //Empty Constructor
    public MusicService() {   }

    //
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }
}
