package DotsBoxes;

import DotsBoxes.board.Action;
import DotsBoxes.board.Board;
import DotsBoxes.player.ActionStrategy;
import DotsBoxes.player.Player;
import DotsBoxes.player.ai.*;
import DotsBoxes.player.automate.AutomatePlayer;
import DotsBoxes.player.automate.FirstValidActionStrategy;
import DotsBoxes.player.automate.GloutonActionStrategy;
import DotsBoxes.player.automate.RandomActionStrategy;
import DotsBoxes.player.human.HumanPlayer;
import DotsBoxes.observers.AlphaBetaPruningObserver;
import DotsBoxes.observers.NodeCounterObserver;
import DotsBoxes.referee.Referee;
import org.jgrapht.alg.util.Pair;

import java.util.*;

/**
 * Moteur principal du jeu Points et Cases (Dots and Boxes).
 * <p>
 * Gère la boucle principale de jeu, l'alternance entre les joueurs,
 * l'affichage du plateau et des scores, ainsi que la détermination du gagnant.
 * </p>
 * 
 * <p>
 * Supporte plusieurs types de joueurs :
 * <ul>
 *   <li>Joueurs humains (entrée utilisateur)</li>
 *   <li>Joueurs automatiques simples (Glouton, Random)</li>
 *   <li>Joueurs IA intelligents (Minimax, Alpha-Beta, Expert)</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Utilise le pattern Singleton pour l'Arbitre et implémente
 * le fonctionnement complet du jeu selon les règles standards.
 * </p>
 * 
 * @version 1.0
 * @author L3 UPS
 * @see Player
 * @see Board
 * @see Referee
 */
public class DotsBoxesGame {
    private final boolean DISPLAY_TIME = true;
    private final boolean DISPLAY_BOARDS = false;
    private final boolean DISPLAY_SCORES = false;
    private final boolean DISPLAY_PLAYER = false;

    /** Plateau de jeu. */
    private final Board board;
    
    /** Premier joueur. */
    private final Player player1;
    
    /** Deuxième joueur. */
    private final Player player2;
    
    /** Joueur actuel (celui dont c'est le tour). */
    private Player currentPlayer;
    
    /** Arbitre singleton pour gérer le score et les règles. */
    private final Referee referee;

    /**
     * Crée une nouvelle partie de Dots and Boxes.
     *
     * @param rows nombre de lignes de points du plateau
     * @param cols nombre de colonnes de points du plateau
     * @param p1   joueur 1 (commence en bleu)
     * @param p2   joueur 2 (commence en rouge)
     */
    public DotsBoxesGame(int rows, int cols, Player p1, Player p2) {
        this.board = new Board(rows, cols);
        this.player1 = p1;
        this.player2 = p2;
        this.currentPlayer = player1;
        this.referee = Referee.getInstance();
        referee.init(player1, player2, board);
    }

    /**
     * Lance et exécute la partie.
     * <p>
     * Boucle principale du jeu :
     * <ol>
     *   <li>Affiche le plateau et les scores</li>
     *   <li>Demande une action au joueur courant</li>
     *   <li>Valide et applique l'action (pénalité -1 si coup invalide)</li>
     *   <li>Change de joueur si aucune case n'a été fermée</li>
     *   <li>Boucle jusqu'à ce que le plateau soit plein</li>
     * </ol>
     * </p>
     *
     * @return le joueur gagnant, ou {@code null} en cas d'égalité
     */
    public Player play() {

        while (!board.isFinished()) {

            if (DISPLAY_BOARDS) displayBoard();
            if (DISPLAY_SCORES) displayScores();

            if (DISPLAY_PLAYER)
                System.out.println("\nTour du joueur " + currentPlayer.getId());

            long startTime = System.nanoTime();
            Action action = currentPlayer.getAction(board);
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            if (DISPLAY_TIME) {
                System.out.println(currentPlayer.getId() + " : " + duration / 1000000000.0 + "s");
            }

            if (action == null || !board.isValid(action)) {
                referee.applyInvalidMovePenalty(currentPlayer);
                System.out.println("❌ Coup invalide — pénalité -1 et tour perdu");
                switchPlayer();
                continue;
            }

            int gainedBoxes = referee.applyAction(currentPlayer, action);

            // Règle essentielle : on rejoue si on ferme une ou plusieurs cases
            if (gainedBoxes == 0) {
                switchPlayer();
            }
        }

        displayBoard();
        displayScores();

        Player winner = referee.getWinner();
        if (winner == null) {
            System.out.println("\n🤝 Match nul !");
        } else {
            System.out.println("\n🏆 Joueur " + winner.getId() + " gagne !");
        }

        return winner;
    }

    /**
     * Passe à l'autre joueur.
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Affiche le plateau en mode ASCII colorisé.
     */
    private void displayBoard() {
        System.out.println("\n================ Plateau ================");
        board.printAscii();
        System.out.println("=========================================");
    }

    /**
     * Renvoie les elos calculés de chaque joueur, et la matrice des victoires / défaites
     * Si M est la matrice des victoires / défaites, alors M[i][j] donne le nombre de victoires du i-ème joueur contre
     * le j-ème, et M[j][i] donne le nombre de défaites du j-ème contre le i-ème joueur.
     * Une égalité est considérée comme une victoire, et une défaite en même temps.
     * @param players la liste des joueurs
     * @param rows Le nombre de lignes pour la taille du plateau
     * @param cols le nombre de colonnes pour la taille du plateau
     * @param iterations le nombre de parties que chaque joueur fait
     * @return les elos, et les victoires / défaites
     */
    public static Pair<List<Integer>, List<List<Integer>>> computeElo(List<ActionStrategy> players, int rows, int cols, int iterations) {
        float K = 40;

        List<Integer> elos = new ArrayList<>(players.size());
        for (int i=0; i<players.size(); i++) {
            elos.add(800);
        }

        List<List<Integer>> matrice_victoires = new ArrayList<>(players.size());
        for (int i=0; i<players.size(); i++) {
            matrice_victoires.add(new ArrayList<>(players.size()));
            for (int j=0; j<players.size(); j++) {
                matrice_victoires.get(i).add(0);
            }
        }

        List<Integer> ints = new ArrayList<>(players.size());
        for (int i=0; i<players.size(); i++) { ints.add(i); }
        List<Integer> ints2 = new ArrayList<>(ints);
        for (int k=0; k<iterations; k++) {
            if (k == 30) K = 20;
            Collections.shuffle(ints);
            for (Integer i : ints) {
                Collections.shuffle(ints2);
                for (Integer j : ints2) {
                    if (!Objects.equals(j, i)) {
                        Board board = new Board(rows, cols);
                        int playerId = 0;
                        // Pénalités pour coup invalide, comme dans play() : -1 point et tour perdu.
                        int[] penalties = {0, 0};
                        while (!board.isFinished()) {
                            Action action = players.get(i + (j - i) * playerId).selectAction(board, playerId);
                            if (action == null || !board.isValid(action)) {
                                // Certaines IA (Expert) passent volontairement leur tour en renvoyant null.
                                penalties[playerId]++;
                                playerId = -playerId + 1;
                                continue;
                            }
                            int c = board.apply(action, playerId);
                            if (c == 0) playerId = -playerId + 1;
                        }
                        int scoreI = board.getScore(0) - penalties[0];
                        int scoreJ = board.getScore(1) - penalties[1];
                        // W = part des points obtenue par le joueur i, bornée à [0, 1].
                        float W;
                        if (scoreI + scoreJ <= 0) W = 0.5f;
                        else W = Math.max(0f, Math.min(1f, (float) scoreI / (scoreI + scoreJ)));

                        if (scoreI > scoreJ) {
                            matrice_victoires.get(i).set(j, matrice_victoires.get(i).get(j)+1);
                        } else if (scoreI < scoreJ) {
                            matrice_victoires.get(j).set(i, matrice_victoires.get(j).get(i)+1);
                        } else {
                            matrice_victoires.get(i).set(j, matrice_victoires.get(i).get(j)+1);
                            matrice_victoires.get(j).set(i, matrice_victoires.get(j).get(i)+1);
                        }

                        int D = elos.get(i) - elos.get(j);
                        // Probabilité de gain attendue du joueur i (formule Elo standard, échelle de 400 points).
                        double proba_gain_i = 1.0 / (1 + Math.pow(10, -D / 400.0));
                        int gain = (int) (K * (W - proba_gain_i));
                        elos.set(i, (elos.get(i) + gain));
                        elos.set(j, (elos.get(j) - gain));
                    }
                }
            }
        }
        return new Pair<>(elos, matrice_victoires);
    }

    /** Code ANSI pour réinitialiser la couleur du terminal. */
    private static final String ANSI_RESET = "\u001B[0m";
    /** Code ANSI pour afficher en bleu (joueur 0). */
    private static final String ANSI_BLUE  = "\u001B[34m";
    /** Code ANSI pour afficher en rouge (joueur 1). */
    private static final String ANSI_RED   = "\u001B[31m";

    /**
     * Affiche le score courant des deux joueurs avec code couleur.
     */
    private void displayScores() {
        System.out.println("\nScores :");

        int score1 = referee.getScore(player1);
        int score2 = referee.getScore(player2);

        System.out.println("Joueur 0 : " + ANSI_BLUE + score1 + ANSI_RESET);
        System.out.println("Joueur 1 : " + ANSI_RED + score2 + ANSI_RESET);
    }

    /* ======================================================
       =============== POINT D’ENTRÉE =======================
       ====================================================== */

    private static int get_max_name_size(List<ActionStrategy> players) {
        int max_name_size = 0;
        for (ActionStrategy player : players) {
            if (player.getName().length() > max_name_size)
                max_name_size = player.getName().length();
        }
        return max_name_size;
    }

    private static int get_max_number_size(List<List<Integer>> matrix) {
        int max_size = 0;
        for (List<Integer> list: matrix) {
            for (Integer i: list) {
                if (Math.log10(i) > max_size) {
                    max_size = (int) Math.log10(i);
                }
            }
        }
        return max_size + 1;
    }

    private static void print_victories_matrix(List<List<Integer>> matrix, List<ActionStrategy> players) {
        int max_name_size = get_max_name_size(players);
        for (int i=0; i<players.size(); i++) {
            System.out.print(players.get(i).getName());
            for (int j=0; j<max_name_size - players.get(i).getName().length(); j++) {
                System.out.print(" ");
            }

            int max_number_size = get_max_number_size(matrix);
            for (int j=0; j<players.size(); j++) {
                int size;
                if (matrix.get(i).get(j) == 0) size = 1;
                else size = (int) Math.log10(matrix.get(i).get(j)) + 1;
                for (int k=0; k<max_number_size-size; k++)
                    System.out.print(" ");
                System.out.print("  " + matrix.get(i).get(j));
            }
            System.out.println();
        }
    }

    /**
     * Point d'entrée en ligne de commande.
     * <p>
     * Demande la taille du plateau, le type des deux joueurs, puis lance
     * une partie complète.
     * </p>
     *
     * @param args arguments de ligne de commande (non utilisés)
     */
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Dots and Boxes ===");

        System.out.println("1 = jouer une partie");
        System.out.println("2 = tester les IA");
        int response1 = scanner.nextInt();

        if (response1 == 2) {
            List<ActionStrategy> players = new ArrayList<>();
            players.add(new GloutonActionStrategy());
            players.add(new RandomActionStrategy());
            players.add(new FirstValidActionStrategy());
            players.add(new AlphaBetaActionStrategy(4, new AlphaBetaPruningObserver(),
                    new NodeCounterObserver()));
            players.add(new MctsActionStrategy(1000));
            players.add(new GloutonMctsActionStrategy(0.1));
            players.add(new ExpertActionStrategy());

            System.out.println("Taille du plateau (rows cols), ex: 4 4 : ");
            int rows = scanner.nextInt();
            int cols = scanner.nextInt();

            System.out.println("Nombre d'itérations : ");
            int iterations = scanner.nextInt();

            Pair<List<Integer>, List<List<Integer>>> competition_infos = computeElo(players, rows, cols, iterations);
            List<Integer> elos = competition_infos.getFirst();
            List<List<Integer>> matrice_victoires = competition_infos.getSecond();
            for (int i=0; i<players.size(); i++) {
                System.out.println(players.get(i).getName() + " : " + elos.get(i));
            }

            print_victories_matrix(matrice_victoires, players);

        }
        else if (response1 == 1) {

            System.out.println("\nTaille du plateau (rows cols), ex: 4 4 : ");
            int rows = scanner.nextInt();
            int cols = scanner.nextInt();

            // Choix joueur 1
            System.out.println("\nType du joueur 1 :");
            System.out.println("1 = Humain");
            System.out.println("2 = Automate (Glouton)");
            System.out.println("3 = Automate (Random)");
            System.out.println("4 = IA (Minimax)");
            System.out.println("5 = IA (Alpha-Beta)");
            System.out.println("6 = IA (Expert)");
            System.out.println("7 = IA (Expert experimental)");
            System.out.println("8 = IA (MCTS)");
            System.out.println("9 = IA (MCTS avec simulation gloutonne)");
            int type1 = scanner.nextInt();
            Player player1 = createPlayer(0, type1);

            // Choix joueur 2
            System.out.println("\nType du joueur 2 :");
            System.out.println("1 = Humain");
            System.out.println("2 = Automate (Glouton)");
            System.out.println("3 = Automate (Random)");
            System.out.println("4 = IA (Minimax)");
            System.out.println("5 = IA (Alpha-Beta)");
            System.out.println("6 = IA (Expert)");
            System.out.println("7 = IA (Expert experimental)");
            System.out.println("8 = IA (MCTS)");
            System.out.println("9 = IA (MCTS avec simulation gloutonne)");
            int type2 = scanner.nextInt();
            Player player2 = createPlayer(1, type2);

            // Lancement du jeu
            DotsBoxesGame game = new DotsBoxesGame(rows, cols, player1, player2);
            game.play();
        }
    }

    /**
     * Fabrique un joueur à partir d'un identifiant et d'un type sélectionné.
     *
     * @param id identifiant du joueur ({@code 0} ou {@code 1})
     * @param type type de joueur choisi au menu
     *             ({@code 1}=Humain, {@code 2}=Glouton, {@code 3}=Random,
     *             {@code 4}=Minimax, {@code 5}=Alpha-Beta, {@code 6}=Expert)
     * @return instance de joueur correspondant au type demandé
     * @throws IllegalArgumentException si le type n'est pas reconnu
     */
    private static Player createPlayer(int id, int type) {

        return switch (type) {
            case 1 -> new HumanPlayer(id);

            case 2 -> new AutomatePlayer(
                    id,
                    new GloutonActionStrategy()
            );

            case 3 -> new AutomatePlayer(
                    id,
                    new RandomActionStrategy()
            );

            case 4 -> new AIPlayer(
                    id,
                    new MinimaxActionStrategy(3)
            );

            case 5 -> new AIPlayer(
                    id,
                    new AlphaBetaActionStrategy(
                            5,
                            new AlphaBetaPruningObserver(),
                            new NodeCounterObserver()
                    )
            );

            case 6 -> new AIPlayer(
                    id,
                    new ExpertActionStrategy()
            );

            case 7 -> new AIPlayer(
                    id,
                    new ExperimentalExpertActionStrategy(1_000_000_000)
            );

            case 8 -> new AIPlayer(
                    id,
                    new MctsActionStrategy(200000)
            );

            case 9 -> new AIPlayer(
                    id,
                    new GloutonMctsActionStrategy(0.5)
            );

            default -> throw new IllegalArgumentException(
                    "Type de joueur invalide : " + type
            );
        };
    }
}
