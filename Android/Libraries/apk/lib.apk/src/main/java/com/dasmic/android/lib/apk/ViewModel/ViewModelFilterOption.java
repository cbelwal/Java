package com.dasmic.android.lib.apk.ViewModel;


import com.dasmic.android.lib.apk.Enum.FilterOptionsEnum;

/**
 * Created by Chaitanya Belwal on 8/30/2015.
 */
public class ViewModelFilterOption {
    //Using explicit name so that here is no confusion in VM/Model layers
    private boolean _values[];
    public boolean DisplaySystemApps;

    private void ResetValues() {
        for(FilterOptionsEnum fo: FilterOptionsEnum.values() )
            _values[fo.ordinal()]=false;
    }

    public ViewModelFilterOption(){
        _values = new boolean[FilterOptionsEnum.values().length];
        ResetValues();
        setSelectedOption(FilterOptionsEnum.NoFilter); //Set Default
        DisplaySystemApps=false;
    }

    public FilterOptionsEnum getSelectedOption(){
        for(FilterOptionsEnum fo: FilterOptionsEnum.values() ){
            if(_values[fo.ordinal()]==true) return fo;
        }
        return FilterOptionsEnum.NoFilter;
    }

    public void setSelectedOption(FilterOptionsEnum option){
        ResetValues();
        _values[option.ordinal()]=true;
    }


}
