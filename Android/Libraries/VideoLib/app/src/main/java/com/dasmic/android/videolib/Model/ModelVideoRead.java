package com.dasmic.android.videolib.Model;

import android.app.Activity;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.videolib.Comparators.ComparatorLastModified;
import com.dasmic.android.videolib.Comparators.ComparatorName;
import com.dasmic.android.videolib.Comparators.ComparatorSize;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.Enum.DisplayOptionsEnum;
import com.dasmic.android.videolib.Enum.SearchOptionsEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chaitanya Belwal on 2/25/2018.
 */

public class ModelVideoRead {
    private Activity _activity;
    private boolean _sortAscending;
    private int _folderSearchDepth;

    public ModelVideoRead(Activity activity){
        _activity=activity;
        _folderSearchDepth = 15;
    }

    public void setFolderSearchDepth(int newValue){
        _folderSearchDepth = newValue;
    }

    public int getFolderSearchDepth(){
        return _folderSearchDepth;
    }
    public ArrayList<DataVideoDisplay>

    getAllInformation(DisplayOptionsEnum doe, SearchOptionsEnum soe,
                      String folder) {
        return getAllInformationVideoFiles(doe,soe, folder);
    }

    private ArrayList<DataVideoDisplay>
        extractListofSpecificFiles(ArrayList<File> allFiles){
        ArrayList<DataVideoDisplay> allVideoFiles = new ArrayList<DataVideoDisplay>();
        for(File f:allFiles){
            if(f!=null) {
                DataVideoDisplay dvd = new DataVideoDisplay(f.getName());
                dvd.AbsoluteFilePath = f.getAbsolutePath();
                dvd.LastModifiedDate = f.lastModified();
                dvd.HasWritePermission = f.canWrite();
                dvd.HasReadPermission = f.canRead();
                dvd.Size = f.length();
                allVideoFiles.add(dvd);
            }
        }
        return allVideoFiles;
    }

    public ArrayList<DataVideoDisplay>
        getListofSpecificFilesInMediaFolder(ArrayList<String> extensions) {

        //Root folder
        File topLevelFolder = FileOperations.getMediaStorageFolder();
        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,15); //Go to 1sth level of root
        return extractListofSpecificFiles(allFiles);
    }

    public ArrayList<DataVideoDisplay>
        getListofSpecificFilesInExternalFolder(ArrayList<String> extensions) {

        //Root folder
        File topLevelFolder = FileOperations.getExternalStorageFolder();
        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,35); //Go to 1sth level of root
        return extractListofSpecificFiles(allFiles);
    }


    public ArrayList<DataVideoDisplay>
        getListofSpecificFilesInEntirePhone(ArrayList<String> extensions, int maxLevel) {

        //Root folder
        File topLevelFolder = new File("/");
        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,maxLevel); //Go to maxLevel  of root

        return extractListofSpecificFiles(allFiles);
    }

    //Returns all video files present in the phone
    private ArrayList<DataVideoDisplay> getAllInformationVideoFiles
                        (DisplayOptionsEnum doe, SearchOptionsEnum soe,
                                                                    String folder){
        ArrayList<String> extensions= new ArrayList<>();
        extensions.add(".3gp");
        extensions.add(".mp4");
        extensions.add(".webm");
        ArrayList<DataVideoDisplay> allVideoFiles=null;

        switch(soe)
        {
            case EntirePhone:
                allVideoFiles =
                        getListofSpecificFilesInEntirePhone(extensions, _folderSearchDepth);
                break;
            case MediaFolder:
                allVideoFiles =
                        getListofSpecificFilesInMediaFolder(extensions);
                break;
            case ExternalFolder:
                allVideoFiles =
                        getListofSpecificFilesInExternalFolder(extensions);
                break;
        }

        ArrayList<DataVideoDisplay> sortedList =
                sortBy(allVideoFiles,doe);
        return sortedList;
    }

    private ArrayList<DataVideoDisplay> sortBy(ArrayList<DataVideoDisplay> allFiles,
                                              DisplayOptionsEnum doe){
        //Sort all files based on doe
        switch(doe)
        {
            case DefaultView:
                Collections.sort(allFiles, new ComparatorName());
                break;
            case SortedByModifiedDate:
                Collections.sort(allFiles, new ComparatorLastModified());
                break;
            case SortedBySize:
                Collections.sort(allFiles, new ComparatorSize());
                break;
            default:
                Collections.sort(allFiles, new ComparatorName());
                break;
        }
        if(!_sortAscending)
            Collections.reverse(allFiles);
        return allFiles;
    }

    public boolean changeSortOrder()
    {
        _sortAscending = !_sortAscending;
        return  getCurrentSortOrder();
    }

    public boolean getCurrentSortOrder() {
        return _sortAscending;
    }

}
