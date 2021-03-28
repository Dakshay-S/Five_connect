package sample;

import javafx.application.Platform;

import javafx.scene.control.Alert;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.Arrays;


public class Board extends Pane {

    final static Color DARK_COLOR = Color.rgb(100, 100, 100);
    final static Color LIGHT_COLOR = Color.rgb(200, 200, 200);
    final static Color HIGHLIGHT_COLOR = Color.rgb(95, 227, 104);
    final static Color HINT_COLOR = Color.rgb(200, 100, 0);
    final static float squareDimension = 60f;
    Square[][] squares;
    Pawn[][] pawnsMatrix = new Pawn[State.ROWS][State.COLS];
    float width;
    float height;
    State state;
    Player player = Player.BLACK;
    Player computer = Player.WHITE;
    final Runnable computerRunnable;
    final int minimumDepth = 5;
    final int stages = 0;
    boolean gameOver = false;
    Player winner = null;

    public Board() {
        this.width = State.COLS * squareDimension;
        this.height = State.ROWS * squareDimension;
        this.squares = new Square[State.ROWS][State.COLS];


        initSquares();

        resetBoard();

        computerRunnable = () -> {

            MAX_Player max_player = new MAX_Player(null);

            int maxDepth = getSuitableDepth();
            //todo
            //maxDepth = MinMax.MAX_DEPTH;

            IntermediateState nexBest = max_player.solveForMax(new IntermediateState(state), maxDepth, maxDepth, computer, computer);

            Platform.runLater(() -> {
                Board.this.makeMove(nexBest);
                switchTurn();
            });

        };

    }

    void resetBoard() {
        resetSquaresColors();

        this.player = Player.BLACK;
        this.computer = Player.WHITE;

        for (int i = 0; i < State.ROWS; i++)
            for (int j = 0; j < State.COLS; j++)
                removePawnAt(i, j);

        state = initialiseState();
        initPawnsMatrix();
        this.gameOver = false;
        this.winner = null;

    }

    void initSquares() {
        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                squares[i][j] = new Square(i, j, (i + j) % 2 == 0 ? DARK_COLOR : LIGHT_COLOR);
                getChildren().add(squares[i][j]);
            }
        }
    }

    void initPawnsMatrix() {
        for (Pawn[] row : pawnsMatrix)
            Arrays.fill(row, null);
    }

    State initialiseState() {
        PieceType[][] stateMatrix = new PieceType[State.ROWS][State.COLS];

        for (PieceType[] row : stateMatrix)
            Arrays.fill(row, null);

        return new State(stateMatrix);
    }


    class Square extends Rectangle {
        int row;
        int col;
        Color color;


        public Square(int row, int col, Color color) {
            super(squareDimension, squareDimension);
            this.row = row;
            this.col = col;
            this.color = color;

            this.setFill(color);
            this.setOnMouseClicked(mouseEvent -> {
                clickAction();
            });

            this.relocate(col * squareDimension, row * squareDimension);
        }


        public void setColor(Color color) {
            this.color = color;
            this.setFill(color);
        }

        void clickAction() {

            resetSquaresColors();
            if (placePawnAt(this.row, this.col))
                switchTurn();
        }
    }


    class Pawn extends StackPane {
        PieceType pieceType;
        int posRow;
        int posCol;
        Circle circle;


        public Pawn(PieceType pieceType, int posRow, int posCol) {
            super();
            this.pieceType = pieceType;

            circle = new Circle();
            circle.setRadius(squareDimension / 2);
            circle.setFill((pieceType == PieceType.WHITE) ? Color.WHITE : Color.BLACK);
            circle.setEffect(new DropShadow());

            this.getChildren().addAll(circle);

            moveTo(posRow, posCol);
        }


        void moveTo(int row, int col) {
            this.posRow = row;
            this.posCol = col;

            this.relocate(posCol * squareDimension, posRow * squareDimension);
        }

    }


    void resetSquaresColors() {
        for (int i = 0; i < State.ROWS; i++)
            for (int j = 0; j < State.COLS; j++)
                squares[i][j].setColor((i + j) % 2 == 0 ? DARK_COLOR : LIGHT_COLOR);
    }


    Pawn removePawnAt(int row, int col) {

        Pawn toBeRemoved = pawnsMatrix[row][col];

        if (toBeRemoved != null)
            this.getChildren().remove(toBeRemoved);

        pawnsMatrix[row][col] = null;

        return toBeRemoved;
    }

    void makeMove(IntermediateState nexBest) {
        Position nextBestMove = new Position(0, 0);

        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (state.stateMatrix[i][j] == null && nexBest.state.stateMatrix[i][j] != null) {
                    nextBestMove.row = i;
                    nextBestMove.col = j;
                    break;
                }
            }
        }

        squares[nextBestMove.row][nextBestMove.col].setColor(HINT_COLOR);

        placePawnAt(nextBestMove.row, nextBestMove.col);





    }

    boolean placePawnAt(int row, int col) {
        if (state.stateMatrix[row][col] == null) {
            PieceType pieceType = player == Player.WHITE ? PieceType.WHITE : PieceType.BLACK;

            state.stateMatrix[row][col] = pieceType;
            Pawn newPawn = new Pawn(pieceType, row, col);
            pawnsMatrix[row][col] = newPawn;

            this.getChildren().add(newPawn);

            return true;
        }

        return false;
    }


    void switchTurn() {

        this.player = this.player == Player.WHITE ? Player.BLACK : Player.WHITE;

        if (computer != null && computer == this.player) {
            Thread backgroundThread = new Thread(computerRunnable);
            backgroundThread.start();
        }
    }

    void computerFirst() {
        this.computer = this.computer.getOpponent();
        this.player = this.player.getOpponent();
        switchTurn();
    }


    void showHint() {
        resetSquaresColors();
        Runnable runnable = () -> {
            {
                MAX_Player max_player = new MAX_Player(null);

                int depth = getSuitableDepth();

                //todo
                //depth = MinMax.MAX_DEPTH;

                IntermediateState nexBest = max_player.solveForMax(new IntermediateState(state), depth, depth, Board.this.player, Board.this.player);

                Position nextBestMove = new Position(0, 0);
                for (int i = 0; i < State.ROWS; i++) {
                    for (int j = 0; j < State.COLS; j++) {

                        if (state.stateMatrix[i][j] == null && nexBest.state.stateMatrix[i][j] != null) {
                            nextBestMove.row = i;
                            nextBestMove.col = j;
                            break;
                        }
                    }
                }

                Platform.runLater(() -> {
                    squares[nextBestMove.row][nextBestMove.col].setColor(HINT_COLOR);
                });

            }
        };

        Thread thread = new Thread(runnable);
        thread.start();

    }

    int getSuitableDepth() {
        int numOfEmptyCells = 0;

        for (int i = 0; i < State.ROWS; i++) {
            for (int j = 0; j < State.COLS; j++) {
                if (state.stateMatrix[i][j] == null)
                    numOfEmptyCells++;
            }
        }

        float emptyPercent = (float) numOfEmptyCells / (State.ROWS * State.COLS);

        return (int) (minimumDepth + Math.round(stages*(-4* Math.pow(emptyPercent - 0.5, 2) + 1)));
    }


}
