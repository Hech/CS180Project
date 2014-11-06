package com.hech.musicplayer;

import java.util.ArrayList;

public class Playlist{
    private long id;
    private String title;
    private ArrayList<Song> pList;

    public Playlist(long ID, String Title)
    {
        id = ID;
        title = Title;
        pList = new ArrayList<Song>();
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public void addSong(Song song){
        pList.add(song);
    }
    public Song getSong(Integer index){
        return pList.get(index);
    }
    public ArrayList<Song> getSongList() { return pList; }
}
