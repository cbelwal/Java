package com.dasmic.android.lib.support.Static;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;


import com.dasmic.android.lib.support.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Chaitanya Belwal on 7/29/2015.
 */
public class SupportFunctions {

    /// Displays Message Box
    public static void AsyncDisplayGenericDialog(Context context,
                                                 String sMessage,
                                                 String appName) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setMessage(sMessage);
        dlgAlert.setTitle(appName);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();

    }

    //Display a Yes/No Dialog and returns the answer

    public static void DebugLog(String sClass, String function, String message)
    {
        Log.d("coju.mobi", sClass + "::" + function + "::" + message);
    }

    public static void InfoLog(String sClass, String function, String message)
    {
        Log.i("coju.mobi", sClass + "::" + function + "::" + message);
    }

    public static void ErrorLog(String sClass, String function, String message)
    {
        Log.e("coju.mobi", sClass + "::" + function + "::" + message);
    }

    public static void DisplayToastMessageShort(Context context,
                                      String sMessage){
        Toast.makeText(context, sMessage, Toast.LENGTH_SHORT).show();

    }

    public static void vibrate(Context context,int milliSeconds){
        try {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(milliSeconds);
        }
        catch(Exception ex){
            //Dont do anything, not worth an exception
        }
    }

    public static void DisplayToastMessageLong(Context context,
                                                String sMessage){
        Toast.makeText(context,sMessage, Toast.LENGTH_LONG).show();
        context=null;
    }

    public static String getPackageLabel(Context context) {
        String retVal="";
        try {
            //android.content.pm.ApplicationInfo.loas(_context.getPackageManager())
            PackageInfo pinfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            CharSequence cs = pinfo.applicationInfo.loadLabel(context.getPackageManager());
            if(cs != null) retVal = String.valueOf(cs);
        } catch (Exception ex) {

        }

        return retVal;
    }

    public static void ShareStringDataViaIntent(Activity activity,
                                        String value,
                                                String title){
        Intent intent;
        intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, value);

        try {
            activity.startActivity(Intent.createChooser(intent,
                    title));
        }
        catch(Exception ex){
            DebugLog("SupportFunction",
                    "ShareString","Error:"+ex.getMessage());
        }
    }

    public static byte[] getBytesFromUri(Uri uri,Context context){
        byte[] data=null;
        try {
            InputStream iStream = context.getContentResolver().
                    openInputStream(uri);
            data = getBytes(iStream);
        }
        catch(FileNotFoundException ex){

        }
        catch(IOException ex){

        }

        return data;
    }

    private static byte[] getBytes(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static void OpenGooglePlayLink(Activity activity,
            String fullPackageName){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Try Google play
        intent.setData(Uri.parse(PackageServices.getGooglePlayLink(fullPackageName)));
        if (!StartActivity(activity,intent)) {
            //Market (Google play) app seems not installed, let's try to open a webbrowser
            intent.setData(Uri.parse(PackageServices.getGooglePlayWebLink(fullPackageName)));
            if (!StartActivity(activity,intent)) {
                //Well if this also fails, we have run out of options, inform the user.
                SupportFunctions.DisplayToastMessageLong(activity,
                        activity.getString(
                                R.string.message_rate_this_app_error).toString());
            }
        }
    }

    private static boolean StartActivity(Context context,
                                         Intent aIntent) {
        try
        {
            context.startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }

    public static void StartActivityForResult(
                                        Activity activity,
                                        Intent intent,
                                        int requestCode){
        try
        {
            activity.startActivityForResult(intent,
                    requestCode);
        }
        catch (ActivityNotFoundException paramMenuItem)
        {
            SupportFunctions.DisplayToastMessageLong(activity,
                    activity.getString(
                            R.string.message_start_activity_error).toString());

        }
    }

    public static void DisplayErrorToast(Activity context, String errorMessage){
        SupportFunctions.DisplayToastMessageLong(
                context, errorMessage);
        SupportFunctions.vibrate(context, 300);
    }
}
