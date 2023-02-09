package com.dasmic.android.videolib.ViewModel;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;


import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.videolib.Data.DataVideoDisplay;
import com.dasmic.android.videolib.Enum.DisplayOptionsEnum;
import com.dasmic.android.videolib.Enum.SearchOptionsEnum;
import com.dasmic.android.videolib.Model.ModelVideoCreate;
import com.dasmic.android.videolib.Model.ModelVideoDelete;
import com.dasmic.android.videolib.Model.ModelVideoRead;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ViewModelVideoBrowser {
    private Activity _activity;
    private ModelVideoRead _modelVideoRead;
    private ModelVideoDelete _modelVideoDelete;
    private ModelVideoCreate _modelVideoCreate;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataVideoDisplay>> _mainList;
    private static ViewModelVideoBrowser _instance;
    private DisplayOptionsEnum _currentDisplayOption;
    private SearchOptionsEnum _currentSearchOption;
    private String _currentFolder;
    private int mDeleteCount;


    //Ctor
    public ViewModelVideoBrowser(Activity activity){
        _activity = activity;
        _mainList = new Hashtable<>();
        _modelVideoRead = new ModelVideoRead(_activity);
        //Set default value
        _currentDisplayOption=DisplayOptionsEnum.DefaultView;
    }

    private final Handler mDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case ModelVideoDelete.DELETE_COUNT_UPDATE:
                    mDeleteCount = msg.arg1;
                    break;
                default:
            }
        }
    };


    public static ViewModelVideoBrowser getInstance(Activity activity){
        if(_instance==null) _instance =
                new ViewModelVideoBrowser(activity);
        return _instance;
    }

    public int DeleteVideos(ArrayList<String> VideoPaths){
        if(_modelVideoDelete ==null) {
            _modelVideoDelete = new ModelVideoDelete(_activity, mDeleteHandler);
        }
        return _modelVideoDelete.DeleteVideoFiles(VideoPaths);
    }

    public int DeleteVideoFile(DataVideoDisplay dfd){
        if(_modelVideoDelete ==null) {
            _modelVideoDelete = new ModelVideoDelete(_activity, mDeleteHandler);
        }
        return _modelVideoDelete.DeleteVideoFile(dfd.AbsoluteFilePath);
    }

    private void CreateModelVideoCreate(){
        if(_modelVideoCreate == null)
            _modelVideoCreate = new ModelVideoCreate(_activity);
    }

    //Create a copy for the Video
    public void CopyVideoFile(DataVideoDisplay dfd){
        CreateModelVideoCreate();

        String sourceVideoPath = dfd.AbsoluteFilePath;
        String origDestVideoPath = dfd.getVideoFileFolder();
        String destVideoPath="";

        int idx=1;
        while(true) {
            destVideoPath = origDestVideoPath + "/" + dfd.getNameWithoutExtension() +
                    "_" + String.valueOf(idx++) + dfd.getExtension();
            if(!FileOperations.DoesFileExist(destVideoPath))
                break;
        }
        _modelVideoCreate.copySingleVideoFile(sourceVideoPath,destVideoPath);

    }

    public void setCurrentDisplayOption(DisplayOptionsEnum value){
        _currentDisplayOption=value;
    }

    public void setCurrentSearchOption(SearchOptionsEnum value){
        _currentSearchOption=value;
    }

    public DisplayOptionsEnum getCurrentDisplayOption(){
        return _currentDisplayOption;
    }

    public ArrayList<DataVideoDisplay> getCheckedItems(){
        ArrayList<DataVideoDisplay> checkedItemList = new ArrayList<>();
        for(int ii=0;ii<_mainList.get(_currentDisplayOption).size();ii++){
            if(_mainList.get(_currentDisplayOption).get(ii).IsChecked)
                checkedItemList.add(_mainList.get(_currentDisplayOption).get(ii));
        }
        return checkedItemList;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataVideoDisplay>
    getCurrentDisplayOptionValues() {
        //_currentDisplayOption = currentDisplayOption;
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfValues();
        return _mainList.get(_currentDisplayOption);
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataVideoDisplay> RegenerateListOfValues() {
        ArrayList<DataVideoDisplay> itemsList;

        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return _mainList.get(_currentDisplayOption);
    }

    //Get a single contact which is added to main ArrayList
    private ArrayList<DataVideoDisplay>
    getValuesForDisplayOption(
            DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataVideoDisplay> finalValues=null;

        finalValues= _modelVideoRead.getAllInformation(displayOption,
                        _currentSearchOption,
                        _currentFolder);

        return finalValues;
    }

    public void InvalidateData(){
        _mainList.clear();
    }


    public boolean changeSortOrder(){
        boolean newOrder = _modelVideoRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    public boolean getCurrentSortOrder(){
        return _modelVideoRead.getCurrentSortOrder();
    }

    public void CreateNewFolderInCurrentFolder(String newFolderName){
        CreateModelVideoCreate();
        _modelVideoCreate.CreateNewFolderInCurrentFolder(_currentFolder,newFolderName);
    }

    public void RenameVideoFile(DataVideoDisplay dfd, String newName){
        CreateModelVideoCreate();
        _modelVideoCreate.RenameVideoFile(dfd, newName);
    }

    public void setCurrentBrowseFolder(String currentFolder){
        //Check if Folder readable, otherwise raise exception
        if(FileOperations.canReadFolder(currentFolder))
            _currentFolder = currentFolder;
        else //Cannot read folder, raise exception
            throw new RuntimeException();
    }

    public int getCurrentSearchDepth(){
        if(_modelVideoRead != null)
            return _modelVideoRead.getFolderSearchDepth();
        else
            return 0;
    }

    public void setCurrentSearchDepth(int value){
        if(_modelVideoRead != null)
            _modelVideoRead.setFolderSearchDepth(value);
    }

















}
