package com.dasmic.android.lib.apk.ViewModel;

import android.app.Activity;
import android.content.Intent;

import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Model.ModelPackageRead;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.support.Static.SupportFunctions;


import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 1/17/2016.
 */
public class ViewModelLinksReport {
    Activity _activity;
    boolean _isMalwareProgram;
    //Ctor
    public ViewModelLinksReport(Activity activity, boolean isMalwareProgram){
        _activity = activity;
        _isMalwareProgram=isMalwareProgram;
    }

    public void SendPackageLinks(
            ArrayList<DataPackageDisplay> allDpd){

        String value=_activity.getResources().getText(
                R.string.message_share_link_prefix).toString() +
                "\r\n\r\n";
        for(DataPackageDisplay dpd:allDpd){
                value=value+dpd.getGoogleStoreLink() + "\r\n\n";
        }

        value=value+_activity.getResources().getText(
                R.string.message_share_link_suffix).toString() +
                "\r\n\r\n";

        SupportFunctions.ShareStringDataViaIntent(_activity,
                value, _activity.getString(
                        R.string.title_share_link));

    }

    public void SendPackageReports(
            ArrayList<DataPackageDisplay> allDpd){
        ModelPackageRead mpr=new ModelPackageRead(_activity,_isMalwareProgram);

        String value=_activity.getResources().getText(
                R.string.message_share_report_prefix) +
                "\r\n\r\n";
        for(DataPackageDisplay dpd:allDpd){
            value=value+getReport(dpd, mpr) + "\r\n";
        }

        value=value+_activity.getResources().getText(
                R.string.message_share_report_suffix) + " " +
                _activity.getString(
                        R.string.app_name) +
                "\r\n";

        SupportFunctions.ShareStringDataViaIntent(_activity,
                value, _activity.getString(
                        R.string.title_report));

    }

    private String getReport(DataPackageDisplay dpd,
                             ModelPackageRead mpr){
        String report="";
        report = report + _activity.getResources().getText(
                R.string.report_app_name)
                + dpd.getPackageLabel() +"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_package_name) +
                dpd.getPackageName()+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_source_folder) + dpd.getPackageDir()+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_size) +
                String.format("%.2f",
                         dpd.getSize())+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_installed_on) +
                dpd.getInstalledOnDate()+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_updated_on) +
                dpd.getUpdatedDate()+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_system_app) +
                isSystemApp(dpd)+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_launch_intent) +
                dpd.getLaunchIntentClass()+"\r\n";
        report = report + _activity.getResources().getText(
                R.string.report_security_permission) + "\r\n" +
                mpr.getSecurityPermissions(dpd.getPackageName())+"\r\n";

        return report;
    }

    private String isSystemApp(DataPackageDisplay dpd){
        return dpd.getIsSystemApp()?_activity.getResources().getText(
                R.string.report_yes).toString():
                _activity.getResources().getText(
                        R.string.report_no).toString();
    }

    private void SendData(String value){
        Intent intent;
        intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, value);

        _activity.startActivity(Intent.createChooser(intent,
                "Share links"));
    }

}
