package com.dasmic.android.lib.contacts.Data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Chaitanya Belwal on 12/25/2015.
 */
public class DatavCardPhoto {
    public String _photoImageType;
    public String _photoEncodingMethod;
    public String _photoEncodedValue; //Used for Import
    private byte[] _baPhoto; //Used for Export sets _photoEncodedValue

    public DatavCardPhoto(){
        _photoImageType ="";
        _photoEncodingMethod ="";
        _photoEncodedValue ="";
    }

    //Parse line of type
    //PHOTO;ENCODING=BASE64;JPEG: OR
    //X-MS-CARDPICTURE;TYPE=JPEG;ENCODING=BASE64:
    private void ParseFirstLine(String firstLine){
        //Split values at index iStart
        String[] sValues = firstLine.split(":"); //Ensure values after : are not accounted for
        sValues = sValues[0].split(";");
        String[] tmp;
        for(String value:sValues){
            if(value.toUpperCase().startsWith("ENCODING")){
                tmp=value.split("=");
                if(tmp.length>=2)
                    _photoEncodingMethod =tmp[1].trim();
            }
            if(value.toUpperCase().startsWith("TYPE")){
                tmp=value.split("=");
                if(tmp.length>=2)
                    _photoImageType =tmp[1].trim();
            }
        }

        if(_photoImageType ==""){ //search known type
            if(firstLine.toUpperCase().contains("JPEG"))
                _photoImageType ="JPEG";
            if(firstLine.toUpperCase().contains("PNG"))
                _photoImageType ="PNG";
        }
        if(_photoImageType =="") //Still empty default to JPEG
            _photoImageType ="JPEG";
    }

    //Returns number of lines read
    //PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE
    //CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/=
    //
    public int setPhotoEncodingAndMovePointer(String[] values,
                                              int iStart) {

        int ii = 0;
        String value=values[iStart];
        String [] sValues = value.split(":");
        _photoEncodedValue ="";
        ParseFirstLine(sValues[0]);
        if(sValues.length>=2) { //Set initial value
            if(!sValues[1].trim().equals(null))
                _photoEncodedValue = sValues[1].trim();
        }

        for (ii = iStart + 1; ii < values.length; ii++) {
            value = values[ii];
            if (value.trim().equals("")) break;
            _photoEncodedValue = _photoEncodedValue + value;
        }
        if (!_photoEncodingMethod.equals("BASE64") &&
                !_photoEncodingMethod.equals("B")){
            _photoEncodedValue ="";
            ii=iStart; //Start from same index as either data id corrupted
            //or there is no encoded data present. This will insure
            //other actual data is not bypassed
        }
        //Remove any spaces inside encoding
        _photoEncodedValue=_photoEncodedValue.replace(" ","");
        return ii;
    }

    public byte[] getDecodedPhotoBytes(){
        if(_photoEncodedValue.matches("")) return null;
        byte[] decodedBytes=null;

        try{
            if(_photoEncodingMethod.equals("BASE64") ||
                _photoEncodingMethod.equals("B")) {
            decodedBytes = Base64.decode(_photoEncodedValue,
                    Base64.DEFAULT);
            }
        }
        catch(Exception ex){
            decodedBytes=null;
        }
        return decodedBytes;
    }

    public void setPhoto(byte[] photo){
        try{
        _baPhoto=photo; // bug reported at this point
        setFormattedBase64PhotoEncoding();
        }
        catch(Exception ex){
            _baPhoto = null;
        }
    }

    public String getRawEncodedBytes(){
        return _photoEncodedValue;
    }

    //return base 64 encoding in JPEG
    private void setFormattedBase64PhotoEncoding(){
        try {
            Bitmap bmp;
            ByteArrayOutputStream baBitmapStream = new ByteArrayOutputStream();
            bmp = BitmapFactory.decodeByteArray(_baPhoto, 0, _baPhoto.length);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baBitmapStream);
            byte[] jpegBytes = baBitmapStream.toByteArray();
            String value = Base64.encodeToString(jpegBytes, Base64.DEFAULT);

            value = value.replace("\n", "\r\n");//'\n' is added by Base64 encoding
            //Remove any spaces inside encoding

            _photoEncodedValue = value.replace(" ", "");
            _photoEncodingMethod = "BASE64";
            _photoImageType = "JPEG";
        }
        catch(Exception ex){ //Do not store any info
            _photoEncodedValue = "";
            _photoEncodingMethod = "";
            _photoImageType = "";
            throw new RuntimeException();
        }
    }


    public String getPhotovCardString()
    {
        String value="PHOTO;ENCODING=" +
                _photoEncodingMethod + ";" +
                _photoImageType + ":" + "\r\n"+
                _photoEncodedValue +
                "" + "\r\n";
        return value;
    }
}
