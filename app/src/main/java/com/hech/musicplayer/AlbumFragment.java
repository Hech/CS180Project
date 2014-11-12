package com.hech.musicplayer;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


public class AlbumFragment extends Fragment {
    private ArrayList<Album> albums;
    private ListView albumView;
    private AlbumMapper albumMap;

    public AlbumFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        setHasOptionsMenu(true);
        albumView = (ListView)view.findViewById(R.id.song_list);
        albums = new ArrayList<Album>();

        getAlbumList();
        albumMap = new AlbumMapper(view.getContext(), albums);
        albumView.setAdapter(albumMap);

        return view;
    }

    private void getAlbumList(){
        Cursor albumCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (albumCursor != null && albumCursor.moveToFirst()) {
            //get columns
            int idColumn = albumCursor.getColumnIndex
                    (MediaStore.Audio.Albums._ID);
            int titleColumn = albumCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ALBUM);
            int artistColumn = albumCursor.getColumnIndex
                    (MediaStore.Audio.Albums.ARTIST);
            //fill playlist List
            do {
                long thisId = albumCursor.getLong(idColumn);
                String thisTitle = albumCursor.getString(titleColumn);
                String thisArtist = albumCursor.getString(artistColumn);
                albums.add(new Album(thisId, thisTitle, thisArtist));
            } while (albumCursor.moveToNext());
        }
    }

    @Override
    public void onPause(){ super.onPause(); }

}
