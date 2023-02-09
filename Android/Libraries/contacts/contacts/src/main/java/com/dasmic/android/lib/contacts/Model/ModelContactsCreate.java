package com.dasmic.android.lib.contacts.Model;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.ContactsContract;

import com.dasmic.android.lib.contacts.Data.DataContactTransfer;
import com.dasmic.android.lib.contacts.Data.DataPostalAddress;
import com.dasmic.android.lib.contacts.Data.DataValuePair;
import com.dasmic.android.lib.contacts.Data.DataValueThreeTuple;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 9/15/2015.
 */
public class ModelContactsCreate {
    private Context _context;
    ModelContactsUpdate _mcu;

    private final String MIME_WhatsApp="vnd.android.cursor.item/vnd.com.whatsapp.profile";

    public ModelContactsCreate(Context context){
        _context=context;
        _mcu = new ModelContactsUpdate(_context);
    }


    //--------------
    private void AddPostalAddresses_New(
            ArrayList <ContentProviderOperation> ops,
            DataContactTransfer dct){
        DataPostalAddress dpa;
        for(int ii=0;ii<dct.getPostalAddress().getCount();ii++){
            dpa=dct.getPostalAddress().getValue(ii);
            //Add all values os Structured Postal
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, dpa.getStreet())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, dpa.getCity())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, dpa.getRegion())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, dpa.getCountry())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, dpa.getPostcode())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, dpa.getType())
                    .build());
        }

    }

    private void AddPostalAddresses_Existing(long rawContactId,
            ArrayList <ContentProviderOperation> ops,
            DataContactTransfer dct){
        DataPostalAddress dpa;
        for(int ii=0;ii<dct.getPostalAddress().getCount();ii++){
            dpa=dct.getPostalAddress().getValue(ii);
            //Add all values os Structured Postal
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, dpa.getStreet())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, dpa.getCity())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, dpa.getRegion())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, dpa.getCountry())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, dpa.getPostcode())
                    .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, dpa.getType())
                    .build());
        }

    }

    //--------------
    private void AddEmailAddresses_New(
        ArrayList <ContentProviderOperation> ops,
        DataContactTransfer dct){
            DataValuePair<Integer,String> dvPair;

            for(int ii=0;ii<dct.getEmailAddresses().getCount();ii++){
                dvPair=dct.getEmailAddresses().getValue(ii);
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.DATA, dvPair.Value)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, dvPair.Key)
                        .build());

            }
    }


    private void AddEmailAddresses_Existing(long rawContactId,
            ArrayList <ContentProviderOperation> ops,
            DataContactTransfer dct){
        DataValuePair<Integer,String> dvPair;

        for(int ii=0;ii<dct.getEmailAddresses().getCount();ii++){
            dvPair=dct.getEmailAddresses().getValue(ii);
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, dvPair.Value)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, dvPair.Key)
                    .build());

        }
    }

    //--------- Add multiple phone number with Back Ref
    private void AddPhoneNumbers_New(
            ArrayList < ContentProviderOperation> ops,
                                 DataContactTransfer dct){
        DataValuePair<Integer,String> dvPair;

        for(int ii = 0; ii<dct.getPhoneNumbers().getCount(); ii++){
            dvPair=dct.getPhoneNumbers().getValue(ii);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, dvPair.Value)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            dvPair.Key)
                    .build());

        }
    }

    private void AddPhoneNumbers_Existing(long rawContactId,
            ArrayList < ContentProviderOperation> ops,
            DataContactTransfer dct){
        DataValuePair<Integer,String> dvPair;

        for(int ii = 0; ii<dct.getPhoneNumbers().getCount(); ii++){
            dvPair=dct.getPhoneNumbers().getValue(ii);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, dvPair.Value)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            dvPair.Key)
                    .build());

        }
    }


    private void addEvents_Existing(long rawContactId,
                                          ArrayList < ContentProviderOperation> ops,
                                          DataContactTransfer dct){
        DataValueThreeTuple<Integer,String, String> dvTuple;

        for(int ii = 0; ii<dct.getEvents().getCount(); ii++){
            dvTuple=dct.getEvents().getValue(ii);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(ContactsContract.CommonDataKinds.Event.START_DATE,
                            dvTuple.Value)
                    .withValue(ContactsContract.CommonDataKinds.Event.LABEL,
                            dvTuple.Label)
                    .withValue(ContactsContract.CommonDataKinds.Event.TYPE,
                            dvTuple.Key)
                    .build());
        }
    }

    private void addEvents_New(    ArrayList < ContentProviderOperation> ops,
                                    DataContactTransfer dct){
        DataValueThreeTuple<Integer,String, String> dvTuple;

        for(int ii = 0; ii<dct.getEvents().getCount(); ii++){
            dvTuple=dct.getEvents().getValue(ii);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Event.START_DATE,
                            dvTuple.Value)
                    .withValue(ContactsContract.CommonDataKinds.Event.LABEL,
                            dvTuple.Label)
                    .withValue(ContactsContract.CommonDataKinds.Event.TYPE,
                            dvTuple.Key)
                    .build());
        }
    }

    //--------------
    private void AddName(ArrayList <ContentProviderOperation> ops,
                         DataContactTransfer dct){
        if (dct.getVCardName() != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                            dct.getName().getFamilyName())
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
                            dct.getName().getMiddleName())
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                            dct.getName().getFirstName())
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX,
                            dct.getName().getPrefix())
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX,
                            dct.getName().getSuffix())
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            dct.getVCardName()).build());
        }
    }

    //--------------
    private void addNote_New(ArrayList <ContentProviderOperation> ops,
                             DataContactTransfer dct){
        String value =dct.getNote().trim();
        if(value == "") return;

        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, value)
                .build());
    }

    private void addWhatsApp_New(
                                 ArrayList < ContentProviderOperation> ops,
                                 DataContactTransfer dct){
        DataValuePair<Integer,String> dvPair;

        for(int ii=0;ii<dct.getWhatsApp().getCount();ii++){
            dvPair=dct.getWhatsApp().getValue(ii);
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            MIME_WhatsApp)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                            dvPair.Value) //Phone.Number is same as  Appid
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            dvPair.Key)
                    .build());

            /*ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            dct.getVCardName()).build());*/

        }
    }

    //--------------
    private void AddPhoto_New(ArrayList < ContentProviderOperation> ops,
                              DataContactTransfer dct){
        byte[] decodedBytes = dct.getDecodedPhotoBytes();
        if (decodedBytes != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                            decodedBytes).build());
        }
    }

    //--------------
    private void AddPhoto_Existing(long rawContactId,
                                ArrayList < ContentProviderOperation> ops,
                              DataContactTransfer dct){
        byte[] decodedBytes = dct.getDecodedPhotoBytes();
        if (decodedBytes != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO,
                            decodedBytes).build());
        }
    }


    //--------------
    private void UpdateOtherInformation(Long contactId, ArrayList <ContentProviderOperation> ops,
                         DataContactTransfer dct){
        if (dct.getLastTimeContacted() != -1) {
            _mcu.setLastTimeContacted(contactId,dct.getLastTimeContacted());
        }
        if (dct.getTimesContacted() != -1) {
            _mcu.setTimesContacted(contactId, dct.getTimesContacted());
        }
        if (dct.getInFavorites() != -1) {
            _mcu.setStarred(contactId,dct.getInFavorites());

        }
    }

    //--------------
    public void CreateContact_New(DataContactTransfer dct)
    {
        ModelContactsRead mcr = new ModelContactsRead(_context);
        ContentResolver cr = _context.getContentResolver();
        ArrayList <ContentProviderOperation> ops = new
                ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        AddName(ops, dct);
        AddPhoneNumbers_New(ops, dct);
        AddEmailAddresses_New(ops, dct);
        AddPostalAddresses_New(ops, dct);
        addNote_New(ops, dct);
        addEvents_New(ops,dct);
        AddPhoto_New(ops, dct);

        // Asking the Contact provider to create a new contact
        try {
            ContentProviderResult[] results = cr.applyBatch(ContactsContract.AUTHORITY, ops);
            long rawContactId = Long.parseLong(
                    results[0].uri.getLastPathSegment());
            UpdateOtherInformation(mcr.getContactId(rawContactId),ops,dct);
        } catch (Exception ex) {
            SupportFunctions.DebugLog("ModelCreateContacts",
                    "CreateContact", "Error:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }


    //-------------------------------------------------------------
    public void CreateContact_New_WhatsApp(DataContactTransfer dct)
    {
        ModelContactsRead mcr = new ModelContactsRead(_context);
        ContentResolver cr = _context.getContentResolver();
        ArrayList <ContentProviderOperation> ops = new
                ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        AddName(ops, dct);
        addWhatsApp_New(ops, dct); //Add MIMEType for Whats App
        AddPhoneNumbers_New(ops, dct);
        AddEmailAddresses_New(ops, dct);
        AddPostalAddresses_New(ops, dct);
        AddPhoto_New(ops, dct);

        // Asking the Contact provider to create a new contact
        try {
            ContentProviderResult[] results = cr.applyBatch(ContactsContract.AUTHORITY, ops);
            long rawContactId = Long.parseLong(
                    results[0].uri.getLastPathSegment());
            UpdateOtherInformation(mcr.getContactId(rawContactId),ops,dct);
        } catch (Exception ex) {
            SupportFunctions.DebugLog("ModelCreateContacts",
                    "CreateContact", "Error:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void addNote_Existing(long rawContactId,
                                ArrayList < ContentProviderOperation> ops,
                                DataContactTransfer dct){
        String value =dct.getNote().trim();
        if(value == "") return;

        ops.add(ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, value)
                .build());
    }

    //Add rows to the data table for existing contact except name
    //Will add contact details in dct to rawContactId
    public int createContactDetails_In_Existing_Contact(long toRawContactId,
                                                        DataContactTransfer dct)
    {
        ContentResolver cr = _context.getContentResolver();
        ContentProviderResult[] results=null;
        ArrayList <ContentProviderOperation> ops = new
                ArrayList<ContentProviderOperation>();

        AddPhoneNumbers_Existing(toRawContactId,ops, dct);
        AddEmailAddresses_Existing(toRawContactId,ops, dct);
        AddPostalAddresses_Existing(toRawContactId,ops, dct);
        AddPhoto_Existing(toRawContactId,ops, dct);
        addNote_Existing(toRawContactId,ops, dct);
        addEvents_Existing(toRawContactId,ops, dct); //Add Events

        // Asking the Contact provider to create a new contact
        try {
            results = cr.applyBatch(ContactsContract.AUTHORITY, ops);

        } catch (Exception ex) {
            SupportFunctions.DebugLog("ModelCreateContacts",
                    "CreateContact", "Error:" + ex.getMessage());
            throw new RuntimeException(ex);
        }
        if(results.length>0) return 1;
        else return 0;
    }
}
