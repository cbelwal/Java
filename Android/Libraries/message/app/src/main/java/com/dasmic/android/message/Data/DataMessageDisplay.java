package com.dasmic.android.lib.message.Data;

/**
 * Created by Chaitanya Belwal on 8/19/2017.
 */

public class DataMessageDisplay {
    private String _id;
    private String _address;
    private String _msg;
    private String _readState; //"0" for have not read sms and "1" for have read sms
    private long _time;
    private String _type;
    private boolean _isChecked;


    public DataMessageDisplay(String id,
                              String address,
                              String body,
                              String readState,
                              long time,
                              String type){
        _id=id;
        _address=address;
        _msg = body;
        _readState=readState;
        _time=time;
        _type =type;
        _isChecked = false;
    }

    public boolean isChecked;

    public String getId() {
        return _id;
    }

    public String getAddress() {
        return _address;
    }

    public String getMsg() {
        return _msg;
    }

    public String getReadState() {
        return _readState;
    }

    public long getTime() {
        return _time;
    }

    public String getFolderName() {
        return _type;
    }

    public String getPrimaryValue(){

    }

    public String getSecondaryValue(){

    }

    public String getTertiaryValue(){

    }


}
