package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.provider.MediaStore.Audio.Playlists.Members.getContentUri;
import static com.hech.musicplayer.R.id.action_continuousPlay;
import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.action_stopPlay;

public class PlaylistFragment extends Fragment{
    private ArrayList<Playlist> playlists;
    private ListView playlistView;
    private MusicService musicService;
    private Intent playIntent;

    public PlaylistFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_playlist,
                        container, false);
        setHasOptionsMenu(true);
        // Get the playlist view
        playlistView = (ListView)view.findViewById(R.id.play_list);
        // Create empty playlist library
        playlists = new ArrayList<Playlist>();
        // Scan device and populate playlist library
        getplaylistList();
        //Map the song list to the song viewer
        PlaylistMapper playlistMap = new PlaylistMapper(view.getContext(), playlists);
        playlistView.setAdapter(playlistMap);
        //Fragments need Click Listeners
        playlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                //Bundle up id and title for sub view
                Bundle bundle = new Bundle();
                bundle.putLong("playlist_id", playlists.get(position).getID());
                bundle.putString("playlist_name", playlists.get(position).getTitle());
                //Switch to subplaylist song view
                Fragment subFragment = new PlaylistSubFragment_Members();
                subFragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                if(subFragment != null) {
                    Log.d("Playlist", "Switch: Playlist Member View");
                    fragmentManager.beginTransaction().replace(R.id.frame_container,
                            subFragment).commit();
                }
               }

        });
        return view;
    }
    public void getplaylistList() {
        Cursor playlistCursor = getActivity().getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (playlistCursor != null && playlistCursor.moveToFirst()) {
            //get columns
            int idColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists._ID);
            int titleColumn = playlistCursor.getColumnIndex
                    (MediaStore.Audio.Playlists.NAME);
            //fill playlist List
            do {
                long thisId = playlistCursor.getLong(idColumn);
                String thisTitle = playlistCursor.getString(titleColumn);
                playlists.add(new Playlist(thisId, thisTitle));
            } while (playlistCursor.moveToNext());
        }
    }
    public void onDestroy()
    {
        //crashes if playIntent is null
        //getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == action_settings) {
            return true;
        }
        if (id == action_continuousPlay)
        {
            musicService.setContinuousPlayMode(true);
            musicService.playSong();
        }
        if(id == action_stopPlay)
        {
            musicService.setContinuousPlayMode(false);
            musicService.stopPlay();
        }
        if(id == R.id.action_end)
        {
            getActivity().stopService(playIntent);
            musicService = null;
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}
