package com.hech.musicplayer;


import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreInfo extends Fragment {
    Number song_price;
    String song_title;
    String song_album;
    String song_artist;
    Number price;

    String album_title;
    String album_artist;
//    String album_genre;
    Number album_price;
    private Number balance;
    private DownloadManager manager;
    private Context context;
    private MusicService musicService;
    private boolean musicBound = false;
    private Fragment currentFrag = this;
    private Intent playIntent;
    private MediaPlayer player;


    private ServiceConnection musicConnection = new ServiceConnection() {
        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) iBinder;
            musicService = binder.getService();
            musicService.setCurrUser(((MainActivity) getActivity()).getUserLoggedin());
            musicBound = true;
            player = musicService.getPlayer();
        }
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

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

    public void onDestroy(){
        getActivity().stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }


    public StoreInfo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_store_info, container, false);
        //RelativeLayout songCover = (RelativeLayout)inflater.inflate
        //        (R.layout.fragment_store_info, container, false);
        final TextView title= (TextView)view.findViewById(R.id.Song_Name);
        final TextView artist= (TextView)view.findViewById(R.id.artist);
        final TextView album= (TextView)view.findViewById(R.id.songalb);

        // Inflate the layout for this fragment
        Bundle bundle = this.getArguments();



        if(!bundle.getBoolean("is_album"))
        {
            //final String s_title= bundle.get("song_title").toString();
            song_title= bundle.get("song_title").toString();
            song_album= bundle.get("song_album").toString();
            song_artist= bundle.get("song_artist").toString();
            song_price= (Number) bundle.get("song_price");
            title.setText(song_title);
            artist.setText(song_artist);
            artist.setTypeface(null, Typeface.ITALIC);
            album.setText("Album: "+song_album);
            album.setTypeface(null, Typeface.ITALIC);

            Button revb= (Button) view.findViewById(R.id.revbutton);

            view.findViewById(R.id.revbutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Click", song_title);
                    revSubmit();
                }
            });

            ImageButton dlb= (ImageButton) view.findViewById(R.id.dlButton);

            view.findViewById(R.id.dlButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlSubmit();
                }
            });
            view.findViewById(R.id.samplebutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playSample(song_title);
                }
            });
        }
        else
        {
            Log.d("STORE INFO",bundle.get("album_artist").toString());

            album_artist= bundle.get("album_artist").toString();
            album_title=bundle.get("album_name").toString();
//            album_genre=bundle.get("album_genre").toString();
            album_price= (Number)bundle.get("album_price");
            Log.d("Album info:", album_title);
            title.setText(album_title);
            artist.setText(album_artist);
            artist.setTypeface(null, Typeface.ITALIC);

            Button revb= (Button) view.findViewById(R.id.revbutton);

            view.findViewById(R.id.revbutton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Click", album_title);
                    verifyAlbumDownloadedandReview(album_title);
                }
            });

            ImageButton dlb= (ImageButton) view.findViewById(R.id.dlButton);

            view.findViewById(R.id.dlButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    albumPicked(album_title);
                }
            });

            view.findViewById(R.id.samplebutton).setVisibility(View.INVISIBLE);
        }
        //OnKeyListener for Back button
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

        balance= (Number) bundle.get("user_bal");
        context = getActivity().getApplicationContext();
        //Log.d("Store Info Song Name: ", s_title);
       // final TextView songName= (TextView)view.findViewById(R.id.Song_Name);
        //Log.d("Store Info Getting Text: ", (String)songName.getText());
       // songName.setText(song_title);
        Log.d("Store Info Song Name: ", (String)title.getText());
        return view;

    }

    public void playSample(String song){
        //TODO finish
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail
        player.reset();
        // Queries Song_Bank for ParseObjects
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereEqualTo("Name", song);
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
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // this code will be executed after 15 seconds
                                player.stop();
                            }
                        }, 15000);
                    } catch (Exception ex) {
                        Log.e("Stream Song", "Error Setting Data Source", ex);
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

    public void ratePrompt(final String t){
        Log.d("In ratePrompt","");
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
                        public void done(List<ParseObject> parseObjects, ParseException e) {
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

    public void rateAlbumPrompt(final String t){
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
                        //TODO check for exception here!!!!
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


    // Rate the song (5-star scale)
    public void rateSong(String s) {
        Log.d("In rate Song","");
        ratePrompt(s);
        reviewSong(s);
    }

    public void rateAlbum(String a){
        rateAlbumPrompt(a);
        reviewAlbum(a);
    }

    // Review the song (text review)
    public void reviewSong(String s) {
        //String title = s;
        Log.d("TITLE", s);
        revPrompt(s);
    }

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
                    Log.d("Error", "");
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

    public void verifySongDownloadedandReview(final String s) {
        Log.d("In Verify Song Downloaded","");
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

    public void downloadSong(String songName)
    {
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail
        Log.d("download song","");
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
                    String name = parseObject.getString("Name");

                    String url = parseObject.getString("Link_To_Download");
                    // Dropbox url must end in ?dl=1
                    Log.d("DownloadSong", url);

                    ParseObject download = new ParseObject("Downloads");
                    download.put("Login", getCurrentUser());
                    download.put("song_Id", name);
                    download.put("Plays", 0);
                    download.saveInBackground();

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
                    BroadcastReceiver onComplete = new BroadcastReceiver(){
                        @Override
                        public void onReceive(Context context, Intent intent){
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
                                        if(getActivity() != null) {
                                            ((MainActivity) getActivity())
                                                    .setRecentlyDownloaded(songName);
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


    public void downloadAlbum(final String albumName)
    {
        // If yes set that new songs are avail and update database
        // else make sure that the purchase is reversed and new songs are not avail
        Log.d("In download Album ", albumName);
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Song_Bank");
        query.whereEqualTo("Album", albumName);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects == null) {
                } else {
                    ParseObject Adownload = new ParseObject("Album_Downloads");
                    Adownload.put("Login", getCurrentUser());
                    Adownload.put("album_Id", albumName);
                    Adownload.saveInBackground();
                    for (int i = 0; i < parseObjects.size(); ++i) {
                        String url = parseObjects.get(i).getString("Link_To_Download");
                        Log.d("Link to Download",url);
                        //Album
                        String AlbumName = albumName;

                        String SongName = parseObjects.get(i).getString("Name");

                        ParseObject download = new ParseObject("Downloads");
                        download.put("Login", getCurrentUser());
                        download.put("song_Id", SongName);
                        download.saveInBackground();
                        downloadSong(SongName);
                        /*DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setDescription(url);
                        request.setTitle(SongName);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC,SongName + ".mp3");
                        //get download service and enqueue file
                        manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);*/
                    }
                }
            }
        });
    }



    public void buyPrompt(final String songn, final Number p, final boolean isAlbum){
        Log.d("Buy Prompt","");
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Confirm Payment of $" + p + "\n"+ "Balance: $"+ balance);
        alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                payment(getCurrentUser(), p.floatValue());
                Log.d("confirmPayment",songn);
                if(isAlbum){
                    downloadAlbum(songn);
                    ((MainActivity)getActivity()).setNewSongsAvail(true);
                }
                else{
                    downloadSong(songn);
                    ((MainActivity)getActivity()).setNewSongsAvail(true);
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {}
        });
        alert.show();
    }

    public void confirmPayment(final String name, final boolean isAlbum)
    {
        Log.d("Inside Confirm Payment ",name);

        boolean AlreadyPurchased = false;
        if(isAlbum){
            Log.d("Inside Confirm Payment ", "true");
            ParseQuery<ParseObject> query =  ParseQuery.getQuery("Album_Downloads");
            query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("album_Id", name);
            try{
                ParseObject po = query.getFirst();
                if(po != null)
                {
                    AlreadyPurchased = true;
                }

            }
            catch(Exception e)
            {
                Log.d("Login", e.getMessage());
            }
            if(AlreadyPurchased)
                price = 0;
            else {
                //price = albumPrices.get(name);
                price= album_price;
                /*ParseQuery<ParseObject> songp =  ParseQuery.getQuery("Song_Bank");
                songp.whereEqualTo("Name",song_title);
                songp.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parseObjects, ParseException e) {

                        for(int i = 0; i < parseObjects.size(); ++i)
                        {
                            if(parseObjects.get(i).getString("Artist")== song_artist)
                            {
                                Number p = parseObjects.get(i).getNumber("Price");
                                price = p;
                                //buyPrompt(name,p,isAlbum);
                            }
                        }


                    }
                });*/
            }
        }
        else {
            ParseQuery<ParseObject> query =  ParseQuery.getQuery("Downloads");
            query.whereEqualTo("Login", getCurrentUser()).whereEqualTo("song_Id", name);
            try{
                ParseObject po = query.getFirst();
                if(po != null)
                {
                    AlreadyPurchased = true;
            }
            }
            catch(Exception e)
            {
                Log.d("Login", e.getMessage());
            }
            if(AlreadyPurchased)
                price = 0;
            else {
                  price= song_price;
               /* ParseQuery<ParseObject> songp =  ParseQuery.getQuery("Song_Bank");
                songp.whereEqualTo("Name",song_title);
                songp.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> parseObjects, ParseException e) {

                        for(int i = 0; i < parseObjects.size(); ++i)
                        {
                            if(parseObjects.get(i).getString("Artist") == song_artist)
                            {
                                Number p = parseObjects.get(i).getNumber("Price");
                                price = p;
                               // buyPrompt(name,p,isAlbum);
                            }
                        }


                    }
                });*/
            }
        }
        buyPrompt(name, price, isAlbum);
    }

    public void songPicked(String songName){
        Log.d("songNamePicked", songName);
        confirmPayment(songName, false);
    }

    public void albumPicked(String albumName){
        Log.d("albumNamePicked", albumName);
        confirmPayment(albumName, true);
    }



    public void revSubmit(){
        Log.d("In RevSubmit","");
        verifySongDownloadedandReview(song_title);
    }

    public void dlSubmit(){
        songPicked(song_title);
    }


}

