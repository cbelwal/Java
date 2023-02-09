package com.dasmic.android.lib.contacts.Activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.ActivityOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 1/30/2016.
 */
public class ActivityBaseContact extends AppCompatActivity {
    String _appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppName(savedInstanceState);
    }

    protected void setAppName(Bundle savedInstanceState){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            _appName = extras.getString(ActivityOptions.APP_NAME);
        }
    }

    //Add unique contact Ids
    protected ArrayList<Long> getUniqueContactIdList
    (ArrayList<DataContactDisplay> contacts){
        if (contacts==null) return null;
        ArrayList<Long> contactIdList = new ArrayList<Long>();
        for(DataContactDisplay cdd: contacts){
            Long cid= new Long(cdd.getContactId());
            if(!contactIdList.contains(cid))
                contactIdList.add(cid);
        }
        return contactIdList;
    }

    protected ArrayList<Long> getUniqueRawContactIdList
            (ArrayList<DataContactDisplay> contacts){
        ArrayList<Long> contactIdList = new ArrayList<Long>();
        for(DataContactDisplay cdd: contacts){
            Long cid= new Long(cdd.getRawContactId());
            if(!contactIdList.contains(cid))
                contactIdList.add(cid);
        }
        return contactIdList;
    }

    protected void showFullContactCard(long contactID){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
            intent.setData(uri);
            startActivity(intent);
        }
        catch(Exception ex)
        {

        }
    }

}
