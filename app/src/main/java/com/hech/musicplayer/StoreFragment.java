package com.hech.musicplayer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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
    private float balance;
    private boolean bought= false;

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
        currentFrag = this;
        if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void revPrompt(final String t){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Review");
        //Set an EditText view to get rating
        final EditText rate = new EditText(getActivity());
        // rate.setHint("Rate: 1-5");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        //alert.setView(rate);
        alert.setView(input);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String user_rev = input.getText().toString();
                //final String user_rate= rate.getText().toString();
                 //Toast.makeText(getActivity().getApplicationContext(), user_rate,
                 //Toast.LENGTH_SHORT).show();
                //sets up a query to DB to look at Users class
                // where the user is the current user login
                ParseQuery<ParseObject>query= ParseQuery.getQuery("Users");
                query.whereEqualTo("Login",getCurrentUser());
                // fetches the row for that current user login
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        //fetch map of song to reviews
                        HashMap<String,String> maprev=
                                   (HashMap <String,String>)parseObject.get("Map_Songs_to_Reviews");

                        //stores the user review for the song in the hashmap maprev
                        if(maprev== null)
                            maprev= new HashMap<String, String>();

                            maprev.put(t,user_rev);
                        //push maprev to DB
                        parseObject.put("Map_Songs_to_Reviews",maprev);
                        parseObject.saveInBackground();
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void buyPrompt(String songn, Number p){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Confirm Payment of $" + p);
        // Set an EditText view to get user input
        //final EditText input = new EditText(getActivity());
        //alert.setView(input);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                bought = true;
                //String user_rev = input.getText().toString();
                // Toast.makeText(getActivity().getApplicationContext(), user_rev,
                // Toast.LENGTH_SHORT).show();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void ratePrompt(final String t){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Rating: Enter 1-5");
        // Set an EditText view to get user input
        final EditText rate = new EditText(getActivity());
        alert.setView(rate);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Number user_rate = Integer.parseInt(rate.getText().toString());

                ParseQuery<ParseObject>query= ParseQuery.getQuery("Ratings");
                query.whereEqualTo("Login",getCurrentUser());
                // fetches the row for that current user login
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List <ParseObject> parseObjects, ParseException e) {
                        boolean found=false;
                        for(int i=0; i<parseObjects.size();i++) {
                            String p= parseObjects.get(i).getString("SongId");
                            if(p.equals(t)) {
                                parseObjects.get(i).put("Reviews", user_rate);
                                parseObjects.get(i).saveInBackground();
                                found =true;
                                break;
                            }
                        }
                        // if the song is not there, add it
                        if (!found) {
                            ParseObject parseObject= new ParseObject("Ratings");
                            parseObject.put("Login",getCurrentUser());
                            parseObject.put("SongId",t);
                            parseObject.put("Reviews",user_rate);
                            parseObject.saveInBackground();
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

    public void rateAlbumPrompt(final String t){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Rating: Enter 1-5");
        // Set an EditText view to get user input
        final EditText rate = new EditText(getActivity());
        alert.setView(rate);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Number user_rate = Integer.parseInt(rate.getText().toString());

                ParseQuery<ParseObject>query= ParseQuery.getQuery("Album_Ratings");
                query.whereEqualTo("Login",getCurrentUser());
                // fetches the row for that current user login
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List <ParseObject> parseObjects, ParseException e) {
                        boolean found=false;
                        for(int i=0; i<parseObjects.size();i++) {
                            String p= parseObjects.get(i).getString("AlbumId");
                            if(p.equals(t)) {
                                parseObjects.get(i).put("Reviews", user_rate);
                                parseObjects.get(i).saveInBackground();
                                found =true;
                                break;
                            }
                        }
                        // if the song is not there, add it
                        if (!found) {
                            ParseObject parseObject= new ParseObject("Album_Ratings");
                            parseObject.put("Login",getCurrentUser());
                            parseObject.put("AlbumId",t);
                            parseObject.put("Reviews",user_rate);
                            parseObject.saveInBackground();
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


    public void songPicked(String songName){
        //Log.d("songPickedId", view.getTag().toString());

        //Song selectedSong = storeList.get(Integer.parseInt(view.getTag().toString()));
        //String songName = selectedSong.getTitle();

        Log.d("songNamePicked", songName);
        confirmPayment(songName, false);
    }

    public void albumPicked(String albumName){
        //Album selectedAlbum = albumList.get(Integer.parseInt(view.getTag().toString()));
        //String albumName = selectedAlbum.getName();

        Log.d("albumNamePicked", albumName);
        confirmPayment(albumName, true);
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
            payment(getCurrentUser(), price.floatValue());
            Log.d("confirmPayment",name);

            if(isAlbum)
            {
                downloadAlbum(name);
                ((MainActivity)getActivity()).setNewSongsAvail(true);
            }
            else
            {
                downloadSong(name);
                ((MainActivity)getActivity()).setNewSongsAvail(true);
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
        storeView = (GridView)view.findViewById(R.id.store_list);
        setHasOptionsMenu(true);
        if(!albumViewMode)
        {
            StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
            storeView.setAdapter(storeMap);
           /* storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, final View v, int position, long id) {
                    Log.d("DEBUG", "I AM HERE!");
                    songPicked(v);

                }

            }); */


//        }
            /*storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, final View view,
                                        int position, long id) {
                    songPicked(view);

                }

            });*/
        }
        else
        {
            AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMap);

            /*storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView parent, final View view, int position, long id) {
                    albumPicked(view);

                }

            });*/
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
                   AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
                   storeView.setAdapter(albumMap);
                  /* storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                       public void onItemClick(AdapterView parent, final View view, int position, long id) {
                           albumPicked(view);
                       }
                   });*/
               }
               else
               {
                   StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
                   storeView.setAdapter(songMap);
               }

           }
       });
       //albumPrices = null;
    }
    //Display Price and get confirmation
    public boolean displayAndWaitForConfirm(String songName, Number price)
    {
        //TODO Get user choice via a button or something and return their choice
        buyPrompt(songName,price);
        if(bought) {
            bought= false;
            return true;
        }
        else
            return false;
        //return false;
        // for testing song download, ignore confirmation for now

    }

    public void downloadAlbum(String albumName)
    {
        //Todo download all songs associated with album (in background) and make sure that download is successful.
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereEqualTo("Album", albumName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects == null) {
                } else {
                    for (int i = 0; i < parseObjects.size(); ++i) {
                        String url = parseObjects.get(i).getString("Link_To_Download");
                        String SongName = parseObjects.get(i).getString("Name");
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setDescription(url);
                        request.setTitle(SongName);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,SongName + ".mp3");

                        //get download service and enqueue file
                        manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                    }
                }
            }
        });
        //((MainActivity)getActivity()).setNewSongsAvail(true);

    }

    public void downloadSong(String songName)
    {
        //Todo download song (in background) and make sure that download is successful.
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
                    Log.d("parseObject2", "parseObject is null. The getFirstRequest failed");
                }
                else {
                    String url = parseObject.getString("Link_To_Download");
                    // Dropbox url must end in ?dl=1
                    Log.d("DownloadSong", url);

                    //new DownloadTask(StoreFragmentView.getContext()).execute( url, songNameF);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setDescription(url);
                    request.setTitle(songNameF);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,songNameF + ".mp3");

                    //get download service and enqueue file
                    manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                    manager.enqueue(request);


                }
            }
        });
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

    public void queryForAlbum()
    {
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
                                Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                                Album a = new Album(album, artist);
                                if (!albumQueryResultPrices.containsKey(album)) {
                                    albumQueryResult.add(a);
                                    albumQueryResultPrices.put(album, aPrice);
                                }
                            }
                            AlbumMapper songMap = new AlbumMapper(StoreFragmentView.getContext(), albumQueryResult, albumQueryResultPrices, currentFrag);
                            storeView.setAdapter(songMap);
                        }
                        else
                        {
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
                        if( e == null)
                        {
                            if(parseObjects.isEmpty())
                            {
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
                        else
                        {
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
    // Rate the song (5-star scale)
    public void rateSong(String s) {
        ratePrompt(s);
        reviewSong(s);
    }

    public void rateAlbum(String a){
        rateAlbumPrompt(a);
        reviewAlbum(a);
    }

    // Review the song (text review)
    public void reviewSong(String s) {
        String title = s;
        Log.d("TITLE", title);

        //Todo get user review and update database to show user has reviewed the song

            revPrompt(title);


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
            else
            {
                StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
                storeView.setAdapter(songMap);
            }

        }
        if(id == R.id.store_search_songs)
        {
            albumViewMode = false;
            queryForSong();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.store_search_albums)
        {
            albumViewMode = true;
            queryForAlbum();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(), albumList, albumPrices, currentFrag);
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

    public void reviewAlbum(final String s){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Review");
        //Set an EditText view to get rating
        final EditText rate = new EditText(getActivity());
        // rate.setHint("Rate: 1-5");
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        //alert.setView(rate);
        alert.setView(input);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String user_rev = input.getText().toString();

                //sets up a query to DB to look at Users class
                // where the user is the current user login
                ParseQuery<ParseObject>query= ParseQuery.getQuery("Users");
                query.whereEqualTo("Login",getCurrentUser());
                // fetches the row for that current user login
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        //fetch map of song to reviews
                        HashMap<String,String> maprev=
                                (HashMap <String,String>)parseObject.get("Map_Albums_to_Reviews");

                        //stores the user review for the song in the hashmap maprev
                        if(maprev== null)
                            maprev= new HashMap<String, String>();

                        maprev.put(s,user_rev);
                        //push maprev to DB
                        parseObject.put("Map_Albums_to_Reviews",maprev);
                        parseObject.saveInBackground();
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }



}
