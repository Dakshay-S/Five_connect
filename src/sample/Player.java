package sample;

public enum Player {
    BLACK, WHITE;

    Player getOpponent()
    {
        return this == BLACK? WHITE:BLACK;
    }
}
