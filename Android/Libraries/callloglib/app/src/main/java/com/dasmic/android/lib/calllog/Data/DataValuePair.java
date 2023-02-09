package com.dasmic.android.lib.calllog.Data;

/**
 * Created by Chaitanya Belwal on 3/19/2017.
 */

public class DataValuePair<S,T> {
    public S Key;
    public T Value;

    public DataValuePair(S key, T value){
        Key=key;
        Value=value;
    }

    @Override
    public String toString(){
        return Value.toString();
    }
}