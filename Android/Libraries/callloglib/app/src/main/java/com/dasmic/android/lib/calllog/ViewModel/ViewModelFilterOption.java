package com.dasmic.android.lib.calllog.ViewModel;

/**
 * Created by Chaitanya Belwal on 3/25/2017.
 */

public class ViewModelFilterOption {
    //Should be in number sequence
    //
    public static final int OptionShowWithStoredContacts =0;
    public static final int OptionShowMoreThanDuration =1;
    public static final int OptionShowLessThanDuration =2;
    public static final int OptionShowMoreThanDays =3;
    public static final int OptionShowLessThanDays =4;
    public static final int OptionShowFromLocation=5;
    public static final int OptionShowNone =6;
    private int _optionsCount= OptionShowNone;

    private boolean _showSelection;
    private String[] _optionValue;
    private int _currentOption;

    public ViewModelFilterOption(){
        _showSelection= true;//Show the current selection
        _currentOption = OptionShowNone;
        _optionsCount= OptionShowNone + 1;
        _optionValue = new String [_optionsCount];

        initOptionValues();
    }

    private void initOptionValues(){
        for(int ii=0;ii<_optionsCount;ii++)
            _optionValue[ii]="";
    }

    public int getOptionsCount(){
        return _optionsCount;
    }

    public void setCurrentOption(int selection){
        _currentOption = selection;
    }

    public int getCurrentOption(){
        return _currentOption;
    }

    public void setOptionValue(String selectionValue, int idx){
        _optionValue[idx] = selectionValue;
    }

    public String getOptionValue(int idx){
        return _optionValue[idx];
    }
    public void setShowSelection(boolean value){
        _showSelection=value;
    }
    public boolean getShowSelection(){
        return _showSelection;
    }

    //Value of selection
}
