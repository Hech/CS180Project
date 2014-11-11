package com.hech.musicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, String>{
    private Context context;
    private PowerManager.WakeLock mWakelock;
    public File file;
    public DownloadTask(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        // creates a path to Music Directory
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        file = new File(path, params[1]);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // If it's not HTTP 200, then return err
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Error: HTTP " + connection.getResponseCode() + " " +
                        connection.getResponseMessage();
            }

            // Downloading the file
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte[] data = new byte[input.available()];
            input.read(data);
            output.write(data);
            input.close();
            output.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
