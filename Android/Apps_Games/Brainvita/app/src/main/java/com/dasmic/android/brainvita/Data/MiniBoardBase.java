package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;

/**
 * Created by Chaitanya Belwal on 4/25/2016.
 */
public class MiniBoardBase {
    //Storage -----
    //private byte[][] mBoard;
    protected long mBoard;
    protected static final byte mSize=7;
    //Storage -----
    public MiniBoardBase(){
        mBoard=0;
    }
    public byte getSize(){return mSize;}

/*
    public byte getValue(int row, int col) {
        return mBoard[row][col];
    }

    public void setValue(int row, int col, boolean value) {
        if(value)
            mBoard[row][col]=1;
        else
            mBoard[row][col]=0;
    }

      public static boolean isPositionAllowed(int row, int col){
        //Check if valid Row, Col for 4 corners
        if(row <2 && col <2) return false;
        if(row <2 && col >4) return false;
        if(row >4 && col <2) return false;
        if(row >4 && col >4) return false;
        return true;
    }

    /*private boolean isPositionStale(int row, int col){
        boolean val1,val2,val3;
        val1=getValue(row, col);

        if (row < mSize - 2 &&
            isPositionAllowed(row+1,col) &&
            isPositionAllowed(row+2,col)){
            val2=getValue(row + 1, col);
            val3=getValue(row + 2, col);
            if (val1 == true && //Down
                val2     == true &&
                    val3 == false)
                return false;

            if (val1 == false && //Down
                    val2 == true &&
                    val3 == true)
                return false;
        }

        //Check Adjacency Right
        if (col < mSize - 2&&
                isPositionAllowed(row,col+1) &&
                isPositionAllowed(row,col+2)){
            val2=getValue(row, col+1);
            val3=getValue(row, col+2);

            if (val1 == true && //Is a peg
                    val2 == true &&
                    val3 == false)
                return false;

            if (val1 == false && //Is a hole
                    val2 == true &&
                    val3 == true)
                return false;
        }
        return true;
    }*/


    public boolean getValue(int row, int col) {
        boolean value = ((mBoard &
                LongToBinary.getInstance().longBinary[row*7+col]) > 0)?true:false;
        return value;
    }

    public void setValue(int row, int col, boolean value)
    {
        if(value) //Set to 1
            mBoard=mBoard | LongToBinary.getInstance().longBinary[row*7+col];
        else
            mBoard=mBoard & ~(LongToBinary.getInstance().longBinary[row*7+col]);
    }


    public MiniBoardBase(MiniBoardBase miniB){
        mBoard = miniB.mBoard;
    }

    public int getPegCount() {
        //return mBoard.cardinality();
        int count = 0;
        for (int row = 0; row < mSize; row++)
            for (int col = 0; col < mSize; col++)
                if (getValue(row,col) == true) count++;
        return count;
    }

    public void setFromLargeBoard(BlockState[][] largeBoard){
        for(byte row=0;row<mSize;row++)
            for(byte col=0;col<mSize;col++){
                switch(largeBoard[row][col]){
                    case isPeg:
                    case isPegSelected:
                    case isPegSelectedInvalid:
                        setValue(row,col,true);
                        break;
                    case isPegHole:
                    case isReadyPegHole:
                        setValue(row,col,false);
                        break;
                    default:
                        setValue(row,col,false);
                        //mBoard[row][col]=-2;
                }
            }
    }

    public BlockState[][] getLargeBoard(){
        BlockState[][] largeBoard=new BlockState[mSize][mSize];
        for(byte row=0;row<mSize;row++)
            for(byte col=0;col<mSize;col++){
                largeBoard[row][col]=BlockState.isInValidLocation;
                if(getValue(row,col))
                    largeBoard[row][col]=BlockState.isPeg;
                else
                    largeBoard[row][col]=BlockState.isPegHole;
            }
        return largeBoard;
    }

    //Check for holes using same logic as in Solver
    protected boolean isPositionStale(int row, int col){
        if (getValue(row,col) == true) return true;//Is a peg
        if (col > 1)
            if (getValue(row,col - 1)==true &&
                    getValue(row,col - 2) == true)
                return false;
        //Check right
        if (col < mSize - 2)
            if (getValue(row,col + 1)==true &&
                    getValue(row,col + 2) == true)
                return false;
        //Check top
        if (row > 1)
            if (getValue(row - 1,col)==true &&
                    getValue(row - 2,col) == true)
                return false;

        //Check bottom
        if (row < mSize - 2)
            if (getValue(row + 1,col)==true &
                    getValue(row + 2,col) == true)
                return false;

        return true;
    }


    public long getHash() {
        return mBoard;
    }

    public boolean isBoardStale(){return true;}

        /*final int p = 16777619;
        long hash = 2166136261l;
        for(int row = 0; row< mBoard.length; row++) {
            for(int col = 0; col < mBoard[row].length; col++) {
                hash = (hash ^ mBoard[row][col] ^ (row * col)) * p;
            }
        }
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        return hash;*/

}
