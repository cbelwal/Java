package com.dasmic.android.lib.contacts.Model;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import com.dasmic.android.lib.contacts.Data.DataAllPostalAddress;
import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Interface.IDataValuePair;
import com.dasmic.android.lib.contacts.Interface.IDataValueThreeTuple;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;
import com.dasmic.android.lib.contacts.ViewModel.ViewModelFilterOption;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ModelContactsRead {
    private Context _context;
    private String _sortOrder;
    private ViewModelFilterOption _filterOption;
    private final String MIME_WhatsApp="vnd.android.cursor.item/vnd.com.whatsapp.profile";

    private String getFilterFromOptions(){
        String filterString="";

        switch(_filterOption.getSelectedOption()){
            case ShowOnlyWithContactedMoreThanOnce:
                filterString = ContactsContract.Data.TIMES_CONTACTED + " > 1";
                break;
            case ShowOnlyWithContactedNone:
                filterString = ContactsContract.Data.TIMES_CONTACTED + " < 1";
                break;
            case ShowOnlyWithLessThan7DayContact:
                filterString = ContactsContract.Data.LAST_TIME_CONTACTED + " > " +
                        String.valueOf(DateOperations.get7DayMilliseconds());
                break;
            case ShowOnlyWithLessThan30DayContact:
                filterString = ContactsContract.Data.LAST_TIME_CONTACTED + " > " +
                        String.valueOf(DateOperations.get30DayMilliseconds());
                break;
            case ShowOnlyWithLessThan1YearContact:
                filterString = ContactsContract.Data.LAST_TIME_CONTACTED + " > " +
                        String.valueOf(DateOperations.getYearMilliseconds());
                break;
            case ShowOnlyWithMoreThan1YearContact:
                filterString = ContactsContract.Data.LAST_TIME_CONTACTED + " < " +
                        String.valueOf(DateOperations.getYearMilliseconds());
                break;
            case ShowOnlySendToVoiceMailList:
                filterString = ContactsContract.Data.SEND_TO_VOICEMAIL + " NOT LIKE '0'";
                break;
            case ShowOnlyDoNotSendToVoiceMailList:
                filterString = ContactsContract.Data.SEND_TO_VOICEMAIL + " NOT LIKE '1'";// +
                break;
            case ShowOnlyWithPictures:
                filterString = ContactsContract.Data.PHOTO_THUMBNAIL_URI + " <> ''";
                break;
            case ShowOnlyStarred:
                filterString = ContactsContract.Data.STARRED + " NOT LIKE '0'";
                break;
            case ShowOnlyInNonVisibleGroups:
                filterString = ContactsContract.Data.IN_VISIBLE_GROUP + " LIKE '0'";// +
                break;

            case None:
                filterString=ContactsContract.Data._ID + " <> ''"; //Will always be satisfied
                break;
            default:
                filterString=ContactsContract.Data._ID + " <> ''"; //Will always be satisfied
                break;
        }
        return filterString;

    }

    private ArrayList<DataContactDisplay> getQueryOn_Data_Table_Sorted_Asc(
            String[] addProjection,
            String basicFilter,
            boolean isPrimaryValueUnique){

        String order = addProjection[0] + " ASC";
        return getDisplayData_Data_Table(addProjection,
                basicFilter, order, "",
                isPrimaryValueUnique);
    }

    //--------- Main Function to fetch data
    private ArrayList<DataContactDisplay> getDisplayData_Data_Table(
            String[] addProjection,
            String basicFilter,
            String order,
            String secondaryValueSuffix,
            boolean isUniqueContactId)
    {
        Uri URI =  ContactsContract.Data.CONTENT_URI;
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.Data.CONTACT_ID);//0
        projection.add(ContactsContract.Data.RAW_CONTACT_ID);//1
        projection.add(ContactsContract.Data.LOOKUP_KEY);//2
        projection.add(ContactsContract.Data.PHOTO_URI);//3
        projection.add(ContactsContract.Data.PHOTO_THUMBNAIL_URI); //4
        projection.add(ContactsContract.Data.HAS_PHONE_NUMBER);
        projection.add(ContactsContract.Data.STARRED);
        projection.add(ContactsContract.Data.LAST_TIME_CONTACTED);



        for(String ap:addProjection)
            projection.add(ap);


        //Filter Section --------------------
        String filter="";
        if(basicFilter != "")
            filter = basicFilter + " AND ";
        filter = filter + "(" + addProjection[0] + " <> '')" + //Dont add blank
                " AND " +
                getFilterFromOptions();
        //------------------------------------------------
        ArrayList<DataContactDisplay> values = new ArrayList<>();

        HashSet<Long> hash = new HashSet<>();
        boolean addFlag;
        Context context = _context;
        ContentResolver cr = context.getContentResolver();

        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);
        //Main query
        Cursor cur = cr.query(URI,
                projectionArray,
                filter,null, order);
                //filter, null, order);

        Uri imageURI;
        Uri thumbURI;

        if(cur==null) return values;
        if (cur.moveToFirst()) {
            do {
                try {
                    long contactId = cur.getLong(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    long rawContactId = cur.getLong(cur.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID));
                    String lookupId = cur.getString(cur.getColumnIndex(ContactsContract.Data.LOOKUP_KEY));
                    if (cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_URI)) != null) {
                        imageURI = Uri.parse(ContactsContract.Data.PHOTO_URI);
                    } else {
                        imageURI = null;
                    }
                    if (cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI)) != null) {
                        thumbURI = Uri.parse(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
                    } else {
                        thumbURI = null;
                    }

                    String primaryValue = cur.getString(cur.getColumnCount() - 2);
                    String secondaryValue = cur.getString(cur.getColumnCount() - 1);
                    //See if secondary Value needs formatting
                    if (addProjection.length > 1)
                        if (addProjection[1] == ContactsContract.Data.LAST_TIME_CONTACTED) {
                            secondaryValue =
                                    DateOperations.getFormattedDate(Long.decode(secondaryValue));
                        }

                    // keep unique
                    addFlag = true;
                    if (isUniqueContactId) {
                        if (hash.add(contactId))// (primaryValue.toLowerCase()))
                            addFlag = true;
                        else addFlag = false;
                    }

                    //SupportFunctions.DebugLog("ModelRead",
                    //        "displayData","Writing for RawContactId:"+rawContactId);

                    if (addFlag) {
                        DataContactDisplay singleContact =
                                new DataContactDisplay(contactId,
                                        rawContactId,
                                        lookupId,
                                        imageURI,
                                        thumbURI,
                                        primaryValue,
                                        secondaryValue + " " + secondaryValueSuffix);

                        singleContact.setHasPhoneNumbers(cur.getInt(cur.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER)) == 1);
                        singleContact.setInFavorites(cur.getInt(cur.getColumnIndex(ContactsContract.Data.STARRED)) == 1);
                        singleContact.setLastContactTime(cur.getLong(cur.getColumnIndex(ContactsContract.Data.LAST_TIME_CONTACTED)));

                        values.add(singleContact);
                    }
                }
                catch(Exception ex){ //Catch exception in a single contact, move to next
                    SupportFunctions.DebugLog("ModelRead",
                            "displayData",ex.getMessage());
                }

            } while (cur.moveToNext());
        }

        cur.close();
        return values;
    }


    /*private ArrayList<DataContactDisplay> getQueryOn_RawContacts_Table_Sorted_Asc(
            String[] addProjection,
            String basicFilter,
            boolean isPrimaryValueUnique){

        String order = addProjection[0] + " ASC";
        return getDisplayData_for_deleted_contacts(addProjection,
                basicFilter, order, "",
                isPrimaryValueUnique);
    }*/

    //--------- Function is to be used only for delete data
    private ArrayList<DataContactDisplay> getDisplayData_for_deleted_contacts()
    {
        Uri URI =  ContactsContract.RawContacts.CONTENT_URI;

        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.RawContacts.CONTACT_ID);//0
        projection.add(ContactsContract.RawContacts._ID);//1
        projection.add(ContactsContract.RawContacts.DELETED);//3
        projection.add(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);//4


        String order = ContactsContract.Data.DISPLAY_NAME + " ASC";

        String filter = "(" +
                ContactsContract.RawContacts.DELETED + " LIKE '" +
                "1" + "'" + ")";
        //Filter option will be taken by when we get DCD object
        //------------------------------------------------
        ArrayList<DataContactDisplay> values = new ArrayList<>();

        HashSet<String> hash = new HashSet<>();
        boolean addFlag;
        Context context = _context;
        ContentResolver cr = context.getContentResolver();

        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);

        boolean isPrimaryValueUnique=true;


        //Main query
        Cursor cur=null;
        try {
            cur = cr.query(URI,
                    projectionArray,
                    filter, null,order);// order);
        }
        catch(Exception ex){
            SupportFunctions.DebugLog("ModelRead",
                    "getDisplayData_for_deleted_contacts","Exception:" + ex.getMessage());
        }

        Uri imageURI;
        Uri thumbURI;

        if(cur==null) return values;
        if (cur.moveToFirst()) {
            do {
                try {
                    long contactId = cur.getLong(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    long rawContactId = cur.getLong(cur.getColumnIndex(ContactsContract.Data._ID));
                    String lookupId="";
                    imageURI = null;
                    thumbURI = null;

                    String primaryValue = cur.getString(cur.getColumnCount() - 1);
                    String secondaryValue = "NA";

                        // keep unique
                        addFlag = true;
                        if (isPrimaryValueUnique && primaryValue != null) {
                            if (hash.add(primaryValue.toLowerCase()))
                                addFlag = true;
                            else addFlag = false;
                        }

                        if (addFlag && primaryValue != null) {
                            DataContactDisplay singleContact =
                                    new DataContactDisplay(contactId,
                                            rawContactId,
                                            lookupId,
                                            imageURI,
                                            thumbURI,
                                            primaryValue,
                                            secondaryValue);
                            values.add(singleContact);
                        }
                }
                catch(Exception ex){ //Catch exception in a single contact, move to next
                    SupportFunctions.DebugLog("ModelRead",
                            "displayData","Exception:" + ex.getMessage());
                }

            } while (cur.moveToNext());
        }

        cur.close();
        return values;
    }

    private Cursor getReadCursor_Data_Table(ArrayList<String> projection,
                                            long contactID, String mimeType,
                                            boolean isRawContact){
        ContentResolver cr = _context.getContentResolver();
        String[] projectionArray = new String[projection.size()];
        projectionArray = projection.toArray(projectionArray);
        String idColumn;

        if(isRawContact)
            idColumn=ContactsContract.Data.RAW_CONTACT_ID;
        else
            idColumn=ContactsContract.Data.CONTACT_ID;


        String filterQuery= ContactsContract.Data.MIMETYPE + " LIKE '" +
                mimeType + "' AND " +
                idColumn + "='" +
                String.valueOf(contactID) +"'";

        //--------- Execute Query
        Cursor cur;
        try {
            cur = cr.query(ContactsContract.Data.CONTENT_URI,
                    projectionArray,
                    filterQuery,
                    null, null);
        }
        catch(Exception ex){
            Log.i("CKIT","Error in Query::ReadCursor: " +ex.getMessage());
            throw new RuntimeException(ex);
        }

        return cur;
    }


    //Pass values by reference
    private void StoreSingleContactPostalAddress(
            long contactID,
            DataAllPostalAddress apa,
            boolean isRawContact) {
        int idx=0;
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.REGION);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
        projection.add(ContactsContract.CommonDataKinds.StructuredPostal.TYPE);

        Cursor cur = getReadCursor_Data_Table(projection,
                contactID,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                isRawContact);
        //---------- End query

        //Start Data Storage - All Email Addresses
        if (cur.moveToFirst()) {
            do {
                apa.Add(
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)),
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)),
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)),
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)),
                        cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)),
                        cur.getInt(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                );

            } while (cur.moveToNext());
        }

        cur.close();
    }

    private void storeSingleContactDataValueThreeTuple(long contactID,
                                                 IDataValueThreeTuple valueTriplet,
                                                 String data,
                                                 String type,
                                                 String label,
                                                 String mimeType,
                                                 boolean isRawContact)
    {

        ArrayList<String> projection = new ArrayList<>();
        projection.add(data);
        projection.add(type);
        projection.add(label);

        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection,contactID,
                mimeType,isRawContact);
        //---------- End query


        //Start data Storage - All Email Addresses
        if (cur.moveToFirst()) {
            do {
                valueTriplet.Add(
                        cur.getInt(cur.getColumnIndex(type)),
                        cur.getString(cur.getColumnIndex(data)),
                        cur.getString(cur.getColumnIndex(label))
                );
            }
            while (cur.moveToNext());
        }
        cur.close();
    }


    //Pass by Reference
    private void StoreSingleContactDataValuePair(long contactID,
                                                 IDataValuePair valuePair,
                                                 String data, String type,
                                                 String mimeType,
                                                 boolean isRawContact)
    {

        ArrayList<String> projection = new ArrayList<>();
        projection.add(data);
        projection.add(type);

        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection,contactID,
                                    mimeType,isRawContact);
        //---------- End query


        //Start data Storage - All Email Addresses
        if (cur.moveToFirst()) {
            do {
                valuePair.Add(
                        cur.getInt(cur.getColumnIndex(type)),
                        cur.getString(cur.getColumnIndex(data))
                );
            } while (cur.moveToNext());
        }
        cur.close();
    }

    private void StoreSingleContactNameAndPhoto(
                                long contactID,
                                DataContactTransfer singleContact,
                                boolean isRawContact){
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.PREFIX);
        projection.add(ContactsContract.CommonDataKinds.StructuredName.SUFFIX);
        projection.add(ContactsContract.Data.PHOTO_URI);

        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection, contactID,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                isRawContact);
        //---------- End query

        //Start Data Storage
        if (cur.moveToFirst()) {
                //Given Name
                 singleContact.setName(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)),
                         cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)),
                         cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)),
                         cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)),
                         cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)),
                         cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)));

            if(cur.getString(cur.getColumnIndex(ContactsContract.Data.PHOTO_URI))
                    != null){
                Uri uri = Uri.parse(cur.getString(
                        cur.getColumnIndex(ContactsContract.Data.PHOTO_URI)));
                singleContact.setPhoto(
                        SupportFunctions.getBytesFromUri(uri, _context));

            }
        }
        cur.close();
    }

    private void StoreSingleContactOrg(
            long contactID,
            DataContactTransfer singleContact,
            boolean isRawContact){
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.CommonDataKinds.Organization.CONTACT_ID);
        projection.add(ContactsContract.CommonDataKinds.Organization.COMPANY);
        projection.add(ContactsContract.CommonDataKinds.Organization.DEPARTMENT);
        projection.add(ContactsContract.CommonDataKinds.Organization.TITLE);

        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection, contactID,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                isRawContact);
        //---------- End query

        //Start Data Storage
        if (cur.moveToFirst()) {
            //Given Name
            singleContact.setOrganization(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)),
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT)),
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
        }
        cur.close();
    }

    private void StoreSingleContactNotes(
            long contactID,
            DataContactTransfer singleContact,
            boolean isRawContact){
        ArrayList<String> projection = new ArrayList<>();

        projection.add(ContactsContract.CommonDataKinds.Note.CONTACT_ID);
        projection.add(ContactsContract.CommonDataKinds.Note.NOTE);

        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection, contactID,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                isRawContact);
        //---------- End query

        //Start Data Storage
        if (cur.moveToFirst()) {
            //Given Name
            singleContact.setNote(
                    cur.getString(
                    cur.getColumnIndex(
                            ContactsContract.CommonDataKinds.Note.NOTE)));

        }
        cur.close();
    }

    private void StoreSingleContactMiscData(long contactID,
                                            DataContactTransfer singleContact,
                                            int flag,
                                            boolean isRawContact){
        ArrayList<String> projection = new ArrayList<>();
        projection.add(ContactsContract.Data.TIMES_CONTACTED);
        projection.add(ContactsContract.Data.LAST_TIME_CONTACTED);
        projection.add(ContactsContract.Data.STARRED);


        //--------- Execute Query
        Cursor cur = getReadCursor_Data_Table(projection, contactID,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                isRawContact);
        //---------- End query

        //Start Data Storage
        if (cur.moveToFirst()) {
            //Given Name
            if((flag&8) > 0)
                singleContact.setTimesContacted(cur.getInt(cur.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.TIMES_CONTACTED)));
            if((flag&16) > 0)
                singleContact.setLastTimeContacted(cur.getLong(cur.getColumnIndex(
                        ContactsContract.CommonDataKinds.StructuredName.LAST_TIME_CONTACTED)));
            if((flag&32) > 0)
                singleContact.setInFavorites(cur.getInt(cur.getColumnIndex(
                        ContactsContract.CommonDataKinds.StructuredName.STARRED)));
        }
        cur.close();
    }

    //Gets Transfer Data for Whats App only
    private ArrayList<DataContactTransfer> getTransferData_WhatsApp(
            ArrayList<Long>  contactIDList,int flag, boolean isRawContact) {
        ArrayList<DataContactTransfer> values = new ArrayList<>();

        for(int idx=0;idx<contactIDList.size();idx++){
            DataContactTransfer singleContact = new
                    DataContactTransfer(contactIDList.get(idx));

            //Given Name
            StoreSingleContactNameAndPhoto(singleContact.getContactId(),
                    singleContact,isRawContact);
            StoreSingleContactOrg(singleContact.getContactId(),
                    singleContact,isRawContact);
            StoreSingleContactNotes(singleContact.getContactId(),
                    singleContact,isRawContact);

            //Store Whats App
            StoreSingleContactDataValuePair(singleContact.getContactId(),
                    singleContact.getWhatsApp(),
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    MIME_WhatsApp,
                    isRawContact);


            //Phone numbers
            if((flag&1)>0) //Do Phone Numbers
                StoreSingleContactDataValuePair(singleContact.getContactId(),
                        singleContact.getPhoneNumbers(),
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        isRawContact);

            if((flag&2)>0) //Email
                StoreSingleContactDataValuePair(singleContact.getContactId(),
                        singleContact.getEmailAddresses(),
                        ContactsContract.CommonDataKinds.Email.ADDRESS,
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        isRawContact);

            if((flag&4)>0)//Postal Address
                StoreSingleContactPostalAddress(singleContact.getContactId(),
                        singleContact.getPostalAddress(),isRawContact);

            if((flag&56)>0) //Times contacted, last contact, starred
                StoreSingleContactMiscData(singleContact.getContactId(),
                        singleContact, flag,isRawContact);

            values.add(singleContact);
        }
        return values;
    }


    //Do for single contact
    public DataContactTransfer getTransferData_Single(
            Long  contactID,int flag, boolean isRawContact) {

        DataContactTransfer singleContact = new
                DataContactTransfer(contactID);
        //Given Name
        StoreSingleContactNameAndPhoto(singleContact.getContactId(),
                singleContact,isRawContact);
        StoreSingleContactOrg(singleContact.getContactId(),
                singleContact,isRawContact);
        StoreSingleContactNotes(singleContact.getContactId(),
                singleContact,isRawContact);

        //Store Events
        storeSingleContactDataValueThreeTuple(singleContact.getContactId(),
                singleContact.getEvents(),
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.LABEL,
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                isRawContact);

        //Phone numbers
        if((flag&1)>0) //Do Phone Numbers
            StoreSingleContactDataValuePair(singleContact.getContactId(),
                    singleContact.getPhoneNumbers(),
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                    isRawContact);

        if((flag&2)>0) //Email
            StoreSingleContactDataValuePair(singleContact.getContactId(),
                    singleContact.getEmailAddresses(),
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Email.TYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                    isRawContact);

        if((flag&4)>0)//Postal Address
            StoreSingleContactPostalAddress(singleContact.getContactId(),
                    singleContact.getPostalAddress(),isRawContact);

        if((flag&56)>0) //Times contacted, last contact, starred
            StoreSingleContactMiscData(singleContact.getContactId(),
                    singleContact, flag,isRawContact);

        return singleContact;
    }


    //There is no limit on number of contacts that can be Read
    private ArrayList<DataContactTransfer> getTransferData_Multiple(
            ArrayList<Long>  contactIDList,int flag, boolean isRawContact) {

        ArrayList<DataContactTransfer> values = new ArrayList<>();

        for(int idx=0;idx<contactIDList.size();idx++){
            DataContactTransfer singleContact = getTransferData_Single(contactIDList.get(idx),
                    flag,isRawContact);
                values.add(singleContact);
        }
        return values;
    }


    //Changes the sort order returns true if current sort order is Descending
    public boolean changeSortOrder()
    {
        if(_sortOrder.equals(" DESC"))
            _sortOrder=" ASC";
        else
            _sortOrder=" DESC";

        return  getCurrentSortOrder();
    }

    public boolean getCurrentSortOrder() {
        return (_sortOrder.equals(" DESC")) ? true : false;
    }

    public ModelContactsRead(Context context) {
        _context=context;
        _filterOption = new ViewModelFilterOption();
        _sortOrder = " ASC"; //Default sort order, do not change
    }

    public ViewModelFilterOption getFilterOption(){
        return _filterOption;
    }

    //Get All Info
    public ArrayList<DataContactDisplay> getAllInformation(
            boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA1};

        String filter = "(" +
                        ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                        " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" +
                        " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'" +
                        ")";
        String order = PROJECTION[0] + _sortOrder;

        return getDisplayData_Data_Table( PROJECTION, filter,
                order, "", isPrimaryValueUnique);

    }

    //Get All Info for Whats App
    public ArrayList<DataContactDisplay> getAllInformationWhatsApp(
            boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA1};

        String filter = "(" +
                ContactsContract.Data.MIMETYPE + " LIKE '" + MIME_WhatsApp
                            + "'" +")";

        return getQueryOn_Data_Table_Sorted_Asc(PROJECTION, filter,isPrimaryValueUnique);
    }

    public  ArrayList<DataContactDisplay>
                getAllInformationWithLastContact(boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.LAST_TIME_CONTACTED};

        String filter = "(" +
                ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'" +
                ")";

        String order = PROJECTION[1] + _sortOrder;
        return getDisplayData_Data_Table( PROJECTION, filter,
                order, "", isPrimaryValueUnique);

    }


    public  ArrayList<DataContactDisplay>
            getAllInformationWithContactCount(boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.TIMES_CONTACTED};

        String filter = "(" +
                ContactsContract.RawContacts.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'" +
                ")";

        String order = PROJECTION[1] + _sortOrder;

        return getDisplayData_Data_Table(PROJECTION, filter, order,
                (String) _context.getResources().getText(R.string.general_times_contacted),true);
    }

    //Get Phone Numbers
    public ArrayList<DataContactDisplay> getPhoneNumbers(
            boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DATA1,
                ContactsContract.Data.DISPLAY_NAME};

        String filter = "(" +
                ContactsContract.Data.MIMETYPE + " LIKE '" +
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                ")";

        String order = PROJECTION[0] + _sortOrder;
        return getDisplayData_Data_Table( PROJECTION, filter,
                order, "", isPrimaryValueUnique);

    }

    //Phone numbers is primary value
    public ArrayList<DataContactDisplay> getPhoneNumbersWithName(
            boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA1};

        String filter = "(" +
                ContactsContract.Data.MIMETYPE + " LIKE '" +
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                ")";

        String order = PROJECTION[0] + _sortOrder;
        return getDisplayData_Data_Table( PROJECTION, filter,
                order, "", isPrimaryValueUnique);

    }


    //Get Email Address
    public ArrayList<DataContactDisplay> getEmails(
            boolean isPrimaryValueUnique) {
        String[] PROJECTION = new String[] {
                ContactsContract.Data.DATA1,
                ContactsContract.Data.DISPLAY_NAME};

        String filter = "(" +
                            ContactsContract.Data.MIMETYPE + " LIKE '" +
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" +
                        ")";

        String order = PROJECTION[0] + _sortOrder;
        return getDisplayData_Data_Table( PROJECTION, filter,
                order, "", isPrimaryValueUnique);
    }

    //Will get phone number/email which ever comes first for a RawContact
    private DataContactDisplay getSingleDetail_RawContact(long rawContactId,
                                                          String[] projection)
    {
        ArrayList<DataContactDisplay> values =
                getRawContactDetails(rawContactId, projection);

        if(values!=null)
            if(values.size() >0) return values.get(0);

        return null;
    }

    //Get all details for a single raw contact
    public ArrayList<DataContactDisplay>
        getRawContactDetailsForId(long rawContactId)
    {
        String[] projection = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.DATA1};

        return getRawContactDetails(rawContactId,projection);
    }

    //Get all details of a RawContact
    public ArrayList<DataContactDisplay> getRawContactDetails(long rawContactId,
                                                           String[] projection){
        String filter = "(" +
                ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'" +
                " OR " + ContactsContract.Data.MIMETYPE + " LIKE '" + ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "')" +
                " AND "+ ContactsContract.Data.RAW_CONTACT_ID + "=" +
                String.valueOf(rawContactId);

        ArrayList<DataContactDisplay> values =
                getQueryOn_Data_Table_Sorted_Asc(projection, filter,false);

        return values;
    }

    //Get Deleted Records
    public ArrayList<DataContactDisplay> getDeleted(
            boolean isPrimaryValueUnique) {
        /*String[] addProjection = new String[] {
                ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.RawContacts.LAST_TIME_CONTACTED};

        String filter = "(" +
                ContactsContract.RawContacts.DELETED + " LIKE '" +
                "1" + "'" + ")";*/


        return getDisplayData_for_deleted_contacts();
    }

    //Works for contact ids
    public ArrayList<DataContactTransfer> getContactsForTransfer(ArrayList<Long> contactIds,
                                                                 int flag){
        return getTransferData_Multiple(contactIds, flag,false);
    }

    public ArrayList<DataContactTransfer> getContactsForTransfer_WhatsApp(ArrayList<Long> contactIds,
                                                                 int flag){
        return getTransferData_WhatsApp(contactIds, flag,false);
    }

    public ArrayList<DataContactTransfer> getContactsForTransfer_RawContacts(
                                            ArrayList<Long> rawContactIds,
                                                                 int flag){
        return getTransferData_Multiple(rawContactIds, flag,true);
    }

    public long getContactId(long rawContactId)
    {
        long contactId=0;
        String[] projection=new String[]{
                ContactsContract.Data.CONTACT_ID};
        String selection=ContactsContract.Data.RAW_CONTACT_ID +"=?";
        String[] selectionArgs=new String[]{String.valueOf(rawContactId)};
        Cursor cur=_context.getContentResolver().query(
                ContactsContract.Data.CONTENT_URI, projection,
                selection, selectionArgs, null);
        if(cur.moveToFirst()) {
            contactId = cur.getLong(
                    cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
        }
        cur.close();
        return contactId;
    }

    public String getVCFDataAllContacts(int maxCount){
        int count=0;

        Object tmpO_1=null;
        Object tmpO_2=null;
        StringBuilder sb = new StringBuilder("");
        Cursor cur = _context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null, null, null);
        if(cur.moveToFirst()){

            do{
                count++;
                if(maxCount > 0 && count>maxCount) break; //For free version
            tmpO_1 = cur.getString(
                    cur.getColumnIndex("lookup"));
            tmpO_1 = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_VCARD_URI,
                    (String)tmpO_1);
            try {
                tmpO_2 = _context.
                        getContentResolver().openAssetFileDescriptor(
                        (Uri) tmpO_1, "r");
                tmpO_1 = new FileInputStream(((AssetFileDescriptor)
                        tmpO_2).getFileDescriptor());
                tmpO_2 = new byte[(int) ((AssetFileDescriptor)
                        tmpO_2).getDeclaredLength()];
                ((InputStream) tmpO_1).read((byte[]) tmpO_2);
                tmpO_2 = new String((byte[]) tmpO_2);

                sb.append(tmpO_2);
                //finValue = finValue + tmpO_2;

                ((InputStream) tmpO_1).close();
            }
            catch (Exception localException)
            {
                for (;;)
                {
                    localException.printStackTrace();
                }
            }
            }while(cur.moveToNext());

        }
        return sb.toString();
    }

    public String getVCFDataSingleContact(long contactId){
        int count=0;

        Object tmpO_1=null;
        Object tmpO_2=null;
        StringBuilder sb = new StringBuilder("");
        String filter = "(" +
                ContactsContract.Contacts._ID + " = " +
                String.valueOf(contactId) + ")";
        Cursor cur = _context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                filter, null, null);
        if(cur.moveToFirst()){

            do{
                count++;

                tmpO_1 = cur.getString(
                        cur.getColumnIndex("lookup"));
                tmpO_1 = Uri.withAppendedPath(
                        ContactsContract.Contacts.CONTENT_VCARD_URI,
                        (String)tmpO_1);
                try {
                    tmpO_2 = _context.
                            getContentResolver().openAssetFileDescriptor(
                            (Uri) tmpO_1, "r");
                    tmpO_1 = new FileInputStream(((AssetFileDescriptor)
                            tmpO_2).getFileDescriptor());
                    tmpO_2 = new byte[(int) ((AssetFileDescriptor)
                            tmpO_2).getDeclaredLength()];
                    ((InputStream) tmpO_1).read((byte[]) tmpO_2);
                    tmpO_2 = new String((byte[]) tmpO_2);

                    sb.append(tmpO_2);
                    //finValue = finValue + tmpO_2;

                    ((InputStream) tmpO_1).close();
                }
                catch (Exception localException)
                {
                    for (;;)
                    {
                        localException.printStackTrace();
                    }
                }
            }while(cur.moveToNext());

        }
        return sb.toString();
    }
}
