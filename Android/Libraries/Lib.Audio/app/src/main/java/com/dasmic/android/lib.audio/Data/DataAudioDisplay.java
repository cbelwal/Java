package com.dasmic.android.lib.audio.Data;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.dasmic.android.lib.audio.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.libaudio.R;

import java.io.File;

/**
 * Created by Chaitanya Belwal on 2/25/2018.
 */

public class DataAudioDisplay {
    public long Size; //in Bytes
    public long LastModifiedDate;
    public String AbsoluteFilePath;
    public boolean IsChecked;
    private String mFileName;
    public boolean HasReadPermission;
    public boolean HasWritePermission;
    public String Artist;

    public DataAudioDisplay(String name){
        mFileName = name;
        IsChecked=false;
    }

    public String getFileFolder(){
        File f = new File(AbsoluteFilePath);
        return f.getParent(); //Returns containing folder of file
    }

    public String getName(){
        return mFileName;
    }

    public String getNameWithoutExtension(){
        String filenameArray[] = mFileName.split("\\.");
        String fName = filenameArray[0];
        return fName;
    }

    public String getExtension(){
        String filenameArray[] = mFileName.split("\\.");
        String extension="";
        if(filenameArray.length > 1)
            extension = filenameArray[filenameArray.length-1];
        return "." + extension;
    }

    @Override
    public String toString() {
        return mFileName;
    }

    public String getPrimaryValue(){return mFileName;}
    public String getSecondaryValue(DisplayOptionsEnum doe) {
        if(doe==DisplayOptionsEnum.SortedByModifiedDate)
            return getModifiedDateFormatted();
        //Else return Artist
        if(Artist != null)
            return Artist;
        else
            return "NA";
    }
    public String getTertiaryValue() {
        return getSizeInKB();
    }


    public static boolean isVideoFile(String fullFileName){
        String fileName = fullFileName.trim().toUpperCase();
        if(     fileName.endsWith("3GP") ||
                fileName.endsWith("MP4") ||
                fileName.endsWith("WEBM"))
            return true;
        else
            return false;
    }


    public boolean isGotoParentFolder(){
        try {
            if (mFileName.trim().equals(".."))
                return true;
            else
                return false;
        }
        catch(Exception ex){
            return false;
        }
    }

    public Drawable getThumbnail(Activity activity){
        Drawable d;
        switch (getExtension().toUpperCase().trim()){
            case ".MP3":
                d = ContextCompat.getDrawable(activity, R.drawable.mp3_256);
                break;
            case ".OGG":
                d = ContextCompat.getDrawable(activity, R.drawable.ogg_256);
                break;
            case ".WAV":
                d = ContextCompat.getDrawable(activity, R.drawable.wav_256);
                break;
            case ".M4A":
                d = ContextCompat.getDrawable(activity, R.drawable.m4a_256);
                break;
            default:
                d = ContextCompat.getDrawable(activity, R.drawable.na_256);
        }

        return d;
    }

    public String getModifiedDateFormatted(){
        return DateOperations.getFormattedDate(LastModifiedDate);
    }

    public String getSizeInKB(){
        return String.valueOf(Size/1024) + " KB";
    }

}
