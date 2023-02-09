package com.dasmic.android.lib.message.Activity;

/**
 * Created by Chaitanya Belwal on 8/19/2017.
 */

public class ActivityMessageDetails {
    private static final ActivityMessageDetails ourInstance = new ActivityMessageDetails();

    public static ActivityMessageDetails getInstance() {

        return ourInstance;
    }

    private ActivityMessageDetails() {
    }
}
