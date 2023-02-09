package com.dasmic.android.lib.audio.Model;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.audio.Comparators.ComparatorLastModified;
import com.dasmic.android.lib.audio.Comparators.ComparatorName;
import com.dasmic.android.lib.audio.Comparators.ComparatorSize;
import com.dasmic.android.lib.audio.Data.DataAudioDisplay;
import com.dasmic.android.lib.audio.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.audio.Enum.SearchOptionsEnum;
import com.dasmic.android.libaudio.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Chaitanya Belwal on 2/25/2018.
 */

public class ModelAudioRead {
    private Activity _activity;
    private boolean _sortAscending;
    private int _folderSearchDepth;

    public ModelAudioRead(Activity activity){
        _activity=activity;
        _folderSearchDepth = 15;
    }

    public void setFolderSearchDepth(int newValue){
        _folderSearchDepth = newValue;
    }
    public int getFolderSearchDepth(){
        return _folderSearchDepth;
    }

    private ArrayList<DataAudioDisplay>
        extractListofSpecificFiles(ArrayList<File> allFiles){
        ArrayList<DataAudioDisplay> allAudioFiles = new ArrayList<DataAudioDisplay>();
        for(File f:allFiles){
            if(f!=null) {
                DataAudioDisplay dvd = new DataAudioDisplay(f.getName());
                dvd.AbsoluteFilePath = f.getAbsolutePath();
                dvd.LastModifiedDate = f.lastModified();
                dvd.HasWritePermission = f.canWrite();
                dvd.HasReadPermission = f.canRead();
                dvd.Size = f.length();
                allAudioFiles.add(dvd);
            }
        }
        return allAudioFiles;
    }

    public ArrayList<DataAudioDisplay>
        getListofSpecificFilesInMediaFolder(ArrayList<String> extensions) {

        //Root folder
        File topLevelFolder = FileOperations.getMediaStorageFolder();
        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,15); //Go to 1sth level of root
        return extractListofSpecificFiles(allFiles);
    }

    public ArrayList<DataAudioDisplay>
        getListofSpecificFilesInExternalFolder(ArrayList<String> extensions) {
        //Root folder
        File topLevelFolder = FileOperations.getExternalStorageFolder();
        ArrayList<File> allFiles =  //Do external storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,35); //Go to 1sth level of root
        return extractListofSpecificFiles(allFiles);
    }


    public ArrayList<DataAudioDisplay>
        getListofSpecificFilesInEntirePhone(ArrayList<String> extensions,
                                            int maxLevel) {

        //Root folder
        File topLevelFolder = new File("/");
        ArrayList<File> allFiles =  //Do internal storage folder
                FileOperations.getAllFileObjectsInFolder(
                        topLevelFolder,
                        extensions,0,maxLevel); //Go to maxLevel  of root

        return extractListofSpecificFiles(allFiles);
    }

    private ArrayList<DataAudioDisplay> getAllInformationAudioFilesMediaStore(){
        ArrayList<DataAudioDisplay> allAudioFiles = new ArrayList<>();

        getAllInformationSpecificMediaStore(allAudioFiles,MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        getAllInformationSpecificMediaStore(allAudioFiles,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);

        return allAudioFiles;
    }

    private ArrayList<DataAudioDisplay> getAllInformationSpecificMediaStore
            (ArrayList<DataAudioDisplay> allAudioFiles, Uri mediaURI){

        ContentResolver cr = _activity.getContentResolver();

        Uri uri = mediaURI;  //EXTERNAL_CONTENT_URI;
        String selection =null;// MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String path = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
                    File f = new File(path);
                    String name = f.getName();// cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    DataAudioDisplay dad = new DataAudioDisplay(name);
                    dad.AbsoluteFilePath = path;
                    //dad.Duration = Long.valueOf(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DURATION)));
                    dad.Artist = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    dad.Size = Long.valueOf(cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                    dad.LastModifiedDate = f.lastModified();

                    // Add code to get more column here
                    allAudioFiles.add(dad);
                    // Save to your list here
                }
            }
        }

        cur.close();
        return allAudioFiles;
    }


    //Returns all video files present in the phone
    public ArrayList<DataAudioDisplay> getAllInformationAudioFiles
                        (DisplayOptionsEnum doe, SearchOptionsEnum soe){
        ArrayList<String> extensions= new ArrayList<>();
        extensions.add(".mp3");
        extensions.add(".wav");
        extensions.add(".m4a");
        extensions.add(".ogg");
        ArrayList<DataAudioDisplay> allVideoFiles=null;

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
            case MediaStore: //Default
                allVideoFiles =
                        getAllInformationAudioFilesMediaStore();
        }

        ArrayList<DataAudioDisplay> sortedList =
                sortBy(allVideoFiles,doe);
        return sortedList;
    }

    private ArrayList<DataAudioDisplay> sortBy(ArrayList<DataAudioDisplay> allFiles,
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
