package com.hech.musicplayer;

import java.util.ArrayList;

public class Playlist{
    private long id;
    private String title;
    public ArrayList<Song> pList;

    public Playlist(long ID, String Title)
    {
        id = ID;
        title = Title;
        pList = new ArrayList<Song>();
    }
    public long getID(){return id;}
    public int getSize(){return pList.size();}
    public String getTitle(){return title;}
    public void addSong(Song song){
        pList.add(0, song);
    }
    public Song getSong(Integer index){
        return pList.get(index);
    }
    public ArrayList<Song> getSongList() { return pList; }
    public void removeSong(long iD) {
        int index = 0;
        for(; index < pList.size(); ++index){
            if(pList.get(index).getID() == iD){
                pList.remove(index);
            }
        }
    }
}
