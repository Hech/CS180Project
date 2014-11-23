package com.hech.musicplayer;


public class StreamSong extends Song {
    public StreamSong(long ID, String Title, String Artist, String Album, String Genre, String URL)
    {
        super(ID, Title, Artist, Album);
        url = URL;
        genre = Genre;
    }
    private String url;
    private String genre;
    public String getGenre() {return genre;}
    public String getUrl(){return url;}
}
