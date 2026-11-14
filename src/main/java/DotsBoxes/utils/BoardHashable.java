package DotsBoxes.utils;
import DotsBoxes.board.Board;

import java.util.Arrays;

public class BoardHashable {
    Board board;
    int playerId;
    public BoardHashable(Board board, int playerId) { this.board = board; this.playerId = playerId; }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(board.getScore(0));
        result = 31 * result + Integer.hashCode(playerId);
        result = 31 * result + Arrays.deepHashCode(board.getHEdges());
        result = 31 * result + Arrays.deepHashCode(board.getVEdges());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        else if (! (o instanceof BoardHashable)) return false;
        else
            return ((BoardHashable) o).board.getScore(0) == board.getScore(0) &&
                    playerId == ((BoardHashable) o).playerId &&
                    Arrays.deepEquals(((BoardHashable) o).board.getHEdges(), board.getHEdges()) &&
                    Arrays.deepEquals(((BoardHashable) o).board.getVEdges(), board.getVEdges()) ;
    }

}
