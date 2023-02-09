
package com.dasmic.android.vcardsplitter.Data;


/**
 * Created by Chaitanya Belwal on 9/20/2015.
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
