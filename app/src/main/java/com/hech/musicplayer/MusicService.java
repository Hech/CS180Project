package com.hech.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
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
import java.util.Date;
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
    //Make private later
    public String songTitle;

    //The playlist that is currently playing
    private ArrayList<Song> nowPlaying;
    //The index into nowPlaying where the current song to be played is located
    private int position;
    //Intent Binder
    private final IBinder musicBind = new MusicBinder();
    //A toggle for continuous playback or one-at-a-time play
    private boolean continuousPlayMode = false;
    //notification id
    private static final int NOTIFY_ID=1;
    //shuffle flag and random
    private boolean shuffle=false;
    private Random rand = new Random();
    //playback position (msec)
    private int playbackPos = 0;

    //boolean to tell if playing
    public boolean playing = false;
    //boolean to tell is stopped
    public boolean stopped = true;
    //boolean to tell is paused
    public boolean paused = false;
    //Current Song's ID
    public long currentSong = -1;


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
    //initializes the playlist variable with a pre-genterated playlist structure
    //public void setPlaylists(Map<String, ArrayList<Song> > lists)
    //{
    //    playlists = lists;
    //}

    // Selects a playlist to play by name or id
    //public void pickPlaylist(String name)
    //{
    //    nowPlaying = playlists.get(name);
    //}

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
    public boolean getContinuousPlayMode(){ return continuousPlayMode; }

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
        Song playSong = nowPlaying.get(position);
        long currSong = playSong.getID();
        //If this song isn't the current one
        if(currentSong != currSong){
            //Update the current song
            currentSong = currSong;
            //Set the song's position to 0
            playbackPos = 0;
        }
        //If this song is the current one
        else{
            //If the song is already playing do nothing
            if(playing) {
                Log.d("MusicService", "Song already playing: "+playSong.getTitle());
                player.seekTo(player.getCurrentPosition());
                return;
            }
            //If the song is paused then resume
            if(paused){
                resumePlay();
                return;
            }
        }
        player.reset();
        songTitle = playSong.getTitle();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch (Exception e)
        {
            Log.e("Music Service", "Error Setting Data Source", e);
        }
        playState();
        player.prepareAsync();
    }
    public void pausePlay(){
        player.pause();
        playbackPos = player.getCurrentPosition();
        pauseState();
    }
    public void resumePlay(){
        player.seekTo(playbackPos);
        player.start();
        playState();
    }

    public ArrayList<Song> shuffle() {
        Collections.shuffle(nowPlaying, new Random());
        return nowPlaying;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        //notification requires API 16+
       // Intent notIntent = new Intent(this, MainActivity.class);
       // notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       // PendingIntent pendInt = PendingIntent.getActivity(this, 0,
       //         notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

       // Notification.Builder builder = new Notification.Builder(this);

       // builder.setContentIntent(pendInt)
       //         .setSmallIcon(R.drawable.ic_action_play)
       //         .setTicker(songTitle)
       //         .setOngoing(true)
       //         .setContentTitle("Playing")
       //         .setContentText(songTitle);
       // Notification not = builder.build();
       // startForeground(NOTIFY_ID, not);
    }

    //Stops playback
    public void stopPlay(){
        stoppedState();
        playbackPos = 0;
        player.stop();
    }

    //Only allow the seekbar to change playbackPos
    public void setPlayerPos(int position){
        playbackPos = position;
    }

    // if there is an error, stop the media player
    @Override
    public boolean onError(MediaPlayer mp, int x, int y) {
        mp.reset();
        return false;
    }

    // On completion of playback we increment the song position
    // if we have not finished the playlist, and then we also play
    // if we are set to continuous play
    public void onCompletion(MediaPlayer mp) {
        final String songTitle =  nowPlaying.get(position).getTitle();
        final String songAlbum = nowPlaying.get(position).getAlbum();
        ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Song_Bank");
        query2.whereEqualTo("Name", songTitle).whereEqualTo("Album", songAlbum);
        query2.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null)
                {
                    parseObject.put("Plays", parseObject.getNumber("Plays").intValue() + 1);
                    parseObject.saveInBackground();
                    ParseObject po = ParseObject.create("Plays");
                    po.put("Login", currUser);
                    po.put("SongName", songTitle);
                    po.put("SongAlbum", songAlbum);
                    Date d  = new Date();
                    //Add a week in milliseconds
                    d.setTime(d.getTime() + 604800000);
                    po.put("Expires", d);
                    po.saveInBackground();
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
            stoppedState();
            mp.reset();
            continuousPlayMode = false;
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

    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    public void playPrev(){
        position--;
        if(position < 0){
            position = songs.size()-1;
        }
        playSong();
    }
    public void playNext(){
        if(shuffle){
            int newSong = position;
            while(newSong == position){
                newSong = rand.nextInt(songs.size());
            }
            position = newSong;
        }
        else{
            position++;
            if(position >= songs.size()){
                position = 0;
            }
        }
        playSong();
    }
    public MediaPlayer getPlayer(){ return player; }
    public void stoppedState(){
        paused = false;
        playing = false;
        stopped = true;
    }
    public void pauseState(){
        paused = true;
        playing = false;
        stopped = false;
    }
    public void playState(){
        paused = false;
        playing = true;
        stopped = false;
    }
    @Override
    public void onDestroy() {
        stopForeground(true);
    }
}
