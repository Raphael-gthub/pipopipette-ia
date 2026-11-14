# Notes et analyses du projet

> Ces notes ont été rédigées par l'équipe pendant le projet et sont reproduites sans modification. Deux remarques pour les lire :
> les observations sur le « MCTS classique » ont été faites avant la correction d'une erreur
> dans la simulation (voir le README), et le classement Elo y était calculé sans l'échelle de
> 400 points de la formule standard.

# Partie 1


## Question 1 : Générez la documentation Javadoc du projet fourni par l’enseignant et analysez l’architecture logicielle :

#### 1. Comment est représenté l’état du jeu ?

L'état du jeu est représenté avec la classe Board.
Celle-ci a une matrice de segments horizontaux, et une matrice de segments verticaux. Une case vaut -1 tant qu'elle n'est pas capturée, puis contient l'identifiant du joueur propriétaire (0 ou 1).\
(horizontaux : matrice rows x (cols - 1)\
(verticaux : matrice (rows - 1) x cols)

#### 2. Comment sont modélisées les actions ?

Une action est représentée par la classe Action. Celle-ci contient l'orientation (horizontal ou vertical) et la position (ligne colonne) du segment à ajouter.

#### 3. Comment est implémentée la fonction de transition ?

La class Board a une méthode *apply(action, playerId)* qui joue le coup sur le board, et renvoie le nombre de cases fermées.

#### 4. Comment est calculé le score ?

La méthode *getScore(int playerId)* calcule le score courant d'un joueur sur le plateau.

#### 5. Comment est géré le fait qu’un joueur puisse rejouer ?

La classe DotsBoxesGame a la logique générale du jeu. Elle a une méthode *play* qui permet de lancer une partie. Dans la
boucle principale de cette méthode, il change de joueur seulement si le nombre de cases fermées change.




## Question 2 : Donnez la définition formelle du jeu Pipopipette

#### 1. Les joueurs :

P = {0, 1}
Le premier joueur est identifié avec 0, et le deuxième avec 1.

#### 2. Les états : 

S = {(H, V) | H la matrice des segments horizontaux, et V la matrice des segments verticaux}

H et V sont à valeur dans {-1, 0, 1}

Ainsi, |S| = 3 ** (rows * (cols - 1) + (rows - 1) * cols) \
    = 3 ** (2 * cols * rows - cols - rows)

#### 3. Les actions :

A = {(type, (line, col))) | type dans {hor, vert}, 0 <= line < rows et 0 <= col < cols}

#### 4. La fonction de transition : 

T : S x A --> S

Si S = (H, V) et A = (type, (line, col))) \
alors T(S, A) = S'\
avec S' = (H', V') où H'[line, col] = idPlayer si type = hor,
et V'[line, col] = idPlayer si type = vert.

#### 5. La fonction d'utilité : 

U: S --> N\
(N est l'ensemble des entiers)

U est le nombre de cases fermées par le joueur 1 - le nombre de cases fermées par le joueur 2




## Question 3 : Analysez les propriétés de l’arbre de jeu :

#### Quel est le facteur de branchement au début de la partie pour une grille N × M ?

Au début de la partie, on peut placer un segment n'importe où sur la grille. Il y a donc N x (M-1) possibilités pour les 
segments horizontaux, et (N-1) x M pour les verticaux.
Il y a ainsi un total de N x (M-1) + (N-1) x M actions possibles.

#### Comment évolue-t-il au cours de la partie ?

À chaque coup, le prochain joueur ne pourra plus jouer ce coup-là. Donc le nombre de coups possibles diminue de 1 à 
chaque tour.

#### Quelle est la profondeur maximale de l’arbre de jeu ?

Une partie se termine quand tous les segments ont été placés. A chaque étage, le joueur place un segment. L'arbre aura
donc N x (M-1) + (N-1) x M étages.

#### Le jeu est-il strictement alterné ? Quelles conséquences cela a-t-il sur Minimax ?

Le jeu n'est pas strictement alterné, c'est-à-dire que parfois, un joueur pourra jouer 2 fois de suite. Ainsi, dans 
l'algorithme minimax, on ne va pas toujours alterner entre des étages min et des étages max. Parfois, on aura plusieurs 
étages max à la suite.




# Partie 2


## Analyse de parties : Glouton vs Random

Après 10 parties, nous avons obtenu un score moyen de 7,3 pour le glouton, et 1,3 pour le random.

On remarque que la stratégie gloutonne est parfois sous-optimale: elle va parfois laisser des occasions de prises à 
l'ennemi. On pourrait corriger cela en ajoutant dans l'heuristique le fait de choisir les actions qui ne laissent pas 
d'opportunités de prises à l'ennemi. Mais ainsi, on a un autre problème. En effet, il ne pourra pas choisir le 
meilleur sacrifice parmi plusieurs sacrifices.

Nous avons besoin d'un algorithme plus fin pour cela.


# Partie 3 

## Choix de l'heuristique

On choisit ici de faire l'heuristique suivante : monScore moins le score de l'adversaire.

En effet, le score monte lorsqu'on est en train de gagner et descend lorsqu'on est en train de perdre. En plus de cela, 
la fonction est extrêmement rapide à être calculée. Cela est primordial, au vu des coûts en temps désastreux du minimax.


On pourrait choisir une heuristique plus fine. Par exemple, on pourrait ajouter des points si on est en possibilité de 
prise. Mais alors, le coût en temps serait beaucoup plus élevé, et donc, ça diminuerait grandement la profondeur 
maximale de l'arbre qu'on pourrait avoir avec le même temps.


## Impact du facteur de branchement

On remarque que le facteur de branchement est immense. Ainsi, le minimax est quasiment inutilisable au début de la 
partie. Voici quelques caractéristiques (sur mon ordinateur portable) :
- Avec une grille 2x2 : < 1s pour une profondeur de 3.
- Avec une grille 4x4 : > 5s pour une profondeur de 3, < 1s pour une profondeur 2

On remarque que ça devient très rapidement inutilisable, dès qu'on commence à avoir une grille grande. On doit donc 
l'optimiser, via par exemple l'élagage alpha bêta.


## Comparaison avec et sans l'élagage alpha-bêta

Si on le teste sur une grille 5x5, avec une profondeur de 5, le minimax sans élagage prend environ **30s**, alors 
qu'avec l'élagage, il prend environ **0.2s**.

La différence est vraiment notable.

D'après le cours, avec et sans élagage, le résultat devrait de plus rester le même ! Nous avons ajouté des tests pour 
vérifier cela.

# Partie 4 : vers un agent expert

## Comment classer les IA

Lorsqu'on va créer des IA, il nous faut un moyen pour connaître leur performance. Voici quelques moyens mis en oeuvres 
pour cela.

### Matrice de victoires / défaites

On commence par faire un tournoi, où chaque agent va jouer un certain nombre de fois contre tous les autres agents. 
Ensuite, on stocke dans une matrice les victoires : si l'agent i gagne contre l'agent j, alors on ajoute +1 à la case de 
la matrice\[i\]\[j\]. Ainsi, dans la case matrice\[j\]\[i\], on a le nombre de défaites de l'IA i contre l'IA j.

### Elo

On a décidé aussi d'ajouter un classement elo des différentes IA. Le code suit les explications trouvées sur 
https://fr.wikipedia.org/wiki/Classement_Elo. Cela nous permet d'avoir une métrique efficace pour savoir quelles IA 
sont meilleures, et lesquelles sont moins bonnes. 

Nous avons remarqué que pour que le classement elo soit le moins discriminant possible, il est important que les 
parties soient faites dans un ordre aléatoire, et de manière uniforme. Une IA ne devrait pas faire beaucoup plus de 
parties qu'une autre, et ne devrait pas faire les parties toujours dans le même ordre.

## Les améliorations pour les IA

### MCTS

Le facteur de branchement, étant immense au début, on pourrait utiliser un MCTS, plutôt qu'avoir un minimax avec une 
profondeur très petite. L'idée serait donc d'utiliser un MCTS tant que le facteur de branchement est trop élevé, et 
lorsqu'il devient plus praticable par un minimax (en fin de partie), on utilisera plutôt un minimax.

#### Première version : MCTS classique

La première version du MCTS a été codée en suivant le cours : la simulation est parfaitement aléatoire.
Mais en utilisant les métriques présentées précédemment, on s'est rendus compte que l'IA avait un score quasiment 
identique au score de l'IA random.

#### Deuxième version : MCTS avec simulation gloutonne

On s'est dit qu'une simulation complètement aléatoire n'est pas représentative d'une vraie partie. En effet, lors d'une 
vraie partie, si on peut choisir une case qui ferme, on la choisie. On a alors décidé de faire une simulation qui 
utilise l'agent glouton.

En le testant, on s'est rendus compte qu'il avait un score plus faible que celui de l'automate glouton ! En effet, 
parfois il ne prend pas des cases qu'il pourrait prendre. 

#### Troisième version : MCTS avec simulation gloutonne, et heuristique gloutonne

À présent, avant de lancer l'MCTS, on vérifie s'il n'y a pas des cases prenables. S'il y en a, il les prend.

À présent, il a un score similaire à celui du minimax !!!


### Améliorations du minimax

On voudrait une profondeur la plus grande possible. Mais pour cela, il faut que l'agent soit le plus rapide possible. 
Cette section a donc pour but de trouver des moyens pour optimiser le minimax.

#### Heuristique pour l'élagage alpha-bêta

On remarque que pour faire aller plus vite l'élagage alpha-bêta, il faudrait lancer les exécutions qui risquent d'être
meilleures en premier. Ainsi, on décide de tenter d'abord les actions qui ferment des cases.

Les tests nous ont montré que parfois, le temps passé à trier la liste est plus grande que celle qu'on gagne avec cette 
nouvelle heuristique. Mais cette heuristique améliore largement la vitesse en milieu de jeu.  
Il nous faudrait donc une autre heuristique pour savoir quand est-ce qu'il est intéressant d'utiliser cette heuristique.
D'autre part, on part du principe qu'on utilisera le minimax lorsque quelques coups auront déjà été joués par le MCTS,
donc ce problème de performances n'est pas un problème pour nous.

#### Se rappeler des nœuds déjà visités

Souvent, on passe plusieurs fois par le même nœud. Par exemple, si la joueur 0 fait (H, (0, 0)), puis le joueur 1 fait 
(H, (0, 1)), ou si le joueur 0 fait (H, (0, 1)), puis le joueur 1 fait (H, (0, 0)), l'état du jeu est le même.

On peut donc garder une table des nœuds déjà visités. Ainsi, lorsqu'on retrouve un nœud identique, on peut directement
voir dans cette table la valeur du nœud associée.

Les tests ont montré une légère amélioration de la vitesse, mais aucune amélioration majeure.

#### Coup aléatoire

Pour l'instant, il prend le premier coup avec le meilleur score. Ainsi, son coup est prévisible, et le début de la 
partie est quasiment toujours le même. On décide plutôt de prendre un coup aléatoire parmi ceux qui maximisent le score.
Les parties contre cette IA seront ainsi beaucoup plus intéressantes.

#### Profondeur variable

En fonction de l'état d'avancement dans la partie, le facteur de branchement n'est pas le même. Ainsi, vers la fin de la
partie, il peut aller beaucoup plus profondément qu'au début. On peut donc mettre une heuristique du nombre de feuilles 
qu'il peut visiter dans l'arbre avant de prendre trop de temps, et choisir la profondeur en fonction de ce nombre.  
Par exemple, si le nombre de feuilles est 300 000, et que le facteur de branchement actuel est 15, il pourra avoir une 
profondeur de 4, car 15x14x13x12 = 32 760 < 300 000, et 15x14x13x12x11 = 360 360 > 300 000.
Et si le facteur de branchement actuel est 9, il pourra avoir une profondeur de 7 (voir quasiment toute la partie).

**Remarque** : Si la profondeur est inférieur à 2, alors le minimax devient très mauvais : il pourrait donner des 
occasions de prises gratuites à l'ennemi. Donc si la profondeur est inférieure à 2, on pourrait passer au MCTS.