package com.dasmic.android.lib.apk.Data;

import android.graphics.drawable.Drawable;

import com.dasmic.android.lib.support.Static.DateOperations;

/**
 * Created by Chaitanya Belwal on 8/6/2015.
 */
public class DataPackageDisplay implements
        Comparable<DataPackageDisplay> {

    private Drawable _icon;
    private String _packageDir;
    private String _packageName;
    private String _packageLabel;
    private String _primaryValue;
    private String _launchIntentClass;
    private String _secondaryValue;
    private String _tertiaryValue;
    private long _installedOnDate;
    private String _updatedDate;
    private double _size;
    private boolean _isSystemApp;
    private double _numberSortingValue;
    private String _strSortingValue;


    public boolean isChecked;
    @Override
    public String toString() {
        if(_primaryValue != null)
            return _primaryValue;
        else
            return _packageName;
    }

    public void setSortingValue(double numberSortingValue,
            String strSortingValue){
        _numberSortingValue=numberSortingValue;
        _strSortingValue=strSortingValue.trim().toUpperCase();
    }

    public double getNumberSortingValue(){
        return _numberSortingValue;
    }

    public String getStringSortingValue(){
        return _strSortingValue;
    }

    @Override
    public int compareTo(DataPackageDisplay obj)
    {
        int value=0;
        if(_strSortingValue.equals("")) //Number sorting
            if ((getNumberSortingValue() - obj.getNumberSortingValue())==0)
                return 0;
        if ((getNumberSortingValue() - obj.getNumberSortingValue())<0)
            return 1;
        if ((getNumberSortingValue() - obj.getNumberSortingValue())>0)
            return -1;

        else {
            value = getStringSortingValue().compareTo(obj.getStringSortingValue());
        }
        return value;
    }

    public DataPackageDisplay(String packageDir,
                              String packageName,
                              String packageLabel,
                              Drawable icon,
                              String launchIntentClass,
                              String primaryValue,
                              String secondaryValue,
                              double size,
                              boolean isSystemApp,
                              long installedDate,
                              String updatedDate){
        super();

        _packageDir=packageDir; //Serves as unique name
        _packageName=packageName;
        _packageLabel=packageLabel;
        _icon = icon;
        _launchIntentClass =launchIntentClass;
        _primaryValue = primaryValue;
        _secondaryValue = secondaryValue;
        _size=size;
        _isSystemApp=isSystemApp;
        _installedOnDate=installedDate;
        _updatedDate=updatedDate;
    }

    public void setTertiaryValue(String tertiaryValue){
        _tertiaryValue=tertiaryValue;
    }

    public Drawable getIcon(){
        return _icon;
    }
    public String getSecondaryValue(){
        return _secondaryValue;
    }
    public String getTertiaryValue(){
        return _tertiaryValue;
    }
    public String getPackageDir(){
        return _packageDir;
    }
    public String getLaunchIntentClass(){
        return _launchIntentClass;
    }
    public String getPackageLabel(){
        return _packageLabel;
    }
    public String getInstalledOnDate(){
        return DateOperations.getFormattedDate(
                _installedOnDate);
    }
    public long getInstalledOnDateLong(){
        return _installedOnDate;
    }
    public String getUpdatedDate(){
        return _updatedDate;
    }
    public String getPackageName(){
        return _packageName;
    }
    public boolean getIsSystemApp(){return _isSystemApp;}
    public double getSize(){return _size;}

    public String getGoogleStoreLink(){
        String link="https://play.google.com/store/apps/details?id="+
                _packageName;
        return link;
    }




}
