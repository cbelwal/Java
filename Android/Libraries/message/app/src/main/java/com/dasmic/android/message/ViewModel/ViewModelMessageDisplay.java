package com.dasmic.android.lib.message.ViewModel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.message.Data.DataMessageDisplay;
import com.dasmic.android.lib.message.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.message.Model.ModelMessageDelete;
import com.dasmic.android.lib.message.Model.ModelMessageRead;
import com.dasmic.android.lib.message.Model.ModelMessageUpdate;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 8/19/2017.
 */

public class ViewModelMessageDisplay {
    private Context _context;
    private ModelMessageRead _modelMessageRead;
    private ModelMessageUpdate _modelMessageUpdate;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataMessageDisplay>> _mainList;
    private String _notAvailable;
    private static ViewModelMessageDisplay _instance;
    private DisplayOptionsEnum _currentDisplayOption;
    private int mDeleteCount;
    //private DuplicateOptionsEnum _doe;
    private boolean _isForWhatsApp;
    ModelMessageDelete _modelContactsDelete;

    //public boolean allChecked;


    //Get a single contact which is added to main ArrayList
    private ArrayList<DataMessageDisplay> getValuesForDisplayOption(
                                    DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataMessageDisplay> finalValues=null;

        switch(displayOption){
            case AllWithPhoneNumber:
                //Get all values allow duplicates
                finalValues= _modelMessageRead.getSmsData_orderby_phoneNumber();
                break;
            case AllWithinOneMonth:
                finalValues= _modelMessageRead.getSmsData_orderby_inOneMonth();

                break;
            case AllReceived:
                finalValues= _modelMessageRead.getSmsData_orderby_received();
                break;
            case AllSent:
                finalValues= _modelMessageRead.getSmsData_orderby_sent();
                break;
        }
        return finalValues;
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataMessageDisplay> RegenerateListOfContacts() {
        ArrayList<DataMessageDisplay> itemsList;
        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return itemsList;
    }

    //Ctor
    private ViewModelMessageDisplay(Context context,
                                     DisplayOptionsEnum defaultOption){
        _context = context;
        _notAvailable = (String)_context.getResources().getText(R.string.general_not_Available);
        _mainList = new Hashtable<>();
        _modelMessageRead = new ModelMessageRead(context);
        _isForWhatsApp=false;
        //Set default value
        _currentDisplayOption=defaultOption;
       // _doe=DuplicateOptionsEnum.NameMatchWithEmailPhone;
    }

    private final Handler mDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case ModelMessageDelete.DELETE_COUNT_UPDATE:
                    mDeleteCount = msg.arg1;
                    break;
                default:
            }
        }
    };

    public boolean getCurrentSortOrder(){
        return _modelMessageRead.getCurrentSortOrder();
    }

    public boolean changeSortOrder(){
        boolean newOrder =  _modelMessageRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    /*public void setDuplicateOption(DuplicateOptionsEnum doe){
        _doe=doe;
    }

    public DuplicateOptionsEnum getDuplicateOption(){
        return _doe;
    }*/

    public int getDeleteCount(){
        return mDeleteCount;
    }

    public void setCurrentDisplayOption(DisplayOptionsEnum value){
        _currentDisplayOption=value;
    }

    public static ViewModelMessageDisplay getInstance(
            Context context){
        if(_instance==null) _instance =
                new ViewModelMessageDisplay(context,
                        DisplayOptionsEnum.AllWithPhoneNumber);
        return _instance;
    }

    public DisplayOptionsEnum getCurrentDisplayOption() {
        return _currentDisplayOption;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataMessageDisplay>
    getCurrentDisplayOptionContacts() {
        //if(_currentDisplayOption==DisplayOptionsEnum.Duplicates)
        //    _mainList.clear(); //Clear main list of everything
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfContacts();
        return _mainList.get(_currentDisplayOption);
    }

    public ArrayList<DataMessageDisplay> getCheckedItems(){
        if(_mainList==null) return null; //Prevent processing
        ArrayList<DataMessageDisplay> checkedItemList = new ArrayList<>();


        if(_mainList.get(_currentDisplayOption)==null)
            RegenerateListOfContacts(); //this is important as in some devices this is getting set to null

        try {
            for (int ii = 0; ii < _mainList.get(_currentDisplayOption).size(); ii++) {
                if (_mainList.get(_currentDisplayOption).get(ii).isChecked)
                    checkedItemList.add(_mainList.get(_currentDisplayOption).get(ii));
            }
        }
        catch(Exception ex)
        {
            SupportFunctions.DebugLog("ViewModelContactsDisplay","getCheckedItems",ex.getMessage());
        }
        return checkedItemList;
    }

    public void InvalidateData(){
        _mainList.clear();
    }

    public void setIsForWhatsApp(){
        _isForWhatsApp=true;
    }

    public boolean getIsForWhatsApp(){
        return _isForWhatsApp;
    }

    public ViewModelFilterOption getFilterOption(){
        return _modelMessageRead.getFilterOption();
    }

    public int DeleteContacts(ArrayList<Long> contactIds){
        if(_modelContactsDelete==null) {
            _modelContactsDelete = new ModelMessageDelete(
                    _context, mDeleteHandler);
        }
        return _modelContactsDelete.DeleteContacts(contactIds);
    }

    public int DeleteContact(Long contactId){
        if(_modelContactsDelete==null) {
            _modelContactsDelete = new ModelMessageDelete(
                    _context, mDeleteHandler);
        }
        return _modelContactsDelete.DeleteMessage(contactId);
    }

    /*public int unDeleteContact(Long contactId){
        if(_modelMessageUpdate ==null) {
            _modelMessageUpdate = new ModelMessageUpdate(_context);
        }
        return _modelMessageUpdate.unDeleteSingleContact(contactId);
    }*/
}
