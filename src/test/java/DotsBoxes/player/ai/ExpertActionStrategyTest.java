package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.NodeCounterObserver;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExpertActionStrategyTest {

    @Test
    void constructorShouldRejectInvalidDepth() {
        assertThrows(IllegalArgumentException.class, () -> new ExpertActionStrategy(0));
    }

    @Test
    void selectActionShouldReturnNullWhenBoardIsFinished() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);
        board.apply(new Action(Action.Type.VERTICAL, 0, 1), 1);

        ExpertActionStrategy strategy = new ExpertActionStrategy(500_000_000);

        assertNull(strategy.selectAction(board, 0));
    }

    @Test
    void selectActionShouldReturnOnlyAvailableMove() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);

        ExpertActionStrategy strategy = new ExpertActionStrategy(500_000_000);

        assertEquals(new Action(Action.Type.VERTICAL, 0, 1), strategy.selectAction(board, 1));
    }

    @Test
    void selectActionShouldPreferImmediateClosureWhenAvailable() {
        Board board = new Board(2, 3);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);

        ExpertActionStrategy strategy = new ExpertActionStrategy(500_000_000);

        assertEquals(new Action(Action.Type.VERTICAL, 0, 1), strategy.selectAction(board, 1));
    }

    @Test
    void shouldDestroySomeNodesAlreadySeen() {
        Board board = new Board(4, 4);

        NodeCounterObserver observer = new NodeCounterObserver();
        ExpertActionStrategy strat = new ExpertActionStrategy(500_000_000, observer);

        strat.selectAction(board, 0);

        assertTrue(observer.getCount() > 0);
    }

    @Test
    void getNameShouldReturnExpert() {
        assertEquals("Expert", new ExpertActionStrategy(500_000_000).getName());
    }
}
