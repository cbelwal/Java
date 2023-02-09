package com.dasmic.android.videolib.Comparators;


import com.dasmic.android.videolib.Data.DataVideoDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorName implements Comparator<DataVideoDisplay> {

    public ComparatorName(){

    }

    @Override
    public int compare(DataVideoDisplay o1, DataVideoDisplay o2) {
        return 0 - o1.getName().compareToIgnoreCase(o2.getName());
    }
}
