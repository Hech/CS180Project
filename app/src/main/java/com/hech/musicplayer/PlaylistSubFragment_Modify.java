package com.hech.musicplayer;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class PlaylistSubFragment_Modify extends Fragment {
    Playlist playlistCopy;
    Playlist options;
    Playlist selected;
    ListView songView;

    public PlaylistSubFragment_Modify() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        //Tell existence of fragment's option list
        setHasOptionsMenu(true);
        // Get the song view
        songView = (ListView) view.findViewById(R.id.song_list);
        //Receive playlist id and title from playlist parent fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
               selected = new Playlist(-1, "selections");
               playlistCopy = new Playlist(bundle.getLong("playlist_id"),
                    bundle.getString("playlist_name"));
                fillPlaylist(playlistCopy);
        }
        options = getMissingSongs(playlistCopy);
        final SongMapper songMap = new SongMapper(view.getContext(), options.getSongList());
        songView.setAdapter(songMap);
        songView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        //MultiChoice Listener for song list
        songView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
                //Get total Item Count
                final int checkCount = songView.getCheckedItemCount();
                mode.setTitle(checkCount + " Selected");
                if (checked) {
                    selected.addSong(options.getSong(position));
                } else {
                    selected.removeSong(options.getSong(position).getID());
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.playlist_modify, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist_modify_add:
                        for(int index = 0; index < selected.getSongList().size(); ++index) {
                            addSongtoPlaylist(playlistCopy.getID(), selected.getSong(index).getID());
                        }
                        break;
                    default:
                }
                //End CAB
                mode.finish();
                //Make fragment back to playlists
                getActivity().getFragmentManager().popBackStackImmediate();
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {}
        });
        //Give Instructions
        try {
            getActivity().getActionBar().setTitle("Hold to Select");
        } catch(NullPointerException e){
            Log.e("Set Title: ",e.toString());
        }
        return view;
    }
    void addSongtoPlaylist(long pID, long sID){
            ContentResolver resolver = getActivity().getContentResolver();
            String[] cols = new String[] {
                    "count(*)"
            };
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", pID);
            Cursor cur = resolver.query(uri, cols, null, null, null);
            cur.moveToFirst();
            final int base = cur.getInt(0);
            cur.close();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Long.valueOf(base + sID));
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, sID);
            resolver.insert(uri, values);
            Toast.makeText(getActivity().getApplicationContext(), "Song(s) Added",
                    Toast.LENGTH_SHORT).show();
    }
    //Fill playlist object with its songs
    public void fillPlaylist(Playlist pList){
        String [] projection = {
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.ALBUM
        };
        Cursor playlistCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external",
                        pList.getID()),
                projection,
                MediaStore.Audio.Media.IS_MUSIC+" != 0",
                null,
                null);
        if(playlistCursor != null && playlistCursor.moveToFirst()){
            //get columns
            int idColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.Members.AUDIO_ID);
            int titleColumn = playlistCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = playlistCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do{
                long thisId = playlistCursor.getLong(idColumn);
                String thisTitle = playlistCursor.getString(titleColumn);
                String thisArtist = playlistCursor.getString(artistColumn);
                String thisAlbum = playlistCursor.getString(albumColumn);
                pList.addSong(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            }while(playlistCursor.moveToNext());
        }
    }
    public Playlist getMissingSongs(Playlist ignore){
        Playlist ret = new Playlist(-1, "missing");
        //retrieve song info
        ContentResolver musicResolver = getActivity().getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri,
                null,
                MediaStore.Audio.Media.IS_MUSIC+" != 0",
                null,
                null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);

                ArrayList<Song> cmp = ignore.getSongList();
                Song s;
                int index = 0;
                for(; index < cmp.size(); ++index) {
                    s = cmp.get(index);
                    if(s.getID() == thisId){
                        break;
                    }
                }
                if(index >= cmp.size()) {
                    ret.addSong(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                }
            }
            while (musicCursor.moveToNext());
        }
        return ret;
    }
}
