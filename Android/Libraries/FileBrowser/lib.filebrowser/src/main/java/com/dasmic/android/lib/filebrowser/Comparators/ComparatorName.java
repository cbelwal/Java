package com.dasmic.android.lib.filebrowser.Comparators;

import com.dasmic.android.lib.filebrowser.Data.DataFileDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorName implements Comparator<DataFileDisplay> {

    public ComparatorName(){

    }

    @Override
    public int compare(DataFileDisplay o1, DataFileDisplay o2) {
        return 0 - o1.getName().compareToIgnoreCase(o2.getName());
    }
}
