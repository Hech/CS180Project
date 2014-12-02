package com.hech.musicplayer;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.EditText;
import android.widget.ListView;
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
    private Fragment currentFrag = this;
    private Context context;
    private DownloadManager manager;
    private ArrayList<Album> albumQueryResult;
    private ArrayList<Song> songQueryResult;
    private HashMap<String, Number> songQueryResultPrices;
    private LinkedHashMap<String, Number> albumQueryResultPrices;
    private Number balance;
    private View view;
    //private boolean bought= false;

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

    public StoreFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        currentFrag = this;
        //if (playIntent == null) {
          //  playIntent = new Intent(getActivity(), MusicService.class);
            //getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            //getActivity().startService(playIntent);
        //}
    }

    public void revPrompt(final String t) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Review");
        //Set an EditText view to get rating
        final EditText rate = new EditText(getActivity());
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
        alert.setView(input);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String user_rev = input.getText().toString();
                //sets up a query to DB to look at Users class
                // where the user is the current user login
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
                query.whereEqualTo("Login", getCurrentUser());
                // fetches the row for that current user login
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        //fetch map of song to reviews
                        //TODO check for exception here!!!!
                        HashMap<String, String> maprev =
                                (HashMap<String, String>) parseObject.get("Map_Songs_to_Reviews");
                        //stores the user review for the song in the hashmap maprev
                        if (maprev == null)
                            maprev = new HashMap<String, String>();

                        maprev.put(t, user_rev);
                        //push maprev to DB
                        parseObject.put("Map_Songs_to_Reviews", maprev);
                        parseObject.saveInBackground();
                    }
                });
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void buyPrompt(final String songn, final Number p, final boolean isAlbum, final boolean alreadyPurchased){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Confirm Payment of $" + p + "\n" + "Balance: $" + balance);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if(payment(getCurrentUser(), p.floatValue()));
                {
                    if (isAlbum) {
                        downloadAlbum(songn, alreadyPurchased);
                        ((MainActivity) getActivity()).setNewSongsAvail(true);
                    } else {
                        if(alreadyPurchased)
                            Log.d("DEBUG", "alreadyPurchased is true");
                        downloadSong(songn, alreadyPurchased);
                        ((MainActivity)getActivity()).setNewSongsAvail(true);
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void ratePrompt(final String t) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Rating: Enter 1-5");
        // Set an EditText view to get user input
        final EditText rate = new EditText(getActivity());
        alert.setView(rate);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Number user_rate = Integer.parseInt(rate.getText().toString());
                if (user_rate.intValue() < 1 || user_rate.intValue() > 5) {
                    Toast.makeText(getActivity().getApplicationContext(), "Invalid rating.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Ratings");
                    query.whereEqualTo("Login", getCurrentUser());
                    // fetches the row for that current user login
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            boolean found = false;
                            for (int i = 0; i < parseObjects.size(); i++) {
                                String p = parseObjects.get(i).getString("SongId");
                                if (p.equals(t)) {
                                    parseObjects.get(i).put("Reviews", user_rate);
                                    parseObjects.get(i).saveInBackground();
                                    found = true;
                                    break;
                                }
                            }
                            // if the song is not there, add it
                            if (!found) {
                                ParseObject parseObject = new ParseObject("Ratings");
                                parseObject.put("Login", getCurrentUser());
                                parseObject.put("SongId", t);
                                parseObject.put("Reviews", user_rate);
                                parseObject.saveInBackground();
                            }
                        }
                    });
                }
            }

        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void rateAlbumPrompt(final String t) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Rating: Enter 1-5");
        // Set an EditText view to get user input
        final EditText rate = new EditText(getActivity());
        alert.setView(rate);
        alert.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final Number user_rate = Integer.parseInt(rate.getText().toString());
                if (user_rate.intValue() < 1 || user_rate.intValue() > 5) {
                    Toast.makeText(getActivity().getApplicationContext(), "Invalid rating.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Album_Ratings");
                    query.whereEqualTo("Login", getCurrentUser());
                    // fetches the row for that current user login
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            boolean found = false;
                            for (int i = 0; i < parseObjects.size(); i++) {
                                String p = parseObjects.get(i).getString("AlbumId");
                                if (p.equals(t)) {
                                    parseObjects.get(i).put("Reviews", user_rate);
                                    parseObjects.get(i).saveInBackground();
                                    found = true;
                                    break;
                                }
                            }
                            // if the song is not there, add it
                            if (!found) {
                                ParseObject parseObject = new ParseObject("Album_Ratings");
                                parseObject.put("Login", getCurrentUser());
                                parseObject.put("AlbumId", t);
                                parseObject.put("Reviews", user_rate);
                                parseObject.saveInBackground();
                            }
                        }
                    });
                }
            }

        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    public void songPicked(String songName) {
        Log.d("songNamePicked", songName);
        confirmPayment(songName, false);
    }

    public void albumPicked(String albumName) {
        Log.d("albumNamePicked", albumName);
        confirmPayment(albumName, true);
    }

    public void confirmPayment(String name, boolean isAlbum) {
        boolean AlreadyPurchased = false;
        Number price;
        if (isAlbum) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Album_Downloads");
            query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("album_Id", name);
            try {
                ParseObject po = query.getFirst();
                if (po != null) {
                    AlreadyPurchased = true;
                }

            } catch (Exception e) {
                Log.d("Login", e.getMessage());
            }
            if (AlreadyPurchased)
                price = 0;
            else
                price = albumPrices.get(name);
        } else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
            query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("song_Id", name);
            try {
                ParseObject po = query.getFirst();
                if (po != null) {
                    AlreadyPurchased = true;
                }
            } catch (Exception e) {
                Log.d("Login", e.getMessage());
            }
            if (AlreadyPurchased)
                price = 0;
            else
                price = songPrices.get(name);
        }
        buyPrompt(name, price, isAlbum, AlreadyPurchased);
    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", getCurrentUser());
        setHasOptionsMenu(true);
        ParseObject po;
        try {
            po = query.getFirst();
            balance = po.getNumber("Money");
        } catch (ParseException e) {
            balance = null;
        }

        getOnlineSongList();
        view = inflater.inflate(R.layout.fragment_store,
                container, false);
        StoreFragmentView = view;
        // Get the store view
        storeView = (ListView) view.findViewById(R.id.store_list);
        context = getActivity().getApplicationContext();
        final Bundle bundle = new Bundle();
        Log.d("ALBUM BOOLEAN IS", " " + albumViewMode);
        if (!albumViewMode) {
            StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
            storeView.setAdapter(storeMap);
            // With a selection of any song/album in store,
            // user will be directed to another screen
            // which displays song/album info and reviews.

            storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, final View view,
                                        int position, long id) {
                    Song s = new Song(storeList.get(position).getID(),
                            storeList.get(position).getTitle(),
                            storeList.get(position).getArtist(),
                            storeList.get(position).getAlbum());
                    Number p = songPrices.get(s.getTitle());

                    Log.d("Store Fragment: On click of Song", s.getTitle());

                    //switching to Song info screen (StoreInfo.java)
                    bundle.putString("song_title", s.getTitle());
                    Log.d("BundleChecking", "Title");
                    bundle.get("song_title");
                    Log.d("BundleChecking", "Done");
                    bundle.putString("song_artist", s.getArtist());
                    bundle.putString("song_album", s.getAlbum());
                    bundle.putInt("song_price", (Integer) p);
                    bundle.putInt("user_bal", (Integer) balance);
                    bundle.putBoolean("is_album", false);
                    Fragment subFragment = new Song_ViewPager();
                    subFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (subFragment != null) {
                        fragmentManager.beginTransaction().replace(R.id.frame_container,
                                subFragment).commit();
                    }
                }
            });

        } else {
            AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMap);
            storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView parent, final View view,
                                        int position, long id) {
                    Album a = new Album(albumList.get(position).getId(),
                            albumList.get(position).getName(),
                            albumList.get(position).getArtist());
                    Number p = albumPrices.get(a.getName());
                    Log.d("Store Fragment: On click of Album", a.getName());
                    //switching to Song info screen (StoreInfo.java)
                    bundle.putString("album_name", a.getName());
                    Log.d("STORE FRAGMENT ALBUM", a.getName());
                    bundle.putString("album_artist", a.getArtist());
                    Log.d("STORE FRAGMENT ALBUM", a.getArtist());
                    bundle.putString("album_genre", a.getGenre());
                    Log.d("STORE FRAGMENT ALBUM", a.getGenre());
                    bundle.putInt("album_price", (Integer) p);
                    bundle.putInt("user_bal", (Integer) balance);
                    bundle.putBoolean("is_album", true);
                    Fragment subFragment = new StoreInfo();
                    subFragment.setArguments(bundle);
                    FragmentManager fragmentManager = getFragmentManager();
                    if (subFragment != null) {
                        fragmentManager.beginTransaction().replace(R.id.frame_container,
                                subFragment).commit();
                    }
                }
            });

        }
        return view;
    }


    public void onDestroy(){
       // getActivity().stopService(playIntent);
        //musicService = null;
        super.onDestroy();
    }
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
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

    public void downloadAlbum(final String albumName, final boolean alreadyPurchased)
    {
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereEqualTo("Album", albumName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects == null) {
                } else {
                    if(!alreadyPurchased) {
                        ParseObject Adownload = new ParseObject("Album_Downloads");
                        Adownload.put("Login", getCurrentUser());
                        Adownload.put("album_Id", albumName);
                        Adownload.saveInBackground();

                        for (int i = 0; i < parseObjects.size(); ++i) {
                            String url = parseObjects.get(i).getString("Link_To_Download");
                            //Album
                            String AlbumName = albumName;
                            final String SongName = parseObjects.get(i).getString("Name");
                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Downloads");
                            query2.whereEqualTo("Login", getCurrentUser()).whereEqualTo("song_Id", SongName);
                            query2.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null && parseObject != null) {
                                    }
                                    else{
                                        ParseObject download = new ParseObject("Downloads");
                                        download.put("Login", getCurrentUser());
                                        download.put("song_Id", SongName);
                                        download.saveInBackground();
                                        ParseObject dl = new ParseObject("TempDownloads");
                                        dl.put("Login", getCurrentUser());
                                        dl.put("SongName", SongName);
                                        Date d = new Date();
                                        d.setTime(d.getTime() + 604800000);
                                        dl.put("Expires", d);
                                        dl.saveInBackground();

                                    }
                                }
                            });


                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                            request.setDescription(url);
                            request.setTitle(SongName);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request.allowScanningByMediaScanner();
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            }
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, SongName + ".mp3");
                            //get download service and enqueue file
                            manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request);
                        }
                    }
                }

            }
        });
    }

    public void downloadSong(String songName, final boolean alreadyPurchased)
    {
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
                  //Song Name
                    final String name = parseObject.getString("Name");

                    String url = parseObject.getString("Link_To_Download");
                    // Dropbox url must end in ?dl=1
                  //  Log.d("DownloadSong", url);

                   // ParseObject download = new ParseObject("Downloads");
                   // download.put("Login", getCurrentUser());
                   // download.put("song_Id", name);
                    //download.put("Plays", 0);
                   // download.saveInBackground();

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
                    //save the ID of this specific download
                    final long downloadID = manager.enqueue(request);
                    SharedPreferences settings = context.getSharedPreferences
                            ("DownloadIDS", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putLong("savedDownloadIds", downloadID);
                    editor.putString("downloadedSong", name);
                    editor.commit();
                    //Make Broadcast Receiver to confirm when download manager is complete
                    final BroadcastReceiver onComplete = new BroadcastReceiver(){
                        @Override
                        public void onReceive(Context context, Intent intent){
                            Log.d("OnRcieve", "Executing OnRevieve...");
                            SharedPreferences downloadIDs = context.getSharedPreferences
                                    ("DownloadIDS", 0);
                            long savedIDs = downloadIDs.getLong("savedDownloadIds", 0);
                            String songName = downloadIDs.getString("downloadedSong", "unknown");

                            Bundle extras = intent.getExtras();
                            DownloadManager.Query q = new DownloadManager.Query();
                            Long downloaded_id = extras.getLong
                                    (DownloadManager.EXTRA_DOWNLOAD_ID);
                            if(savedIDs == downloaded_id){ //Its the file we're waiting for
                                q.setFilterById(downloaded_id);
                                DownloadManager manager = (DownloadManager)context.getSystemService
                                        (Context.DOWNLOAD_SERVICE);
                                Cursor c = manager.query(q);
                                if(c.moveToFirst()){
                                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                    if(status == DownloadManager.STATUS_SUCCESSFUL){
                                        Toast.makeText(context,
                                                songName + " Downloaded", Toast.LENGTH_LONG).show();
                                        ((MainActivity)getActivity()).setRecentlyDownloaded(songName);
                                        ParseObject dl = new ParseObject("TempDownloads");
                                        dl.put("Login", getCurrentUser());
                                        dl.put("SongName", name);
                                        Date d  = new Date();
                                        d.setTime(d.getTime() + 604800000);
                                        dl.put("Expires", d);
                                        dl.saveInBackground();
                                        if(!alreadyPurchased) {
                                            if(alreadyPurchased == false)
                                                Log.d("DEBUG", "already purchased is false");
                                            Log.d("DEBUG", "Adding song to download list...");
                                            ParseObject download = ParseObject.create("Downloads");
                                            download.put("Login", getCurrentUser());
                                            download.put("song_Id", name);
                                            //download.put("Plays", 0);
                                            download.saveInBackground();
                                        }
                                    }
                                }
                                c.close();
                            }
                        }
                    };
                    //Register the receiver for Downloads
                    getActivity().getApplication().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }
            }
        });
    }

    public void verifySongDownloadedandReview(final String s) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
        query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("song_Id", s);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.d("Verify", e.getMessage());
                    Toast.makeText(getActivity().getApplicationContext(), "Please purchase song before you rate.",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (parseObject == null) {
                        Log.d("Verify", "parse object null...");
                    } else {
                        rateSong(s);
                    }
                }
            }
        });
    }

    public void verifyAlbumDownloadedandReview(final String s) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Album_Downloads");
        query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("album_Id", s);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e != null) {
                    Log.d("Verify", e.getMessage());
                    Toast.makeText(getActivity().getApplicationContext(), "Please purchase album before you rate.",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (parseObject == null) {
                        Log.d("Verify", "parse object null...");
                    } else {
                        rateAlbum(s);
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
    // Deducts the song price from the account balance
    public boolean payment(String user, final Float songPrice) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", user);
        ParseObject PO = null;
        try{
            PO = query.getFirst();
        }
        catch(ParseException pe){
            Toast.makeText(getActivity().getApplicationContext(), "Error Processing Charge.",
                    Toast.LENGTH_LONG).show();
            return false;
        }


        if (PO == null) {
            Log.d("Error", "What the Fuck?");
            return false;
        }
        else if (PO.getDouble("Money") >= songPrice){
            balance =  PO.getDouble("Money") - songPrice;
            PO.put("Money", balance);
            try {
                PO.save();
            }
            catch (ParseException pe){
                Toast.makeText(getActivity().getApplicationContext(), "Error Processing Charge.",
                        Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }
        else{
            Toast.makeText(getActivity().getApplicationContext(), "Insufficient Funds.",
                    Toast.LENGTH_LONG).show();
            return false;
        }

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
                            albumList = albumQueryResult;
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
                            storeList = songQueryResult;
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
                            storeList = songQueryResult;
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
                            albumList = albumQueryResult;
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

    public void queryMostPlayed(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.orderByDescending("Plays");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if( e == null){
                    if(parseObjects.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(), "Nothing Here.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    songQueryResult = new ArrayList<Song>();
                    songQueryResultPrices = new HashMap<String, Number>();
                    Log.d("INFO", "HERE I AM!!!!");
                    Log.d("INFO", "parse objects size = " + parseObjects.size());
                    for(int i = 0; i < 20 && i < parseObjects.size(); ++i) {
                        String artist = parseObjects.get(i).getString("Artist");
                        String name = parseObjects.get(i).getString("Name");
                        Log.d("SongName = ", name);
                        String album = parseObjects.get(i).getString("Album");
                        Number price = parseObjects.get(i).getNumber("Price");
                        Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                        Song s = new Song(0, name, artist, album);
                        songQueryResult.add(s);
                        songQueryResultPrices.put(name, price);
                    }
                    Log.d("INFO", "HERE I AM AGAIN!!!!");
                    StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), songQueryResult, songQueryResultPrices, currentFrag);
                    storeView.setAdapter(songMap);
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Nothing Here.",
                            Toast.LENGTH_SHORT).show();
                    Log.d("Exception:", e.getMessage());
                }
            }
        });
    }

    public void queryMostDownloaded(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.orderByDescending("Downloads");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if( e == null){
                    if(parseObjects.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(), "Nothing Here.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    songQueryResult = new ArrayList<Song>();
                    songQueryResultPrices = new HashMap<String, Number>();
                   // Log.d("INFO", "HERE I AM!!!!");
                    //Log.d("INFO", "parse objects size = " + parseObjects.size());
                    for(int i = 0; i < 20 && i < parseObjects.size(); ++i) {
                        String artist = parseObjects.get(i).getString("Artist");
                        String name = parseObjects.get(i).getString("Name");
                        Log.d("SongName = ", name);
                        String album = parseObjects.get(i).getString("Album");
                        Number price = parseObjects.get(i).getNumber("Price");
                        Number aPrice = parseObjects.get(i).getNumber("Album_Price");
                        Song s = new Song(0, name, artist, album);
                        songQueryResult.add(s);
                        songQueryResultPrices.put(name, price);
                    }
                   // Log.d("INFO", "HERE I AM AGAIN!!!!");
                    StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(), songQueryResult, songQueryResultPrices, currentFrag);
                    storeView.setAdapter(songMap);
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Nothing Here.",
                            Toast.LENGTH_SHORT).show();
                    Log.d("Exception:", e.getMessage());
                }
            }
        });
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
        revPrompt(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.store_album_mode) {
            albumViewMode = !albumViewMode;
            final Bundle bundle= new Bundle();
            if(!albumViewMode){
                StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
                storeView.setAdapter(storeMap);
                // With a selection of any song/album in store,
                // user will be directed to another screen
                // which displays song/album info and reviews.
                storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView parent, final View view,
                                            int position, long id) {
                        Song s = new Song(storeList.get(position).getID(),
                                storeList.get(position).getTitle(),
                                storeList.get(position).getArtist(),
                                storeList.get(position).getAlbum());
                        Number p = songPrices.get(s.getTitle());

                        //Log.d("Store Fragment: On click of Song", s.getTitle());

                        //switching to Song info screen (StoreInfo.java)
                        bundle.putString("song_title", s.getTitle());
                        bundle.putString("song_artist", s.getArtist());
                        bundle.putString("song_album", s.getAlbum());
                        bundle.putInt("song_price", (Integer) p);
                        bundle.putInt("user_bal", (Integer) balance);
                        bundle.putBoolean("is_album", false);
                        Fragment subFragment = new Song_ViewPager();
                        subFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        if (subFragment != null) {
                            fragmentManager.beginTransaction().replace(R.id.frame_container,
                                    subFragment).addToBackStack(null).commit();
                        }
                        Log.d("GO IN HERE!!!!!!!", "3");
                    }
                });
            }
            else{
                AlbumMapper albumMap = new AlbumMapper(view.getContext(), albumList, albumPrices, currentFrag);
                storeView.setAdapter(albumMap);
                storeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                    public void onItemClick(AdapterView parent, final View view,
                                            int position, long id) {
                        Album a = new Album(albumList.get(position).getId(),
                                albumList.get(position).getName(),
                                albumList.get(position).getArtist());
                        Number p = albumPrices.get(a.getName());

    //                    Log.d("Store Fragment: On click of Album", a.getName());

                        //switching to Song info screen (StoreInfo.java)

                        bundle.putString("album_name", a.getName());
    //                    Log.d("STORE FRAGMENT ALBUM", a.getName());
                        bundle.putString("album_artist", a.getArtist());
    //                    Log.d("STORE FRAGMENT ALBUM", a.getArtist());
    //                    bundle.putString("album_genre", a.getGenre());
    //                    Log.d("STORE FRAGMENT ALBUM", a.getGenre());
                        bundle.putInt("album_price", p.intValue());
                        bundle.putInt("user_bal", balance.intValue());
                        bundle.putBoolean("is_album", true);

                        Fragment subFragment = new Song_ViewPager();
                        subFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        if (subFragment != null) {
                            fragmentManager.beginTransaction().replace(R.id.frame_container,
                                    subFragment).addToBackStack(null).commit();
                        }

                    }
                });
                //storeView.setOnItemClickListener(storeMap.onClickListener() )
            }
        }
        if(id == R.id.most_played){
            albumViewMode = false;
            queryMostPlayed();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(),
                    storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.most_downloaded)
        {
            albumViewMode = false;
            queryMostDownloaded();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(),
                    storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.store_search_songs){
            albumViewMode = false;
            queryForSong();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(),
                    storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.store_search_albums){
            albumViewMode = true;
            queryForAlbum();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(),
                    albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMapper);
        }
        if(id == R.id.store_search_genres_album){
            albumViewMode = true;
            queryForGenreAlbum();
            AlbumMapper albumMapper = new AlbumMapper(StoreFragmentView.getContext(),
                    albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMapper);
        }
        if(id == R.id.store_search_genres_song){
            albumViewMode = false;
            queryForGenreSong();
            StoreMapper songMap = new StoreMapper(StoreFragmentView.getContext(),
                    storeList, songPrices, currentFrag);
            storeView.setAdapter(songMap);
        }
        if(id == R.id.action_end){
            getActivity().stopService(playIntent);
            musicService = null;
            Log.d("StoreFragment", "AppCloseCalled");
            getActivity().finish();
        }if(id == R.id.store_subscribe){
            storeSubscribe();
            Log.d("StoreFragment", "subscribe");
        }
        return super.onOptionsItemSelected(item);
    }

    public void reviewAlbum(final String s){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Review");
        //Set an EditText view to get rating
        final EditText rate = new EditText(getActivity());
        // Set an EditText view to get user input
        final EditText input = new EditText(getActivity());
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
    void storeSubscribe()
    {
        String user = ((MainActivity)getActivity()).getUserLoggedin();
        ParseQuery<ParseObject> checkQuery = ParseQuery.getQuery("Users");
        checkQuery.whereEqualTo("Login", user);
        checkQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if(e == null && parseObject != null)
                {
                    if(parseObject.getBoolean("subscribed")){
                        Toast.makeText(getActivity().getApplicationContext(), "Already Subscribed.",
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        balance = parseObject.getNumber("Money");
                        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                        alert.setTitle("Confirm Payment of $" + 10 + "\n"+ "Balance: $"+ balance);
                        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (balance.floatValue() >= (float) 10) {
                                    balance = balance.floatValue() - 10;
                                    parseObject.put("Money", balance);
                                    parseObject.put("subscribed", true);
                                    parseObject.put("subDate", new Date());
                                    ((MainActivity) getActivity()).setSubscribed(true);
                                    parseObject.saveInBackground();

                                }
                                else{
                                    Toast.makeText(getActivity().getApplicationContext(), "Insufficient Funds.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {}
                        });
                        alert.show();
                    }
                }
            }
        });
    }

    @Override
    public void onResume(){
        getOnlineSongList();
        if(!albumViewMode)  {
        StoreMapper storeMap = new StoreMapper(view.getContext(), storeList, songPrices, currentFrag);
        storeView.setAdapter(storeMap);
        }
        else{
            AlbumMapper albumMap = new AlbumMapper(StoreFragmentView.getContext(),
                    albumList, albumPrices, currentFrag);
            storeView.setAdapter(albumMap);
        }
        super.onResume();
    }

}