package com.dasmic.android.brainvita.Model;

import com.dasmic.android.brainvita.Data.BoardNodeSingle;
import com.dasmic.android.brainvita.Data.MiniBoardBase;
import com.dasmic.android.brainvita.Data.MiniBoardStandard;
import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;

/**
 * Created by Chaitanya Belwal on 5/3/2016.
 */
public class SolverEngineBase {
    private Object syncLockNewThread;
    private Object syncLockBoardStale;
    private Object syncLockEndStateReached;
    private Object syncLockAddHash;
    protected OnSolverUpdate mListenerSolverUpdate;


    protected boolean mAbort;
    protected int mCurrentLowestPegCount;
    protected BoardNodeSingle mLowestCountBoardNodeLeaf;
    protected int mStateCount;
    protected int threadCount;
    protected Thread [] mThreads;
    protected HashMap<Long,Boolean> mBoardStates;
    protected Lock mLock;
    public static int MAX_STATES =3200000;
    public static int MAX_THREADS=124;


    protected BoardNodeSingle getNewBoard(MiniBoardBase miniB,
                                        BoardNodeSingle parent){
        BoardNodeSingle board=new BoardNodeSingle();
        board.mBoard = new MiniBoardStandard(miniB);
        board.parent=parent;
        return board;
    }

    //Assume Hash function is unique
    protected boolean isStateAlreadySolved(MiniBoardBase cBoard){
        long hash = cBoard.getHash();
        if(mBoardStates.containsKey(hash)) return true;
        else{
            synchronized (syncLockAddHash) {
                mBoardStates.put(hash, true);
            }
            return false;
        }
    }

    //Board End State
    protected  boolean hasEndBoardStateReached(
            BoardNodeSingle board, int count){
        boolean retValue=false;

        if(count <= AppOptions.MinimumSolverPegCount) {
            synchronized (syncLockEndStateReached) {
                //Run protected
                retValue = true;
                mLowestCountBoardNodeLeaf = board;
                mCurrentLowestPegCount = count;
                mAbort = true;
                SupportFunctions.DebugLog("SolverEngineBase",
                        "hasEndBoardStateReached", "Count:" + count);
            }//Synclock
        }
        else if(board.mBoard.isBoardStale()) {

            retValue= true;
            if(count >= mCurrentLowestPegCount) { //Board is useless
                board.parent = null;
            }
            else{ //Preserve board
                synchronized (syncLockBoardStale) {//Run protected
                    mLowestCountBoardNodeLeaf = board;
                    mCurrentLowestPegCount = count;
                }
            }
        }

        return retValue;
    }

    protected void updateStateCount(){
        if(mStateCount++ % 100==0){//Run protected

            mListenerSolverUpdate.onSolverUpdate(mStateCount,
                    mCurrentLowestPegCount);
            SupportFunctions.DebugLog("SolverEngineStandard","updateStateCount","Lowest Peg:"
                    + mCurrentLowestPegCount);
            try {
                Thread.sleep(50); //To refresh UI
            }
            catch(InterruptedException ex)
            {

            }
        }
    }

    //Customized for 7x7 Board
    protected void solveBoard(BoardNodeSingle board, int count) {}


    //Find moves, create new board and do recursive calls
    //Checks for Holes
    protected void findMoves(BoardNodeSingle board,
                           int row, int col, int count){
        int size = board.mBoard.getSize();
        MiniBoardBase miniB = board.mBoard;
        BoardNodeSingle newBoard;


        if (miniB.getValue(row,col) == false) { //Is a hole
            //Check left
            if (col > 1)
                if (miniB.getValue(row,col - 1)==true &&
                        miniB.getValue(row,col - 2) == true) {
                    newBoard =
                            getNewBoard(miniB, board);
                    newBoard.mBoard.setValue(row, col - 2, false);
                    newBoard.mBoard.setValue(row, col - 1, false);
                    newBoard.mBoard.setValue(row, col, true);
                    solveBoardThreaded(newBoard, count - 1);
                }

            //Check right
            if (col < size - 2)
                if (miniB.getValue(row,col + 1)==true &&
                        miniB.getValue(row,col + 2) == true) {
                    newBoard =
                            getNewBoard(miniB, board);
                    newBoard.mBoard.setValue(row, col + 2, false);
                    newBoard.mBoard.setValue(row, col + 1, false);
                    newBoard.mBoard.setValue(row, col, true);
                    solveBoardThreaded(newBoard, count - 1);
                }

            //Check top
            if (row > 1)
                if (miniB.getValue(row - 1,col)==true &&
                        miniB.getValue(row - 2,col) == true) {
                    newBoard =
                            getNewBoard(miniB, board);
                    newBoard.mBoard.setValue(row - 2, col, false);
                    newBoard.mBoard.setValue(row - 1, col, false);
                    newBoard.mBoard.setValue(row, col, true);
                    solveBoardThreaded(newBoard, count - 1);
                }

            //Check bottom
            if (row < size - 2)
                if (miniB.getValue(row + 1,col)==true &
                        miniB.getValue(row + 2,col) == true) {
                    newBoard =
                            getNewBoard(miniB, board);
                    newBoard.mBoard.setValue(row + 2, col, false);
                    newBoard.mBoard.setValue(row + 1, col, false);
                    newBoard.mBoard.setValue(row, col, true);
                    solveBoardThreaded(newBoard, count - 1);
                }
        }
    }

    protected void solveBoardThreaded(final BoardNodeSingle board,
                                      final int count){
        Thread th=null;

            SupportFunctions.DebugLog("SolverEnginerBase",
                    "solveBoardThreaded","threadCount:" +threadCount);

            if (threadCount < MAX_THREADS) {
                synchronized (syncLockNewThread) {
                    if(threadCount < MAX_THREADS) {
                        mThreads[threadCount] = new Thread() {
                            @Override
                            public void run() {
                                solveBoard(board, count);
                            }
                        };
                        th = mThreads[threadCount];
                        threadCount++;
                    } //Secondary if
                }//Synchronized
        }
        if(th != null) {
             th.start();
        }
        else
             solveBoard(board, count);
    }

    private void waitForThreadsToJoin(){
        SupportFunctions.DebugLog("SolverEnginerBase",
                            "waitForThreadsJoin","In Function");
        if(mThreads.length<=0) return; //No threads
        //Wait till first thread is not null
        while(mThreads[0] == null && !mAbort);
        boolean bFlag=true;
        while(bFlag && !mAbort){
            bFlag=false;
            for(int ii=0;ii<threadCount;ii++)
            if(mThreads[ii] != null){
                if(mThreads[ii].isAlive())
                {
                    bFlag=true;
                    try {
                        mThreads[ii].join();
                    }
                    catch(InterruptedException ex){

                    }
                }
            }
        }
        SupportFunctions.DebugLog("SolverEnginerBase",
                "waitForThreadsJoin","Out of Function");
    }

    public int getCurrentLowestCount(){
        return mCurrentLowestPegCount;
    }

    public ArrayList<MiniBoardBase> getCurrentLowestBoard(){
        ArrayList<MiniBoardBase> allBoardStates=
                new ArrayList<>();

        BoardNodeSingle  boardParent= mLowestCountBoardNodeLeaf;
        while(boardParent != null){
            allBoardStates.add(boardParent.mBoard);
            boardParent=boardParent.parent;
        }
        return allBoardStates;
    }


    public void startSolver(MiniBoardStandard startBoard){
        BoardNodeSingle bns=new BoardNodeSingle();
        bns.mBoard=startBoard;
        bns.parent=null;
        mCurrentLowestPegCount =startBoard.getPegCount();
        mBoardStates = new HashMap<>();
        mThreads = new Thread[MAX_THREADS];
        mAbort=false;
        threadCount=0;
        syncLockNewThread = new Object();
        syncLockEndStateReached = new Object();
        syncLockBoardStale=new Object();
        syncLockAddHash = new Object();
        solveBoard(bns, mCurrentLowestPegCount);
        
        //Wait for all threads to join
        waitForThreadsToJoin();
    }

    public SolverEngineBase(OnSolverUpdate listenerSolverUpdate){
        mAbort=false;
        mListenerSolverUpdate= listenerSolverUpdate;
    }

    public void abortComputation(){
        mAbort=true;
    }

    public interface OnSolverUpdate {
        void onSolverUpdate( int currentState,int lowestCount);
    }

}
