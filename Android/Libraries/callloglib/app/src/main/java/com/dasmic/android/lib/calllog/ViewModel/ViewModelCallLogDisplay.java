package com.dasmic.android.lib.calllog.ViewModel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;
import com.dasmic.android.lib.calllog.Enum.DisplayCallLogCountEnum;
import com.dasmic.android.lib.calllog.Enum.DisplayCallLogEnum;
import com.dasmic.android.lib.calllog.Model.ModelCallLogDelete;
import com.dasmic.android.lib.calllog.Model.ModelCallLogRead;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;
import java.util.Hashtable;
import com.dasmic.android.lib.calllog.R;

/**
 * Created by Chaitanya Belwal on 7/16/2016.
 */
public class ViewModelCallLogDisplay {
    private Context _context;
    private ModelCallLogRead _modelCallLogRead;
    //private ModelContactsUpdate _modelContactsUpdate;
    private Hashtable<DisplayCallLogEnum,ArrayList<DataCallLogDisplay>> _mainList;
    private String _notAvailable;
    private static ViewModelCallLogDisplay _instance;
    private DisplayCallLogEnum  _currentDisplayOption;
    private  DisplayCallLogCountEnum _currentDisplayCount;
    private ViewModelFilterOption _vmFilterOption;
    private int mDeleteCount;
    //private DuplicateOptionsEnum _doe;
    private boolean _isForWhatsApp;
    protected boolean _internalDBChange; //For contentObserver to detect internal changes
    ModelCallLogDelete _modelCallLogDelete;


    public boolean getInternalDBChange(){return _internalDBChange;}
    public void setInternalDBChange(boolean value){_internalDBChange=value;}

    //Get a single contact which is added to main ArrayList
    private ArrayList<DataCallLogDisplay> getValuesForDisplayOption(
            DisplayCallLogEnum displayOption){
        String id;
        ArrayList<DataCallLogDisplay> finalValues=null;

        switch(displayOption){
            case AllCalls:
                //Get all values allow duplicates
                finalValues= _modelCallLogRead.getAllCallLogs(getContactDisplayCount());
                break;
            case MissedCalls:
                finalValues= _modelCallLogRead.getMissedCallLogs(getContactDisplayCount());
                break;
            case IncomingCalls:
                finalValues= _modelCallLogRead.getIncomingCallLogs(getContactDisplayCount());
                break;
            case OutgoingCalls:
                finalValues= _modelCallLogRead.getOutgoingCallLogs(getContactDisplayCount());
                break;
            case SortedByCallDuration:
                finalValues= _modelCallLogRead.getAllCallsSortedByDuration(getContactDisplayCount());
                break;
            case SortedByGeocodedLocation:
                finalValues= _modelCallLogRead.getAllCallsSortedByGeoLocation(getContactDisplayCount());
                break;
            default:
                finalValues= _modelCallLogRead.getAllCallLogs(getContactDisplayCount());
                break;

        }
        return finalValues;
    }

    //Modifies DisplayOptions and creates new instance
    private ArrayList<DataCallLogDisplay> RegenerateListOfContacts() {
        ArrayList<DataCallLogDisplay> itemsList;
        itemsList = getValuesForDisplayOption(_currentDisplayOption);
        _mainList.put(_currentDisplayOption,itemsList);
        return itemsList;
    }

    //Ctor
    private ViewModelCallLogDisplay(Context context,
                                     DisplayCallLogEnum defaultOption){
        _context = context;
        _notAvailable = (String)_context.getResources().
                getText(R.string.general_not_Available);
        _mainList = new Hashtable<>();
        _vmFilterOption =new ViewModelFilterOption();//Should be before _modelCallLogRead
        _modelCallLogRead = new ModelCallLogRead(context,_vmFilterOption);
        _isForWhatsApp=false;
        //Set default value
        _currentDisplayOption=defaultOption;
        _currentDisplayCount=DisplayCallLogCountEnum.View100;
        _internalDBChange =false;

    }

    private final Handler mDeleteHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            switch (msg.what) {
                //case ModelContactsDelete.DELETE_COUNT_UPDATE:
                //    mDeleteCount = msg.arg1;
                //    break;
                default:
            }
        }
    };

    //Mapping between Enum and
    private int getContactDisplayCount(){
        int count=0;
        switch(_currentDisplayCount){
            case View100:
                count=100;
                break;
            case View500:
                count=500;
                break;
            case ViewAll :
                count=-1;
                break;
            default:
                count=100;
        }
        return count;
    }

    /*public void setDuplicateOption(DuplicateOptionsEnum doe){
        _doe=doe;
    }

    public DuplicateOptionsEnum getDuplicateOption(){
        return _doe;
    }*/

    public boolean getCurrentSortOrder(){
        return _modelCallLogRead.getCurrentSortOrder();
    }

    public boolean changeSortOrder(){
        boolean newOrder = _modelCallLogRead.changeSortOrder();
        //Make sure to Invalidate Data as sort order is invalid now
        InvalidateData();
        return newOrder;
    }

    public ViewModelFilterOption getFilterData(){
        return _vmFilterOption;
    }

    public void setCurrentDisplayOption(DisplayCallLogEnum value){
        _currentDisplayOption=value;
    }

    public void setCurrentDisplayCount(DisplayCallLogCountEnum value){
        _currentDisplayCount=value;

    }

    public static ViewModelCallLogDisplay getInstance(
            Context context){
        if(_instance==null) _instance =
                new ViewModelCallLogDisplay(context,
                        DisplayCallLogEnum.AllCalls);
        return _instance;
    }

    public static ViewModelCallLogDisplay getInstance(){
        return _instance;
    }

    public DisplayCallLogEnum getCurrentDisplayOption() {
        return _currentDisplayOption;
    }

    public DisplayCallLogCountEnum getCurrentDisplayCount() {
        return _currentDisplayCount;
    }

    //Only modifies the DisplayOptions
    public ArrayList<DataCallLogDisplay>
        getCurrentDisplayOptionContacts() {
        //if(_currentDisplayOption==DisplayCallLogEnum.Duplicates)
        //    _mainList.clear(); //Clear main list of everything
        if(_mainList.get(_currentDisplayOption)==null)
            return RegenerateListOfContacts();
        return _mainList.get(_currentDisplayOption);
    }

    public ArrayList<DataCallLogDisplay> getCheckedItems(){
        if(_mainList==null) return null; //Prevent processing
        ArrayList<DataCallLogDisplay> checkedItemList = new ArrayList<>();


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
            SupportFunctions.DebugLog("ViewModelCallLogDisplay","getCheckedItems",ex.getMessage());
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

    /*public ViewModelFilterOption getFilterOption(){
        return _modelCallLogRead.getFilterOption();
    }*/

    public int DeleteContacts(ArrayList<Long> contactIds){
        /*if(_modelCallLogDelete==null) {
            _modelCallLogDelete = new ModelContactsDelete(
                    _context, mDeleteHandler);
        }
        return _modelCallLogDelete.deleteContacts(contactIds);*/
        return 0;
    }

    public int deleteCallLog(Long callLogId){
        if(_modelCallLogDelete==null) {
            _modelCallLogDelete = new ModelCallLogDelete(
                    _context, mDeleteHandler);
        }
        return _modelCallLogDelete.deleteSingle(callLogId);
    }

    public int unDeleteContact(Long contactId){
        /*if(_modelContactsUpdate==null) {
            _modelContactsUpdate = new ModelContactsUpdate(_context);
        }
        return _modelContactsUpdate.unDeleteSingleContact(contactId);*/
        return 0;
    }
}

