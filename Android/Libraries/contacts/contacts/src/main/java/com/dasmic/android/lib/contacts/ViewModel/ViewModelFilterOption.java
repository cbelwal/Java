package com.dasmic.android.lib.contacts.ViewModel;

import com.dasmic.android.lib.contacts.Enum.FilterOptionsEnum;

/**
 * Created by Chaitanya Belwal on 8/30/2015.
 */
public class ViewModelFilterOption {
    //Using explicit name so that here is no confusion in VM/Model layers
    private boolean _values[];

    private void ResetValues() {
        for(FilterOptionsEnum fo: FilterOptionsEnum.values() )
            _values[fo.ordinal()]=false;
    }

    public ViewModelFilterOption(){
        _values = new boolean[FilterOptionsEnum.values().length];
        ResetValues();
        setSelectedOption(FilterOptionsEnum.None); //Set Default
    }

    public FilterOptionsEnum getSelectedOption(){
        for(FilterOptionsEnum fo: FilterOptionsEnum.values() ){
            if(_values[fo.ordinal()]==true) return fo;
        }
        return FilterOptionsEnum.None;
    }

    public void setSelectedOption(FilterOptionsEnum option){
        ResetValues();
        _values[option.ordinal()]=true;
    }


}
