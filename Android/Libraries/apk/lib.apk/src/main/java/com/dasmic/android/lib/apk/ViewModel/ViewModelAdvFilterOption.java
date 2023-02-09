package com.dasmic.android.lib.apk.ViewModel;

import com.dasmic.android.lib.apk.Enum.FilterAdvOptionsEnum;
import com.dasmic.android.lib.apk.Enum.FilterOptionsEnum;

/**
 * Created by Chaitanya Belwal on 8/30/2015.
 */
public class ViewModelAdvFilterOption {
    //Using explicit name so that here is no confusion in VM/Model layers
    private boolean _values[];
    public boolean DisplaySystemApps;
    public boolean ActivateFilter;

    public void ResetValues() {
        for(FilterAdvOptionsEnum fo: FilterAdvOptionsEnum.values())
            _values[fo.ordinal()] = true;
        ActivateFilter=false;
    }

    public ViewModelAdvFilterOption(){
        _values = new boolean[FilterOptionsEnum.values().length];
        ResetValues();
        DisplaySystemApps=false;
    }

    public boolean getSelectedOption(int idx){
        return _values[idx];// FilterOptionsEnum.NoFilter;
    }

    public FilterAdvOptionsEnum getAdvFilterOptionAtIdx(int idx ){
        return FilterAdvOptionsEnum.values()[idx];
    }

    public boolean isSelectedOption(FilterOptionsEnum fo){
        return _values[fo.ordinal()];
    }

    public void setSelectedOption(int idx,boolean value){
        _values[idx]=value;
    }


}
