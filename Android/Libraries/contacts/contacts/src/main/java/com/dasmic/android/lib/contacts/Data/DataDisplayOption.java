package com.dasmic.android.lib.contacts.Data;

import android.content.Context;

import com.dasmic.android.lib.contacts.Enum.DisplayOptionsEnum;
import com.dasmic.android.lib.contacts.R;

/**
 * Created by Chaitanya Belwal on 8/8/2015.
 */
public class DataDisplayOption {
    private DisplayOptionsEnum _displayOption;
    private Context _context;

    @Override
    public String toString() {
        switch(_displayOption){
            case AllInformation:return (String) _context.getResources().getText(R.string.display_options_all);
            case AllWithContactCount:return (String) _context.getResources().getText(R.string.display_options_all_TimesContacted);
            case AllWithLastContact:return (String) _context.getResources().getText(R.string.display_options_all_last_contact);
            case Email:return (String) _context.getResources().getText(R.string.display_options_email);
            case PhoneNumber:return (String) _context.getResources().getText(R.string.display_options_phonenumber);
            case Error:return (String) _context.getResources().getText(R.string.general_error);
            default:return "";
        }
    }

    public DisplayOptionsEnum getDisplayOption(){
        return  _displayOption;
    }

    public DataDisplayOption(DisplayOptionsEnum displayOption, Context context){
        _displayOption = displayOption;
        _context = context;
    }



}
