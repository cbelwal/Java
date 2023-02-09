package com.dasmic.android.lib.audio.ViewModel;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;


import com.dasmic.android.lib.audio.Interface.IGenericEvent;
import com.dasmic.android.lib.support.Static.FileOperations;
import com.dasmic.android.lib.audio.Data.DataAudioDisplay;
import com.dasmic.android.lib.audio.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.audio.Enum.SearchOptionsEnum;
import com.dasmic.android.lib.audio.Model.ModelAudioCreate;
import com.dasmic.android.lib.audio.Model.ModelVideoDelete;
import com.dasmic.android.lib.audio.Model.ModelAudioRead;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ViewModelAudioBrowser {
    private Activity _activity;
    private ModelAudioRead _modelAudioRead;
    private ModelVideoDelete _modelVideoDelete;
    private ModelAudioCreate _modelAudioCreate;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataAudioDisplay>> _mainList;
    private static ViewModelAudioBrowser _instance;
    private DisplayOptionsEnum _currentDisplayOption;
    private SearchOptionsEnum _currentSearchOption;
    private String _currentFolder;
    MediaPlayer _mediaPlayer;
    private int mDeleteCount;


    //Ctor
    public ViewModelAudioBrowser(Activity activity){
        _activity = activity;
        _mainList = new Hashtable<>();
        _modelAudioRead = new ModelAudioRead(_activity);
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


    public static ViewModelAudioBrowser getInstance(Activity activity){
        if(_instance==null) _instance =
                new ViewModelAudioBrowser(activity);
        return _instance;
    }

    public int DeleteAudioFiles(ArrayList<String> VideoPaths){
        if(_modelVideoDelete ==null) {
            _modelVideoDelete = new ModelVideoDelete(_activity, mDeleteHandler);
        }
        return _modelVideoDelete.DeleteVideoFiles(VideoPaths);
    }

    public int DeleteVideoFile(DataAudioDisplay dfd){
        if(_modelVideoDelete ==null) {
            _modelVideoDelete = new ModelVideoDelete(_activity, mDeleteHandler);
        }
        return _modelVideoDelete.DeleteVideoFile(dfd.AbsoluteFilePath);
    }

    private void createModelAudioCreate(){
        if(_modelAudioCreate == null)
            _modelAudioCreate = new ModelAudioCreate(_activity);
    }

    //Create a copy for the Video
    public void CopyAudioFile(DataAudioDisplay dad){
        createModelAudioCreate();

        String sourceVideoPath = dad.AbsoluteFilePath;
        String origDestVideoPath = dad.getFileFolder();
        String destVideoPath="";

        int idx=1;
        while(true) { //This loop executes to add the index _N to the file name
            destVideoPath = origDestVideoPath + "/" + dad.getNameWithoutExtension() +
                    "_" + String.valueOf(idx++) + dad.getExtension();
            if(!FileOperations.DoesFileExist(destVideoPath))
                break;
        }
        _modelAudioCreate.copySingleVideoFile(sourceVideoPath,destVideoPath);

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

    public ArrayList<DataAudioDisplay> getCheckedItems(){
        ArrayList<DataAudioDisplay> checkedItemList = new ArrayList<>();
        for(int ii=0;ii<_mainList.get(_currentDisplayOption).size();ii++){
            if(_mainList.get(_currentDisplayOption).get(ii).IsChecked)
                checkedItemList.add(_mainList.get(_currentDisplayOption).get(ii));
        }
        return checkedItemList;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataAudioDisplay>
    getCurrentDisplayOptionValues() {
        //_currentDisplayOption = currentDisplayOption;
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfValues();
        return _mainList.get(_currentDisplayOption);
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataAudioDisplay> RegenerateListOfValues() {
        ArrayList<DataAudioDisplay> itemsList;

        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return _mainList.get(_currentDisplayOption);
    }

    //Get a single contact which is added to main ArrayList
    private ArrayList<DataAudioDisplay>
    getValuesForDisplayOption(
            DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataAudioDisplay> finalValues=null;

        finalValues= _modelAudioRead.getAllInformationAudioFiles(displayOption,
                        _currentSearchOption);

        return finalValues;
    }

    public void InvalidateData(){
        _mainList.clear();
    }


    public boolean changeSortOrder(){
        boolean newOrder = _modelAudioRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    public boolean getCurrentSortOrder(){
        return _modelAudioRead.getCurrentSortOrder();
    }

    public void CreateNewFolderInCurrentFolder(String newFolderName){
        createModelAudioCreate();
        _modelAudioCreate.CreateNewFolderInCurrentFolder(_currentFolder,newFolderName);
    }

    public void RenameAudioFile(DataAudioDisplay dad, String newName){
        //Create the model
        createModelAudioCreate();
        //Make Sure extension is added
        _modelAudioCreate.RenameFile(dad,
                newName+dad.getExtension());
    }

    public void setCurrentBrowseFolder(String currentFolder){
        //Check if Folder readable, otherwise raise exception
        if(FileOperations.canReadFolder(currentFolder))
            _currentFolder = currentFolder;
        else //Cannot read folder, raise exception
            throw new RuntimeException();
    }

    public int getCurrentSearchDepth(){
        if(_modelAudioRead != null)
            return _modelAudioRead.getFolderSearchDepth();
        else
            return 0;
    }

    public void setCurrentSearchDepth(int value){
        if(_modelAudioRead != null)
            _modelAudioRead.setFolderSearchDepth(value);
    }


    public void playAudio(String fullFilePath, final IGenericEvent listener){
        //set up MediaPlayer
        _mediaPlayer = new MediaPlayer();

        try {
            _mediaPlayer.setDataSource(fullFilePath);
            _mediaPlayer.prepare();
            _mediaPlayer.start();
            _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    //Call function passed to it
                    listener.onEvent(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCurrentAudio(){
        if(_mediaPlayer != null)
            _mediaPlayer.stop();
    }


















}
