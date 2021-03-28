package sample;


import java.util.*;

public class State {
    final static int N = 11; //board dimension
    final static int ROWS = N;
    final static int COLS = N;

    PieceType[][] stateMatrix;


    public State(PieceType[][] stateMatrix) {
        this.stateMatrix = stateMatrix;
    }


    State getClone() {
        PieceType[][] clonedMatrix = new PieceType[N][];

        for (int i = 0; i < N; i++)
            clonedMatrix[i] = stateMatrix[i].clone();

        return new State(clonedMatrix);
    }


    State cloneAndMakeMove(Move move) throws Exception {

        if (!positionIsInRange(move.position.row, move.position.col) || stateMatrix[move.position.row][move.position.col] != null) {
            throw new Exception(Thread.currentThread().getStackTrace()[1].getMethodName() + ": Invalid Position given to make move");
        }

        State clonedState = this.getClone();
        clonedState.stateMatrix[move.position.row][move.position.col] = move.pieceToBePlaced;

        return clonedState;
    }


    boolean positionIsInRange(int row, int col) {
        return row >= 0 && col >= 0 && row < State.ROWS && col < State.COLS;
    }


    boolean isEmpty(int row, int col) {
        return stateMatrix[row][col] == null;
    }


}
