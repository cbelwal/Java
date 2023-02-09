package com.dasmic.android.lib.contacts.ViewModel;

import android.content.Context;

import com.dasmic.android.lib.contacts.Model.ModelContactsUpdate;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/8/2015.
 */
public class ViewModelOperations {
    ModelContactsUpdate _modelContactsUpdate;

    public ViewModelOperations(Context context){
        _modelContactsUpdate = new ModelContactsUpdate(context);
    }

    public void UpdateSendToVoiceMail(ArrayList<Long> contactIds,
                                     boolean setToValue){
        _modelContactsUpdate.setSendtoVoiceMail(contactIds, setToValue);
    }

    public void UpdateStarred(ArrayList<Long> contactIds,
                              boolean setToValue){
        _modelContactsUpdate.setStarred(contactIds, setToValue);
    }

    public void resetTimesCalled(ArrayList<Long> contactIds){
        _modelContactsUpdate.resetTimesContacted(contactIds);
    }

    public void setLastContactToNow(ArrayList<Long> contactIds){
        _modelContactsUpdate.setTimesContactedToNow(contactIds);
    }
}
