package com.dasmic.android.brainvita.Model;

import com.dasmic.android.brainvita.Data.BoardNodeSingle;

/**
 * Created by Chaitanya Belwal on 5/22/2016.
 */
public class SolverEngineFrench extends SolverEngineBase {
    //Customized for 7x7 Board
    @Override
    protected void solveBoard(BoardNodeSingle board, int count) {
        if(mAbort) return; //No More Processing
        if(hasEndBoardStateReached(board,count)) return;
        if(count <=33) //Store only when less than 30
            if(isStateAlreadySolved(board.mBoard)) return;

        updateStateCount();


        //Do middle Rows
        for (int row = 2; row <= 4; row++)
            for (int col = 0; col < 7; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        //Do Bottom Rows
        for (int row = 5; row <= 5; row++)
            for (int col = 1; col <=5; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        for (int row = 6; row <= 6; row++)
            for (int col = 2; col <=4; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        //Do Top Rows
        for (int row = 0; row <= 0; row++)
            for (int col = 2; col <=4 ; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col

        for (int row = 1; row <= 1; row++)
            for (int col = 1; col <=5 ; col++) {
                //Check for holes
                findMoves(board,row,col,count);
            } //for col





    }

    public SolverEngineFrench(OnSolverUpdate listenerSolverUpdate){
        super(listenerSolverUpdate);
    }

}
