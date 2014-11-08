package com.hech.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment {
    private EditText  username=null;
    private EditText  password=null;
    private TextView attempts;

    public LoginFragment(){};

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        return view;
    }

    public void userLoginCheck(View view){
        username=(EditText)view.findViewById(R.id.userText);
        password= (EditText)view.findViewById(R.id.pwText);
        String usern= username.getText().toString();
        String pw= password.getText().toString();
        Log.d("Username:",usern);
        Log.d("Password:",pw);

        //if login fail: SUSIE CHECK DB FOR matching username/pw
        Toast.makeText(getActivity().getApplicationContext(), "Wrong Credentials",
                Toast.LENGTH_SHORT).show();
    }

    public void newLoginCheck(View view){
        username=(EditText)view.findViewById(R.id.userText);
        password= (EditText)view.findViewById(R.id.pwText);
        String usern= username.getText().toString();
        String pw= password.getText().toString();

        //SUSIE STORE usern and pw to DB
        Toast.makeText(getActivity().getApplicationContext(), "Cool, You're Registered!",
                Toast.LENGTH_SHORT).show();

        //SHOULD SWITCH OR START THE STORE FRAGMENT SCREEN NOW//

    }

}
