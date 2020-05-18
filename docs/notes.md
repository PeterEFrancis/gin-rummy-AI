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
 - number of cards in
 - face value of intended lay-off card
 - number of hits needed to knock or Gin
 - number of melds
 - number of cards left in the deck
 - usefulness measure of the cards that we know are in the opponent's hand
 - number of cards that the opponent has drawn face up
 - point difference between the two players
 - the knock count
 - for each card:
   - the number of adjacent cards in the opponent's hand
   - probability that the card is in the other person's hand
   - probability that the opponent desires that card
 - net points
 - number of cards left in the deck
 - number of hit cards (cards that would fit into a meld)
 - measure of how close the melds in your hand are to being the same size




### Best features

1. number of hit cards
2. player scores
3. deadwood points


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
