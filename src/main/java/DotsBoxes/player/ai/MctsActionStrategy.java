package DotsBoxes.player.ai;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;
import DotsBoxes.utils.MCTSTree;

import java.util.List;
import java.util.Random;

/**
 * Recherche arborescente Monte-Carlo (MCTS) avec un nombre fixe d'itérations.
 * <p>
 * Chaque itération enchaîne sélection (UCB1), expansion d'un coup non exploré,
 * simulation d'une fin de partie aléatoire et rétropropagation du résultat.
 * Le coup retenu est celui dont le noeud a été le plus visité.
 * </p>
 */
public class MctsActionStrategy implements ActionStrategy {

    private final int iterations;
    private final Random random;

    public MctsActionStrategy(int iterations) {
        if (iterations <= 0) throw new IllegalArgumentException();
        this.iterations = iterations;
        random = new Random(0);
    }

    public MctsActionStrategy(int iterations, long seed) {
        if (iterations <= 0) throw new IllegalArgumentException();
        this.iterations = iterations;
        random = new Random(seed);
    }

    private MCTSTree selection(MCTSTree tree, int iteration) {
        List<Action> actions = tree.board.getAvailableActions();
        if (actions.size() > tree.children.size() || actions.isEmpty()) {
            return tree;
        }
        else {
            double max_res = -Double.MAX_VALUE;
            MCTSTree best = null;
            for (MCTSTree node : tree.children.values()) {
                double candidate_res = node.UCB1(iteration);
                if (candidate_res > max_res) {
                    best = node;
                    max_res = candidate_res;
                }
            }
            assert best != null;
            return selection(best, iteration);
        }
    }

    private MCTSTree expansion(MCTSTree node) {
        for (Action action : node.board.getAvailableActions()) {
            if (! node.children.containsKey(action)) {
                Board new_board = new Board(node.board);
                int c = new_board.apply(action, node.playerId);
                int new_playerId = node.playerId;
                if (c == 0) new_playerId = -new_playerId + 1;
                MCTSTree new_node = new MCTSTree(new_board, new_playerId, node);

                node.children.put(action, new_node);
                return new_node;
            }
        }

        // If node.board.getAvailableActions() is empty
        return node;
    }

    private float simulation(MCTSTree node, int MCTSPlayer) {
        // La partie simulée se joue sur une copie : le plateau du noeud doit rester intact,
        // sinon le noeud devient terminal dès sa première simulation.
        Board board = new Board(node.board);
        int playerId = node.playerId;

        while (!board.isFinished()) {
            List<Action> actions = board.getAvailableActions();
            Action action = actions.get(random.nextInt(actions.size()));

            int c = board.apply(action, playerId);
            if (c == 0) playerId = -playerId + 1;
        }

        int otherPlayer = -MCTSPlayer + 1;
        return Integer.compare(board.getScore(MCTSPlayer), board.getScore(otherPlayer));
    }

    private void backpropagation(MCTSTree node, float value) {
        while (node != null) {
            node.add_sim(value);
            node = node.father;
        }
    }

    @Override
    public Action selectAction(Board board, int playerId) {
        MCTSTree tree = new MCTSTree(board, playerId);

        for (int i=0; i<iterations; i++) {
            MCTSTree node = selection(tree, i);
            node = expansion(node);
            float value = simulation(node, playerId);
            backpropagation(node, value);
        }

        float max_gain = -Float.MAX_VALUE;
        Action best = null;
        for (Action action : tree.children.keySet()) {
            float nbSim = tree.children.get(action).getNbSim();
            if (nbSim > max_gain) {
                max_gain = nbSim;
                best = action;
            }
        }
        return best;
    }

    @Override
    public String getName() {
        return "MCTS";
    }
}