package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;
import org.jgrapht.alg.util.Pair;

import java.util.List;

/**
 * Minimax à profondeur limitée, sans élagage.
 * <p>
 * Le joueur qui ferme une case rejoue, donc plusieurs niveaux « max » (ou « min ») peuvent
 * se suivre. Les feuilles sont évaluées par la différence de score entre les deux joueurs.
 * </p>
 */
public class MinimaxActionStrategy implements ActionStrategy {

    private final int maxDepth;

    public MinimaxActionStrategy(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * @param board le plateau de jeu
     * @param playerId le joueur qui lance le minimax, celui pour lequel on cherche la meilleure action
     * @param playerIdCurrent celui qui peut jouer actuellement
     * @param depth la profondeur de l'arbre actuellement
     * @return le score associé au plateau de jeu, en l'évaluant grâce à un arbre, et l'action qui a permis d'avoir le
     * meilleur score.
     */
    private Pair<Float, Action> evaluationWithTree(Board board, int playerId, int playerIdCurrent, int depth) {
        if (depth == maxDepth) { return new Pair<>(evaluationFunction(board, playerId), null); }
        List<Action> actions = board.getAvailableActions();
        if (actions.isEmpty()) { return new Pair<>(evaluationFunction(board, playerId), null); }

        float result;
        if (playerId == playerIdCurrent) result = -Float.MAX_VALUE; // On maximise
        else result = Float.MAX_VALUE; // On minimise

        Action bestAction = null;

        for (Action action: actions) {
            Board nextBoard = new Board(board);
            int placed = nextBoard.apply(action, playerIdCurrent);

            int nextPlayerId;
            if (placed == 0) nextPlayerId = -playerIdCurrent + 1;
            else nextPlayerId = playerIdCurrent;

            float res_candidate = evaluationWithTree(nextBoard, playerId, nextPlayerId, depth+1).getFirst();

            if (playerId == playerIdCurrent && res_candidate > result) { // On maximise
                result = res_candidate;
                bestAction = action;
            } else if (playerId != playerIdCurrent && res_candidate < result) { // On minimise
                result = res_candidate;
                bestAction = action;
            }
        }
        return new Pair<>(result, bestAction);
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        return evaluationWithTree(board, playerId, playerId, 0).getSecond();
    }

    /**
     * Renvoie le score du joueur moins le score de l'adversaire
     */
    public static float evaluationFunction(Board board, int playerId) {
        return board.getScore(playerId) - board.getScore(-playerId + 1);
    }

    @Override
    public String getName() {
        return "Minimax";
    }
}
