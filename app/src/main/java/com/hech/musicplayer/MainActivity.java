package com.hech.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.PushService;

import java.util.ArrayList;


public class MainActivity extends Activity{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean loggedin = false;
    private boolean newSongsAvail = false;
    private String userLoggedin = "";
    private boolean subscribed = false;
    //Playlist of Recently Played Songs
    private Playlist recentlyPlayed = new Playlist(-1, "Recently Played");
    private Playlist recentlyDownloaded = new Playlist(-1, "Recently Downloaded");

    //Drawer Title
    private CharSequence mDrawerTitle;
    //App Title
    private CharSequence mTitle;
    //Menu Items
    private String[] navMenuTitles;
    private TypedArray navMenuIcons;
    private ArrayList<NavDrawerItem> navDrawerItems;
    private NavDrawerListAdapter adapter;
    //Slide Menu Listener Class
    private class SlideMenuClickListener implements
            ListView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent,
                                View view, int pos, long id){
            displayView(pos);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("Main", "Set View");
        //Save Title
        mTitle = mDrawerTitle = getTitle();
        //Load Menu Items
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerList = (ListView)findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<NavDrawerItem>();
        //Songs
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0,-1)));
        //Playlists
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1,-1)));
        //Albums
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(1, -1)));
        //Store
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(2, -1)));
        //Subscription
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(0, -1)));
        //TODO FIXME not sure if this is right.


        navMenuIcons.recycle();

        adapter = new NavDrawerListAdapter(getApplicationContext(), navDrawerItems);
        mDrawerList.setAdapter(adapter);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,   //Menu Icon
                R.string.app_name,      //Drawer Open
                R.string.app_name       //Drawer Close
        ){
            public void onDrawerClosed(View drawerView){
                getActionBar().setTitle(mTitle);
                //Prepare action bar icons
                invalidateOptionsMenu();
            }
            public void onDrawerOpened(View drawerView){
                getActionBar().setTitle(mDrawerTitle);
                //Prepare to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if(savedInstanceState == null){
            //Start at the first nav item
            displayView(0);
        }
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                displayView(position);
            }
        });

        Parse.initialize(this, "7jtpsiOWgrEbiH58R4XTSKcfz3egn8sZsFNxcLbd", "aU220REHUXoxAzdqiz1bjzjmI06Dr3aQCVQ4BHUZ");
        // Also in this method, specify a default Activity to handle push notifications
        PushService.setDefaultPushCallback(this, MainActivity.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setLoggedin(boolean opt) {loggedin = opt;}
    public void setUserLoggedin(String name) {userLoggedin = name;}
    public String getUserLoggedin() {return userLoggedin;}
    public void setSubscribed(boolean t) {subscribed = t;}
    public boolean getSubscribed() {return subscribed;}
    public boolean getNewSongsAvailable() {return newSongsAvail;}
    public void setNewSongsAvail(boolean b) {newSongsAvail = b;}
    public void setRecentlyPlayed(Song song) {
        Log.d("RecentlyPlayedSong Title: ", song.getTitle());
        if(recentlyPlayed.getSize() > 10){
            recentlyPlayed.removeSong(recentlyPlayed.getSong(0).getID());
        }
        Song s = new Song(song.getID(), song.getTitle(), song.getArtist(), song.getAlbum());
        recentlyPlayed.addSong(s);
    }
    public void setRecentlyDownloaded(String songName){
        Cursor songCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null);
        if (songCursor != null && songCursor.moveToFirst()) {
            int idColumn = songCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int titleColumn = songCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int artistColumn = songCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = songCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                long thisId = songCursor.getLong(idColumn);
                String thisTitle = songCursor.getString(titleColumn);
                String thisArtist = songCursor.getString(artistColumn);
                String thisAlbum = songCursor.getString(albumColumn);
                setRecentlyDownloaded(new Song(thisId, thisTitle, thisArtist, thisAlbum));
            } while (songCursor.moveToNext());
        }
    }
    public void setRecentlyDownloaded(Song song){
        Log.d("RecentlyDownloaded Title", song.getTitle());
        if(recentlyDownloaded.getSize() > 10){
            recentlyDownloaded.removeSong(recentlyDownloaded.getSong(0).getID());
        }
        recentlyDownloaded.addSong(song);
    }
    public Playlist getRecentlyPlayed(){ return recentlyPlayed; }
    public Playlist getRecentlyDownloaded(){ return recentlyDownloaded; }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        //Toggle drawer
        if(mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        switch (item.getItemId()){
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*Called on invalidateOptionsMenu()*/
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        boolean drawerOprn = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_settings).setVisible(!drawerOprn);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public void setTitle(CharSequence title){
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
    /*Called on onPostCreate() and onConfiguratoinChanged()*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        //Sync toggle after Restore Instance State
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        //Pass config to toggle
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    private void displayView(int pos){
        Fragment fragment = null;
        switch (pos){
            case 0:
                if(loggedin) {
                    Log.d("Song", "Fragment Made");
                    fragment = new SongFragment();
                }
                else
                {
                    Bundle b = new Bundle();
                    b.putInt("Intended", 0);
                    fragment = new LoginFragment();
                    fragment.setArguments(b);
                }
                break;
            case 1:
                if(loggedin) {
                    Log.d("Playlist", "Fragment Made");
                    fragment = new PlaylistFragment();
                }
                else
                {
                    Bundle b = new Bundle();
                    b.putInt("Intended", 1);
                    fragment = new LoginFragment();
                    fragment.setArguments(b);
                }
                break;
            case 2:
                if(loggedin) {
                    Log.d("Album", "Fragment Made");
                    fragment = new AlbumFragment();
                }
                else
                {
                    Bundle b = new Bundle();
                    b.putInt("Intended", 2);
                    fragment = new LoginFragment();
                    fragment.setArguments(b);
                }
                break;
            case 3:
                // Check for log in
                // if logged in: launch store
                if ( loggedin ) {
                    Log.d("Store", "Fragment Made");
                    fragment = new StoreFragment();
                }
                // else: show log in screen
                else {
                    Log.d("Login", "Fragment Made");
                    Bundle b = new Bundle();
                    b.putInt("Intended", 3);
                    fragment = new LoginFragment();
                    fragment.setArguments(b);
                }
                break;
            case 4:
                // Check for log in
                // if logged in and subscribed: launch sub fragment

                Log.d("Subscribe", "???????");
                if ( loggedin && subscribed ) {
                    Log.d("Subscribe", "Fragment Made");
                    fragment = new SubFragment();
                }
                // else: show log in screen
                else if(loggedin) {
                //TODO ??????

                    Log.d("Subscribe", "Not subscribed!");
                }
                else
                {
                        Log.d("Login", "Fragment Made");
                        Bundle b = new Bundle();
                        b.putInt("Intended", 3);
                        fragment = new LoginFragment();
                        fragment.setArguments(b);
                }
                break;

            default:
                break;
        }
        if(fragment != null){
            FragmentManager fragmanage = getFragmentManager();
            fragmanage.beginTransaction().replace(R.id.frame_container,
                        fragment).commit();
            //Update Item, Close Drawer
            mDrawerList.setItemChecked(pos, true);
            mDrawerList.setSelection(pos);
            setTitle(navMenuTitles[pos]);
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        else{
            Log.e("Main", "Error loading fragment");
        }
    }
    @Override
    public void onPause(){ super.onPause(); }
}
