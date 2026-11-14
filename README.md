# IA pour le jeu de la Pipopipette (Dots and Boxes)

Agents d'intelligence artificielle pour la Pipopipette : algorithme glouton, Minimax, élagage alpha-bêta, recherche arborescente Monte-Carlo (MCTS) et un agent «expert» qui combine plusieurs optimisations.

Projet du cours d'IA symbolique (L3, double licence informatique-mathématiques, Université Paris-Saclay, 2026), réalisé en équipe de quatre. Notre IA a terminé 2e sur 8 équipes au tournoi de la promotion.


## Le jeu

Deux joueurs tracent à tour de rôle un segment entre deux points voisins d'une grille. Celui qui trace le quatrième côté d'une case la gagne et rejoue. Quand tous les segments sont tracés, le joueur qui possède le plus de cases gagne.

Le jeu n'est donc pas strictement alterné, et fermer une case dès que possible est souvent une erreur : cela peut obliger à ouvrir ensuite une longue chaîne de cases à l'adversaire.

## Les agents

| Agent | Principe |
| --- | --- |
| Random | Coup tiré au hasard |
| First valid | Premier segment libre |
| Glouton | Ferme une case s'il le peut, sinon joue au hasard |
| Minimax | Recherche à profondeur limitée, évaluation par la différence de score |
| Alpha-Beta | Minimax avec élagage alpha-bêta |
| MCTS | Sélection UCB1, expansion, simulation aléatoire, rétropropagation |
| MCTS glouton | MCTS limité en temps, avec des simulations jouées par l'agent glouton et prise immédiate des cases disponibles |
| Expert | Alpha-bêta amélioré, décrit ci-dessous |

L'agent expert ajoute à l'alpha-bêta :

- une **profondeur adaptative** : elle est recalculée à chaque coup à partir d'un budget de feuilles, donc la recherche s'approfondit à mesure que les segments libres se raréfient ;
- un **tri des coups** : ceux qui ferment une case sont explorés en premier, ce qui déclenche les coupes plus tôt ;
- une **table des positions déjà évaluées**, pour ne pas recalculer un plateau atteint par un autre ordre de coups ;
- un **tirage aléatoire parmi les meilleurs coups**, pour ne pas rejouer toujours la même partie ;
- une **passe volontaire** : dans une position perdante où son coup offrirait une case à l'adversaire, il préfère le plus souvent la pénalité d'un point pour coup invalide.

Le raisonnement suivi, les mesures de temps et les essais abandonnés sont dans les notes du projet : [`docs/rapport.md`](docs/rapport.md).

## Résultats

Tournoi entre les sept agents sur une grille de 4 × 4 points (9 cases) : chaque paire s'affronte 60 fois, 30 fois avec chaque joueur en premier. Le tableau donne le nombre de victoires de l'agent en ligne contre l'agent en colonne, et le classement Elo calculé au fil des parties.

| | Elo | Expert | Alpha-Beta | Glouton | MCTS glouton | MCTS | First valid | Random |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **Expert** | 1070 | – | 55 | 55 | 54 | 60 | 60 | 60 |
| **Alpha-Beta** (profondeur 4) | 966 | 6 | – | 56 | 36 | 60 | 60 | 60 |
| **Glouton** | 913 | 7 | 4 | – | 15 | 59 | 60 | 60 |
| **MCTS glouton** (0,1 s par coup) | 881 | 7 | 24 | 45 | – | 58 | 60 | 60 |
| **MCTS** (1000 itérations) | 659 | 0 | 0 | 1 | 2 | – | 53 | 52 |
| **First valid** | 578 | 0 | 0 | 0 | 0 | 7 | – | 33 |
| **Random** | 533 | 0 | 0 | 0 | 0 | 8 | 27 | – |

Une partie nulle compte pour les deux joueurs. Ces chiffres viennent d'une exécution du mode « tester les IA » ; ils varient un peu d'une exécution à l'autre, parce que plusieurs agents tirent leurs coups au hasard.

L'agent expert gagne environ 90 % de ses parties contre l'alpha-bêta simple, le glouton et le MCTS glouton. Le MCTS à simulations aléatoires bat nettement le hasard mais perd contre le glouton : sur ce jeu, une fin de partie jouée au hasard ne ressemble pas à une vraie partie.

## Compiler et lancer

Il faut Java 17 et Maven 3.8 ou plus récent.

```bash
mvn clean compile        # compilation
mvn test                 # tests unitaires (JUnit 5)
mvn javadoc:javadoc      # documentation dans target/site/apidocs/
```

Deux points d'entrée, à lancer depuis un IDE :

- `DotsBoxes.DotsBoxesGame` : interface texte. Le choix 1 lance une partie entre deux joueurs au choix (humain ou agent), le choix 2 lance le tournoi entre agents et affiche le classement Elo et la matrice des victoires ;
- `DotsBoxes.ui.DotsBoxesSwingUI` : interface graphique.

## Organisation du code

```
src/main/java/DotsBoxes/
  board/        plateau et actions
  referee/      arbitre : scores et pénalités
  player/
    automate/   Random, First valid, Glouton
    ai/         Minimax, Alpha-Beta, MCTS, MCTS glouton, Expert
    human/      joueur humain en mode texte
  observers/    compteurs de nœuds visités et de coupes alpha-bêta
  utils/        arbre du MCTS, clé de hachage d'un plateau
  ui/           interface graphique Swing
  DotsBoxesGame.java   boucle de jeu, tournoi et classement Elo
src/test/java/  tests unitaires
docs/rapport.md notes et analyses rédigées pendant le projet
```

Le moteur de jeu (plateau, actions, arbitre), les deux interfaces, les observateurs et une partie des tests ont été fournis par l'équipe enseignante. Notre travail porte sur les stratégies des paquets `automate` et `ai`, le paquet `utils`, le tournoi avec classement Elo et les tests ajoutés pour nos agents.

## Corrections apportées après le rendu

Trois erreurs ont été corrigées lors de la reprise du projet pour sa publication :

- **Simulation du MCTS.** La partie simulée se jouait directement sur le plateau du nœud au lieu d'une copie. Après sa première simulation, chaque nœud contenait donc un plateau terminé et n'était plus jamais développé. Sur 200 parties contre l'agent Random (grille de 4 × 4 points, 1000 itérations), le MCTS passe de 157 à 185 victoires une fois l'erreur corrigée.
- **Tournoi.** Le mode « tester les IA » s'arrêtait sur une erreur quand l'agent expert passait volontairement son tour. Un coup nul ou invalide y est maintenant traité comme en partie normale : un point de pénalité et le tour passe.
- **Classement Elo.** La probabilité de gain attendue n'utilisait pas l'échelle de 400 points de la formule Elo.

## Limites et pistes

- La fonction d'évaluation ne regarde que la différence de score. Elle ne reconnaît pas les chaînes de cases, qui décident pourtant des fins de partie.
- Dans le MCTS, les nœuds de l'adversaire sont choisis avec le même critère que ceux du joueur, alors qu'ils devraient minimiser son gain.
- Le budget de feuilles de l'agent expert est fixé à la main ; un budget en temps serait plus robuste d'une machine à l'autre.

## Auteurs

Niccolò Angeli, Raphaël Dubach, Clara Chebout et Léonor Martin-Nourry.

