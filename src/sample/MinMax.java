package sample;

import java.util.List;

public class MinMax {

    final static Player MAX = Player.WHITE;
    final static Player MIN = Player.BLACK;
    final static int MAX_DEPTH = 6;

    int alpha = Integer.MAX_VALUE;
    int beta = Integer.MIN_VALUE;
    MinMax parent ;

    public MinMax(MinMax parent) {
        this.parent = parent;
    }


    public int getParentAlpha() {
        return parent == null ? Integer.MAX_VALUE : parent.alpha;
    }

    public int getParentBeta() {
        return parent == null ? Integer.MIN_VALUE : parent.beta;
    }

}

class MAX_Player extends MinMax {

    public MAX_Player(MinMax parent) {
        super(parent);
    }

    IntermediateState solveForMax(IntermediateState given, int depth, int currMaxDepth, Player currPlayer , Player max_player) {

        int heuristicValueForMaxPlayer = Heuristic.getEvaluation(given.state, max_player, currPlayer);

        if(heuristicValueForMaxPlayer == Heuristic.DEFINITE_WIN){
            PieceType playersPiece = (currPlayer == Player.BLACK ? PieceType.BLACK : PieceType.WHITE);
            Position winPosition = Heuristic.definiteWinPosition(given.state, currPlayer);
            State clonedState = given.state.getClone();
            assert winPosition != null;
            clonedState.stateMatrix[winPosition.row][winPosition.col] = playersPiece;

            this.beta = heuristicValueForMaxPlayer;
            return new IntermediateState(clonedState);
        }



        if(depth <= 0){
            this.beta = heuristicValueForMaxPlayer;
            return null;
        }


        List<IntermediateState> nextStatesForMaxPlayer = given.getNextIntermediateStates(max_player,depth,currMaxDepth);
        IntermediateState nextBestState = null;

        for(IntermediateState state: nextStatesForMaxPlayer)
        {
            // BETA PRUNING
            if(this.beta >= this.getParentAlpha())
                break;


            MIN_Player MIN = new MIN_Player(this);
            MIN.solveForMin(state, depth-1,currMaxDepth, currPlayer == Player.WHITE? Player.BLACK : Player.WHITE , max_player);

            if(MIN.alpha > this.beta) {
                nextBestState = state;
                this.beta = MIN.alpha;
            }
        }

        return nextBestState;
    }
}


class MIN_Player extends MinMax
{
    public MIN_Player(MinMax parent) {
        super(parent);
    }

    IntermediateState solveForMin(IntermediateState given, int depth, int currMaxDepth, Player currPlayer , Player max_player)
    {

        int heuristicValueForMaxPlayer = Heuristic.getEvaluation(given.state, max_player,currPlayer);
        if(heuristicValueForMaxPlayer == Heuristic.DEFINITE_LOSE || depth <= 0){
            this.alpha = heuristicValueForMaxPlayer;
            return null;
        }

        List<IntermediateState> nextStatesForMinPlayer = given.getNextIntermediateStates(max_player.getOpponent(),depth,currMaxDepth);


        IntermediateState nextBestState = null;

        for(IntermediateState state: nextStatesForMinPlayer)
        {
            // ALPHA PRUNING
            if(this.alpha <= this.getParentBeta())
                break;


            MAX_Player MAX = new MAX_Player(this);
            MAX.solveForMax(state, depth-1,currMaxDepth, currPlayer == Player.WHITE? Player.BLACK: Player.WHITE, max_player);

            if(MAX.beta < this.alpha) {
                nextBestState = state;
                this.alpha = MAX.beta;
            }
        }

        return nextBestState;
    }
}
