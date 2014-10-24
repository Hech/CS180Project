package com.hech.musicplayer;

/**
 * Created by Zach on 10/24/2014.
 */
public class Song {
    private long id;
    private String title;
    private String artist;

    public Song(long ID, String Title, String Artist)
    {
        id = ID;
        title = Title;
        artist = Artist;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
