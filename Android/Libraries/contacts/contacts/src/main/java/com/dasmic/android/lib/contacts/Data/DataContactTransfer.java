package com.dasmic.android.lib.contacts.Data;

import android.content.Context;

import com.dasmic.android.lib.contacts.R;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 8/6/2015.
 */

public class DataContactTransfer{
    private long _contactId;
    private DataName _name;
    private DataAllPhone _phoneNumbers;
    private DataAllWhatsApp _whatsApp;
    private DataAllEmail _emailAddresses;
    private DataAllPostalAddress _postalAddress;
    private DataAllEvent _contactEvents;
    private int _timesContacted;
    private long _lastTimeContacted;
    private int _inFavorites;
    private DatavCardPhoto _datavCardPhoto;
    private static final String CSVGroupSeparator =",<>,";
    private DataOrganization _organization;
    private String _note;

    public static final String CrLf="\r\n";


    public DataContactTransfer(long contactId){
        super();
        _name= new DataName("","","","","");
        _contactId = contactId;
        _phoneNumbers = new DataAllPhone();
        _emailAddresses = new DataAllEmail();
        _postalAddress = new DataAllPostalAddress();
        _datavCardPhoto=new DatavCardPhoto();
        _organization = new DataOrganization();
        _whatsApp= new DataAllWhatsApp();
        _contactEvents = new DataAllEvent();
        _timesContacted=-1;
        _lastTimeContacted=-1;
        _inFavorites=-1;
    }

    public void setPhoto(byte[] photo){
        _datavCardPhoto.setPhoto(photo);
    }
    public void setInFavorites(int value){
        _inFavorites=value;
    }
    public void setTimesContacted(int value){
        _timesContacted=value;
    }
    public void setLastTimeContacted(long value){
        _lastTimeContacted=value;
    }
    public void setNote(String value){
        _note=value;
    }

    public boolean setName( String firstName,
                            String middleName,
                            String familyName,
                            String displayName,
                            String prefix,
                            String suffix){
        _name = new DataName(firstName,
                middleName,
                familyName,prefix,suffix);
        return true;
    }

    public boolean setOrganization(String company,
                           String department,
                           String title){
        _organization = new DataOrganization(company,
                                    department,
                                    title);
        return true;
    }

    public long getContactId(){return _contactId;}
    public DataOrganization getOrganization(){
        return  _organization;
    }
    public DataAllPhone getPhoneNumbers(){
        return  _phoneNumbers;
    }
    public DataAllEvent getEvents() {return _contactEvents;}
    public DataAllWhatsApp getWhatsApp(){
        return  _whatsApp;
    }

    public DataAllEmail getEmailAddresses(){
        return _emailAddresses;
    }
    public DatavCardPhoto getvCardPhotoObject(){
        return _datavCardPhoto; //Primarily used in unit tests
    }


    public DataAllPostalAddress getPostalAddress(){
        return _postalAddress;
    }
    public Long getLastTimeContacted(){
        return _lastTimeContacted;
    }
    public int getTimesContacted(){
        return _timesContacted;
    }
    public int getInFavorites(){
        return _inFavorites;
    }

    public String getFormattedString(Context _context){
        String value =  _name.getFormattedString(_context) + CrLf;
        value = value + _organization.getFormattedString(_context) ;
        value = value + _phoneNumbers.getFormattedString(_context);
        value = value + _emailAddresses.getFormattedString(_context);
        value = value + _postalAddress.getFormattedString(_context);
        value = value + _contactEvents.getFormattedString(_context);

        if(_timesContacted != -1)
            value =  value +
                    (String) _context.getResources().getText(R.string.transfer_times_contacted) +
                    String.valueOf(_timesContacted) + CrLf;
        if(_lastTimeContacted != -1)
            value = value +
                    (String) _context.getResources().getText(R.string.transfer_last_time_contacted) +
                    DateOperations.getFormattedDate(_lastTimeContacted) + CrLf;
        if(_inFavorites != -1) {
            if(_inFavorites == 1)
                value = value +
                        (String) _context.getResources().getText(R.string.transfer_in_favorites) + CrLf;
            else
                value = value +
                        (String) _context.getResources().getText(R.string.transfer_not_in_favorites)+CrLf;
        }
        return value + CrLf;
    }

    public String getVCardName(){
        return _name.getVCardName();
    }
    public DataName getName(){return _name;}

    public String getNoteVCardString(){
        if(_note != null)
            return "NOTE:" + _note + CrLf;
        else
            return "";
    }

    public String getNote(){
        if(_note != null)
            return _note;
        else
            return "";
    }

    //Of the form NOTE:Value
    public void setNoteFromVCardString(String vCardString)
    {
        if(vCardString.equals("")) return;
        String []values=vCardString.split(":");
        if(values.length>=2) {
            _note=values[1].trim();
        }
    }


    //Only vCard 4.0 is supported through object
    public String getVCardString(){
        String value="BEGIN:VCARD" + CrLf;
        value=value+"VERSION:4.0"+ CrLf;
        value =  value + _name.getVCardString();
        value = value + "FN:" +_name.getVCardName() + CrLf;
        value = value + _organization.getVCardString();
        value = value + _phoneNumbers.getVCardString();
        value = value + _whatsApp.getVCardString();
        value = value + _emailAddresses.getVCardString();
        value = value + _postalAddress.getVCardString();
        value = value + _contactEvents.getVCardString();
        value = value + _datavCardPhoto.getPhotovCardString();
        value = value + getNoteVCardString();
        value=value+"END:VCARD"+CrLf;
        return value;
    }

    /*String will be in form
        :VCARD\r\n
        VERSION:\r\n
        X-DASMIC-WHATSAPP
        ....
        END:VCARD
     */
    private void setFromVcardString_40(String []values){
        String value;
        setName("","","","","","");//Create Name Object
        for(int ii=0;ii<values.length;ii++){
            try {
                value = values[ii];
                if (value.toUpperCase().startsWith("N:") || value.startsWith("N;"))
                    _name.setFromVCard40String(value.trim());
                if (value.toUpperCase().startsWith("FN:")) {
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard30String(value.trim());
                }
                if (value.toUpperCase().startsWith("ORG:")) {
                    _organization.setOrgFromVCardString(value.trim());
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard40String(value.trim());
                }
                if (value.toUpperCase().startsWith("TITLE:"))
                    _organization.setTitleFromVCardString(value.trim());
                if (value.toUpperCase().startsWith("TEL"))
                    _phoneNumbers.setFromVCard40String(value.trim());
                if (value.toUpperCase().startsWith("X-DASMIC-WHATSAPP")) //WhatsApp is only in 4.0
                    _whatsApp.setFromVCard40String(value.trim());
                if (value.toUpperCase().startsWith("ADR"))
                    _postalAddress.setFromVCard40String(value.trim());
                if (value.toUpperCase().startsWith("EMAIL")) //Could be ; or :
                    _emailAddresses.setFromVCard40String(value.trim());
                if (value.toUpperCase().contains(".EMAIL")) //Seen some vcfs with item1.EMAIL
                    _emailAddresses.setFromVCard40String(value.trim());
                if (value.toUpperCase().startsWith("PHOTO") //X-MS-CARDPICTURE is for Outlook
                        || value.startsWith("X-MS-CARDPICTURE")) //Could be ; or :
                    ii=ParsePhotoEncodingAndMovePointer(values, ii);
                if (value.toUpperCase().startsWith("NOTE"))
                    setNoteFromVCardString((value.trim()));
                if (value.toUpperCase().startsWith("BDAY"))
                    _contactEvents.setFromVCard40String((value.trim()));
                if (value.toUpperCase().startsWith("ANNIVERSARY"))
                    _contactEvents.setFromVCard40String((value.trim()));

            }
            catch(Exception ex){
                //Let caller know that some errors were encountered
                SupportFunctions.DebugLog("ContactTranfer", "SetFromVcard", ex.getMessage());
                throw new RuntimeException(ex);
            }
        }

    }

    /*String will be in form
        :VCARD\r\n
        VERSION:\r\n
        ....
        END:VCARD
     */
    private void setFromVcardString_30(String []values){
        String value;
        setName("", "", "","","","");//Create Name Object
        for(int ii=0;ii<values.length;ii++){
            try {
                value = values[ii];
                if (value.toUpperCase().startsWith("N:")|| value.startsWith("N;"))
                    _name.setFromVCard30String(value.trim());
                if (value.toUpperCase().startsWith("FN:")) {
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard30String(value.trim());
                }
                if (value.toUpperCase().startsWith("ORG:")) {
                    _organization.setOrgFromVCardString(value.trim());
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard30String(value.trim());
                }
                if (value.toUpperCase().startsWith("TITLE:"))
                    _organization.setTitleFromVCardString(value.trim());
                if (value.toUpperCase().startsWith("TEL"))
                    _phoneNumbers.setFromVCard30String(value.trim());
                if (value.toUpperCase().startsWith("ADR"))
                    _postalAddress.setFromVCard30String(value.trim());
                if (value.toUpperCase().startsWith("EMAIL")) //Could be ; or :
                    _emailAddresses.setFromVCard30String(value.trim());
                if (value.toUpperCase().startsWith("PHOTO") //X-MS-CARDPICTURE is for Outlook
                        || value.startsWith("X-MS-CARDPICTURE")) //Could be ; or :
                    ii=ParsePhotoEncodingAndMovePointer(values, ii);
                if (value.toUpperCase().startsWith("NOTE")) //Do note change N: as it can be confused with Note
                    setNoteFromVCardString((value.trim()));
                if (value.toUpperCase().startsWith("BDAY"))
                    _contactEvents.setFromVCard30String((value.trim()));
                if (value.toUpperCase().startsWith("ANNIVERSARY"))
                    _contactEvents.setFromVCard30String((value.trim()));
            }
            catch(Exception ex)
            { //Let caller know that some errors were encountered
                //Let caller know that some errors were encountered
                SupportFunctions.DebugLog("ContactTranfer", "SetFromVcard", ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }

    /*String will be in form
        :VCARD\r\n
        VERSION:\r\n
        ....
        END:VCARD
     */
    private void setFromVcardString_21(String []values){
        String value;
        setName("", "", "","","","");//Create Name Object
        for(int ii=0;ii<values.length;ii++){
            try {
                value = values[ii] ;
                if (value.toUpperCase().startsWith("N:") || value.startsWith("N;"))//Could be N: or N;
                    _name.setFromVCard21String(value.trim());
                if (value.toUpperCase().startsWith("FN:")) {
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard30String(value.trim());
                }
                if (value.toUpperCase().startsWith("ORG:")) {
                    _organization.setOrgFromVCardString(value.trim());
                    if(_name.getVCardName().equals("")) //Set name if not set yet, it is missing
                        _name.setFromVCard21String(value.trim());
                }
                if (value.toUpperCase().startsWith("TITLE:"))
                    _organization.setTitleFromVCardString(value.trim());
                if (value.toUpperCase().startsWith("TEL"))
                    _phoneNumbers.setFromVCard21String(value.trim());
                if (value.toUpperCase().startsWith("ADR"))
                    _postalAddress.setFromVCard21String(value.trim());
                if (value.toUpperCase().startsWith("EMAIL")) //Could be ; or :
                    _emailAddresses.setFromVCard21String(value.trim());
                if (value.toUpperCase().startsWith("PHOTO") //X-MS-CARDPICTURE is for Outlook
                        || value.startsWith("X-MS-CARDPICTURE")) //Could be ; or :
                    ii=ParsePhotoEncodingAndMovePointer(values, ii);
                if (value.toUpperCase().startsWith("NOTE")) //Do note change N: as it can be confused with Note
                    setNoteFromVCardString((value.trim()));
                if (value.toUpperCase().startsWith("BDAY"))
                    _contactEvents.setFromVCard21String((value.trim()));
                if (value.toUpperCase().startsWith("ANNIVERSARY"))
                    _contactEvents.setFromVCard21String((value.trim()));
            }
            catch(Exception ex)
            {   //Let caller know that some errors were encountered
                SupportFunctions.DebugLog("ContactTranfer", "SetFromVcard", ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
    }

    //PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE
    //CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/=
    //
    private int ParsePhotoEncodingAndMovePointer(String []values,
                                                 int iStart){
        return _datavCardPhoto.setPhotoEncodingAndMovePointer(values,
                iStart);
    }


    /*String will be in form
        :VCARD\r\n
        VERSION:\r\n
        ....
        END:VCARD
     */
    //NOTE: '\r\n' are both present
    public void setFromVCardString(String vCardString){
        double version=0.0;

        //Replace encoding characters
        vCardString = vCardString.replace("=20"," ");

        if(!vCardString.toUpperCase().contains("VERSION"))
            throw new RuntimeException("Version not found");
        String []values=vCardString.split("\r\n");//Be careful on changing this
        for(String value:values) {
            if (value.toUpperCase().startsWith("VERSION:")) {
                String [] sTemp=value.split(":");
                if(sTemp.length>=2)
                    version=Double.parseDouble(sTemp[1]);
                break;
            }
        }

        if(version==4.0)
            setFromVcardString_40(values);
        else if(version==3.0)
            setFromVcardString_30(values);
        else if(version==2.1)
            setFromVcardString_21(values);
        else //default option if version older
            throw new RuntimeException("Version not found");
    }

    public byte[] getDecodedPhotoBytes(){
       return _datavCardPhoto.getDecodedPhotoBytes();
    }


    public String getCSVHeader(Context _context){
        String value=_context.getResources().getText(R.string.csv_header_name)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_organization)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_phone)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_email)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_postal)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_timescontacted)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_lasttimecontact)+CSVGroupSeparator;
        value=value+_context.getResources().getText(R.string.csv_header_favorite);

        value=value+CSVGroupSeparator+CrLf;
        return value;
    }

    public String getCSVString(){
        String value =   _name.getCSVString() +
                            CSVGroupSeparator;
        value = value + _organization.getCSVString()+ CSVGroupSeparator;
        value = value + _phoneNumbers.getCSVString()+ CSVGroupSeparator;
        value = value + _emailAddresses.getCSVString()+ CSVGroupSeparator;
        value = value + _postalAddress.getCSVString()+ CSVGroupSeparator;
        value =  value +
                    String.valueOf(_timesContacted) + CSVGroupSeparator;
        value = value + String.valueOf(_lastTimeContacted) + CSVGroupSeparator;
        value = value + String.valueOf(_inFavorites) + CSVGroupSeparator;
        value=value+CrLf;
        return value;
    }

    public void setFromCSVString(String csvString){
        String []values=csvString.split(CSVGroupSeparator);
        setName("", "", "","","","");//Create Name Object
        try {
            if (values.length >= 1) _name.setFromCSVString(values[0]);
            if (values.length >= 2) _organization.setFromCSVString(values[1]);
            if (values.length >= 3) _phoneNumbers.setFromCSVString(values[2]);
            if (values.length >= 4) _emailAddresses.setFromCSVString(values[3]);
            if (values.length >= 5) _postalAddress.setFromCSVString(values[4]);
            if (values.length >= 6) _timesContacted = Integer.valueOf(values[5]);
            if (values.length >= 7) _lastTimeContacted = Long.valueOf(values[6]);
            if (values.length >= 8) _inFavorites = Integer.valueOf(values[7]);
        }
        catch(Exception ex){
            //Let caller know that some errors were encountered
            SupportFunctions.DebugLog("ContactTranfer", "SetFromCSV", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

}
