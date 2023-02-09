package com.dasmic.android.lib.apk.ViewModel;

import android.app.Activity;


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.apk.Model.ModelPackageDelete;
import com.dasmic.android.lib.apk.Model.ModelPackageRead;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 8/6/2015.
 */
public class ViewModelAppsDisplay {
    private Activity _activity;
    private ModelPackageRead _modelPackageRead;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataPackageDisplay>> _mainList;
    private static ViewModelAppsDisplay _instance;
    private DisplayOptionsEnum _currentDisplayOption;



    //public boolean allChecked;


    //Get a single contact which is added to main ArrayList
    private ArrayList<DataPackageDisplay>
                    getValuesForDisplayOption(
                        DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataPackageDisplay> finalValues=null;

        finalValues= _modelPackageRead.getAllInformation(displayOption);

        return finalValues;
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataPackageDisplay> RegenerateListOfPackages() {
        ArrayList<DataPackageDisplay> itemsList;

        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return _mainList.get(_currentDisplayOption);
    }

    //Ctor
    private ViewModelAppsDisplay(Activity activity,
                                 boolean isMalwareProgram){
        _activity = activity;
        _mainList = new Hashtable<>();
        _modelPackageRead = new ModelPackageRead(_activity,
                isMalwareProgram);
        //Set default value
        _currentDisplayOption=DisplayOptionsEnum.defaultView;
    }

    public void setCurrentDisplayOption(DisplayOptionsEnum value){
        _currentDisplayOption=value;
    }
    public DisplayOptionsEnum getCurrentDisplayOption(){
        return _currentDisplayOption;
    }

    public static ViewModelAppsDisplay getInstance(
            Activity activity,boolean isMalwareProgram){
        if(_instance==null) _instance =
                new ViewModelAppsDisplay(activity, isMalwareProgram);
        return _instance;
    }

    //Will not create a new instance, assumes was preiously created
    public static ViewModelAppsDisplay getInstance(){
        return _instance;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataPackageDisplay>
                getCurrentDisplayOptionContacts() {
        //_currentDisplayOption = currentDisplayOption;
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfPackages();
        return _mainList.get(_currentDisplayOption);
    }

    public ArrayList<DataPackageDisplay> getCheckedItems(){
        ArrayList<DataPackageDisplay> checkedItemList = new ArrayList<>();
        for(int ii=0;ii<_mainList.get(_currentDisplayOption).size();ii++){
            if(_mainList.get(_currentDisplayOption).get(ii).isChecked)
                checkedItemList.add(_mainList.get(_currentDisplayOption).get(ii));
        }
        return checkedItemList;
    }

    public void InvalidateData(){
        _mainList.clear();
    }

    public ViewModelFilterOption getFilterOption(){
        return _modelPackageRead.getFilterOption();
    }
    public ViewModelAdvFilterOption getAdvFilterOption(){
        _modelPackageRead.setUseAdvancedFilter();
        return _modelPackageRead.getAdvFilterOption();

    }

    public int Delete(ArrayList<DataPackageDisplay> allDpd){
        ModelPackageDelete modelContactsDelete = new
                ModelPackageDelete(_activity);
        return modelContactsDelete.DeleteContacts(allDpd);
    }
}
