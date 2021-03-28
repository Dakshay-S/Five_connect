package sample;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Stack;

public class Heuristic {

    final static int MULTIPLIER_FOR_NON_CONTINUOUS_STREAK = 420;
    final static int ONE_STREAK_POINT = MULTIPLIER_FOR_NON_CONTINUOUS_STREAK;
    final static int TWO_STREAK_POINT = 2 * MULTIPLIER_FOR_NON_CONTINUOUS_STREAK;
    final static int THREE_STREAK_POINT = 3 * MULTIPLIER_FOR_NON_CONTINUOUS_STREAK;
    final static int ONE_SIDED_FOUR_STREAK_POINTS = 10 * MULTIPLIER_FOR_NON_CONTINUOUS_STREAK;
    final static int TWO_SIDED_FOUR_STREAK_POINTS = 20 * MULTIPLIER_FOR_NON_CONTINUOUS_STREAK;
    final static int FULL_FOCUS_POINTS = 10000;
    final static int GAME_FINISHER_POINTS = 99999;
    final static int DEFINITE_WIN = Integer.MAX_VALUE - 2;
    final static int DEFINITE_LOSE = -DEFINITE_WIN;

    static int getEvaluation(State state,Player maxPlayer,  Player currPlayer) {
        return getHeuristic(state, maxPlayer, currPlayer);
    }

    private static int getHeuristic(State state, Player max_player, Player currPlayer) {

        Player min_player = max_player.getOpponent();

        int maxPlayerPoints = pointsForPlayer(state, max_player);
        int minPlayerPoints = pointsForPlayer(state, min_player);

        if (currPlayer == max_player && maxPlayerPoints == DEFINITE_WIN)
            return DEFINITE_WIN;
        else if (currPlayer != max_player && minPlayerPoints == DEFINITE_WIN)
            return DEFINITE_LOSE;
        else return maxPlayerPoints - minPlayerPoints;

    }

    private static int pointsForPlayer(State state, Player player) {

        PieceType playersPiece = (player == Player.BLACK ? PieceType.BLACK : PieceType.WHITE);
        Position position = new Position(0, 0);

        int totalPoints = 0;


        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {

                if (state.stateMatrix[i][j] != playersPiece)
                    continue;

                position.row = i;
                position.col = j;


                int cont_horizontal = getContiguousHorizontalHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_horizontal == DEFINITE_WIN)
                    return DEFINITE_WIN;

                int cont_vertical = getContiguousVerticalHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_vertical == DEFINITE_WIN)
                    return DEFINITE_WIN;

                int cont_posSlope = getContiguousPosSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_posSlope == DEFINITE_WIN)
                    return DEFINITE_WIN;

                int cont_negSlope = getContiguousNegSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_negSlope == DEFINITE_WIN)
                    return DEFINITE_WIN;

                int nonCont_horizontal = getNonContiguousHorizontalHeuristicPoints(state.stateMatrix, playersPiece, position);
                int nonCont_vertical = getNonContiguousVerticalHeuristicPoints(state.stateMatrix, playersPiece, position);
                int nonCont_posSlope = getNonContiguousPosSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);
                int nonCont_negSlope = getNonContiguousNegSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);


                totalPoints += cont_horizontal + nonCont_horizontal + cont_vertical + nonCont_vertical + cont_posSlope + nonCont_posSlope + cont_negSlope + nonCont_negSlope;

            }

        }

        return totalPoints;
    }


    static Position definiteWinPosition(State state, Player player)
    {
        PieceType playersPiece = (player == Player.BLACK ? PieceType.BLACK : PieceType.WHITE);
        Position position = new Position(0, 0);

        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {

                if (state.stateMatrix[i][j] != playersPiece)
                    continue;

                position.row = i;
                position.col = j;


                int cont_horizontal = getContiguousHorizontalHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_horizontal == DEFINITE_WIN)
                    return position;

                int cont_vertical = getContiguousVerticalHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_vertical == DEFINITE_WIN)
                    return position;

                int cont_posSlope = getContiguousPosSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_posSlope == DEFINITE_WIN)
                    return position;

                int cont_negSlope = getContiguousNegSlopeHeuristicPoints(state.stateMatrix, playersPiece, position);
                if (cont_negSlope == DEFINITE_WIN)
                    return position;

            }

        }

        return null;
    }


    static int[][] getPrioritiesForSquares(Player player, State state) {
        PieceType playersPiece = (player == Player.BLACK ? PieceType.BLACK : PieceType.WHITE);

        PieceType[][] matrix = state.stateMatrix;

        int[][] priorityMatrix = new int[State.ROWS][State.COLS];

        Position position = new Position(0, 0);

        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (matrix[i][j] == null) {
                    position.row = i;
                    position.col = j;

                    try {
                        int priority = getAttackPoints(state, playersPiece, position) + getDefencePoints(state, playersPiece, position);
                        priorityMatrix[i][j] = priority;
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }

                }

            }

        }

        return priorityMatrix;

    }

    private static int getAttackPoints(State state, PieceType attackingPiece, Position position) throws Exception {
        PieceType[][] matrix = state.stateMatrix;


        if (position.row < 0 || position.row >= State.ROWS || position.col < 0 || position.col >= State.COLS) {
            throw new Exception(Thread.currentThread().getStackTrace()[1].getMethodName() + " : Out of bound cell given to calculate points");
        }


        if (matrix[position.row][position.col] != null) {
            throw new Exception(Thread.currentThread().getStackTrace()[1].getMethodName() + " : Non empty cell given to calculate points");
        }


        int totalAttackPoints = 0;


        totalAttackPoints += getContiguousHorizontalPoints(matrix, attackingPiece, position) + getNonContiguousHorizontalPoints(matrix, attackingPiece, position);
        totalAttackPoints += getContiguousVerticalPoints(matrix, attackingPiece, position) + getNonContiguousVerticalPoints(matrix, attackingPiece, position);
        totalAttackPoints += getContiguousPosSlopePoints(matrix, attackingPiece, position) + getNonContiguousPosSlopePoints(matrix, attackingPiece, position);
        totalAttackPoints += getContiguousNegSlopePoints(matrix, attackingPiece, position) + getNonContiguousNegSlopePoints(matrix, attackingPiece, position);


        return totalAttackPoints;
    }


    private static int getDefencePoints(State state, PieceType defendingPiece, Position position) throws Exception {
        PieceType[][] matrix = state.stateMatrix;


        if (position.row < 0 || position.row >= State.ROWS || position.col < 0 || position.col >= State.COLS) {
            throw new Exception(Thread.currentThread().getStackTrace()[1].getMethodName() + " : Out of bound cell given to calculate points");
        }


        if (matrix[position.row][position.col] != null) {
            throw new Exception(Thread.currentThread().getStackTrace()[1].getMethodName() + " : Non empty cell given to calculate points");
        }


        int totalDefencePoints = 0;

        PieceType opponentPiece = defendingPiece.getOppositePiece();


        totalDefencePoints += getContiguousHorizontalPoints(matrix, opponentPiece, position) + getNonContiguousHorizontalPoints(matrix, opponentPiece, position);
        totalDefencePoints += getContiguousPosSlopePoints(matrix, opponentPiece, position) + getNonContiguousPosSlopePoints(matrix, opponentPiece, position);
        totalDefencePoints += getContiguousNegSlopePoints(matrix, opponentPiece, position) + getNonContiguousNegSlopePoints(matrix, opponentPiece, position);
        totalDefencePoints += getContiguousVerticalPoints(matrix, opponentPiece, position) + getNonContiguousVerticalPoints(matrix, opponentPiece, position);

        return totalDefencePoints;
    }


    static int getContiguousHorizontalPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {
        int numOfFreeEnds;
        int totalPoints = 0;


        int horizontalStreak = 1;
        int nextLeftPointer = position.col - 1;
        int nextRightPointer = position.col + 1;
        while ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == interestedPiece) ||
                (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == interestedPiece)) {

            if (nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == interestedPiece) {
                nextLeftPointer--;
                horizontalStreak++;
            }

            if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == interestedPiece) {
                nextRightPointer++;
                horizontalStreak++;
            }
        }


        numOfFreeEnds = 0;
        if ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == null))
            numOfFreeEnds++;
        if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == null)
            numOfFreeEnds++;

        switch (horizontalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += FULL_FOCUS_POINTS;
                break;
            default:    // streak > 4
                totalPoints += GAME_FINISHER_POINTS;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }

        return totalPoints;
    }

    static int getContiguousVerticalPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int verticalStreak = 1;
        int nextBottomPointer = position.row - 1;
        int nextTopPointer = position.row + 1;
        while ((nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == interestedPiece) ||
                (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == interestedPiece)) {

            if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == interestedPiece) {
                nextBottomPointer--;
                verticalStreak++;
            }

            if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == interestedPiece) {
                nextTopPointer++;
                verticalStreak++;
            }

        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;

        if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == null)
            numOfFreeEnds++;
        if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == null)
            numOfFreeEnds++;

        switch (verticalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += FULL_FOCUS_POINTS;
                break;
            default:    // Streak > 4
                totalPoints += GAME_FINISHER_POINTS;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }


        return totalPoints;
    }

    static int getContiguousPosSlopePoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {


        int posSlopeDiagonalStreak = 1;
        int nextLowerLeftPointer = -1;
        int nextUpperRightPointer = 1;
        while ((positionIsInRange(position.row + nextLowerLeftPointer, position.col + nextLowerLeftPointer) && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == interestedPiece) ||
                (positionIsInRange(position.row + nextUpperRightPointer, position.col + nextUpperRightPointer) && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == interestedPiece)) {

            if (position.row + nextLowerLeftPointer >= 0 && position.col + nextLowerLeftPointer >= 0 && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == interestedPiece) {
                nextLowerLeftPointer--;
                posSlopeDiagonalStreak++;
            }

            if (position.row + nextUpperRightPointer < State.ROWS && position.col + nextUpperRightPointer < State.COLS && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == interestedPiece) {
                nextUpperRightPointer++;
                posSlopeDiagonalStreak++;
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;


        if (positionIsInRange(position.row + nextLowerLeftPointer, position.col + nextLowerLeftPointer) && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row + nextUpperRightPointer, position.col + nextUpperRightPointer) && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == null)
            numOfFreeEnds++;


        switch (posSlopeDiagonalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += FULL_FOCUS_POINTS;
                break;
            default:    // Streak > 4
                totalPoints += GAME_FINISHER_POINTS;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }

        return totalPoints;

    }

    static int getContiguousNegSlopePoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int negSlopeDiagonalStreak = 1;
        int nextUpperLeftPointer = 1;
        int nextLowerRightPointer = 1;
        while ((positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == interestedPiece) ||
                (positionIsInRange(position.row - nextLowerRightPointer, position.col + nextLowerRightPointer) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == interestedPiece)) {

            if (positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == interestedPiece) {
                nextUpperLeftPointer++;
                negSlopeDiagonalStreak++;
            }

            if (positionIsInRange(position.row - nextLowerRightPointer, position.col + nextLowerRightPointer) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == interestedPiece) {
                nextLowerRightPointer++;
                negSlopeDiagonalStreak++;
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;

        if (positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row - nextLowerRightPointer, position.col + nextLowerRightPointer) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == null)
            numOfFreeEnds++;

        switch (negSlopeDiagonalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += FULL_FOCUS_POINTS;
                break;
            default:    // Streak > 4
                totalPoints += GAME_FINISHER_POINTS;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }


        return totalPoints;
    }

    static int getNonContiguousHorizontalPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int numOfFreeEnds;
        int totalPoints;


        int samePieceCount = 1;
        int nextLeftPointer = position.col - 1;
        int nextRightPointer = position.col + 1;


        int leftMostIndexOfAttackingPiece = position.col;
        int rightMostIndexOfAttackingPiece = position.col;

        for (int i = 0; i < 4; i++) {
            if (nextLeftPointer >= 0) {
                if (matrix[position.row][nextLeftPointer] == null)
                    nextLeftPointer--;

                else if (matrix[position.row][nextLeftPointer] == interestedPiece) {
                    leftMostIndexOfAttackingPiece = nextLeftPointer;
                    nextLeftPointer--;
                    samePieceCount++;
                }
            }

            if (nextRightPointer < State.COLS) {

                if (matrix[position.row][nextRightPointer] == null)
                    nextRightPointer++;

                else if (matrix[position.row][nextRightPointer] == interestedPiece) {
                    rightMostIndexOfAttackingPiece = nextRightPointer;
                    nextRightPointer++;
                    samePieceCount++;
                }
            }
        }

        int potentialStreakLength = rightMostIndexOfAttackingPiece - leftMostIndexOfAttackingPiece + 1;


        numOfFreeEnds = 0;
        if ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == null))
            numOfFreeEnds++;
        if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;
    }

    static int getNonContiguousVerticalPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int samePieceCount = 1;
        int nextBottomPointer = position.row - 1;
        int nextTopPointer = position.row + 1;

        int bottomMostIndexOfAttackingPiece = position.row;
        int topMostIndexOfAttackingPiece = position.row;

        for (int i = 0; i < 4; i++) {

            if (nextBottomPointer >= 0) {
                if (matrix[nextBottomPointer][position.col] == null)
                    nextBottomPointer--;

                else if (matrix[nextBottomPointer][position.col] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomPointer;
                    nextBottomPointer--;
                    samePieceCount++;
                }
            }

            if (nextTopPointer < State.ROWS) {

                if (matrix[nextTopPointer][position.col] == null)
                    nextTopPointer++;

                else if (matrix[nextTopPointer][position.col] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopPointer;
                    nextTopPointer++;
                    samePieceCount++;
                }
            }

        }


        int potentialStreakLength = topMostIndexOfAttackingPiece - bottomMostIndexOfAttackingPiece + 1;


        int numOfFreeEnds = 0;
        int totalPoints;

        if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == null)
            numOfFreeEnds++;
        if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;
    }

    static int getNonContiguousPosSlopePoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int samePieceCount = 1;
        int nextBottomLeftPointer = -1;
        int nextTopRightPointer = 1;

        int bottomMostIndexOfAttackingPiece = 0;
        int topMostIndexOfAttackingPiece = 0;

        for (int i = 0; i < 4; i++) {

            if (positionIsInRange(position.row + nextBottomLeftPointer, position.col + nextBottomLeftPointer)) {

                if (matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == null)
                    nextBottomLeftPointer--;

                else if (matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomLeftPointer;
                    nextBottomLeftPointer--;
                    samePieceCount++;
                }
            }


            if (positionIsInRange(position.row + nextTopRightPointer, position.col + nextTopRightPointer)) {

                if (matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == null)
                    nextTopRightPointer++;

                else if (matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopRightPointer;
                    nextTopRightPointer++;
                    samePieceCount++;
                }
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints;

        int potentialStreakLength = topMostIndexOfAttackingPiece - bottomMostIndexOfAttackingPiece + 1;


        if (positionIsInRange(position.row + nextBottomLeftPointer, position.col + nextBottomLeftPointer) && matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row + nextTopRightPointer, position.col + nextTopRightPointer) && matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;

    }

    static int getNonContiguousNegSlopePoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {


        int samePieceCount = 1;
        int nextTopLeftPointer = 1;
        int nextBottomRightPointer = 1;

        int bottomMostIndexOfAttackingPiece = 0;
        int topMostIndexOfAttackingPiece = 0;

        for (int i = 0; i < 4; i++) {

            if (positionIsInRange(position.row + nextTopLeftPointer, position.col - nextTopLeftPointer)) {

                if (matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == null)
                    nextTopLeftPointer++;

                else if (matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopLeftPointer;
                    nextTopLeftPointer++;
                    samePieceCount++;
                }
            }

            if (positionIsInRange(position.row - nextBottomRightPointer, position.col + nextBottomRightPointer)) {

                if (matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == null)
                    nextBottomRightPointer++;

                else if (matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomRightPointer;
                    nextBottomRightPointer++;
                    samePieceCount++;
                }
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints;

        int potentialStreakLength = topMostIndexOfAttackingPiece + bottomMostIndexOfAttackingPiece + 1;


        if (positionIsInRange(position.row + nextTopLeftPointer, position.col - nextTopLeftPointer) && matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row - nextBottomRightPointer, position.col + nextBottomRightPointer) && matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == null)
            numOfFreeEnds++;

       /*  // todo
        if(potentialStreakLength - samePieceCount + 1 <=0)
        {
            System.out.println("Row = "+position.row+" Col = "+position.col);
            printBoard(matrix);
            System.out.println("bottomMostIndexOfAttackingPiece :  "+bottomMostIndexOfAttackingPiece);
            System.out.println("topMostIndexOfAttackingPiece : "+topMostIndexOfAttackingPiece);
            System.out.println("potentialStreakLength : "+potentialStreakLength);
            System.out.println("samePieceCount : "+samePieceCount);
            System.out.println("\n");
        }*/


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;
    }


    static int getContiguousHorizontalHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int numOfFreeEnds;
        int totalPoints = 0;


        int horizontalStreak = 1;
        int nextLeftPointer = position.col - 1;
        int nextRightPointer = position.col + 1;
        while ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == interestedPiece) ||
                (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == interestedPiece)) {

            if (nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == interestedPiece) {
                nextLeftPointer--;
                horizontalStreak++;
            }

            if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == interestedPiece) {
                nextRightPointer++;
                horizontalStreak++;
            }
        }


        numOfFreeEnds = 0;
        if ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == null))
            numOfFreeEnds++;
        if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == null)
            numOfFreeEnds++;

        switch (horizontalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += TWO_SIDED_FOUR_STREAK_POINTS;
                break;
            default:    // streak > 4
                totalPoints = DEFINITE_WIN;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }

        return totalPoints;
    }

    static int getContiguousVerticalHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int verticalStreak = 1;
        int nextBottomPointer = position.row - 1;
        int nextTopPointer = position.row + 1;
        while ((nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == interestedPiece) ||
                (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == interestedPiece)) {

            if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == interestedPiece) {
                nextBottomPointer--;
                verticalStreak++;
            }

            if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == interestedPiece) {
                nextTopPointer++;
                verticalStreak++;
            }

        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;

        if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == null)
            numOfFreeEnds++;
        if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == null)
            numOfFreeEnds++;

        switch (verticalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += TWO_SIDED_FOUR_STREAK_POINTS;
                break;
            default:    // Streak > 4
                totalPoints = DEFINITE_WIN;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }


        return totalPoints;
    }

    static int getContiguousPosSlopeHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int posSlopeDiagonalStreak = 1;
        int nextLowerLeftPointer = -1;
        int nextUpperRightPointer = 1;

        while ((positionIsInRange(position.row + nextLowerLeftPointer, position.col + nextLowerLeftPointer) && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == interestedPiece) ||
                (positionIsInRange(position.row + nextUpperRightPointer, position.col + nextUpperRightPointer)
                        && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == interestedPiece)) {

            if (position.row + nextLowerLeftPointer >= 0 && position.col + nextLowerLeftPointer >= 0 && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == interestedPiece) {
                nextLowerLeftPointer--;
                posSlopeDiagonalStreak++;
            }

            if (position.row + nextUpperRightPointer < State.ROWS && position.col + nextUpperRightPointer < State.COLS && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == interestedPiece) {
                nextUpperRightPointer++;
                posSlopeDiagonalStreak++;
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;



        if (positionIsInRange(position.row + nextLowerLeftPointer , position.col + nextLowerLeftPointer) && matrix[position.row + nextLowerLeftPointer][position.col + nextLowerLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row + nextUpperRightPointer , position.col + nextUpperRightPointer) && matrix[position.row + nextUpperRightPointer][position.col + nextUpperRightPointer] == null)
            numOfFreeEnds++;


        switch (posSlopeDiagonalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += TWO_SIDED_FOUR_STREAK_POINTS;
                break;
            default:    // Streak > 4
                totalPoints = DEFINITE_WIN;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }

        return totalPoints;

    }

    static int getContiguousNegSlopeHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int negSlopeDiagonalStreak = 1;
        int nextUpperLeftPointer = 1;
        int nextLowerRightPointer = 1;

        while ((positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer ) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == interestedPiece) ||
                (positionIsInRange(position.row - nextLowerRightPointer ,position.col + nextLowerRightPointer ) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == interestedPiece)) {

            if (positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer ) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == interestedPiece) {
                nextUpperLeftPointer++;
                negSlopeDiagonalStreak++;
            }

            if (positionIsInRange(position.row - nextLowerRightPointer ,position.col + nextLowerRightPointer ) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == interestedPiece) {
                nextLowerRightPointer++;
                negSlopeDiagonalStreak++;
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints = 0;


        if (positionIsInRange(position.row + nextUpperLeftPointer, position.col - nextUpperLeftPointer) && matrix[position.row + nextUpperLeftPointer][position.col - nextUpperLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row - nextLowerRightPointer, position.col + nextLowerRightPointer) && matrix[position.row - nextLowerRightPointer][position.col + nextLowerRightPointer] == null)
            numOfFreeEnds++;

        switch (negSlopeDiagonalStreak) {
            case 1:
                totalPoints += ONE_STREAK_POINT * numOfFreeEnds;
                break;
            case 2:
                totalPoints += TWO_STREAK_POINT * numOfFreeEnds;
                break;
            case 3:
                totalPoints += THREE_STREAK_POINT * numOfFreeEnds;
                break;
            case 4:
                if (numOfFreeEnds == 1)
                    totalPoints += ONE_SIDED_FOUR_STREAK_POINTS;
                else if (numOfFreeEnds == 2)
                    totalPoints += TWO_SIDED_FOUR_STREAK_POINTS;
                break;
            default:    // Streak > 4
                totalPoints = DEFINITE_WIN;
                //throw new MatchAlreadyOverException("AttackPoints(): Match is already over. Why to calculate streak");
        }


        return totalPoints;
    }

    static int getNonContiguousHorizontalHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int numOfFreeEnds;
        int totalPoints;


        int samePieceCount = 1;
        int nextLeftPointer = position.col - 1;
        int nextRightPointer = position.col + 1;


        int leftMostIndexOfAttackingPiece = position.col;
        int rightMostIndexOfAttackingPiece = position.col;

        for (int i = 0; i < 4; i++) {
            if (nextLeftPointer >= 0) {
                if (matrix[position.row][nextLeftPointer] == null)
                    nextLeftPointer--;

                else if (matrix[position.row][nextLeftPointer] == interestedPiece) {
                    leftMostIndexOfAttackingPiece = nextLeftPointer;
                    nextLeftPointer--;
                    samePieceCount++;
                }
            }

            if (nextRightPointer < State.COLS) {

                if (matrix[position.row][nextRightPointer] == null)
                    nextRightPointer++;

                else if (matrix[position.row][nextRightPointer] == interestedPiece) {
                    rightMostIndexOfAttackingPiece = nextRightPointer;
                    nextRightPointer++;
                    samePieceCount++;
                }
            }
        }

        int potentialStreakLength = rightMostIndexOfAttackingPiece - leftMostIndexOfAttackingPiece + 1;


        numOfFreeEnds = 0;
        if ((nextLeftPointer >= 0 && matrix[position.row][nextLeftPointer] == null))
            numOfFreeEnds++;
        if (nextRightPointer < State.COLS && matrix[position.row][nextRightPointer] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);


        return totalPoints;
    }

    static int getNonContiguousVerticalHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int samePieceCount = 1;
        int nextBottomPointer = position.row - 1;
        int nextTopPointer = position.row + 1;

        int bottomMostIndexOfAttackingPiece = position.row;
        int topMostIndexOfAttackingPiece = position.row;

        for (int i = 0; i < 4; i++) {

            if (nextBottomPointer >= 0) {
                if (matrix[nextBottomPointer][position.col] == null)
                    nextBottomPointer--;

                else if (matrix[nextBottomPointer][position.col] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomPointer;
                    nextBottomPointer--;
                    samePieceCount++;
                }
            }

            if (nextTopPointer < State.ROWS) {

                if (matrix[nextTopPointer][position.col] == null)
                    nextTopPointer++;

                else if (matrix[nextTopPointer][position.col] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopPointer;
                    nextTopPointer++;
                    samePieceCount++;
                }
            }

        }


        int potentialStreakLength = topMostIndexOfAttackingPiece - bottomMostIndexOfAttackingPiece + 1;


        int numOfFreeEnds = 0;
        int totalPoints;

        if (nextBottomPointer >= 0 && matrix[nextBottomPointer][position.col] == null)
            numOfFreeEnds++;
        if (nextTopPointer < State.ROWS && matrix[nextTopPointer][position.col] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;
    }

    static int getNonContiguousPosSlopeHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int samePieceCount = 1;
        int nextBottomLeftPointer = -1;
        int nextTopRightPointer = 1;

        int bottomMostIndexOfAttackingPiece = 0;
        int topMostIndexOfAttackingPiece = 0;

        for (int i = 0; i < 4; i++) {

            if (positionIsInRange(position.row + nextBottomLeftPointer , position.col + nextBottomLeftPointer)) {

                if (matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == null)
                    nextBottomLeftPointer--;

                else if (matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomLeftPointer;
                    nextBottomLeftPointer--;
                    samePieceCount++;
                }
            }




            if (positionIsInRange(position.row + nextTopRightPointer , position.col + nextTopRightPointer)) {

                if (matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == null)
                    nextTopRightPointer++;

                else if (matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopRightPointer;
                    nextTopRightPointer++;
                    samePieceCount++;
                }
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints;

        int potentialStreakLength = topMostIndexOfAttackingPiece - bottomMostIndexOfAttackingPiece + 1;



        if (positionIsInRange(position.row + nextBottomLeftPointer, position.col + nextBottomLeftPointer) && matrix[position.row + nextBottomLeftPointer][position.col + nextBottomLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row + nextTopRightPointer, position.col + nextTopRightPointer) && matrix[position.row + nextTopRightPointer][position.col + nextTopRightPointer] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;

    }

    static int getNonContiguousNegSlopeHeuristicPoints(PieceType[][] matrix, PieceType interestedPiece, Position position) {

        int samePieceCount = 1;
        int nextTopLeftPointer = 1;
        int nextBottomRightPointer = 1;

        int bottomMostIndexOfAttackingPiece = 0;
        int topMostIndexOfAttackingPiece = 0;

        for (int i = 0; i < 4; i++) {

            if (positionIsInRange(position.row + nextTopLeftPointer, position.col - nextTopLeftPointer)) {

                if (matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == null)
                    nextTopLeftPointer++;

                else if (matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == interestedPiece) {
                    topMostIndexOfAttackingPiece = nextTopLeftPointer;
                    nextTopLeftPointer++;
                    samePieceCount++;
                }
            }



            if (positionIsInRange(position.row - nextBottomRightPointer, position.col + nextBottomRightPointer)) {

                if (matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == null)
                    nextBottomRightPointer++;

                else if (matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == interestedPiece) {
                    bottomMostIndexOfAttackingPiece = nextBottomRightPointer;
                    nextBottomRightPointer++;
                    samePieceCount++;
                }
            }
        }


        int numOfFreeEnds = 0;
        int totalPoints;

        int potentialStreakLength = topMostIndexOfAttackingPiece + bottomMostIndexOfAttackingPiece + 1;



        if (positionIsInRange(position.row + nextTopLeftPointer, position.col - nextTopLeftPointer) && matrix[position.row + nextTopLeftPointer][position.col - nextTopLeftPointer] == null)
            numOfFreeEnds++;
        if (positionIsInRange(position.row - nextBottomRightPointer, position.col + nextBottomRightPointer) && matrix[position.row - nextBottomRightPointer][position.col + nextBottomRightPointer] == null)
            numOfFreeEnds++;


        if (potentialStreakLength <= 4 && numOfFreeEnds == 0)
            totalPoints = 0;
        else
            totalPoints = (MULTIPLIER_FOR_NON_CONTINUOUS_STREAK * samePieceCount * (numOfFreeEnds + 1)) / (potentialStreakLength - samePieceCount + 1);

        return totalPoints;
    }


    static boolean positionIsInRange(int row, int col) {
        return row >= 0 && col >= 0 && row < State.ROWS && col < State.COLS;
    }

    static void printBoard(PieceType[][] matrix)
    {
        for (int i = 0; i <matrix.length ; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(matrix[i][j]+"\t");
            }
            System.out.println();
        }
    }

}
