package com.dasmic.android.lib.apk.Model;

import android.Manifest;
import android.content.Context;

import com.dasmic.android.lib.apk.R;

/**
 * Created by Chaitanya Belwal on 7/9/2016.
 */
public class ModelSecurityReadableText {
    Context _context;
    boolean _isMalwareProgram;
    public ModelSecurityReadableText(Context context,
                                     boolean isMalwareProgram){
        _context=context;
        _isMalwareProgram=isMalwareProgram;
    }

    private String getRiskySecurityPermissions_ReadableText(String value){
        String retVal;
        switch(value.trim()){
            case Manifest.permission.READ_CALL_LOG:
                retVal= _context.getString(R.string.permission_read_call_log);
                break;
            case Manifest.permission.READ_CALENDAR:
                retVal= _context.getString(R.string.permission_read_calender);
                break;
            case Manifest.permission.WRITE_CALENDAR:
                retVal= _context.getString(R.string.permission_write_calender);
                break;
            case Manifest.permission.CAMERA:
                retVal= _context.getString(R.string.permission_camera);
                break;
            case "com.android.vending.BILLING":
                retVal= _context.getString(R.string.permission_inapp_billing);
                break;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                retVal= _context.getString(R.string.permission_location_fine);
                break;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                retVal= _context.getString(R.string.permission_location_coarse);
                break;
            case Manifest.permission.RECORD_AUDIO:
                retVal= _context.getString(R.string.permission_mic);
                break;
            case Manifest.permission.READ_CONTACTS:
                retVal= _context.getString(R.string.permission_read_contacts);
                break;
            case Manifest.permission.WRITE_CONTACTS:
                retVal= _context.getString(R.string.permission_write_contacts);
                break;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
                retVal= _context.getString(R.string.permission_read_ext_storage);
                break;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                retVal= _context.getString(R.string.permission_write_ext_storage);
                break;
            case Manifest.permission.READ_SMS:
                retVal= _context.getString(R.string.permission_read_sms);
                break;
            case Manifest.permission.SEND_SMS:
                retVal= _context.getString(R.string.permission_send_sms);
                break;
            case Manifest.permission.CALL_PHONE:
                retVal= _context.getString(R.string.permission_call_phone);
                break;
            case Manifest.permission.CALL_PRIVILEGED:
                retVal= _context.getString(R.string.permission_call_privileged);
                break;
            case Manifest.permission.INSTALL_PACKAGES:
                retVal= _context.getString(R.string.permission_call_privileged);
                break;
            case Manifest.permission.INTERNET:
                retVal= _context.getString(R.string.permission_internet);
                break;
            case Manifest.permission.WAKE_LOCK:
                retVal= _context.getString(R.string.permission_wake_lock);
                break;
            case Manifest.permission.ACCESS_NETWORK_STATE:
                retVal= _context.getString(R.string.permission_network_state);
                break;
            case Manifest.permission.READ_VOICEMAIL:
                retVal= _context.getString(R.string.permission_read_voicemail);
                break;
            default:
                retVal="";
                break;
        }
        return retVal;
    }


    public String getFormattedSecurityPermission(String value) {
        String retVal;

        if(_isMalwareProgram)
            return getRiskySecurityPermissions_ReadableText(value);
        else {
            retVal = value.replace("android.permission.", "");
            return retVal;
        }
    }
}
