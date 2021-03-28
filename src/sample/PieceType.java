package sample;

public enum PieceType {
    BLACK, WHITE;
    PieceType getOppositePiece()
    {
        return this == BLACK? WHITE:BLACK;
    }
}


