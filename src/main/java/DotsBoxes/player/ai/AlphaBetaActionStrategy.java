package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.AlphaBetaPruningObserver;
import DotsBoxes.observers.NodeCounterObserver;
import DotsBoxes.player.ActionStrategy;

import java.util.List;

/**
 * Minimax à profondeur limitée avec élagage alpha-bêta.
 * <p>
 * Le jeu n'est pas strictement alterné : un joueur qui ferme une case rejoue, donc le joueur
 * suivant est recalculé après chaque coup. Les observateurs comptent les noeuds visités et
 * les coupes alpha et bêta.
 * </p>
 */
public class AlphaBetaActionStrategy implements ActionStrategy {

    private final int maxDepth;
    private final AlphaBetaPruningObserver observer;
    private final NodeCounterObserver nodeCounter;

    public AlphaBetaActionStrategy(int maxDepth, AlphaBetaPruningObserver observer, NodeCounterObserver nodeCounter) {
        this.maxDepth = maxDepth;
        this.observer = observer;
        this.nodeCounter = nodeCounter;
    }

    private float selectActionWithTreeFloat(Board board, int playerId, int playerIdCurrent, int depth,
                                                          float alpha, float beta) {
        nodeCounter.increment();
        if (depth >= maxDepth || board.isFinished()) {
            return MinimaxActionStrategy.evaluationFunction(board, playerId);
        }

        List<Action> actions = board.getAvailableActions();
        float bestValue;

        if (playerId == playerIdCurrent) {

            bestValue = -Float.MAX_VALUE;

            for (Action action: actions) {
                Board nextBoard = new Board(board);
                int placed = nextBoard.apply(action, playerIdCurrent);

                int nextPlayerId;
                if (placed == 0) nextPlayerId = -playerIdCurrent + 1;
                else nextPlayerId = playerIdCurrent;

                bestValue = Float.max(bestValue, selectActionWithTreeFloat(nextBoard, playerId, nextPlayerId, depth+1,
                        alpha, beta));
                if (bestValue >= beta) {
                    observer.incrementBetaCut();
                    return bestValue;
                }
                if (bestValue > alpha) alpha = bestValue;
            }
        }

        else {

            bestValue = Float.MAX_VALUE;

            for (Action action: actions) {
                Board nextBoard = new Board(board);
                int placed = nextBoard.apply(action, playerIdCurrent);

                int nextPlayerId;
                if (placed == 0) nextPlayerId = -playerIdCurrent + 1;
                else nextPlayerId = playerIdCurrent;

                bestValue = Float.min(bestValue, selectActionWithTreeFloat(nextBoard, playerId, nextPlayerId,
                        depth+1, alpha, beta));
                if (bestValue <= alpha) {
                    observer.incrementAlphaCut();
                    return bestValue;
                }
                if (bestValue < beta) beta = bestValue;
            }
        }
        return bestValue;
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions();
        float bestValue = -Float.MAX_VALUE;
        Action bestAction = null;
        for (Action action: actions) {
            Board nextBoard = new Board(board);
            int placed = nextBoard.apply(action, playerId);

            int nextPlayerId;
            if (placed == 0) nextPlayerId = -playerId + 1;
            else nextPlayerId = playerId;

            float value = selectActionWithTreeFloat(nextBoard, playerId, nextPlayerId, 1,
                    -Float.MAX_VALUE, Float.MAX_VALUE);
            if (value > bestValue) {
                bestValue = value;
                bestAction = action;
            }
        }
        return bestAction;
    }

    @Override
    public String getName() {
        return "Alpha-Beta";
    }
}
