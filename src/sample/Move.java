package sample;

public class Move implements Cloneable, Comparable<Move>{
    Position position;
    PieceType pieceToBePlaced;
    int incentive;

    public Move(Position position, PieceType pieceToBePlaced, int incentive) {
        this.position = position;
        this.pieceToBePlaced = pieceToBePlaced;
        this.incentive = incentive;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "Place : "+pieceToBePlaced+" at Row = "+ this.position.row+"  Col = "+ this.position.col;
    }

    @Override
    public int compareTo(Move o) {
        return Integer.compare(this.incentive, o.incentive);
    }
}
