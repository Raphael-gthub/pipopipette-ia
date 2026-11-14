package DotsBoxes.utils;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;

import java.util.Hashtable;

/**
 * Arbre des parties visites
 */
public class MCTSTree {
    static float UCB_CONSTANT = 0.5f;

    public Board board;
    public int playerId;
    public Hashtable<Action, MCTSTree> children;
    public MCTSTree father;
    private float cum_gain;
    private int nb_sim;

    public MCTSTree(Board board, int playerId) {
        this.board = board;
        children = new Hashtable<>();
        father = null;
        this.playerId = playerId;
    }

    public MCTSTree(Board board, int playerId, MCTSTree father) {
        this.board = board;
        children = new Hashtable<>();
        this.father = father;
        this.playerId = playerId;
    }

    public void add_sim(float gain) {
        nb_sim ++;
        cum_gain += gain;
    }

    public double UCB1(int total_nb_simulations) {
        return cum_gain / nb_sim + UCB_CONSTANT * Math.sqrt(Math.log(total_nb_simulations) / nb_sim);
    }

    public float getNbSim() {
        return nb_sim;
    }

    public float getGain() {
        return cum_gain / nb_sim;
    }
}
