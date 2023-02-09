package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/23/2016.
 */
public class BoardStandard extends BoardBase {
    public BoardStandard(final OnBoardDisplayStateChanged listnerBoardStateChange,
                       final OnBoardIsStale listenerBoardStale,
                       final OnBoardInitialized listenerBoardInitialized){
        super(listnerBoardStateChange,
                listenerBoardStale,
                listenerBoardInitialized);
        mMiniBoard = new MiniBoardFrench();
    }

    public void init(){
        mBoard=new BlockState[mSize][mSize];

        for(byte row = 0; row< mSize; row++)
            for(byte col = 0; col< mSize; col++){
                setValue(row,col,BlockState.isPeg);
                //setValue(row,col,BlockState.isPegHole);
            }
        setValue((byte)3,(byte)3,BlockState.isPegHole);
        /*setValue((byte)3,(byte)3,BlockState.isPeg);
        setValue((byte)3,(byte)4,BlockState.isPeg);
        setValue((byte)4,(byte)3,BlockState.isPeg);
        setValue((byte)4,(byte)4,BlockState.isPeg);*/

        mListenerBoardInitialized.onBoardInitialized();
    }

    //Set up for Standard Board
    @Override
    protected void setValue(byte row, byte col, BlockState value)
    {
        if(row<0 || col <0) return;//Dont do anything
        if(row >=2 && row <=4){ //Fill all columns
            mBoard[row][col] = value;
        }
        else{
            if(col>=2 && col<=4)
                mBoard[row][col] = value;
            else
                mBoard[row][col] = BlockState.isInValidLocation;
        }
        mListenerBoardStateChanged.onBoardStateChanged(row,col);
    }
}
