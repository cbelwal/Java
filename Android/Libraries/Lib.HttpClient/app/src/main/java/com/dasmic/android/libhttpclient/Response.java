package com.dasmic.android.libhttpclient;

public class Response {
    public int StatusCode;
    public boolean IsSuccessful;
    public String Value;
    public String LastError;

    public Response(int StatusCode,String Value, boolean hasError){

    }

}
