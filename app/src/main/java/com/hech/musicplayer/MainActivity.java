package com.hech.musicplayer;

import android.app.Activity;
<<<<<<< HEAD
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;


public class MainActivity extends Activity{
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

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
        //Store
        navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2,-1)));

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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
        menu.findItem(R.id.action_settings).setVisible(!drawerOprn);
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
                Log.d("Song", "Fragment Made");
                fragment = new SongFragment();
                break;
            case 1:
                Log.d("Playlist", "Fragment Made");
                fragment = new PlaylistFragment();
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
=======

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.ListView;
import android.content.Intent;
import com.hech.musicplayer.MusicService;
import com.hech.musicplayer.MusicService.MusicBinder;

import static com.hech.musicplayer.R.id.action_continuousPlay;
import static com.hech.musicplayer.R.id.action_settings;
import static com.hech.musicplayer.R.id.action_stopPlay;


public class MainActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {


    private ArrayList<Song> songList;
    private ListView songView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the song view
        songView = (ListView)findViewById(R.id.song_list);
        // Create empty song library
        songList = new ArrayList<Song>();
        // Scan device and populate song library
        getSongList();

        //Map the song list to the song viewer
        SongMapper songMap = new SongMapper(this, songList);
        songView.setAdapter(songMap);


        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    // Create the connection to the music service
    private ServiceConnection musicConnection = new ServiceConnection() {

        //Initialize the music service once a connection is established
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                MusicBinder binder = (MusicBinder) iBinder;
                musicService = binder.getService();
                musicService.setSongsList(songList);
                //TODO set playlists here when we have them available in a file on the device
                musicBound = true;

        }

        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    // Connects MainActivity to the music service on startup, also starts the music service
    protected void onStart(){
        super.onStart();
        if(playIntent == null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    //If the user selects a song from the list, play it
    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
    }


    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == action_settings) {
            return true;
        }
        if (id == action_continuousPlay)
        {
            musicService.setContinuousPlayMode(true);
            musicService.playSong();
        }
        if(id == action_stopPlay)
        {
            musicService.setContinuousPlayMode(false);
            musicService.stopPlay();
        }
        if(id == R.id.action_end)
        {
            stopService(playIntent);
            musicService = null;
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onDestroy()
    {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    public void getSongList() {
        //retrieve song info

        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                songList.add(new Song(thisId, thisTitle, thisArtist));
            }
            while (musicCursor.moveToNext());
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

>>>>>>> master
}
