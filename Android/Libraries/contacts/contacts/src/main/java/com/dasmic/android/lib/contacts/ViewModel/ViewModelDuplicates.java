package com.dasmic.android.lib.contacts.ViewModel;

import android.graphics.Color;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;
import com.dasmic.android.lib.contacts.Enum.DuplicateOptionsEnum;
import com.dasmic.android.lib.contacts.Extension.ListViewAdapter;
import com.dasmic.android.lib.support.Static.SupportFunctions;

/**
 * Created by Chaitanya Belwal on 2/15/2016.
 */
public class ViewModelDuplicates {

    public ViewModelDuplicates(){

    }

    public void modifyForDuplicatesList(ListViewAdapter lvm,
                                    DataContactDisplay dcd,
                                    DuplicateOptionsEnum doe,
                                    int startPosition){
        if(dcd.isChecked || dcd.isDuplicateMaster) return; //Dont do anything
        boolean isMatch;
        DataContactDisplay lDcd; //New function

        for(int ii=startPosition;ii<lvm.getCount();ii++) {
            lDcd = lvm.getItem(ii);
            isMatch = false;

            if (true) //They can have same raw contacts Ids as they are loaded from Data table
                {
                switch (doe) {
                    case NameMatchWithEmailPhone:
                        if (isPrimaryValueMatch(dcd, lDcd) &&
                                isSecondaryValueMatch(dcd, lDcd, 0))
                            isMatch = true;
                        break;
                    case NameAndPhoneMatch:
                        if (isPrimaryValueMatch(dcd, lDcd) &&
                                isSecondaryValueMatch(dcd, lDcd, 1))
                            isMatch = true;
                        break;
                    case NameAndEmailMatch:
                        if (isPrimaryValueMatch(dcd, lDcd) &&
                                isSecondaryValueMatch(dcd, lDcd, 2))
                            isMatch = true;
                        break;
                    case SameNumberMultipleNames:
                        if (isSecondarySamePrimaryDifferent(dcd, lDcd, false))
                            isMatch = true;
                        break;
                    case SameEmailMultipleNames:
                        if (isSecondarySamePrimaryDifferent(dcd, lDcd, true))
                            isMatch = true;
                        break;
                    default:
                        break;
                }
                if (isMatch) {
                    dcd.isDuplicateMaster = true;
                    dcd.colorDuplicates = //Set color only in this location
                            Color.argb((int)(dcd.getRawContactId()%90),
                                    (int)(dcd.getRawContactId()%90+
                                            dcd.getPrimaryValue().length()*3)
                                            %255,
                                    (int)(dcd.getRawContactId()*3)%255, //Green
                                    (dcd.getSecondaryValue().length()*3)
                                            %255);
                    if (!lDcd.isDuplicateMaster) {
                        lDcd.isChecked = true;
                        lDcd.isDuplicateSlave=true;
                        lDcd.colorDuplicates=dcd.colorDuplicates; //Copy color from Master
                    }
                }
            }
        }
    }

    private boolean isPrimaryValueMatch(DataContactDisplay dcd1,
                                        DataContactDisplay dcd2){
        String val1=dcd1.getPrimaryValue().toUpperCase().replace(" ","");
        String val2=dcd2.getPrimaryValue().toUpperCase().replace(" ","");
        if(val1.equals(val2))
                return true;
        else
            return false;
    }

    //option = 0 Both
    //option = 1 Phone
    //option = 2 Email
    private boolean isSecondaryValueMatch(DataContactDisplay dcd1,
                                        DataContactDisplay dcd2,
                                          int option){
        String val1=dcd1.getSecondaryValue().toUpperCase().replace(" ","");
        String val2=dcd2.getSecondaryValue().toUpperCase().replace(" ","");

        //Check if same type phone or email
        if(!areValuesSameType(val1,val2)) return false;

        if(option==2) { //Dont go further if both not email
            if (!isEmail(val1)) return false;
        }

        if(option==1) { //Dont go further if both not phone
            if (isEmail(val1)) return false;
        }

        if(!isEmail(val2) && !isEmail(val1)) { //if comparing both
            val1 = cleanPhoneNumber(val1);
            val2 = cleanPhoneNumber(val2);
        }

        if(val1.equals(val2))
            return true;
        else
            return false;
    }

    private boolean areValuesSameType(String val1, String val2){
        if(isEmail(val1) && !isEmail(val1)) return false;
        if(isEmail(val2) && !isEmail(val1)) return false;
        return true;
    }

    private boolean isEmail(String value){
        if(value.contains("@")) return true;
        else return false;
    }

    private String cleanPhoneNumber(String pNumber){
        //Remove spaces
        pNumber=pNumber.trim().replace(" ","");
        //Remove +,(),-
        pNumber=pNumber.replace("+","");
        pNumber=pNumber.replace("(","");
        pNumber=pNumber.replace(")","");
        pNumber=pNumber.replace("-","");
        return pNumber;
    }

    private boolean isSecondarySamePrimaryDifferent(DataContactDisplay dcd1,
                                                    DataContactDisplay dcd2,
                                                    boolean isEmail) {
        int val=isEmail?2:1;
        if(isPrimaryValueMatch(dcd1,dcd2)) return false; //Should be in duplicates
        if(isSecondaryValueMatch(dcd1,dcd2,val))
            return  true;
        else return false;
    }

    public ListViewAdapter RemoveNonDuplicatesFromAdapter(
            ListViewAdapter lvm){
        DataContactDisplay dcd;
        for(int ii=0;ii<lvm.getCount();ii++) {
            dcd = lvm.getItem(ii);
            if(dcd != null){
                if(!(dcd.isDuplicateSlave ||
                        dcd.isDuplicateMaster)){
                    lvm.remove(dcd);
                    ii--;
                }
            }
        }
        return lvm;
    }
}
