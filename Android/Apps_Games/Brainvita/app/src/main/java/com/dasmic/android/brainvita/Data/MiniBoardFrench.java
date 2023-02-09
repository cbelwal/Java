package com.dasmic.android.brainvita.Data;

/**
 * Created by Chaitanya Belwal on 5/22/2016.
 */
public class MiniBoardFrench extends MiniBoardBase {

    public MiniBoardFrench(MiniBoardBase miniB){
        super(miniB);
    }

    public MiniBoardFrench(){
        super();
    }

    //Return if these is a single adjancency
    @Override
    public boolean isBoardStale(){
        for(byte row = 0; row<= 0; row++)
            for(byte col = 2; col<=4; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }

        for(byte row = 1; row<= 1; row++)
            for(byte col = 1; col<=5; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }

        //Middle Row
        for(byte row = 2; row<=4; row++)
            for(byte col = 0; col<=6; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }

        for(byte row = 5; row<= 5; row++)
            for(byte col = 1; col<=5; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }


        for(byte row = 6; row<= 6; row++)
            for(byte col = 2; col<=4; col++) {
                //Check Adjacency Bottom
                if(!isPositionStale(row,col)) return false;
            }
        return true;
    }
}
