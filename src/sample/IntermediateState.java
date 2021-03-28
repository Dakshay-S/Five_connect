package sample;

import java.util.*;

public class IntermediateState {
    State state;
    private final static int MAX_BRANCHING_FACTOR = 80;
    private final static int MIN_BRANCHING_FACTOR = 2;
    private final static double ALPHA = -Math.log((float) MIN_BRANCHING_FACTOR / MAX_BRANCHING_FACTOR) / (MinMax.MAX_DEPTH * MinMax.MAX_DEPTH);


    public IntermediateState(State state) {
        this.state = state;
    }


    List<IntermediateState> getNextIntermediateStates(Player currPlayer, int currDepth,  int currMaxDepth) {


        int[][] prioritiesForSquares = Heuristic.getPrioritiesForSquares(currPlayer, state);

        PieceType playersPiece = (currPlayer == Player.BLACK ? PieceType.BLACK : PieceType.WHITE);


        List<Move> wiseMoves = new ArrayList<>();

        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (state.stateMatrix[i][j] != null)
                    continue;
                wiseMoves.add(new Move(new Position(i, j), playersPiece, prioritiesForSquares[i][j]));
            }
        }


        wiseMoves.sort(Comparator.reverseOrder());

        List<IntermediateState> nextIntermediateStates = new ArrayList<>(MAX_BRANCHING_FACTOR);

        int currBranchFactor = getBranchFactorForCurrDepth(currDepth, currMaxDepth);

        //todo
        //System.out.println("CurrMaxDepth = "+currMaxDepth+" currDepth = "+currDepth+" branch = "+currBranchFactor);

        int maxForLoop = Integer.min(currBranchFactor, wiseMoves.size());

        for (int i = 0; i < maxForLoop; i++) {
            try {
                nextIntermediateStates.add(new IntermediateState(this.state.cloneAndMakeMove(wiseMoves.get(i))));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }


        return nextIntermediateStates;
    }

    static int getBranchFactorForCurrDepth(int depth, int currMaxDepth) {
        int x = currMaxDepth - depth;
        double fx = MAX_BRANCHING_FACTOR * Math.pow(Math.E, -ALPHA * x * x);

        return (int) Math.round(fx);
    }

}
