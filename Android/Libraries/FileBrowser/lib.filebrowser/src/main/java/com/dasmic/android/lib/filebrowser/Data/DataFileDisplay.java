package com.dasmic.android.lib.filebrowser.Data;

import com.dasmic.android.lib.filebrowser.R;
import com.dasmic.android.lib.support.Static.DateOperations;

import java.io.File;

/**
 * Created by Chaitanya Belwal on 9/30/2017.
 */

public class DataFileDisplay {

    public long Size; //in Bytes
    public boolean IsFolder;
    public long LastModifiedDate;
    public String AbsoluteFilePath;
    public boolean IsChecked;
    private boolean mIsTopFolder;
    private String mName;
    public boolean HasReadPermission;
    public boolean HasWritePermission;

    public DataFileDisplay(String name){
        if(name.equals(".."))
            mIsTopFolder=true;
        else
            mIsTopFolder = false;
        IsChecked=false;
        mName = name;
    }

    public String getFileFolder(){
        if(IsFolder) return AbsoluteFilePath;
        File f = new File(AbsoluteFilePath);
        return f.getParent(); //Returns containing folder of file
    }

    public boolean getIsTopFolder(){
        return mIsTopFolder;
    }

    public String getName(){
        return mName;
    }

    @Override
    public String toString() {
            return getName();
    }

    public String getSecondaryValue() {
        return DateOperations.getFormattedDate(LastModifiedDate);
    }

    public String getTertiaryValue() {
            return GetSizeInKB();
    }

    private String GetSizeInKB()
    {
        double sizeKb = Size/1024;
        return String.format("%.2f", sizeKb) + " KB";
    }

    public int getIconResouceId(){

        if(getIsTopFolder()){
            return R.drawable.up_128;
        }
        else if(IsFolder){
            return R.drawable.files_128;
        }
        else{
            return R.drawable.document_128;
        }
    }

    public boolean isImageFile(){
        if(IsFolder) return false;
        String fileName = getName().trim().toUpperCase();
        if(     fileName.endsWith("PNG") ||
                fileName.endsWith("JPG") ||
                fileName.endsWith("GIF") ||
                fileName.endsWith("BMP") )
            return true;
        else
            return false;
    }

    public boolean isVideoFile(){
        if(IsFolder) return false;
        String fileName = getName().trim().toUpperCase();
        if(fileName.endsWith("MP4"))
            return true;
        else
            return false;
    }

    public boolean isGotoParentFolder(){
        try {
            if (mName.trim().equals(".."))
                return true;
            else
                return false;
        }
        catch(Exception ex){
            return false;
        }
    }

}
