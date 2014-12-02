package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.hech.musicplayer.R.id.play_pause_toggle;


public class AlbumFragment extends Fragment {
    private ArrayList<Album> albums;
    private ListView albumView;
    private AlbumMapper albumMap;
    View view;
    private SeekBar seekBar;
    private Runnable runnable;
    private final Handler handler = new Handler();

    public AlbumFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_song, container, false);
        setHasOptionsMenu(true);
        //Get the seekbar view
        seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        albumView = (ListView)view.findViewById(R.id.song_list);
        albums = new ArrayList<Album>();
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
            seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
            hideController();
        }
        getAlbumList();
        //Set Title
        try {
            getActivity().getActionBar().setTitle("Albums");
        } catch(NullPointerException e){
            Log.e("Set Title: ",e.toString());
        }
        albumMap = new AlbumMapper(view.getContext(), albums);
        albumView.setAdapter(albumMap);

        albumView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                //Bundle up id and title for sub view
                Bundle bundle = new Bundle();
                bundle.putLong("album_id", albums.get(position).getId());
                bundle.putString("album_name", albums.get(position).getName());

                //Switch to subplaylist song view
                Fragment subFragment = new AlbumSubFragment();
                subFragment.setArguments(bundle);
                FragmentManager fragmentManager = getFragmentManager();
                if(subFragment != null) {
                    Log.d("Album", "Switch: Album Sub View");
                    fragmentManager.beginTransaction().replace(R.id.frame_container,
                            subFragment).addToBackStack(null).commit();
                }
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
    @Override
    public void onResume(){

        super.onResume();
        //Update toggle if needed after return from back stack
        //If paused, toggle the controller correctly
        if (((MainActivity) getActivity()).getMusicService().paused) {
            ToggleButton toggle = (ToggleButton) view.findViewById(play_pause_toggle);
            toggle.setChecked(true);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.global, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
