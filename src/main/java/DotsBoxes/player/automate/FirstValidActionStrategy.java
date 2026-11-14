package DotsBoxes.player.automate;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;

import java.util.List;

/**
 * Joue le premier segment libre du plateau
 * (renvoie {@code null} s'il n'en reste aucun).
 */
public class FirstValidActionStrategy implements ActionStrategy {

    @Override
    public Action selectAction(Board board, int playerId) {
        List<Action> actions = board.getAvailableActions();
        if (actions.isEmpty()) {
            return null;
        } else {
            return actions.get(0);
        }
    }

    @Override
    public String getName() {
        return "First valid";
    }
}
