package com.dasmic.android.lib.contacts.Data;

/**
 * Created by Chaitanya Belwal on 1/8/2017.
 */

public class DataValueThreeTuple<S,T,U> {

        public S Key;
        public T Value;
        public U Label;

        public DataValueThreeTuple(S key, T value, U label){
            Key=key;
            Value=value;
            Label = label;
        }

        @Override
        public String toString(){
            return Value.toString();
        }

}
