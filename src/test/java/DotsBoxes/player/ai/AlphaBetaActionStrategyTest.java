package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.AlphaBetaPruningObserver;
import DotsBoxes.observers.NodeCounterObserver;
import DotsBoxes.player.ActionStrategy;
import DotsBoxes.player.automate.RandomActionStrategy;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AlphaBetaActionStrategyTest {

    @Test
    void selectActionShouldReturnNullWhenNoActionAvailable() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);
        board.apply(new Action(Action.Type.VERTICAL, 0, 1), 1);

        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(3,
                new AlphaBetaPruningObserver(), new NodeCounterObserver());

        assertNull(strategy.selectAction(board, 0));
    }

    @Test
    void selectActionShouldChooseImmediateBoxClosingMoveAndIncrementNodeCounter() {
        Board board = new Board(2, 2);
        board.apply(new Action(Action.Type.HORIZONTAL, 0, 0), 0);
        board.apply(new Action(Action.Type.HORIZONTAL, 1, 0), 1);
        board.apply(new Action(Action.Type.VERTICAL, 0, 0), 0);

        AlphaBetaPruningObserver observer = new AlphaBetaPruningObserver();
        NodeCounterObserver nodeCounter = new NodeCounterObserver();
        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(2, observer, nodeCounter);

        Action best = strategy.selectAction(board, 1);

        assertEquals(new Action(Action.Type.VERTICAL, 0, 1), best);
        assertTrue(nodeCounter.getCount() > 0);
        assertTrue(observer.getAlphaCutCount() >= 0);
        assertTrue(observer.getBetaCutCount() >= 0);
    }

    @Test
    void shouldHaveSameResultsThanMinimax() {
        Board board = new Board(4, 4);
        ActionStrategy random = new RandomActionStrategy();
        ActionStrategy minimax = new MinimaxActionStrategy(3);
        ActionStrategy alphaBeta = new AlphaBetaActionStrategy(3, new AlphaBetaPruningObserver(),
                new NodeCounterObserver());

        Random rand = new Random();

        while (! board.isFinished()) {
            int playerId = rand.nextInt(2);
            Action minimax_action = minimax.selectAction(board, playerId);
            Action alphaBeta_action = alphaBeta.selectAction(board, playerId);
            assertEquals(minimax_action.getCol(), alphaBeta_action.getCol());
            assertEquals(minimax_action.getRow(), alphaBeta_action.getRow());
            assertEquals(minimax_action.getType(), alphaBeta_action.getType());

            board.apply(random.selectAction(board, playerId), playerId);
        }
    }

    @Test
    void getNameShouldReturnAlphaBeta() {
        AlphaBetaActionStrategy strategy = new AlphaBetaActionStrategy(1,
                new AlphaBetaPruningObserver(), new NodeCounterObserver());
        assertEquals("Alpha-Beta", strategy.getName());
    }
}
