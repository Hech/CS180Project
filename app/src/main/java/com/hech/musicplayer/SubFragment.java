package com.hech.musicplayer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SubFragment extends Fragment {
    private ListView subView;
    private View SubFragmentView;
    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean albumViewMode = false;
    private ArrayList<StreamSong> subSongList;
    private ArrayList<Album> albumList;
    private HashMap<String, Number> songPrices;
    private LinkedHashMap<String, Number> albumPrices;
    private Fragment currentFrag = this;
    private Context context;
    private DownloadManager manager;
    private ArrayList<Album> albumQueryResult;
    private ArrayList<StreamSong> songQueryResult;
    private HashMap<String, Number> songQueryResultPrices;
    private LinkedHashMap<String, Number> albumQueryResultPrices;
    private Number balance;
    private boolean bought= false;
    private MediaPlayer player;
    private int mediaLengthInMS;
    private int position;

    private ServiceConnection musicConnection = new ServiceConnection() {
        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setCurrUser(((MainActivity) getActivity()).getUserLoggedin());
            player = musicService.getPlayer();
            musicBound = true;
        }
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    public SubFragment(){}

    @Override
    public void onStart(){
        super.onStart();
        currentFrag = this;
        //player = new MediaPlayer();
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void songPicked(String songName){
        Log.d("songNamePicked", songName);
        streamSong(songName);
    }

    public void albumPicked(String albumName){
        Log.d("albumNamePicked", albumName);
        //confirmPayment(albumName, true); TODO needed for streaming?
    }

    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", getCurrentUser());
        setHasOptionsMenu(true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                for(int i=0; i< parseObjects.size();i++){
                    balance= parseObjects.get(i).getNumber("Money");
                }
            }
        });
        getOnlineSongList();
        View view = inflater.inflate(R.layout.fragment_sub,
                        container, false);
        SubFragmentView = view;

        // Get the subscription view
        subView = (ListView)view.findViewById(R.id.sub_list);
        context = getActivity().getApplicationContext();

        // Map the Online Song List to the Subscription Song View
        if(!albumViewMode){
            SubMapper subMap = new SubMapper(view.getContext(), subSongList);
            subView.setAdapter(subMap);
        }
        else{
            AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices, currentFrag);
            subView.setAdapter(albumMap);
        }


        subView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, final View view,
                                    int position, long id) {
                StreamSong s = subSongList.get(Integer.parseInt(view.getTag().toString()));
                songPicked(s.getTitle());
            }
        });
        return view;
    }

    public void onDestroy(){
        //player.stop();
        getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.sub, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Get all the songs from the database and show on the device
    public void getOnlineSongList(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereLessThan("Price", 10);
        query.findInBackground(new FindCallback<ParseObject>() {
           public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<StreamSong> result = new ArrayList<StreamSong>();
                LinkedHashMap<String, Number> albumResult = new LinkedHashMap<String, Number>();
                HashMap<String, Number> songResult = new HashMap<String, Number>();
                ArrayList<Album> result2 = new ArrayList<Album>();
                for(int i = 0; i < parseObjects.size(); ++i)
                {
                    Integer i2 = i;
                    Log.d("Song", i2.toString() );
                    String artist =  parseObjects.get(i).getString("Artist");
                    String name =  parseObjects.get(i).getString("Name");
                    String album =  parseObjects.get(i).getString("Album");
                    String genre = parseObjects.get(i).getString("Genre");
                    String url = parseObjects.get(i).getString("Link_To_Download");
                    Number price = parseObjects.get(i).getNumber("Price");
                    Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                    StreamSong  s = new StreamSong(0, name, artist, album, genre, url);
                    result.add(s);
                    songResult.put(name, price);
                    if(!albumResult.containsKey(album))
                    {
                        Log.d("Album name", album);
                        Log.d("Album price", aPrice.toString());
                        albumResult.put(album, aPrice);
                        Log.d("check", albumResult.get(album).toString() );
                        Album a = new Album(album, artist, genre);
                        result2.add(a);
                    }
                }
                songPrices = songResult;
                subSongList = result;
                albumPrices = albumResult;
                albumList = result2;
                Log.d("Info album list size = ", ((Integer)result2.size()).toString());
                if(albumViewMode) {
                   Log.d("AlbumViewMode: ", "started");
                   AlbumMapper albumMap = new AlbumMapper(SubFragmentView.getContext(), albumList, albumPrices, currentFrag);
                   subView.setAdapter(albumMap);
               }
               else{
                   Log.e("getOnlineSongList - subSongList size", Integer.toString(result.size()));
                   SubMapper songMap = new SubMapper(SubFragmentView.getContext(), subSongList);
                   subView.setAdapter(songMap);
               }

           }
       });
    }


    public void streamSong(String songName) {
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail
        player.reset();
        // Queries Song_Bank for ParseObjects
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereEqualTo("Name", songName);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null) {
                    Log.d("parseObject3", "parseObject is null. The getFirstRequest failed");
                } else {
                    //Song Name
                    String name = parseObject.getString("Name");

                    String url = parseObject.getString("Link_To_Download");
                    // Dropbox url must end in ?dl=1
                    Log.d("StreamSong", url);
                    try {
                        // set up song from url to media player source
                        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(url);
                        player.prepare();
                        player.start();
                    } catch (Exception ex) {
                        Log.e("Stream Song", "Error Setting Data Source", ex);
                    }

                }
            }
        });
    }

    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return ((MainActivity)getActivity()).getUserLoggedin();
    }

    public void queryForAlbum(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("What album are you looking for");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String SearchQuery = input.getText().toString();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
                query.whereContains("Album", SearchQuery);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if( e == null) {
                            if (parseObjects.isEmpty()) {
                                Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            albumQueryResult = new ArrayList<Album>();
                            albumQueryResultPrices = new LinkedHashMap<String, Number>();
                            for (int i = 0; i < parseObjects.size(); ++i) {
                                String artist = parseObjects.get(i).getString("Artist");
                                String album = parseObjects.get(i).getString("Album");
                                String genre = parseObjects.get(i).getString("Genre");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                Album a =   new Album(album, artist, genre);
                                if (!albumQueryResultPrices.containsKey(album)) {
                                    albumQueryResult.add(a);
                                    albumQueryResultPrices.put(album, aPrice);
                                }
                            }
                            AlbumMapper songMap = new AlbumMapper(SubFragmentView.getContext(), albumQueryResult, albumQueryResultPrices, currentFrag);
                            subView.setAdapter(songMap);
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Exception:", e.getMessage());
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void queryForSong()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("What song are you looking for");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String SearchQuery = input.getText().toString();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
                query.whereContains("Name", SearchQuery);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if( e == null){
                            if(parseObjects.isEmpty()){
                                Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            songQueryResult = new ArrayList<StreamSong>();
                            songQueryResultPrices = new HashMap<String, Number>();
                            for(int i = 0; i < parseObjects.size(); ++i) {
                                String artist = parseObjects.get(i).getString("Artist");
                                String name = parseObjects.get(i).getString("Name");
                                String album = parseObjects.get(i).getString("Album");
                                String genre = parseObjects.get(i).getString("Genre");
                                String url = parseObjects.get(i).getString("Link_To_Download");
                                Number price = parseObjects.get(i).getNumber("Price");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");

                                StreamSong s = new StreamSong(0, name, artist, album, genre, url);
                                songQueryResult.add(s);
                                songQueryResultPrices.put(name, price);
                            }
                            subSongList = songQueryResult;
                            SubMapper songMap = new SubMapper(SubFragmentView.getContext(), songQueryResult);
                            subView.setAdapter(songMap);
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                    Toast.LENGTH_SHORT).show();
                              Log.d("Exception:", e.getMessage());
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void queryForGenreSong()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("What Genre are you looking for");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String SearchQuery = input.getText().toString();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
                query.whereContains("Genre", SearchQuery);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if( e == null){
                            if(parseObjects.isEmpty()){
                                Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            songQueryResult = new ArrayList<StreamSong>();
                            songQueryResultPrices = new HashMap<String, Number>();
                            for(int i = 0; i < parseObjects.size(); ++i) {
                                String artist = parseObjects.get(i).getString("Artist");
                                String name = parseObjects.get(i).getString("Name");
                                String album = parseObjects.get(i).getString("Album");
                                String genre = parseObjects.get(i).getString("Genre");
                                String url = parseObjects.get(i).getString("Link_To_Download");
                                Number price = parseObjects.get(i).getNumber("Price");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                StreamSong s = new StreamSong(0, name, artist, album, genre, url);
                                songQueryResult.add(s);
                                songQueryResultPrices.put(name, price);
                            }
                            SubMapper songMap = new SubMapper(SubFragmentView.getContext(), songQueryResult);
                            subView.setAdapter(songMap);
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Exception:", e.getMessage());
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void queryForGenreAlbum()
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("What genre are you looking for");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String SearchQuery = input.getText().toString();
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
                query.whereContains("Genre", SearchQuery);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if( e == null) {
                            if (parseObjects.isEmpty()) {
                                Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            albumQueryResult = new ArrayList<Album>();
                            albumQueryResultPrices = new LinkedHashMap<String, Number>();
                            for (int i = 0; i < parseObjects.size(); ++i) {

                                String artist = parseObjects.get(i).getString("Artist");
                                String album = parseObjects.get(i).getString("Album");
                                String genre = parseObjects.get(i).getString("Genre");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                Album a =   new Album(album, artist, genre);
                                if (!albumQueryResultPrices.containsKey(album)) {
                                    albumQueryResult.add(a);
                                    albumQueryResultPrices.put(album, aPrice);
                                }
                            }
                            AlbumMapper songMap = new AlbumMapper(SubFragmentView.getContext(), albumQueryResult, albumQueryResultPrices, currentFrag);
                            subView.setAdapter(songMap);
                        }
                        else{
                            Toast.makeText(getActivity().getApplicationContext(), "No results found.",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("Exception:", e.getMessage());
                        }
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_stopPlay) {
            Log.d("SubFragment", "MusicStopCalled");
            player.stop();
        }
        if(id == R.id.store_search_songs){
            albumViewMode = false;
            queryForSong();
            SubMapper songMap = new SubMapper(SubFragmentView.getContext(), subSongList);
            subView.setAdapter(songMap);
        }
        if(id == R.id.action_end){
            getActivity().stopService(playIntent);
            musicService = null;
            Log.d("StoreFragment", "AppCloseCalled");
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }


}