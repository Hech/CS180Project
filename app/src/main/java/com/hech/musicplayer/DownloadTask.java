package com.hech.musicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends AsyncTask<String, Integer, String>{
    private Context context;
    private PowerManager.WakeLock mWakelock;
    public File file;
    public DownloadTask(Context context) {
        this.context = context;
    }

    @Override
    public String doInBackground(String... params) {
        // creates a path to Music Directory
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        file = new File(path, params[1]+".mp3");
        String s1 = params[0];
        String s2 = params[1];
        Log.d("doInBackground", s1 + " " + s2);
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        Log.d("DownloadTask", "pre try-catch");
        try {
            Log.d("DownloadTask", "try catch");
            URL url = new URL(params[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // If it's not HTTP 200, then return err
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d("Error: HTTP " + connection.getResponseCode(),  connection.getResponseMessage());
                return "Error: HTTP " + connection.getResponseCode() + " " +
                        connection.getResponseMessage();
            }

            int fileLength = connection.getContentLength();

            // Downloading the file
            input = connection.getInputStream();
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publish progress
                if (fileLength > 0) {
                    publishProgress((int) total*100 / fileLength);
                }
                output.write(data, 0, count);
            }
            /*
            byte[] data = new byte[input.available()];
            input.read(data);
            output.write(data);
            input.close();
            output.close();
            */
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored){}
            if (connection != null) {
                connection.disconnect();
            }
        }
        Log.d("doInBackground", "end of function");

        return null;
    }
}
