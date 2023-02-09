package com.dasmic.android.lib.contacts.Data;

import android.net.Uri;
import android.provider.ContactsContract;

/**
 * Created by Chaitanya Belwal on 8/6/2015.
 */
public class DataContactDisplay {
    private long _contactId;
    private long _rawContactId;
    private String _primaryValue;
    private Uri _pictureURI;
    private Uri _thumbURI;
    private String _lookupId;
    private String _secondaryValue;
    private boolean _hasPhoneNumbers;
    private boolean _isInFavorites;
    private long _lastContactTime;

    public boolean isDuplicateMaster;
    public boolean isDuplicateSlave;
    public boolean isChecked;
    public int colorDuplicates;

    @Override
    public String toString() {
        return _primaryValue;
    }


    public DataContactDisplay(long contactId, long rawContactId, String lookupId,
                              Uri pictureURI, Uri thumbURI,
                              String primaryValue,
                              String secondaryValue){
        super();
        _contactId = contactId;
        _rawContactId = rawContactId;
        _primaryValue = primaryValue;
        _pictureURI = pictureURI;
        _thumbURI = thumbURI;
        _lookupId = lookupId;
        _secondaryValue = secondaryValue;
        isDuplicateMaster=false;
        isDuplicateSlave=false;
        colorDuplicates=-1; //Should be -ve, very important
    }

    public void setLastContactTime(long value){_lastContactTime=value;}
    public long getLastContactTime(){return _lastContactTime;}
    public boolean getInFavorites(){return _isInFavorites;}
    public void setInFavorites(boolean value){_isInFavorites=value;}
    public void setHasPhoneNumbers(boolean value){_hasPhoneNumbers=value;}
    public boolean getHasPhoneNumbers(){return _hasPhoneNumbers;}
    public long getContactId(){return _contactId;}
    public long getRawContactId(){return _rawContactId;}
    public Uri getPictureUri(){
        return _pictureURI;
    }
    public String getLookupId(){
        return _lookupId;
    }
    public String getSecondaryValue(){
        return  _secondaryValue;
    }
    public String getPrimaryValue(){
        return _primaryValue;
    }

    public Uri getThumbUri(){
        return _thumbURI;
    }
    public Uri getContactUri(){
        Uri uri=ContactsContract.Contacts.getLookupUri(
                _contactId,
                _lookupId);
        return uri;
    }
}
