package com.hech.musicplayer;


public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;

    public Song(long ID, String Title, String Artist, String Album)
    {
        id = ID;
        title = Title;
        artist = Artist;
        album = Album;
    }


    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getAlbum(){return album;}
}
