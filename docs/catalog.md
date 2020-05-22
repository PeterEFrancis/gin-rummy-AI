# Code developement catalog


## Standard Cycle of developement

### Preliminary

- Make a player that, when confronted with a decision, uses features about its own game state and pre-calculated data to probabilistically decide which choice will lead to a most probable win.
    - `Player`, does the playing and calls
    - `BlackBox` which runs the probability function on the game state, using
    - `OurUtilities` which has a function that calculates all of the features of the player
- Make a way to save features about game states and their respective winner outcomes (`Collect`)
- Make a dummy player (`SimpleGinRummyPlayer`)


### Cycle
- Add features to `OurUtilities.calculateFeatures()`
    - The .csv output in the header of `Collect` also needs to be changed
- Have the current version `Player` play itself multiple times
    - It uses the `OurUtilities.calculateFeatures()` but only regards the features that are used in the regression for the player's `version` and `type`
- Collect the data from the games (all possible features and results)
- Do a regression analysis on the data that predicts the result from the features
    - the features of one player state is a vector. The regression gives us another vector of coefficients with which to dot any feature vector and get an approximation of the outcome (plus a constant)
- Add the line of coefficients to the list of coefficient versions
- Change the player to be the next version
- Repeat


----




## Week 1

### Tuesday 5/19/20

Review starter code and began development on basic features by modifying "GinRummyGame.java", through basic linear regression visualization.

**Features**: scores, deadwood count, numOptions -> current player wins hand, wins game

current_player_deadwood vs. is_current_player_hand_winner:
![](img/5-19-20_a_linear.png)

---

### Wednesday 5/20/20

Today we finished developing the alpha data production collection. Set up jupyter notebook for linear and logistic regression. Added many utilities involving hit cards. Started work on another simple player based on hit cards as well as melds and deadwood.

current_player_num_hit_cards vs. is_current_player_hand_winner (linear)
![](img/5-20-20_a-2_linear.png)


current_player_num_hit_cards vs. is_current_player_hand_winner (logistic)
![](img/5-20-20_a-2_logistic.png)


### Thursday 5/21/20

Made `BlackBox` and `Player` class for data driven function generation development. Made and deleted the `State` class. Got a regression image that was more accurate (not including the hand winner as input). Modeled a decision tree regarding a maximization problem in the `Player` class. Learned about how the `GinRummyGame` interface works with the `Player` classes by attempting to cheat.


### Friday 5/21/20

We fixed the alpha player from making most illegal moves. Added 9 features to `OurUtilities.calculateFeatures()` for progress on the Beta Player.
