package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;

/**
 * Created by Chaitanya Belwal on 4/25/2016.
 */
public class MiniBoardStandard extends MiniBoardBase {

    public MiniBoardStandard(MiniBoardBase miniB){
        super(miniB);
    }

    public MiniBoardStandard(){
        super();
    }

    //Return if these is a single adjancency
    @Override
    public boolean isBoardStale(){
        for(byte row = 0; row<= 1; row++)
            for(byte col = 2; col<=4; col++) {
                    //Check Adjacency Bottom
                    if(!isPositionStale(row,col)) return false;
                }

        for(byte row = 2; row<=4; row++)
            for(byte col = 0; col<=6; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }

        for(byte row = 5; row<= 6; row++)
            for(byte col = 2; col<=4; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }
        return true;
    }

}
