package com.dasmic.android.lib.contacts.Interface;

import com.dasmic.android.lib.contacts.Data.DataContactDisplay;

import java.util.EventListener;

/**
 * Created by Chaitanya Belwal on 10/3/2015.
 */
public interface IGenericEvent extends EventListener {
    void onEvent(DataContactDisplay value);
}
