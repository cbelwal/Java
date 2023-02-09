package com.dasmic.android.libhttpclient;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpClient {
    URL mURL;
    HttpURLConnection mConn;
    //Constructor
    public HttpClient(String urlString){
        try {
            mURL = new URL(urlString);
            mConn = (HttpURLConnection) mURL.openConnection();
        }
        catch(MalformedURLException ex)
        {
            Log.e("Lib.HttpClient:","Got MalformedURLException");
            mURL = null;
            throw new RuntimeException(ex);
        }
        catch(IOException ex)
        {
            Log.e("Lib.HttpClient:","Got MalformedURLException");
            mConn = null;
            throw new RuntimeException(ex);
        }
    }

    public String Get()
    {
        int responseCode;
        StringBuilder allOutput = new StringBuilder();
        try {
            mConn.setRequestMethod("GET");
             mConn.setRequestProperty("Accept", "application/json");
            responseCode=mConn.getResponseCode();
        }
        catch (Exception ex){
           // Response response = new Response();

            //response.LastError = ex.getMessage();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (mConn.getInputStream())));


            String output;
            //System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                allOutput.append(output);
            }
        }
        catch (IOException ex){

        }

        return allOutput.toString();
    }

    //private Response getResponseObject(String)
}
