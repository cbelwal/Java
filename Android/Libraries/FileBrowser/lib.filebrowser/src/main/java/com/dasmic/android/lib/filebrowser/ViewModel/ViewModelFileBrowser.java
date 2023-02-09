package com.dasmic.android.lib.filebrowser.ViewModel;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;
import com.dasmic.android.lib.filebrowser.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.filebrowser.Model.ModelFileCreate;
import com.dasmic.android.lib.filebrowser.Model.ModelFileDelete;
import com.dasmic.android.lib.filebrowser.Model.ModelFileRead;
import com.dasmic.android.lib.support.Static.FileOperations;


import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ViewModelFileBrowser {
    private Activity _activity;
    private ModelFileRead _modelFileRead;
    private ModelFileDelete _modelFileDelete;
    private ModelFileCreate _modelFileCreate;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataFileDisplay>> _mainList;
    private static ViewModelFileBrowser _instance;
    private DisplayOptionsEnum _currentDisplayOption;
    private String _currentFolder;
    private int mDeleteCount;


    //Ctor
    public ViewModelFileBrowser(Activity activity){
        _activity = activity;
        _mainList = new Hashtable<>();
        _modelFileRead = new ModelFileRead(_activity);
        //Set default value
        _currentDisplayOption=DisplayOptionsEnum.DefaultView;
    }

    private final Handler mDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case ModelFileDelete.DELETE_COUNT_UPDATE:
                    mDeleteCount = msg.arg1;
                    break;
                default:
            }
        }
    };


    public void setCurrentBrowseFolder(String currentFolder){
        //Check if Folder readable, otherwise raise exception
        if(FileOperations.canReadFolder(currentFolder))
            _currentFolder = currentFolder;
        else //Cannot read folder, raise exception
            throw new RuntimeException();
    }

    public String getCurrentFolder(){
        return _currentFolder;
    }

    public static ViewModelFileBrowser getInstance(Activity activity){
        if(_instance==null) _instance =
                new ViewModelFileBrowser(activity);
        return _instance;
    }

    public int DeleteFiles(ArrayList<String> filePaths){
        if(_modelFileDelete ==null) {
            _modelFileDelete = new ModelFileDelete(_activity, mDeleteHandler);
        }
        return _modelFileDelete.DeleteFiles(filePaths);
    }

    public int DeleteFile(DataFileDisplay dfd){
        if(_modelFileDelete ==null) {
            _modelFileDelete = new ModelFileDelete(_activity, mDeleteHandler);
        }
        return _modelFileDelete.DeleteFile(dfd.AbsoluteFilePath);
    }

    private void CreateModelFileCreate(){
        if(_modelFileCreate == null)
            _modelFileCreate = new ModelFileCreate(_activity);
    }

    //Create a copy for the file
    public void CopyFile(DataFileDisplay dfd){
        if(dfd.IsFolder)
            throw new RuntimeException();

        CreateModelFileCreate();

        String sourceFilePath = dfd.AbsoluteFilePath;
        String origDestFilePath = dfd.getFileFolder();
        String destFilePath="";

        int idx=1;
        while(true) {
            destFilePath = origDestFilePath + "/" + dfd.getName() +
                                    "_" + String.valueOf(idx++);
            if(!FileOperations.DoesFileExist(destFilePath))
                break;
        }
        if(!_modelFileCreate.copySingleFile(sourceFilePath,destFilePath))
            throw new RuntimeException();
    }

    public void setCurrentDisplayOption(DisplayOptionsEnum value){
        _currentDisplayOption=value;
    }

    public DisplayOptionsEnum getCurrentDisplayOption(){
        return _currentDisplayOption;
    }

    public ArrayList<DataFileDisplay> getCheckedItems(){
        ArrayList<DataFileDisplay> checkedItemList = new ArrayList<>();
        for(int ii=0;ii<_mainList.get(_currentDisplayOption).size();ii++){
            if(_mainList.get(_currentDisplayOption).get(ii).IsChecked)
                checkedItemList.add(_mainList.get(_currentDisplayOption).get(ii));
        }
        return checkedItemList;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataFileDisplay>
        getCurrentDisplayOptionValues() {
            //_currentDisplayOption = currentDisplayOption;
            if(_mainList.get(_currentDisplayOption)==null)
                return RegenerateListOfValues();
            return _mainList.get(_currentDisplayOption);
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataFileDisplay> RegenerateListOfValues() {
        ArrayList<DataFileDisplay> itemsList;

        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return _mainList.get(_currentDisplayOption);
    }

    //Get a single contact which is added to main ArrayList
    private ArrayList<DataFileDisplay>
        getValuesForDisplayOption(
            DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataFileDisplay> finalValues=null;

        finalValues= _modelFileRead.getAllInformation(displayOption,
                                            _currentFolder);

        return finalValues;
    }

    public void InvalidateData(){
        _mainList.clear();
    }


    public boolean changeSortOrder(){
        boolean newOrder = _modelFileRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    public boolean getCurrentSortOrder(){
        return _modelFileRead.getCurrentSortOrder();
    }

    public void CreateNewFolderInCurrentFolder(String newFolderName){
        CreateModelFileCreate();
        _modelFileCreate.CreateNewFolderInCurrentFolder(_currentFolder,newFolderName);
    }

    public void RenameFile(DataFileDisplay dfd, String newName){
        CreateModelFileCreate();
        _modelFileCreate.RenameFile(dfd, newName);
    }

}
