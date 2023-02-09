package com.dasmic.android.lib.apk.Model;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.provider.Settings;


import com.dasmic.android.lib.apk.Data.DataPackageDisplay;
import com.dasmic.android.lib.apk.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.apk.Enum.FilterAdvOptionsEnum;
import com.dasmic.android.lib.apk.R;
import com.dasmic.android.lib.apk.ViewModel.ViewModelAdvFilterOption;
import com.dasmic.android.lib.apk.ViewModel.ViewModelFilterOption;
import com.dasmic.android.lib.support.Static.DateOperations;
import com.dasmic.android.lib.support.Static.FileOperations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Chaitanya Belwal on 8/29/2015.
 */
public class ModelPackageRead {
    private Context _context;
    private ViewModelFilterOption _filterOption;
    private ViewModelAdvFilterOption _advFilterOption;
    ModelSecurityReadableText _modelSRT;
    private boolean _useAdvancedFilter;

    public ModelPackageRead(Context context, boolean isMalwareProgram) {
        _context=context;
        _filterOption = new ViewModelFilterOption();
        _advFilterOption = new ViewModelAdvFilterOption();
        _modelSRT = new ModelSecurityReadableText(context,
                isMalwareProgram);
    }

    public void setUseAdvancedFilter(){
        _useAdvancedFilter=true;
    }


    public ArrayList<DataPackageDisplay>
        getAllInformation(DisplayOptionsEnum doe) {
        if(doe == DisplayOptionsEnum.CurrentlyRunning)
            return getAllInformationRunningPackages(doe);
        else
            return getAllInformationPackages(doe);
    }

    //Versions before Android Lollipop (5.1) had
    //getRunningAppProcesses() working
    private List<PackageInfo>
        getAllInformationRunningPackagesPostLollipop() {

        Hashtable<String,PackageInfo> hashAllPackages = new Hashtable<>();
        final ActivityManager activityManager = (ActivityManager)
                _context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> procInfos =
                activityManager.getRunningServices(Integer.MAX_VALUE);
        PackageManager pm = _context.getPackageManager();

        if (procInfos != null)
        {
            for (final ActivityManager.RunningServiceInfo rsi : procInfos) {
                String pkg=rsi.service.getPackageName();
                if(!hashAllPackages.containsKey(pkg)) { //Use HashTable
                    try {
                        PackageInfo pi = pm.getPackageInfo(pkg, 0); // use appropriate flag
                        hashAllPackages.put(pkg, pi);
                    } catch (PackageManager.NameNotFoundException ex) {

                    }
                }
            }
        }
        Collection<PackageInfo> colAllPackages = hashAllPackages.values();
        List<PackageInfo> packages= new ArrayList<>(colAllPackages);

        return  packages;
    }

    //Versions before Android Lollipop (5.1) had
    //getRunningAppProcesses() working
    private List<PackageInfo>
        getAllInformationRunningPackagesPreLollipop() {
        List<PackageInfo> packages = new ArrayList<>();

        final ActivityManager activityManager = (ActivityManager)
                _context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos =
                activityManager.getRunningAppProcesses();
        PackageManager pm = _context.getPackageManager();

        if (procInfos != null) {
            //String currentPackage = _context.getPackageName();
            for (final ActivityManager.RunningAppProcessInfo rapi : procInfos) {
                for (String pkg : rapi.pkgList) {
                    try {
                        //if(pkg.equals(currentPackage)) { //Do not add current package name
                        PackageInfo pi = pm.getPackageInfo(pkg, 0); // use appropriate flag
                        if (!packages.contains(pi))
                            packages.add(pi);
                        //}
                    } catch (PackageManager.NameNotFoundException ex) {

                    }
                }
            }
        }
        return packages;
    }

    private ArrayList<DataPackageDisplay>
                getAllInformationRunningPackages(DisplayOptionsEnum doe) {

        List<PackageInfo> packages;
        //Make sure system packages are also displayed
        //_filterOption.DisplaySystemApps=false;
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            packages = getAllInformationRunningPackagesPostLollipop();
        else
            packages = getAllInformationRunningPackagesPreLollipop();
        return getAllInformation(doe,packages);
    }

    //Get Names
    private ArrayList<DataPackageDisplay>
                getAllInformationPackages(DisplayOptionsEnum doe) {
        //get a list of installed apps.
        List<PackageInfo> packages = _context.getPackageManager().getInstalledPackages(
                PackageManager.GET_PERMISSIONS);

        return getAllInformation(doe,packages);
    }


    private ArrayList<DataPackageDisplay> getAllInformation(
                                            DisplayOptionsEnum doe,
                                            List<PackageInfo> packages){
        ArrayList<DataPackageDisplay> allDpd = new ArrayList<>();
        ApplicationInfo ai;
        DataPackageDisplay dpd;
        PackageManager pm = _context.getPackageManager();

        for (PackageInfo pi : packages) {
            ai = pi.applicationInfo;
            if(!(!isDisplaySystemPackages() &&
                    isSystemPackage(pi))) { //Truth table deciphered condition
                if (checkFilter(pm, pi, ai)) {
                    Drawable icon = ai.loadIcon(pm);

                    dpd = new DataPackageDisplay(ai.sourceDir,
                            ai.packageName,
                            ai.loadLabel(pm).toString(),
                            icon,
                            getLaunchIntentClass(pm, pi),
                            ai.loadLabel(pm).toString(),
                            FileOperations.getFileSizeString(ai.sourceDir),
                            FileOperations.getFileSizeDoubleMB(ai.sourceDir),
                            isSystemPackage(pi),
                            pi.firstInstallTime,
                            DateOperations.getFormattedDate(
                                    pi.lastUpdateTime));

                    setTertiaryString(doe,
                            dpd, pm, pi, ai);
                    allDpd.add(dpd);
                }
            }

        }
        Collections.sort(allDpd);
        return allDpd;
    }


    private boolean isDisplaySystemPackages(){
        if(_useAdvancedFilter && _advFilterOption !=null)
                return _advFilterOption.DisplaySystemApps;
        if(_filterOption != null) return _filterOption.DisplaySystemApps;
        return false;//default
    }

    private void setTertiaryString(DisplayOptionsEnum doe,
                                     DataPackageDisplay dpd,
                                     PackageManager pm,
                                     PackageInfo pi,
                                     ApplicationInfo ai){
        String value="";

        switch (doe){
            case defaultView:
                dpd.setTertiaryValue("");
                dpd.setSortingValue(0, dpd.getPackageLabel());
                break;
            case CurrentlyRunning:
                dpd.setTertiaryValue("");
                dpd.setSortingValue(0, dpd.getPackageLabel());
                break;
            case InstalledOnDate:
                dpd.setTertiaryValue(dpd.getInstalledOnDate());
                dpd.setSortingValue(pi.firstInstallTime,"");
                break;
            case LaunchIntent:
                value=getLaunchIntentClass(pm, pi);
                if(value.equals(""))
                    value = _context.getResources().getText(
                        R.string.general_not_Available).toString();
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(0.0,dpd.getPackageLabel());
                break;
            case PackageSourceName:
                value = ai.sourceDir;
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(0.0,dpd.getPackageLabel());
                break;
            case SecurityPermissions:
                value = getSecurityPermissions(pi.packageName);
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(0.0,dpd.getPackageLabel());
                break;
            case UpdatedOnDate:
                value = dpd.getUpdatedDate();
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(pi.lastUpdateTime,"");
                break;
            case LastUsedDate:
                long date =FileOperations.getLastModifiedData(dpd.getPackageDir());
                value = DateOperations.getFormattedDate(date);
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(date,"");
                break;
            case PackageSize:
                value = "";
                dpd.setTertiaryValue(value);
                dpd.setSortingValue(FileOperations.getFileSizeDoubleMB(
                                ai.sourceDir),
                        "");
                break;
            default:
                break;
        }

    };



    private String getLaunchIntentClass(PackageManager pm,
                                        PackageInfo pi){
        String value="";
        if(pm.getLaunchIntentForPackage(pi.packageName) != null)
            value = pm.getLaunchIntentForPackage(pi.packageName).
                    getComponent().getClassName(); // toString();
        return value;
    }

    public String getSecurityPermissions(String packageName){
        String value="";
        String tmpVal="";
        PackageInfo packI=null;
        try {
            packI = _context.getPackageManager().getPackageInfo(
                    packageName,
                    PackageManager.GET_PERMISSIONS);
        }
        catch(PackageManager.NameNotFoundException ex){

        }
        String [] appP = packI.requestedPermissions;

        if(appP != null)
            for(String s:appP){
                tmpVal=_modelSRT.getFormattedSecurityPermission(s);
                if(!tmpVal.equals(""))
                    value = value + tmpVal+ "\r\n";
            }
        return value;
    }

    private String getLaunchIntent(PackageManager pm,
                                        PackageInfo pi){
        String value="";
        if(pm.getLaunchIntentForPackage(pi.packageName) != null)
            value = pm.getLaunchIntentForPackage(pi.packageName).toString();
        return value;
    }

    private boolean checkFilter(PackageManager pm,
                                PackageInfo pi,
                                ApplicationInfo ai){
        if(_useAdvancedFilter)
            return checkFilterAdv(pm, pi, ai);
        else
            return checkFilterSimple(pm, pi, ai);

    }

    private boolean checkFilterAdv(PackageManager pm,
                                      PackageInfo pi,
                                      ApplicationInfo ai){
        int idx=0;
        if(!_advFilterOption.ActivateFilter)
            return true;//No filter is required if 'No Filter' is selected show all
        for(FilterAdvOptionsEnum fo: FilterAdvOptionsEnum.values()){
            if(_advFilterOption.getSelectedOption(idx))
                if(checkFilterAdvOptions(pm, pi, ai,
                        _advFilterOption.getAdvFilterOptionAtIdx(idx)))
                    return true;
                    idx++;
        }
        return false;
    }

    private boolean checkFilterAdvOptions(PackageManager pm,
                                   PackageInfo pi,
                                   ApplicationInfo ai,
                                   FilterAdvOptionsEnum aoe){
        switch(aoe){
            case ShowAccessLocation:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        ||checkSecurityPermission(pi.packageName,
                        Manifest.permission.ACCESS_FINE_LOCATION) )
                    return true;
                break;
            case ShowSecurityMic:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.RECORD_AUDIO))
                    return true;
                break;
            case ShowSecurityCamera:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.CAMERA))
                    return true;
                break;

            case ShowInstalledLast30days:
                if(pi.firstInstallTime
                        >= DateOperations.get30DayMilliseconds())
                    return true;
                break;
            case ShowSecurityReadCalender:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CALENDAR))
                    return true;
                break;
            case ShowSecurityReadCallLog:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CALL_LOG))
                    return true;
                break;
            case ShowSecurityReadContacts:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CONTACTS))
                    return true;
                break;
            case ShowSecurityReadSMS:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_SMS))
                    return true;
                break;
            case ShowSecuritySendSMS:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.SEND_SMS))
                    return true;
                break;
            case ShowSecurityWriteExternalStorage:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    return true;
                break;
            case ShowSecurityInAppBilling:
                if(checkSecurityPermission(pi.packageName,
                        "com.android.vending.BILLING")) //Replace by Enum when found
                    return true;
                break;
            default:
                break;
        }
        return false;

    }

    //Return true if allowed
    private boolean checkFilterSimple(PackageManager pm,
                                PackageInfo pi,
                                ApplicationInfo ai){
        switch(_filterOption.getSelectedOption()){
            case ShowOnlyLessThan20KB:
                if(FileOperations.getFileSizeDoubleMB(ai.sourceDir)
                    <=20) return true;
                break;
            case ShowOnlyMoreThan20KB:
                if(FileOperations.getFileSizeDoubleMB(ai.sourceDir)
                        >20) return true;
                break;

            case ShowOnlyInstalledLast30days:
                if(pi.firstInstallTime
                            >= DateOperations.get30DayMilliseconds())
                    return true;
                break;
            case ShowOnlyInstalledLast1Year:
                if(pi.firstInstallTime
                        >= DateOperations.getYearMilliseconds())
                    return true;
                break;
            case ShowOnlySecurityReadCalender:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CALENDAR))
                    return true;
                break;
            case ShowOnlySecurityReadCallLog:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CALL_LOG))
                    return true;
                    break;
            case ShowOnlySecurityReadContacts:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_CONTACTS))
                    return true;
                break;
            case ShowOnlySecurityReadSMS:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.READ_SMS))
                    return true;
                break;

            case ShowOnlySecurityWriteExternalStorage:
                if(checkSecurityPermission(pi.packageName,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    return true;
                break;
            case ShowOnlySecurityInAppBilling:
                if(checkSecurityPermission(pi.packageName,
                        "com.android.vending.BILLING")) //Replace by Enum when found
                    return true;
                break;
            case NoFilter:
                return true;

            default:
                break;
        }
        return false;
    }

    private boolean checkSecurityPermission(String packageName,
                                            String reqPermission){
        PackageInfo pi=null;
        try {
            pi = _context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_PERMISSIONS);
        }
        catch(PackageManager.NameNotFoundException ex){

        }
        String [] appP = pi.requestedPermissions;
        if(appP != null) {
            for (String p : appP) {
                if (p.equals(reqPermission))
                    return true;
            }
        }
        return false;//Not found
    }

    private boolean isSystemPackage(PackageInfo pi){
        return ((pi.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private boolean isCurrentlyRunning(PackageInfo pi){
        return ((pi.applicationInfo.flags &
                ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    public ViewModelFilterOption getFilterOption(){
        return _filterOption;
    }
    public ViewModelAdvFilterOption getAdvFilterOption(){
        return _advFilterOption;
    }


}
