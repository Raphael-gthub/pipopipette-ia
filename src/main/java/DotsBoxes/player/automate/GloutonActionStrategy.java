package DotsBoxes.player.automate;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

import java.util.List;
import java.util.Random;

/**
 * Stratégie gloutonne : joue un coup qui ferme une case s'il en existe un,
 * sinon un coup aléatoire (renvoie {@code null} s'il ne reste aucun segment).
 */
public class GloutonActionStrategy implements ActionStrategy {
    private final Random rng;

    public GloutonActionStrategy() {
        rng = new Random();
    }

    public GloutonActionStrategy(long seed) {
        rng = new Random(seed);
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions();
        if (actions.isEmpty()) {
            return null;
        }
        for (Action action: actions) {
            int cases = new Board(board).apply(action, playerId);
            if (cases > 0) {
                return action;
            }
        }
        return actions.get(rng.nextInt(actions.size()));
    }

    @Override
    public String getName() {
        return "Glouton";
    }
}
