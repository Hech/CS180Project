package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import java.util.Map;

import static com.hech.musicplayer.R.id.action_settings;

public class StoreFragment extends Fragment {
    private ListView storeView;
    private View StoreFragmentView;
    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;
    private boolean albumViewMode = false;
    private ArrayList<Song> storeList;
    private ArrayList<Album> albumList;
    private HashMap<String, Number> songPrices;
    private LinkedHashMap<String, Number> albumPrices;

    private float balance;

    private ServiceConnection musicConnection = new ServiceConnection() {

        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicBound = true;
        }

        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };



    public StoreFragment(){}
    @Override

    public void onStart(){
        super.onStart();

        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void songPicked(View view){
        confirmPayment(view.getTag().toString(), false);

    }

    public void albumPicked(View view){
        confirmPayment(view.getTag().toString(), true);
    }

    public void confirmPayment(String name, boolean isAlbum)
    {
        Number price;
        if(isAlbum)
        {
            price = albumPrices.get(name);
        }
        else
        {
            price = songPrices.get(name);
        }


        boolean confirmation = displayAndWaitForConfirm(name, price);
        if(confirmation)
        {

            if(isAlbum)
            {
                downloadAlbum(name);
            }
            else
            {
                downloadSong(name);
            }

        }
        else
        {
            Toast.makeText(getActivity().getApplicationContext(), "Transaction cancelled.",
                Toast.LENGTH_SHORT).show();
        }
    }

    public View onCreateView (LayoutInflater inflater,
                              ViewGroup container,
                              Bundle savedInstanceState){
        getOnlineSongList();
        View view = inflater.inflate(R.layout.fragment_store,
                        container, false);
        StoreFragmentView = view;
        // Get the store view
        storeView = (ListView)view.findViewById(R.id.store_list);
        setHasOptionsMenu(true);
        if(!albumViewMode)
        {
            StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices);
            storeView.setAdapter(storeMap);
            storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, final View view,
                                        int position, long id) {
                    songPicked(view);

                }

            });
        }
        else
        {
            AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices);
            storeView.setAdapter(albumMap);
            storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, final View view, int position, long id) {
                    albumPicked(view);

                }

            });
        }
        //Fragments need Click Listeners


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
    public void getOnlineSongList() {
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
                        Album a = new Album(album, artist);
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
                   AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices);
                   storeView.setAdapter(albumMap);
                   storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                       public void onItemClick(AdapterView parent, final View view, int position, long id) {
                           albumPicked(view);
                       }
                   });
               }
               else
               {
                   StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices);
                   storeView.setAdapter(songMap);
                   storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                       @Override
                       public void onItemClick(AdapterView parent, final View view,
                                               int position, long id) {
                           songPicked(view);

                       }

                   });
               }

           }
       });
       //albumPrices = null;
    }
    //Display Price and get confirmation
    public boolean displayAndWaitForConfirm(String songName, Number price)
    {
        //TODO Get user choice via a button or something and return their choice
        return false;
    }

    public void downloadAlbum(String albumName)
    {
        //Todo download all songs associated with album (in background) and make sure that download is successful.
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail


        //((MainActivity)getActivity()).setNewSongsAvail(true);

    }

    public void downloadSong(String songName)
    {
        //Todo download song (in background) and make sure that download is successful.
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail

    }


    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return ((MainActivity)getActivity()).getUserLoggedin();
    }

    // Deducts the song price from the account balance
    public void payment(String user, final Float songPrice) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject PO, ParseException e) {
                if (PO == null) {
                    Log.d("Error", "What the Fuck?");
                } else if (PO.getDouble("Money") >= songPrice)
                {
                    PO.put("Money", PO.getDouble("Money") - songPrice);
                    PO.saveInBackground();


                }
            }
        });

    }

    // Rate the song (5-star scale)
    public void rateSong() {
        //Todo get user choice, update database, and give user option to write full review
    }

    // Review the song (text review)
    public void reviewSong() {
        //Todo get user review and update database to show user has reviewd the song
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
                AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices);
                storeView.setAdapter(albumMap);
                storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView parent, final View view, int position, long id) {
                        albumPicked(view);
                    }
                });
            }
            else
            {
                StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices);
                storeView.setAdapter(songMap);
                storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView parent, final View view,
                                            int position, long id) {
                        songPicked(view);

                    }

                });
            }

        }
        if(id == R.id.action_end)
        {
            getActivity().stopService(playIntent);
            musicService = null;
            Log.d("StoreFragment", "AppCloseCalled");
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }
}
