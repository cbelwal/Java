package com.dasmic.android.lib.audio.Comparators;


import com.dasmic.android.lib.audio.Data.DataAudioDisplay;

import java.util.Comparator;

/**
 * Created by Chaitanya Belwal on 12/11/2017.
 */

public class ComparatorName implements Comparator<DataAudioDisplay> {

    public ComparatorName(){

    }

    @Override
    public int compare(DataAudioDisplay o1, DataAudioDisplay o2) {
        return 0 - o1.getName().compareToIgnoreCase(o2.getName());
    }
}
