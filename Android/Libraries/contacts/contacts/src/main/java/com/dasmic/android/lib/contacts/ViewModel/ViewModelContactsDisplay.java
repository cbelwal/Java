package com.dasmic.android.lib.contacts.ViewModel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.contacts.Enum.DuplicateOptionsEnum;
import com.dasmic.android.lib.contacts.Enum.FilterOptionsEnum;
import com.dasmic.android.lib.contacts.Model.ModelContactsRead;
import com.dasmic.android.lib.contacts.Model.ModelContactsDelete;
import com.dasmic.android.lib.contacts.Model.ModelContactsUpdate;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Chaitanya Belwal on 8/6/2015.
 */
public class ViewModelContactsDisplay {
    private Context _context;
    private ModelContactsRead _modelContactsRead;
    private ModelContactsUpdate _modelContactsUpdate;
    private Hashtable<DisplayOptionsEnum,ArrayList<DataContactDisplay>> _mainList;
    private String _notAvailable;
    private static ViewModelContactsDisplay _instance;
    private DisplayOptionsEnum _currentDisplayOption;
    private int mDeleteCount;
    private DuplicateOptionsEnum _doe;
    private boolean _isForWhatsApp;
    ModelContactsDelete _modelContactsDelete;

    //public boolean allChecked;


    //Get a single contact which is added to main ArrayList
    private ArrayList<DataContactDisplay>
                    getValuesForDisplayOption(
                        DisplayOptionsEnum displayOption){
        String id;
        ArrayList<DataContactDisplay> finalValues=null;

        switch(displayOption){
            case AllInformation:
                //Get all values allow duplicates
                finalValues= _modelContactsRead.getAllInformation(false);
                break;
            case AllWithContactCount:
                finalValues= _modelContactsRead.
                        getAllInformationWithContactCount(true);
                break;
            case AllWithLastContact:
                finalValues= _modelContactsRead.
                        getAllInformationWithLastContact(true);
                break;
            case PhoneNumber:
                finalValues= _modelContactsRead.getPhoneNumbers(true);
                break;
            case PhoneNumberWithName:
                finalValues= _modelContactsRead.getPhoneNumbersWithName(false);
                break;
            case Email:
                finalValues= _modelContactsRead.getEmails(true);
                break;

            case Duplicates:
                if(_isForWhatsApp)
                    finalValues= _modelContactsRead.getAllInformationWhatsApp(false);
                else
                    finalValues= _modelContactsRead.getAllInformation(false);
                break;
            case Deleted:
                finalValues= _modelContactsRead.getDeleted(false);
                break;
            case AllInformationWhatsApp:
                finalValues= _modelContactsRead.getAllInformationWhatsApp(false);
                break;

        }
        return finalValues;
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataContactDisplay> RegenerateListOfContacts() {
        ArrayList<DataContactDisplay> itemsList;
        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return itemsList;
    }

    //Ctor
    private ViewModelContactsDisplay(Context context,
                                     DisplayOptionsEnum defaultOption){
        _context = context;
        _notAvailable = (String)_context.getResources().getText(R.string.general_not_Available);
        _mainList = new Hashtable<>();
        _modelContactsRead = new ModelContactsRead(context);
        _isForWhatsApp=false;
        //Set default value
        _currentDisplayOption=defaultOption;
        _doe=DuplicateOptionsEnum.NameMatchWithEmailPhone;
    }

    private final Handler mDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                case ModelContactsDelete.DELETE_COUNT_UPDATE:
                    mDeleteCount = msg.arg1;
                    break;
                default:
            }
        }
    };


    public boolean getCurrentSortOrder(){
        return _modelContactsRead.getCurrentSortOrder();
    }

    public boolean changeSortOrder(){
        boolean newOrder =  _modelContactsRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    public void setDuplicateOption(DuplicateOptionsEnum doe){
        _doe=doe;
    }

    public DuplicateOptionsEnum getDuplicateOption(){
        return _doe;
    }

    public int getDeleteCount(){
        return mDeleteCount;
    }

    public void setCurrentDisplayOption(DisplayOptionsEnum value){
        _currentDisplayOption=value;
    }

    public static ViewModelContactsDisplay getInstance(
            Context context){
        if(_instance==null) _instance =
                new ViewModelContactsDisplay(context,
                        DisplayOptionsEnum.AllWithLastContact);
        return _instance;
    }

    public DisplayOptionsEnum getCurrentDisplayOption() {
        return _currentDisplayOption;
    }


    //Only modifies the DisplayOptions
    public ArrayList<DataContactDisplay>
                getCurrentDisplayOptionContacts() {
        if(_currentDisplayOption==DisplayOptionsEnum.Duplicates)
            _mainList.clear(); //Clear main list of everything
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfContacts();
        return _mainList.get(_currentDisplayOption);
    }

    public ArrayList<DataContactDisplay> getCheckedItems(){
        if(_mainList==null) return null; //Prevent processing
        ArrayList<DataContactDisplay> checkedItemList = new ArrayList<>();


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
        return _modelContactsRead.getFilterOption();
    }

    public int DeleteContacts(ArrayList<Long> contactIds){
        if(_modelContactsDelete==null) {
            _modelContactsDelete = new ModelContactsDelete(
                    _context, mDeleteHandler);
        }
        return _modelContactsDelete.DeleteContacts(contactIds);
    }

    public int DeleteContact(Long contactId){
        if(_modelContactsDelete==null) {
            _modelContactsDelete = new ModelContactsDelete(
                    _context, mDeleteHandler);
        }
        return _modelContactsDelete.DeleteContact(contactId);
    }

    public int unDeleteContact(Long contactId){
        if(_modelContactsUpdate==null) {
            _modelContactsUpdate = new ModelContactsUpdate(_context);
        }
        return _modelContactsUpdate.unDeleteSingleContact(contactId);
    }
}
