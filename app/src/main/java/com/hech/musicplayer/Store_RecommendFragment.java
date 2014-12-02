package com.hech.musicplayer;

import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Store_RecommendFragment extends Fragment{
    private int totalDownloads = 0;
    private ArrayList<String> ownedSongs = new ArrayList<String>();
    private HashMap<String, Integer> genrePref = new HashMap<String, Integer>();
    private ArrayList<Song> unownedSongs = new ArrayList<Song>();

    private ListView storeView;
    private Fragment currentFrag;
    private ArrayList<Song> storeList = new ArrayList<Song>();
    private HashMap<String, Number> songPrices = new HashMap<String, Number>();
    private DownloadManager manager;
    private Number balance;
    Context context;
    private MusicService musicService;
    private boolean musicBound = false;
    private Intent playIntent;


    public Store_RecommendFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        setHasOptionsMenu(true);
        context = getActivity().getApplicationContext();
        try {
            getOwnedSongs(getCurrentUser());
        } catch (ParseException e) {
            e.printStackTrace();
        }
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

        Log.d("Recommend", "Owned List Size: " + totalDownloads);
        View view = inflater.inflate(R.layout.fragment_store,
                container, false);
        // Get the store view
        storeView = (ListView)view.findViewById(R.id.store_list);
        getUnownedSongs();
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
                Bundle bundle = new Bundle();
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


        //Will load an empty list until the Parse background job is done
        StoreRecommendMapper songMap = new StoreRecommendMapper(view.getContext(), storeList, songPrices, currentFrag);
        storeView.setAdapter(songMap);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener( new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if( i == KeyEvent.KEYCODE_BACK )
                {
                    //Switch to subplaylist song view
                    Fragment subFragment = new Store_ViewPager();
                    FragmentManager fragmentManager = getFragmentManager();
                    if(subFragment != null) {
                        Log.d("ViewPager", "Switch: StoreView");
                        fragmentManager.beginTransaction().replace(R.id.frame_container,
                                subFragment).commit();
                    }
                    return true;
                }
                return false;
            }
        });
        return view;
    }

    //Returns the currently logged in user
    public String getCurrentUser()
    {
        return ((MainActivity)getActivity()).getUserLoggedin();
    }

    public ArrayList<String> getOwnedSongs(String user)
        throws ParseException {
            final ParseQuery<ParseObject> query = ParseQuery.getQuery("Downloads");
            query.whereEqualTo("Login", getCurrentUser());
            List<ParseObject> parseObjects = query.find();
            totalDownloads = parseObjects.size();
            for (int i = 0; i < totalDownloads; ++i) {
                ownedSongs.add(parseObjects.get(i).getString("song_Id"));
            }
        Log.d("Recommend", "Owned Songs List Retrieved");
        return ownedSongs;
    }

    public void getUnownedSongs(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereLessThan("Price", 10);
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> parseObjects, ParseException e) {
                ArrayList<String> genreIndex = new ArrayList<String>();
                for (int i = 0; i < parseObjects.size(); ++i) {
                    String name = parseObjects.get(i).getString("Name");
                    if(!ownedSongs.contains(name)) {
                        String artist = parseObjects.get(i).getString("Artist");
                        String album = parseObjects.get(i).getString("Album");
                        Number price = parseObjects.get(i).getNumber("Price");
                        String genre = parseObjects.get(i).getString("Genre");

                        genreIndex.add(genre);
                        Song s = new Song(0, name, artist, album);
                        unownedSongs.add(s);
                        //Log.d("Recommend", name+" Added");
                        songPrices.put(name, price);
                    }
                    else{
                        String genre = parseObjects.get(i).getString("Genre");
                        Integer count = genrePref.get(genre);
                        if(count == null){
                            genrePref.put(genre, 1);
                        }
                        else{
                            genrePref.put(genre, count+1);
                        }
                        parseObjects.remove(i);
                        --i;
                    }
                }
                genrePref = sortByValue(genrePref);
                sortByPref(genreIndex);
                StoreRecommendMapper songMap = new StoreRecommendMapper(getActivity().
                        getApplicationContext(), storeList, songPrices, currentFrag);
                storeView.setAdapter(songMap);
            }
        });
    }

    public void sortByPref(ArrayList<String> genre){
        HashMap<String, Integer> sortMap = new HashMap<String, Integer>();
        for(int i = 0; i < unownedSongs.size(); ++i){
            Integer count = genrePref.get(genre.get(i));
            if(count == null){
                count = new Integer(0);
            }
            sortMap.put(unownedSongs.get(i).getTitle(), count);
        }
        sortMap = sortByValue(sortMap);
        ArrayList<String> sortedList = new ArrayList<String>(sortMap.keySet());
        while(!sortedList.isEmpty()){
            for(int i = 0; i < unownedSongs.size(); ++i){
                if(storeList.size() >= 10){
                    Log.d("Recommend", "Unowned List Weighted");
                    return;
                }
                if((unownedSongs.get(i).getTitle()).equals(sortedList.get(0))){
                    storeList.add(unownedSongs.get(i));
                    sortedList.remove(0);
                    Log.d("Recommend Weights: ", unownedSongs.get(i).getTitle() + " " + sortMap.get(unownedSongs.get(i).getTitle()));
                    break;
                }
            }
        }
        Log.d("Recommend", "Unowned List Weighted");

    }
    public HashMap sortByValue(HashMap genremap){
        List list = new LinkedList(genremap.entrySet());
        Collections.sort(list, new Comparator() {
            @Override
            public int compare(Object lhs, Object rhs) {
                return ((Comparable) ((Map.Entry) (rhs)).getValue())
                        .compareTo(((Map.Entry) (lhs)).getValue());
            }
        });
        HashMap sortedHashMap = new LinkedHashMap();
        for(Iterator it = list.iterator(); it.hasNext();){
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
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
                            //getActivity().getApplication().unregisterReceiver(this);
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
    // Rate the song (5-star scale)
    public void rateSong(String s) {
        ratePrompt(s);
        reviewSong(s);
    }
    // Review the song (text review)
    public void reviewSong(String s) {
        String title = s;
        Log.d("TITLE", title);
        revPrompt(title);
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
                if (user_rate.intValue() < 1 || user_rate.intValue() > 5){
                    Toast.makeText(getActivity().getApplicationContext(), "Invalid rating.",
                            Toast.LENGTH_SHORT).show();
                }
                else{
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
            }

        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }
    public void revPrompt(final String t){
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
    public void buyPrompt(final String songn, final Number p, final boolean alreadyPurchased){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Confirm Payment of $" + p + "\n"+ "Balance: $"+ balance);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                payment(getCurrentUser(), p.floatValue());
                Log.d("confirmPayment",songn);
                if(alreadyPurchased)
                    Log.d("DEBUG", "alreadyPurchased is true");
                downloadSong(songn, alreadyPurchased);
                ((MainActivity)getActivity()).setNewSongsAvail(true);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }
    // Deducts the song price from the account balance
    public void payment(String user, final Float songPrice) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", user);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            public void done(ParseObject PO, ParseException e) {
                if (PO == null) {
                    Log.d("Error", "What the Fuck?");
                }
                else if (PO.getDouble("Money") >= songPrice){
                    balance =  PO.getDouble("Money") - songPrice;
                    PO.put("Money", balance);
                    PO.saveInBackground();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Insufficient Funds.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
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
    public void songPicked(String songName){
        Log.d("songNamePicked", songName);
        confirmPayment(songName, false);
    }
    public void confirmPayment(String name, boolean isAlbum)
    {
        boolean AlreadyPurchased = false;
        Number price;
        ParseQuery<ParseObject> query =  ParseQuery.getQuery("Downloads");
        query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("song_Id", name);
        try {
            ParseObject po = query.getFirst();
            if(po != null)
            {
                Log.d("DEBUG", "already purchased is true");
                AlreadyPurchased = true;
            }

        }catch(Exception e) {
                Log.d("Login", e.getMessage());
        }
        if(AlreadyPurchased)
            price = 0;
        else
            price = songPrices.get(name);
        buyPrompt(name, price, AlreadyPurchased);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        //getActivity().stopService(playIntent);
        //musicService = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        currentFrag = this;
        /*if(playIntent == null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }*/
    }

}
