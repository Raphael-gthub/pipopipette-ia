package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.observers.NodeCounterObserver;
import DotsBoxes.player.ActionStrategy;
import DotsBoxes.utils.BoardHashable;
import org.jgrapht.alg.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Variante de travail de {@link ExpertActionStrategy}, conservée pour comparer les deux
 * versions : même recherche alpha-bêta (profondeur adaptative, tri des coups, table des
 * positions), mais sans la passe volontaire.
 */
public class ExperimentalExpertActionStrategy implements ActionStrategy {


    private final long maxNumberLeafs;
    private int maxDepth;
    // Malheureusement, Board ne surcharge pas la méthode hashCode, on doit donc passer par les array qui eux
    // surchargent bien la méthode hashCode.
    private HashMap<BoardHashable, Float> knownNodes;
    public NodeCounterObserver counter;
    private final Random rnd;

    public ExperimentalExpertActionStrategy(long maxNumberLeafs, NodeCounterObserver counter, long seed) {
        if (maxNumberLeafs <= 0) {
            throw new IllegalArgumentException("Le nombre de feuilles max doit etre > 0");
        }
        this.maxNumberLeafs = maxNumberLeafs;
        maxDepth = 0;
        knownNodes = null;
        this.counter = counter;
        rnd = new Random(seed);
    }

    public ExperimentalExpertActionStrategy(int maxLeafsNumber, NodeCounterObserver counter) {
        if (maxLeafsNumber <= 0) {
            throw new IllegalArgumentException("Le nombre de feuilles max doit etre > 0");
        }
        this.maxNumberLeafs = maxLeafsNumber;
        maxDepth = 0;
        knownNodes = null;
        this.counter = counter;
        rnd = new Random();
    }

    public ExperimentalExpertActionStrategy(int maxNumberLeafs, long seed) {
        if (maxNumberLeafs <= 0) {
            throw new IllegalArgumentException("Le nombre de feuilles max doit etre > 0");
        }
        this.maxNumberLeafs = maxNumberLeafs;
        maxDepth = 0;
        knownNodes = null;
        counter = null;
        rnd = new Random(seed);
    }

    public ExperimentalExpertActionStrategy(int maxNumberLeafs) {
        if (maxNumberLeafs <= 0) {
            throw new IllegalArgumentException("Le nombre de feuilles max doit etre > 0");
        }
        this.maxNumberLeafs = maxNumberLeafs;
        maxDepth = 0;
        knownNodes = null;
        counter = null;
        rnd = new Random();
    }

    private Pair<Board, Integer>[] sortFutureChildren(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions();
        Pair<Board, Integer>[] res = new Pair[actions.size()];

        int i_closing = 0;
        int i_unclosing = actions.size() - 1;
        int otherPlayerId = - playerId + 1;
        for (Action action : actions){
            Board new_board = new Board(board);
            int c = new_board.apply(action, playerId);
            if (c > 0) {
                res[i_closing] = new Pair<>(new_board, playerId);
                i_closing++;
            } else {
                res[i_unclosing] = new Pair<>(new_board, otherPlayerId);
                i_unclosing--;
            }
        }
        return res;
    }

    private float selectActionWithTreeFloat(Board board, int playerId, int playerIdCurrent, int depth,
                                            float alpha, float beta) {
        if (depth >= maxDepth || board.isFinished()) {
            return MinimaxActionStrategy.evaluationFunction(board, playerId);
        }
        BoardHashable boardWithHash = new BoardHashable(board, playerIdCurrent);
        if (knownNodes.containsKey(boardWithHash)) {
            if (counter != null)
                counter.increment();
            return knownNodes.get(boardWithHash);
        }

        Pair<Board, Integer>[] gameStates = sortFutureChildren(board, playerIdCurrent);

        float bestValue;
        if (playerId == playerIdCurrent) {
            bestValue = -Float.MAX_VALUE;

            for (Pair<Board, Integer> gameState: gameStates) {

                bestValue = Float.max(bestValue, selectActionWithTreeFloat(gameState.getFirst(), playerId,
                        gameState.getSecond(), depth+1, alpha, beta));
                if (bestValue >= beta) return bestValue;
                if (bestValue > alpha) alpha = bestValue;
            }
        }

        else {
            bestValue = Float.MAX_VALUE;

            for (Pair<Board, Integer> gameState: gameStates) {

                bestValue = Float.min(bestValue, selectActionWithTreeFloat(gameState.getFirst(), playerId,
                        gameState.getSecond(), depth+1, alpha, beta));
                if (bestValue <= alpha) return bestValue;
                if (bestValue < beta) beta = bestValue;
            }
        }
        knownNodes.put(boardWithHash, bestValue);
        return bestValue;
    }

    private void computeMaxDepth(int numberActions) {
        int res = 0;
        long numberLeafs = 1;
        while (numberLeafs < maxNumberLeafs && numberActions > 1) {
            res++;
            numberLeafs *= numberActions;
            numberActions--;
        }
        maxDepth = res - 1;
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        knownNodes = new HashMap<>();
        List<Action> actions = board.getAvailableActions();

        if (actions.isEmpty()) return null;
        computeMaxDepth(actions.size());

        List<Action> bestActions = new LinkedList<>();
        float alpha = -Float.MAX_VALUE;
        float bestValue = -Float.MAX_VALUE;
        for (Action action: actions) {
            Board nextBoard = new Board(board);
            int placed = nextBoard.apply(action, playerId);

            int nextPlayerId;
            if (placed == 0) nextPlayerId = -playerId + 1;
            else nextPlayerId = playerId;

            float value = selectActionWithTreeFloat(nextBoard, playerId, nextPlayerId, 1,
                    alpha, Float.MAX_VALUE);
            if (bestValue > alpha) alpha = bestValue;
            if (value == bestValue) {
                bestActions.add(action);
            }
            if (value > bestValue) {
                bestValue = value;
                bestActions.clear();
                bestActions.add(action);
            }
        }
        int randomIndex = rnd.nextInt(bestActions.size());
        knownNodes = null;
        return bestActions.get(randomIndex);
    }

    @Override
    public String getName() {
        return "Expert";
    }

}