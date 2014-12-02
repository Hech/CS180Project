package com.hech.musicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.hech.musicplayer.R.id.action_newplaylist;
import static com.hech.musicplayer.R.id.play_pause_toggle;

public class PlaylistFragment extends Fragment {
    private ArrayList<Playlist> playlists;
    private ListView playlistView;
    private PlaylistMapper playlistMap;
    private long playlistTransactID;
    private String playlistTranscactStr;
    View view;
    private SeekBar seekBar;
    private Runnable runnable;
    private final Handler handler = new Handler();

    public PlaylistFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song,
                container, false);
        setHasOptionsMenu(true);
        // Get the playlist view
        playlistView = (ListView) view.findViewById(R.id.song_list);
        // Create empty playlist library
        playlists = new ArrayList<Playlist>();
        //Add faux recent playlists
        playlists.add(getRecentlyAdded());
        playlists.add(getRecentlyPlayed());
        playlists.add(getRecentlyDownloaded());
        //Show/Hide the Controller View
        String currSong = ((MainActivity) getActivity()).getCurrentSongName();
        if (((MainActivity) getActivity()).getMusicService().playing ||
                ((MainActivity) getActivity()).getMusicService().paused) {
            showController();
            setControllerSong(currSong);
            //If paused, toggle the controller correctly
            if (((MainActivity) getActivity()).getMusicService().paused) {
                ToggleButton toggle = (ToggleButton) view.findViewById(play_pause_toggle);
                toggle.setChecked(true);

            }
            //Track the song's progress
            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            trackProgressBar();
        } else {
            seekBar = (SeekBar)view.findViewById(R.id.seek_bar);
            hideController();
        }
        //Set Title
        try {
            getActivity().getActionBar().setTitle("Playlists");
        } catch(NullPointerException e){
            Log.e("Set Title: ",e.toString());
        }
        // Scan device and populate playlist library
        getplaylistList();
        //Map the song list to the song viewer
        playlistMap = new PlaylistMapper(view.getContext(), playlists);
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
                playlistTransactID = playlists.get(position).getID();
                playlistTranscactStr = playlists.get(position).getTitle();
                //Switch to subplaylist song view
                Fragment subFragment = new PlaylistSubFragment_Members();
                subFragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                if (subFragment != null) {
                    Log.d("Playlist", "Switch: Playlist Member View");
                    fragmentManager.beginTransaction().replace(R.id.frame_container,
                            subFragment).addToBackStack(null).commit();
                }
            }

        });
        playlistView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, final View view,
                                           int position, long id) {
                playlistTransactID = playlists.get(position).getID();
                playlistTranscactStr = playlists.get(position).getTitle();
                if (playlistTransactID >= 0) {
                    final PopupMenu popupMenu = new PopupMenu(getActivity(), view);
                    popupMenu.inflate(R.menu.playlist_popup_menu);
                    popupMenu.show();
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();
                            if (id == R.id.playlist_add) {
                                //Bundle up id and title for sub view
                                Bundle bundle = new Bundle();
                                bundle.putLong("playlist_id", playlistTransactID);
                                bundle.putString("playlist_name", playlistTranscactStr);
                                bundle.putString("playlist_opt", "add");
                                //Switch to subplaylist song view
                                Fragment subFragment = new PlaylistSubFragment_Modify();
                                subFragment.setArguments(bundle);
                                FragmentManager fragmentManager = getFragmentManager();
                                if (subFragment != null) {
                                    Log.d("Playlist", "Switch: Playlist Member View");
                                    fragmentManager.beginTransaction().replace(R.id.frame_container,
                                            subFragment).addToBackStack(null).commit();
                                }
                            } else if (id == R.id.playlist_delete) {
                                //Bundle up id and title for sub view
                                Bundle bundle = new Bundle();
                                bundle.putLong("playlist_id", playlistTransactID);
                                bundle.putString("playlist_name", playlistTranscactStr);
                                bundle.putString("playlist_opt", "delete");
                                //Switch to subplaylist song view
                                Fragment subFragment = new PlaylistSubFragment_Modify();
                                subFragment.setArguments(bundle);
                                FragmentManager fragmentManager = getFragmentManager();
                                if (subFragment != null) {
                                    Log.d("Playlist", "Switch: Playlist Member View");
                                    fragmentManager.beginTransaction().replace(R.id.frame_container,
                                            subFragment).addToBackStack(null).commit();
                                }
                            }
                            return true;
                        }
                    });
                }
                return true;
            }
        });
        //Click Listener for Play/Pause
        view.findViewById(R.id.play_pause_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity) getActivity()).getMusicService().playing) {
                    ((MainActivity) getActivity()).getMusicService().pausePlay();
                } else {
                    ((MainActivity) getActivity()).getMusicService().resumePlay();
                    trackProgressBar();
                }
            }
        });
        //Listen for when the seekbar is touched
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            //If the seekbar was touched by the user
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(getActivity() != null  && fromUser){
                    int duration = ((MainActivity) getActivity())
                            .getMusicService().getPlayer().getDuration();
                    Log.d("SeekBar Heading To ", String.valueOf(progress*duration/100));
                    //Manually seek to position
                    ((MainActivity)getActivity())
                            .getMusicService().getPlayer().seekTo(progress*duration/100);
                    //Update the music service's position
                    ((MainActivity)getActivity())
                            .getMusicService().setPlayerPos(progress*duration/100);
                }
            }
        });


        return view;
    }

    public Playlist getRecentlyAdded() {
        return new Playlist(-1, "Recently Added");
    }

    public Playlist getRecentlyPlayed() {
        return new Playlist(-1, "Recently Played");
    }

    public Playlist getRecentlyDownloaded() {
        return new Playlist(-1, "Recently Downloaded");
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

    public void updatePlaylists() {
        //clear list
        playlists.clear();
        playlistMap.notifyDataSetChanged();
        //Add a faux recently playlists
        playlists.add(getRecentlyAdded());
        playlists.add(getRecentlyPlayed());
        //update list
        getplaylistList();
        playlistMap.notifyDataSetChanged();
    }

    public void namePrompt() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Create a new Playlist");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String playlistName = input.getText().toString();
                createPlaylist(playlistName);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void createPlaylist(String playlistName) {
        String[] projection = new String[]{
                MediaStore.Audio.Playlists._ID,
                MediaStore.Audio.Playlists.NAME,
                MediaStore.Audio.Playlists.DATA
        };
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, playlistName);
        values.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        Uri uri = getActivity().getApplicationContext().getContentResolver().insert
                (MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
            getActivity().getApplicationContext().getContentResolver().query
                    (uri, projection, null, null, null);
        }
        updatePlaylists();
    }

    public void deletePlaylist(String playlistName) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.playlist, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == action_newplaylist) {
            namePrompt();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(runnable);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Update toggle if needed after return from back stack
        //If paused, toggle the controller correctly
        if (((MainActivity) getActivity()).getMusicService().paused) {
            ToggleButton toggle = (ToggleButton) view.findViewById(play_pause_toggle);
            toggle.setChecked(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setControllerSong(String songName) {
        TextView currentSong = (TextView) view
                .findViewById(R.id.music_current_song);
        currentSong.setText(songName);

    }

    public void showController() {
        LinearLayout current = (LinearLayout) view
                .findViewById(R.id.music_current);
        current.setVisibility(View.VISIBLE);
        RelativeLayout controller = (RelativeLayout) view.findViewById(R.id.music_controller);
        controller.setVisibility(View.VISIBLE);
    }

    public void hideController() {
        LinearLayout current = (LinearLayout) view
                .findViewById(R.id.music_current);
        current.setVisibility(View.GONE);
        RelativeLayout controller = (RelativeLayout) view
                .findViewById(R.id.music_controller);
        controller.setVisibility(View.GONE);

    }
    public void trackProgressBar(){
        runnable = new Runnable() {
            @Override
            public void run() {
                String currentTime;
                String endTime;
                if (getActivity() != null && ((MainActivity)getActivity())
                        .getMusicService().getPlayer().isPlaying()) {

                    int currentPosition = ((MainActivity) getActivity())
                            .getMusicService().getPlayer().getCurrentPosition();
                    int duration = ((MainActivity) getActivity())
                            .getMusicService().getPlayer().getDuration();
                    int progress = (currentPosition * 100) / duration;

                    currentTime = String.format("%01d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(currentPosition),
                            TimeUnit.MILLISECONDS.toSeconds(currentPosition) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                            .toMinutes(currentPosition))
                    );
                    endTime = String.format("%01d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(duration),
                            TimeUnit.MILLISECONDS.toSeconds(duration) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
                                            .toMinutes(duration))
                    );

                    TextView currentSong = (TextView) view
                            .findViewById(R.id.seek_bar_curr);
                    currentSong.setText(currentTime);

                    TextView currentEnd = (TextView) view
                            .findViewById(R.id.seek_bar_max);
                    currentEnd.setText(endTime);

                    seekBar.setProgress(progress);
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.postDelayed(runnable, 1000);

    }
}