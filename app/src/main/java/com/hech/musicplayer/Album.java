package com.hech.musicplayer;

import java.util.ArrayList;

public class Album {
    //private ArrayList<Song> album;
    private String name;
    private String artist;

    Album(String Name, String Artist)//, ArrayList<Song> Album)
    {
       // album = new ArrayList<Song>(Album);
        name = Name;
        artist = Artist;
    }

  /*  public ArrayList<Song> getAlbum() {
        return album;
    }
*/
    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }
}
