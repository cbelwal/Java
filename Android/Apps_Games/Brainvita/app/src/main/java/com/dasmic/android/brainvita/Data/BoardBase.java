package com.dasmic.android.brainvita.Data;

import com.dasmic.android.brainvita.Enum.BlockState;

import java.util.ArrayList;

/**
 * Created by Chaitanya Belwal on 4/23/2016.
 */
public class BoardBase {
    protected final int mSize =7;
    protected DataBlock _lastClickedBlock;
    protected ArrayList<DataBlock> mListReadyHoles;
    protected MiniBoardBase mMiniBoard;
    protected boolean mIsCustomBoard;

    //Events
    protected OnBoardDisplayStateChanged mListenerBoardStateChanged;
    protected OnBoardIsStale mListenerBoardIsStale;
    protected OnBoardDataStateChanging mListenerBoardDataStateChanging;
    protected OnBoardDataStateChanged mListenerBoardDataStateChanged;
    protected OnBoardInitialized mListenerBoardInitialized;

    protected BlockState[][] mBoard;

    protected void ApplyBoardLogicOnClick(byte rowClicked,
                                        byte colClicked){
        //   mBoard[rowClicked][colClicked]=BlockState.isPegSelected;

        if(mBoard[rowClicked][colClicked]==BlockState.isPeg)
            ApplyBoardLogic_ClickPeg(rowClicked,
                    colClicked);
        else if(mBoard[rowClicked][colClicked]==BlockState.isReadyPegHole)
            ApplyBoardLogic_ClickReadyPegHole(rowClicked,
                    colClicked);

    }

    protected void ApplyBoardLogic_ClickReadyPegHole(byte rowClicked,
                                                   byte colClicked){
        switch(_lastClickedBlock.getBS()){ //Current Click
            case isPeg: //Undo previous Peg
            case isPegSelected:
                cleanReadyHoles();
                if(mListenerBoardDataStateChanging != null)
                    mListenerBoardDataStateChanging.onBoardDataStateChanging();
                setValue(_lastClickedBlock.getRow(),_lastClickedBlock.getCol(),
                        BlockState.isPegHole);
                setValue(rowClicked,colClicked,
                        BlockState.isPeg);
                removeMiddlePeg(_lastClickedBlock.getRow(),_lastClickedBlock.getCol(),
                        rowClicked,colClicked);
                if(mListenerBoardDataStateChanged != null)
                    mListenerBoardDataStateChanged.onBoardDataStateChanged();
                if (isBoardStale()) //After move check board state
                    mListenerBoardIsStale.onBoardIsStale();
                break;
            default:
        }
        _lastClickedBlock.setNullValues();
    }

    //Return if these is a single adjancency
    protected boolean isBoardStale(){
        mMiniBoard.setFromLargeBoard(mBoard);
        return mMiniBoard.isBoardStale();
    }


    protected void removeMiddlePeg(byte rowLast,byte colLast,
                                 byte rowClicked, byte colClicked){
        byte diff;
        if(rowLast==rowClicked) {
            if (colClicked - colLast > 0) { //Assume ready holes are marked correctly
                setValue(rowLast, (byte) (colLast + 1), BlockState.isPegHole);
            }
            else
                setValue(rowLast, (byte) (colLast - 1), BlockState.isPegHole);
        }
        else if(colLast==colClicked){
            if (rowClicked - rowLast > 0)
                setValue((byte)(rowLast+1), colLast, BlockState.isPegHole);
            else
                setValue((byte)(rowLast-1), colLast, BlockState.isPegHole);
        }

    }

    protected void ApplyBoardLogic_ClickPeg(byte rowClicked,
                                          byte colClicked){

        switch(_lastClickedBlock.getBS()){ //Current Click
            case isPeg: //Undo previous Peg
            case isPegSelected:
            case isPegSelectedInvalid:
                setValue(_lastClickedBlock.getRow(),_lastClickedBlock.getCol(),
                        BlockState.isPeg);
                break;
            default:
        }
        if(markReadyHoles(rowClicked, colClicked))
            setValue(rowClicked,colClicked,
                    BlockState.isPegSelected);
        else
            setValue(rowClicked,colClicked,
                    BlockState.isPegSelectedInvalid);

        _lastClickedBlock.setValues(rowClicked,colClicked,
                BlockState.isPeg);
    }

    protected void cleanReadyHoles(){
        for(DataBlock db:mListReadyHoles){
            setValue(db.getRow(),db.getCol(),BlockState.isPegHole);
        }
        mListReadyHoles.clear();
    }

    protected void setReadyHole(byte rowClicked, byte colClicked){
        setValue(rowClicked, colClicked,
                BlockState.isReadyPegHole);
        mListReadyHoles.add(new DataBlock(rowClicked,colClicked));
    }

    //Returns true is single hole available
    protected boolean markReadyHoles(byte rowClicked, byte colClicked){
        boolean retVal=false;
        cleanReadyHoles(); //clean previous selection
        if(rowClicked -2 >= 0 &&
                mBoard[rowClicked-1][colClicked]==BlockState.isPeg &&
                mBoard[rowClicked-2][colClicked]==BlockState.isPegHole) {
            setReadyHole((byte) (rowClicked - 2), colClicked);
            retVal=true;
        }
        if(rowClicked +2 < getSize() &&
                mBoard[rowClicked+1][colClicked]==BlockState.isPeg &&
                mBoard[rowClicked+2][colClicked]==BlockState.isPegHole) {
            setReadyHole((byte) (rowClicked + 2), colClicked);
            retVal=true;
        }
        if(colClicked -2 >= 0 &&
                mBoard[rowClicked][colClicked-1]==BlockState.isPeg &&
                mBoard[rowClicked][colClicked-2]==BlockState.isPegHole) {
            setReadyHole(rowClicked, (byte) (colClicked - 2));
            retVal=true;
        }
        if(colClicked + 2 < getSize() &&
                mBoard[rowClicked][colClicked+1]==BlockState.isPeg &&
                mBoard[rowClicked][colClicked+2]==BlockState.isPegHole) {
            setReadyHole(rowClicked, (byte) (colClicked + 2));
            retVal=true;
        }
        return retVal;
    }


    public BoardBase(final OnBoardDisplayStateChanged listnerBoardStateChange,
                         final OnBoardIsStale listenerBoardStale,
                         final OnBoardInitialized listenerBoardInitialized){
        mListenerBoardStateChanged =listnerBoardStateChange;
        mListenerBoardIsStale = listenerBoardStale;
        mListenerBoardInitialized = listenerBoardInitialized;

        _lastClickedBlock=new DataBlock();
        mListReadyHoles=new ArrayList<>();
        setIsCustomBoardSetup(false);
    }

    public boolean getIsCustomBoardSetup(){
        return mIsCustomBoard;
    }

    public void setIsCustomBoardSetup(boolean value){
        mIsCustomBoard =value;

        if(mIsCustomBoard ==true){
            clean();
        }
        else{ //Raise event to initialize board
            mListenerBoardInitialized.onBoardInitialized();
        }

    }

    public void setDataStateListeners(final OnBoardDataStateChanging listenerDatastateChanging,
                                      final OnBoardDataStateChanged listenerDatastateChanged){
        mListenerBoardDataStateChanging = listenerDatastateChanging;
        mListenerBoardDataStateChanged = listenerDatastateChanged;
    }

    public BlockState[][] getBoardState(){
        BlockState[][] currentState=new BlockState[mSize][mSize];
        for(byte row = 0; row< mSize; row++)
            for(byte col = 0; col< mSize; col++){
                currentState[row][col]=mBoard[row][col];
            }
        return currentState;
    }

    public int getSize(){
        return mSize;
    }

    public void setCurrentBoardStateTo(BlockState[][] newState){
        for(byte row = 0; row< mSize; row++)
            for(byte col = 0; col< mSize; col++){
                switch(newState[row][col])
                {
                    case isPeg:
                    case isPegSelected:
                    case isPegSelectedInvalid:
                        mBoard[row][col]=BlockState.isPeg;
                        break;
                    case isPegHole:
                    case isReadyPegHole:
                        mBoard[row][col]=BlockState.isPegHole;
                        break;
                    default:
                        mBoard[row][col]=newState[row][col];
                }
                setValue(row,col,mBoard[row][col]);
            }
    }



    public void clean(){
        mBoard=new BlockState[mSize][mSize];

        for(byte row = 0; row< mSize; row++)
            for(byte col = 0; col< mSize; col++){
                setValue(row,col,BlockState.isPegHole);
            }
        mListenerBoardInitialized.onBoardInitialized();
    }

    public BlockState getImage(int row, int col){
        return mBoard[row][col];
    }

    public int getCount(){
        return mMiniBoard.getPegCount();
    }

    public void setClick(byte row,byte col){
        if(mIsCustomBoard){
            if(mBoard[row][col]==BlockState.isPegHole)
                setValue(row,col,BlockState.isPeg); //Allow for switch
            else
                setValue(row,col,BlockState.isPegHole);

            mListenerBoardDataStateChanged.onBoardDataStateChanged();
        }
        else {

            ApplyBoardLogicOnClick(row, col);

        }
    }

    public interface OnBoardDisplayStateChanged {
        void onBoardStateChanged(int row,
                                 int col);
    }

    public interface OnBoardIsStale {
        void onBoardIsStale();
    }

    public interface OnBoardDataStateChanging {
        void onBoardDataStateChanging();
    }

    public interface OnBoardDataStateChanged {
        void onBoardDataStateChanged();
    }

    public interface OnBoardInitialized {
        void onBoardInitialized();
    }

    protected void setValue(byte row, byte col, BlockState value){}
    public void init(){} //Show be overridden
}
