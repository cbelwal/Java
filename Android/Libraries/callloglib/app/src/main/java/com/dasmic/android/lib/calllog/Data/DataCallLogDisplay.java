package com.dasmic.android.lib.calllog.Data;

import android.app.Activity;
import android.net.Uri;
import android.provider.CallLog;
import android.telecom.Call;

import com.dasmic.android.lib.calllog.R;
import com.dasmic.android.lib.support.Static.DateOperations;

/**
 * Created by Chaitanya Belwal on 7/16/2016.
 */
public class DataCallLogDisplay {
    private final String CrLf="\r\n";
    private long _date;
    private long _duration;
    private String _name;
    private String _number;
    private long _callLogId;
    //private long _contactId;
    private long _type;
    private Uri _pictureURI;
    private String _geoLocation;
    public boolean isChecked;

    public DataCallLogDisplay( long callLogId,
                              long type,
                              long date,
                              long duration,
                              String name,
                              String number,
                              String geoLocation,
                              Uri pictureURI){
        _callLogId =callLogId;
        _type=type;
        _duration=duration;
        _name=name;
        _number=number;
        _pictureURI=pictureURI;
        _geoLocation = geoLocation;
        _date=date;
    }

    public DataCallLogDisplay(){
        _callLogId =-1;
        _type=1;
        _duration=0;
        _name="";
        _number="";
        _pictureURI=null;
        _geoLocation = "";
        _date=0;
    }

    @Override
    public String toString() {
        return getName();
    }


    public long getDate(){return _date;}

    public String getSecondaryValue(){
        return DateOperations.getFormattedDate(_date);
    }
    public String getName(){
        if(_name==null){
            return _number;
        }
        else return _name;
    }

    public String getNumber(){
        return _number;
    }

    public long getId(){
        return _callLogId;
    }

    public String getDurationString(){
        long minutes = _duration/60;
        long seconds = _duration % 60;
        String value = String.valueOf(minutes) + "m" +
                String.valueOf(seconds)+"s";

        return value;
    }

    public long getDuration(){
        return _duration;
    }

    public String getGeoLocation(){
        return _geoLocation;
    }

    public Uri getPictureUri(){
        return _pictureURI;
    }

    public boolean isMissedCall(){
        if(_type== CallLog.Calls.MISSED_TYPE) return true;
        return false;
    }

    public boolean isIncomingCall(){
        if(_type== CallLog.Calls.INCOMING_TYPE) return true;
        return false;
    }

    public boolean isOutgoingCall(){
        if(_type== CallLog.Calls.OUTGOING_TYPE) return true;
        return false;
    }

    public long getType(){
        return _type;
    }

    public String getFormattedString(Activity activity){
        StringBuilder sb=new StringBuilder("");

        sb.append(activity.getString(R.string.tag_formatted_name) + " " + getName() + CrLf) ;
        sb.append(activity.getString(R.string.tag_formatted_number) + " " +getNumber()+ CrLf);
        sb.append(activity.getString(R.string.tag_formatted_type) + " " +
                getTypeString(activity)+ CrLf);
        sb.append(activity.getString(R.string.tag_formatted_datetime) +" " +
                                DateOperations.getFormattedDate(getDate())+ CrLf);
        sb.append(activity.getString(R.string.tag_formatted_duration) +" " +
                                getDurationString() + CrLf);
        sb.append(activity.getString(R.string.tag_formatted_location) +" " +
                                getGeoLocation() + CrLf);

        return sb.toString();
    }

    private String getTypeString(Activity activity){
        if(isIncomingCall())
            return activity.getString(R.string.tag_formatted_type_incoming);
        if(isOutgoingCall())
            return activity.getString(R.string.tag_formatted_type_outgoing);
        if(isMissedCall())
            return activity.getString(R.string.tag_formatted_type_missed);
        return "";
    }

    //Get the CLM String
    //Store in a CSV like format
    //Number, Date, Type, Duration
    public String getCLMString(){
        StringBuilder sb=new StringBuilder("");

        sb.append(getNumber()+ ",");
        sb.append(String.valueOf(getDate()) + ",");
        sb.append(String.valueOf(getType()) + ",");
        sb.append(String.valueOf(_duration) + "\r\n");

        return sb.toString();
    }

    //Number, Date, Type, Duration
    public void setFromCLMString(String clmString){
        String []values=clmString.split(",");

        if(values.length < 4)
            throw new RuntimeException("Invalid CLM File entry");

        _number=values[0].trim();
        _date=Long.parseLong(values[1].trim());
        _type=Long.parseLong(values[2].trim());
        _duration = Long.parseLong(values[3].trim());
    }

    // Returns a unique name for file creation for each call log
    //This is the date/time without spaces
    public String getUniqueName(){
        return DateOperations.getFormattedDate(_date).replace(" ","");
    }

}
