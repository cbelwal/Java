package com.dasmic.android.lib.filebrowser.Model;

import android.app.Activity;
import android.util.Log;

import com.dasmic.android.lib.filebrowser.Comparators.ComparatorLastModified;
import com.dasmic.android.lib.filebrowser.Comparators.ComparatorName;
import com.dasmic.android.lib.filebrowser.Comparators.ComparatorSize;
import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;
import com.dasmic.android.lib.filebrowser.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.support.Static.FileOperations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chaitanya Belwal on 9/30/2017.
 */

public class ModelFileRead {
    private Activity _activity;
    private boolean _sortAscending;

    public ModelFileRead(Activity activity){
        _activity=activity;
    }

    public ArrayList<DataFileDisplay>
        getAllInformation(DisplayOptionsEnum doe, String folder) {

        return getAllInformationFiles(doe, folder);
    }


    //Returns all objects in a specific folder
    //Also adds a top level folder
    private ArrayList<DataFileDisplay> getAllInformationFiles(DisplayOptionsEnum doe,
                                                              String folder){
        ArrayList<DataFileDisplay> allFiles = new ArrayList<DataFileDisplay>();
        File parentDir = new File(folder);
        if(parentDir==null) {
            Log.i("CKIT", "getAllFileObjectsInFolder::parentDir is null");
            return allFiles;
        }

        File[] files = parentDir.listFiles();
        for (File file : files) {
            DataFileDisplay dfd = new DataFileDisplay(file.getName());
            if (file.isDirectory())
                dfd.IsFolder = true;
            else { //Check if File matches Extension
                dfd.IsFolder = false;
            }

            if(file.canRead())
                dfd.HasReadPermission = true;
            else
                dfd.HasReadPermission = false;

            if(file.canWrite())
                dfd.HasWritePermission = true;
            else
                dfd.HasWritePermission = false;

            dfd.AbsoluteFilePath = file.getAbsolutePath();
            dfd.LastModifiedDate = file.lastModified();
            dfd.Size = file.length();
            allFiles.add(dfd);

        } //for loop
        ArrayList<DataFileDisplay> sortedList =
                sortBy(allFiles,doe);

        //Add top level folder
        { //Create a separate block to set local scope for dfd
            DataFileDisplay dfd = new DataFileDisplay("..");
            dfd.IsFolder = true;
            dfd.AbsoluteFilePath = parentDir.getAbsolutePath();
            dfd.HasReadPermission=true;
            sortedList.add(0,dfd); //Add to top
        }
        return sortedList;
    }

    private ArrayList<DataFileDisplay> sortBy(ArrayList<DataFileDisplay> allFiles,
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
