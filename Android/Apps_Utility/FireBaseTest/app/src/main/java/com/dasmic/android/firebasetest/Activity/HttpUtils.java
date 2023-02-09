package com.dasmic.android.firebasetest.Activity;


import com.google.android.gms.ads.internal.gmsg.HttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpUtils {
    private static final String BASE_URL = "http://api.twitter.com/1/";

    private static HttpURLConnection client = new HttpURLConnection() {
        @Override
        public void disconnect() {
             
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {

        }
    };

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static void postByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(url, params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}