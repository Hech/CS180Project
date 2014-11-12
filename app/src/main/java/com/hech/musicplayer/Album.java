package com.hech.musicplayer;

public class Album {
    //private ArrayList<Song> album;
    private long id;
    private String name;
    private String artist;
    private String genre;
    Album(String Name, String Artist, String Genre){
        name = Name;
        artist = Artist;
        genre = Genre;
    }

    Album(long ID, String Name, String Artist)//, ArrayList<Song> Album)
    {
       // album = new ArrayList<Song>(Album);
        id = ID;
        name = Name;
        artist = Artist;
    }

  /*  public ArrayList<Song> getAlbum() {
        return album;
    }
*/
    public long getId(){ return id; }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public String getGenre() { return genre; }
}
