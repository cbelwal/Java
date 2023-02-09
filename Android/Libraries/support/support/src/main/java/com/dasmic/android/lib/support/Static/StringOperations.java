package com.dasmic.android.lib.support.Static;

/**
 * Created by Chaitanya Belwal on 2/4/2016.
 */
public class StringOperations {
    //Returns number of subStrings in mainString
    public static int getSubStringCount(String subString,
                                           String mainString){

        int count=0,idx=0;

        while(idx>=0){
            idx=mainString.indexOf(subString,idx);
            count++;
        }
        return count;
    }


}
