package com.hech.musicplayer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class LoginFragment extends Fragment {
    private EditText  username=null;
    private EditText  password=null;
    private boolean loggedin = false;
    private String userLoggedIn;
    private int intended = -1;

    public LoginFragment(){};

    @Override
    public void setArguments(Bundle args) {
        intended = args.getInt("Intended");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        username= (EditText)view.findViewById(R.id.userText);
        password= (EditText)view.findViewById(R.id.pwText);

        final Button nbutton =
                (Button) view.findViewById(R.id.newButton);
        nbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newLoginCheck(v);
            }
        });

        final Button ebutton =
                (Button) view.findViewById(R.id.userButton);
        ebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                userLoginCheck(v);
            }
        });
        return view;
    }

    public void userLoginCheck(View view){
        final String usern = username.getText().toString();
        final String pw= password.getText().toString();
        Log.d("Username:",usern);
        Log.d("Password:",pw);
        //ParseObject users = new ParseObject("Users");
        //users.put("Login", usern);
        //users.put("Password", pw);
        //users.saveInBackground();
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("Login", usern);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if(e == null)
                {
                    if(parseObject == null)
                    {
                       Toast.makeText(getActivity().getApplicationContext(), "invalid username or password",
                                Toast.LENGTH_SHORT).show();
                        Log.d("Login", "no user found");
                    }
                    else if(parseObject.getString("Password").compareTo(pw) == 0)
                    {
                        loggedin = true;
                        ((MainActivity)getActivity()).setLoggedin(true);
                        Log.d("Login", usern + "loggedin");
                        userLoggedIn = usern;
                        ((MainActivity)getActivity()).setUserLoggedin(usern);
                        Toast.makeText(getActivity().getApplicationContext(), usern + " logged in.",
                                Toast.LENGTH_SHORT).show();
                        Fragment frag = null;
                        switch(intended)
                        {
                        case 0:
                            frag = new SongFragment();
                            break;
                        case 1:
                            frag = new PlaylistFragment();
                            break;
                        case 2:
                            frag = new AlbumFragment();
                            break;
                        case 3:
                            frag = new StoreFragment();
                            break;
                        default:
                            frag = new SongFragment();

                        }

                        //stFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frame_container, frag).commit();
                    }
                    else
                    {
                        Log.d("error",parseObject.getString("Login"));
                        Log.d("error",parseObject.getString("Password"));
                        Toast.makeText(getActivity().getApplicationContext(), "invalid username or password",
                                Toast.LENGTH_SHORT).show();
                        Log.d("Login", "incorrect password");
                    }

                }
                else
                {
                    Log.d("Exception", e.getMessage());
                    if(e.getMessage().compareTo("no results found for query") == 0)
                    {
                        Toast.makeText(getActivity().getApplicationContext(), "invalid username or password",
                                Toast.LENGTH_SHORT).show();
                        Log.d("Login", "no user found");
                    }
                }
            }
        });

        //Bundle bundle= new Bundle();


        /*if(pw.length() < 8 || usern.length() < 8)
            Toast.makeText(getActivity().getApplicationContext(),
                    "User credentials need to be at least 8 characters",
                    Toast.LENGTH_SHORT).show();
        else {
            // error checking. just displays the user credentials that are entered
            Toast.makeText(getActivity().getApplicationContext(), usern + " " + pw,
                    Toast.LENGTH_SHORT).show();

            //Stores username and password to bundle to send to StoreFragment
            bundle.putString("username", usern);
            bundle.putString("password", pw);



        //if login fail: SUSIE CHECK DB FOR matching username/pw
        /*
        if (false)
        Toast.makeText(getActivity().getApplicationContext(), "Wrong Credentials",
                Toast.LENGTH_SHORT).show();
                */
    }

    public void newLoginCheck(View view){
        final String usern= username.getText().toString();
        final String pw= password.getText().toString();
        Bundle bundle= new Bundle();

        if(pw.length() < 4 || usern.length() < 4)
            Toast.makeText(getActivity().getApplicationContext(),
                    "User credentials need to be at least 4 characters",
                    Toast.LENGTH_SHORT).show();
        else {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
            query.whereEqualTo("Login", usern);
            query.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if(e == null) {
                        if (parseObject == null) {

                            Log.d("Login", "object null");
                        }
                        Toast.makeText(getActivity().getApplicationContext(), "Login already in use, try another.",
                                Toast.LENGTH_SHORT).show();

                    }
                    else
                    {
                        ParseObject users = new ParseObject("Users");
                        users.put("Login", usern);
                        users.put("Password", pw);
                        users.put("Money", 100);
                        users.saveInBackground();
                        Toast.makeText(getActivity().getApplicationContext(), usern + " account created! Enjoy your $100 sign on bonus!!!",
                                Toast.LENGTH_SHORT).show();
                        Fragment stFragment = new StoreFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("Login", usern);
                        loggedin = true;
                        ((MainActivity)getActivity()).setLoggedin(true);
                        Log.d("Login", usern + "loggedin");
                        userLoggedIn = usern;
                        ((MainActivity)getActivity()).setUserLoggedin(usern);

                        stFragment.setArguments(bundle);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.frame_container, stFragment).commit();
                    }
                }
            });
            //SUSIE STORE usern and pw to DB
           /* Toast.makeText(getActivity().getApplicationContext(), "Cool, You're Registered!",
                    Toast.LENGTH_SHORT).show();

            //Stores username and password to bundle to send to StoreFragment
            bundle.putString("username", usern);
            bundle.putString("password", pw);
            */
        }
        //SHOULD SWITCH OR START THE STORE FRAGMENT SCREEN NOW//

    }
    @Override
    public void onPause(){ super.onPause(); }

}
