package com.dasmic.android.brainvita.Model;

import com.dasmic.android.brainvita.Data.BoardNodeSingle;
import com.dasmic.android.brainvita.Data.MiniBoardStandard;
import com.dasmic.android.brainvita.Enum.AppOptions;
import com.dasmic.android.lib.support.Static.SupportFunctions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Chaitanya Belwal on 5/3/2016.
 */
public class SolverEngineStandard extends SolverEngineBase{

    //Customized for 7x7 Board
    @Override
    protected void solveBoard(BoardNodeSingle board, int count) {
        if(mAbort) return; //No More Processing
        if(hasEndBoardStateReached(board,count)) return;
        if(count <=27) //Store only when less than 27
            if(isStateAlreadySolved(board.mBoard)) return;

        SupportFunctions.DebugLog("SolverEngineStandard",
                "solverBoard","Count:"+count);

        updateStateCount();

        int size = board.mBoard.getSize();
        //Do middle Rows
        for (int row = 2; row <= 4; row++)
            for (int col = 0; col < 7; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        //Do Top Rows
        for (int row = 0; row <= 1; row++)
            for (int col = 2; col <=4 ; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        //Do Bottom Rows
        for (int row = 5; row <= 6; row++)
            for (int col = 2; col <=4; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col
    }


    public SolverEngineStandard(OnSolverUpdate listenerSolverUpdate){
        super(listenerSolverUpdate);
    }


}
