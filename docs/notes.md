# notes

Rank the top three (derived) features you would select to predict (1) hand score, and (2) probability of winning the game



### Ideas for features
- "Gin rating"
- number of deadwood points in hand
    - number of load cards
    - point summation of load cards
    - number of cards in the knock cache
    - point summation of cards in the knock cache
    - number of cards in combinations
    - point summation of cards in combinations
- face value of intended lay-off card
- number of hits needed to knock or Gin
- number of melds
- number of cards left in the deck / number of cards in
- usefulness measure of the cards that we know are in the opponent's hand
- number of cards that the opponent has drawn face up
- point difference between the two players
- for each card:
    - the number of adjacent cards in the opponent's hand
    - probability that the card is in the other person's hand
    - probability that the opponent desires that card
- net points
- number of hit cards (cards that would fit into a meld)
- measure of how close the melds in your hand are to being the same size
- number of cards that when added would allow you to shuffle your hand and reduce deadwood
- number of "combo"-hit cards

### Alpha Features
 - **current player score**
 - **opponent score**
 - **deadwood**
 - **number of hit cards**

### Beta Features
- current player score
- opponent score
- deadwood
- number of hit cards
- **number of melds**
- **point sum of melds**
- **number of combos**
- **point sum of combos**
- **size of knock cache**
- **point sum of knock cache**
- **number of load cards**
- **point sum of load cards**
- **turns taken**


### Gamma Features
- **num_nearby_opponent_cards**


### Features not implemented (since beta)
- face value of intended lay-off card
- number of hits needed to Gin
- number of cards left in the stock (slightly different from turn total)
- usefulness measure of the cards that we know are in the opponent's hand
- number of cards that the opponent has drawn face up
- point difference between the two players (already know both scores)
- for each card:
    - the number of adjacent cards in the opponent's hand
    - probability that the card is in the other person's hand
    - probability that the opponent desires that card
- net points
- measure of how close the melds in your hand are to being the same size
- number of cards that when added would allow you to shuffle your hand and reduce deadwood
- number of "combo"-hit cards
- number of set/run melds/combos



##### Our Function

- Input
    - current game state (which cards we know, probability of other cards)
    - our cards in the hand
    - how far we are in the game
    - deadwood points

- Output - 2 actions:
    - which card to pick
    - which card to discard



### Questions
 - How can the player decide what meld to choose if one card is a part of two?




---

For each state, put into the Hand object:
`score1`, `score2`, `current_player`, `deadwood`, `num_hit_cards`

For each hand, put into the Game object:
 - each state from current player perspective PLUS whether the player won/lost the Hand

After game is over Output:

`current_player_score`, `opponent_score`, `current_player_deadwood`, `current_player_num_hit_cards`, **`is_current_player_hand_winner`**, **`is_current_player_game_winner`**

---






 ```java
 public static void main(String[] args) {
   System.out.println("Ryzeson can get in.");
 }
 ```


___
### Glossary
- **meld** - a set or a run
- **deadwood** - cards in a hand not melded
    - **combinations** - groups of cards that are close to being melded
    - **knock cache** - low rank cards in hand whose face values sum to less than the knock count
    - **load card** - card not in a combination or knock cache
- **hit card** - a card that if added would make a combination a meld
