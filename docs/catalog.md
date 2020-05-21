# Code developement catalog


## Week 1

### Tuesday 5/19/20

Review starter code and began developement on basic features by modifying "GinRummyGame.java", through basic linear regression visualization.

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
