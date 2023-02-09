package com.dasmic.android.lib.calllog.Interface;

import com.dasmic.android.lib.calllog.Data.DataCallLogDisplay;

import java.util.EventListener;

/**
 * Created by Chaitanya Belwal on 10/3/2015.
 */
public interface IGenericEvent extends EventListener {
    void onEvent(DataCallLogDisplay value);
}
