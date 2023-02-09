package com.dasmic.android.lib.contacts.Activity;

import android.os.Bundle;

import com.dasmic.android.lib.contacts.Enum.DuplicateOptionsEnum;
import com.dasmic.android.lib.contacts.R;


/**
 * Created by Chaitanya Belwal on 9/5/2016.
 */
public class ActivityDuplicatesWhatsApp extends ActivityDuplicates {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTextForDuplicateOption(DuplicateOptionsEnum doe){
        String displayText;
        switch(doe){
            case NameMatchWithEmailPhone:
                displayText = (String)
                        this.getResources().getText(
                                R.string.duplicate_whatsapp_name_all_match);
                break;
            case SameEmailMultipleNames:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_whatsapp_same_number_multi_name);
                break;
            case None:
                displayText = (String) this.getResources().getText(
                        R.string.duplicate_options_none);
                break;
            default:
                displayText="";
                break;
        }
        return displayText;
    }
}
