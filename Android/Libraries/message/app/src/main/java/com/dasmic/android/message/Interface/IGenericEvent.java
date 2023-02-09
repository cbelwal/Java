package com.dasmic.android.lib.message.Interface;

import com.dasmic.android.lib.message.Data.DataMessageDisplay;

import java.util.EventListener;

/**
 * Created by Chaitanya Belwal on 10/3/2015.
 */
public interface IGenericEvent extends EventListener {
    void onEvent(DataMessageDisplay value);
}
