package com.hech.musicplayer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SubFragment extends Fragment {
    private GridView storeView;
    private View StoreFragmentView;
    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean albumViewMode = false;
    private ArrayList<Song> storeList;
    private ArrayList<Album> albumList;
    private HashMap<String, Number> songPrices;
    private LinkedHashMap<String, Number> albumPrices;
    private Fragment currentFrag = this;
    private DownloadTask dlTask;
    private Context context;
    private DownloadManager manager;
    private ArrayList<Album> albumQueryResult;
    private ArrayList<Song> songQueryResult;
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
        player = new MediaPlayer();
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void songPicked(String songName){
        Log.d("songNamePicked", songName);
        //confirmPayment(songName, false); TODO needed for streaming?
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
        View view = inflater.inflate(R.layout.fragment_store,
                        container, false);
        StoreFragmentView = view;
        // Get the store view
        storeView = (GridView)view.findViewById(R.id.store_list);
        context = getActivity().getApplicationContext();
        if(!albumViewMode){
            StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
            storeView.setAdapter(storeMap);
        }
        else{
            AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMap);
        }
        return view;
    }

    public void onDestroy(){
        getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.store, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Get all the songs from the database and show on the device
    public void getOnlineSongList(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereLessThan("Price", 10);
        query.findInBackground(new FindCallback<ParseObject>() {
           public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<Song> result = new ArrayList<Song>();
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
                    Number price = parseObjects.get(i).getNumber("Price");
                    Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                    Song  s = new Song(0,name, artist, album);
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
                storeList = result;
                albumPrices = albumResult;
                albumList = result2;
                Log.d("Info album list size = ", ((Integer)result2.size()).toString());
                if(albumViewMode) {
                   Log.d("AlbumViewMode: ", "started");
                   AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
                   storeView.setAdapter(albumMap);
               }
               else{
                   StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
                   storeView.setAdapter(songMap);
               }

           }
       });
    }


    public void streamSong(String songName) {
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail

        final String songNameF = songName;
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
                    //String url = "http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3";
                    // Dropbox url must end in ?dl=1
                    Log.d("StreamSong", url);
                    try {
                        // set up song from url to media player source
                        //player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        player.setDataSource(url);
                        player.prepare();
                        player.start();
                    } catch (Exception ex) {
                        Log.e("Stream Song", "Error Setting Data Source", ex);
                    }
                    // gets song length in milliseconds
                    //mediaLengthInMS = player.getDuration();

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
                String SearchQuery = input.getText().toString().toLowerCase();
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
                            AlbumMapper songMap = new AlbumMapper(StoreFragmentView.getContext(), albumQueryResult, albumQueryResultPrices, currentFrag);
                            storeView.setAdapter(songMap);
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
                String SearchQuery = input.getText().toString().toLowerCase();
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
                            songQueryResult = new ArrayList<Song>();
                            songQueryResultPrices = new HashMap<String, Number>();
                            for(int i = 0; i < parseObjects.size(); ++i) {
                                String artist = parseObjects.get(i).getString("Artist");
                                String name = parseObjects.get(i).getString("Name");
                                String album = parseObjects.get(i).getString("Album");
                                Number price = parseObjects.get(i).getNumber("Price");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                Song s = new Song(0, name, artist, album);
                                songQueryResult.add(s);
                                songQueryResultPrices.put(name, price);
                            }
                            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), songQueryResult, songQueryResultPrices, currentFrag);
                            storeView.setAdapter(songMap);
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
                String SearchQuery = input.getText().toString().toLowerCase();
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
                            songQueryResult = new ArrayList<Song>();
                            songQueryResultPrices = new HashMap<String, Number>();
                            for(int i = 0; i < parseObjects.size(); ++i) {
                                String artist = parseObjects.get(i).getString("Artist");
                                String name = parseObjects.get(i).getString("Name");
                                String album = parseObjects.get(i).getString("Album");
                                Number price = parseObjects.get(i).getNumber("Price");
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                Song s = new Song(0, name, artist, album);
                                songQueryResult.add(s);
                                songQueryResultPrices.put(name, price);
                            }
                            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), songQueryResult, songQueryResultPrices, currentFrag);
                            storeView.setAdapter(songMap);
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
                String SearchQuery = input.getText().toString().toLowerCase();
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
                            AlbumMapper songMap = new AlbumMapper(StoreFragmentView.getContext(), albumQueryResult, albumQueryResultPrices, currentFrag);
                            storeView.setAdapter(songMap);
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
        if (id == R.id.store_album_mode) {
            Log.d("StoreFragment", "Album mode = " + albumViewMode);
            albumViewMode = !albumViewMode;
            if(albumViewMode) {
                Log.d("AlbumViewMode: ", "started");
                AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
                storeView.setAdapter(albumMap);
            }
            else{
                StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
                storeView.setAdapter(songMap);
            }
        }
        if(id == R.id.store_search_songs){
            albumViewMode = false;
            queryForSong();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.store_search_albums){
            albumViewMode = true;
            queryForAlbum();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
        }
        if(id == R.id.store_search_genres_album){
            albumViewMode = true;
            queryForGenreAlbum();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
        }
        if(id == R.id.store_search_genres_song){
            albumViewMode = true;
            queryForGenreSong();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
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