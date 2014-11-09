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

public class LoginFragment extends Fragment {
    private EditText  username=null;
    private EditText  password=null;


    public LoginFragment(){};

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
        String usern= username.getText().toString();
        String pw= password.getText().toString();
        Log.d("Username:",usern);
        Log.d("Password:",pw);
        Bundle bundle= new Bundle();


        if(pw.length() < 8 || usern.length() < 8)
            Toast.makeText(getActivity().getApplicationContext(),
                    "User credentials need to be at least 8 characters",
                    Toast.LENGTH_SHORT).show();
        else
        // error checking. just displays the user credentials that are entered
        Toast.makeText(getActivity().getApplicationContext(), usern + " " + pw,
                Toast.LENGTH_SHORT).show();

        bundle.putString("username",usern);
        bundle.putString("password",pw);

        Fragment stFragment = new StoreFragment();
        stFragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_container, stFragment).commit();

        //if login fail: SUSIE CHECK DB FOR matching username/pw
        if (false)
        Toast.makeText(getActivity().getApplicationContext(), "Wrong Credentials",
                Toast.LENGTH_SHORT).show();
    }

    public void newLoginCheck(View view){
        String usern= username.getText().toString();
        String pw= password.getText().toString();

        if(pw.length() < 8 || usern.length() < 8)
            Toast.makeText(getActivity().getApplicationContext(),
                    "User credentials need to be at least 8 characters",
                    Toast.LENGTH_SHORT).show();

        //SUSIE STORE usern and pw to DB
        Toast.makeText(getActivity().getApplicationContext(), "Cool, You're Registered!",
                Toast.LENGTH_SHORT).show();

        //SHOULD SWITCH OR START THE STORE FRAGMENT SCREEN NOW//

    }

}
