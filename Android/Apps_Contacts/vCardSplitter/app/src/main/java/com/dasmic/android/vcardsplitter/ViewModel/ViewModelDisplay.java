package com.dasmic.android.vcardsplitter.ViewModel;

import android.app.Activity;
import android.net.Uri;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.vcardsplitter.Data.DataValuePair;
import com.dasmic.android.vcardsplitter.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/17/2016.
 * Class is a SingleTon
 */
public class ViewModelDisplay {
    private ArrayList<String> mExtensions;
    private Activity mActivity;
    private String mExternalFolderName;
    private static ViewModelDisplay _instance;
    private Uri _fileUri;
    private String _fileName;
    private Long _fileSize;
    private int _numberOfSplit;
    private int _radioSplitOption;
    private int _radioSaveAsOption;

    public String mInternalFolderName;

    public void setExtensions(){
        mExtensions= new ArrayList<>();
        mExtensions.add(".vcf");
    }

    public void setSelectedFile(Uri uri, String fileName,Long fileSize){
        _fileUri=uri;
        _fileName=fileName;
        _fileSize=fileSize;
    }

    public Uri getSelectedFileUri(){
        return _fileUri;
    }
    public String getSelectedFileName(){
        return _fileName;
    }
    public Long getSelectedFileSize(){
        return _fileSize;
    }


    public void setSplitProperties(int numberOfSplit, int radioSplitOption,
                                    int radioSaveOption)
    {
        _numberOfSplit=numberOfSplit;
        _radioSplitOption=radioSplitOption;
        _radioSaveAsOption =radioSaveOption;
    }

    public int getNumberOfSplit(){
        return _numberOfSplit;
    }

    public int getRadioSplitOption(){
        return _radioSplitOption;
    }

    public int getRadioSaveAsOption(){
        return _radioSaveAsOption;
    }

    private ViewModelDisplay(Activity activity)
    {
        mActivity=activity;
        setExtensions();
        String appName=mActivity.getResources().getString(R.string.app_name_small);
        mInternalFolderName = appName+"_Internal";
        mExternalFolderName = appName +"_Share"; // Should match one in file

        //Set properties for Split Display
        setDefaultForSplit();
    }

    private void setDefaultForSplit(){
        _numberOfSplit=2;
        _radioSplitOption=R.id.radioSplitPerContact;
        _radioSaveAsOption =R.id.radioExternalStorage;
    }

    public static ViewModelDisplay getInstance(
            Activity activity){
        if(_instance==null) _instance =
                new ViewModelDisplay(activity);
        return _instance;
    }

    public ArrayList<DataValuePair<String,String>>
            getListofVCFFilesInPhone() {
        ArrayList<DataValuePair<String,String>> allMaps =
                new ArrayList<DataValuePair<String,String>>();
        DataValuePair<String,String> dvPair;

        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        FileOperations.getInternalStorageFolder(mActivity),
                        mExtensions);
        allFiles.addAll( //Add external storage folder
                FileOperations.getAllFileObjectsInFolder(
                        FileOperations.getExternalStorageFolder(),
                        mExtensions));
        for(File f:allFiles){
            if(f!=null) {
                dvPair = new DataValuePair<String, String>(f.getPath(), f.getName());
                allMaps.add(dvPair);
            }
        }
        return allMaps;
    }

    public boolean isValidFileName(String fileName){
        if(fileName==null) return false;
        for(String ex: mExtensions) {
            if(fileName.endsWith(ex))
                return true;
        }
        return false;
    }

}
