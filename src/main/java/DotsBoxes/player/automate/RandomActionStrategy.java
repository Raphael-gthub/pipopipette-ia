package DotsBoxes.player.automate;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

import java.util.List;
import java.util.Random;

/**
 * Joue un coup tiré uniformément parmi les segments disponibles
 * (renvoie {@code null} s'il n'en reste aucun).
 */
public class RandomActionStrategy implements ActionStrategy {
    private final Random rng = new Random();

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions();
        if (actions.isEmpty()) {
            return null;
        }
        int index = rng.nextInt(actions.size());
        return actions.get(index);
    }

    @Override
    public String getName() {
        return "Random";
    }
}
