import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class Player implements GinRummyPlayer{

	public int type;
	public int version;

	public int playerNum;
	public int startingPlayerNum;
	public int turn;

	public int[] scores = new int[2];

	public boolean opponentKnocked = false;

	public Random random = new Random();

	public Card drawnCard;
	public Card toDiscard;

	public ArrayList<Card> hand;
	public ArrayList<Card> unknownCards;
	public ArrayList<Card> possibleCards;
	public ArrayList<Long> drawDiscardBitstrings;
	public ArrayList<Card> opponentDiscards;
	public ArrayList<Card> opponentRejectedCards;
	public ArrayList<Card> opponentAllHand;   // opponent picked up cards throughout the startGame
	public ArrayList<Card> opponentHand;      // opponent picked up cards at the moment
	public ArrayList<Card> visibleCards;

	public Stack<Card> discardedCards;

	int[] discardCases = new int[52];


	public Player(int version, int type) {
		this.version = version;
		this.type = type;
	}

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] hand) {

		this.hand = new ArrayList<Card>();
		unknownCards = new ArrayList<Card>();
		possibleCards = new ArrayList<Card>();
		drawDiscardBitstrings = new ArrayList<Long>();
		opponentDiscards = new ArrayList<Card>();
		opponentRejectedCards = new ArrayList<Card>();
		opponentAllHand = new ArrayList<Card>();
		opponentHand = new ArrayList<Card>();
		discardedCards = new Stack<Card>();
		visibleCards = new ArrayList<Card>();

		drawnCard = null;
		toDiscard = null;

		turn = 0;
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.hand.clear();
		unknownCards.addAll(Arrays.asList(Card.allCards));
		possibleCards.addAll(Arrays.asList(Card.allCards));

		for (Card card : hand) {
			this.hand.add(card);
			unknownCards.remove(card);
			possibleCards.remove(card);
		}
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
	}




	@Override
	public boolean willDrawFaceUpCard(Card card) {
		card = OurUtilities.transformCard(card);

		// first turn
		if (discardedCards.isEmpty()) {
			discardedCards.push(card);
			unknownCards.remove(card);
			possibleCards.remove(card);
		}

		hand.add(discardedCards.pop()); // draw face up

		double max = -10000000;
		for (int i = 0; i < 10; i++) {
			//discard the first card in the hand
			Card discarded = hand.remove(0);
			discardedCards.push(discarded);

			double value = BlackBox.regFunction(this);
			if (value > max) {
				max = value;
				toDiscard = discarded;
			}
			// undo the discard
			hand.add(discardedCards.pop());
		}
		// System.out.println("faceup value: " + max);

		discardedCards.push(hand.remove(0)); //undoes the draw

		double average = 0;
		for (int i = 0; i < possibleCards.size(); i++) {
			// draw card
			Card newCard = possibleCards.remove(0);
			unknownCards.remove(newCard);

			hand.add(newCard);

			double localmax = -10000000;
			Card bestDiscard = null;
			for (int j = 0; j < 11; j++) {
				// discard the first card in the hand
				Card discarded = hand.remove(0);
				discardedCards.add(discarded);

				double value = BlackBox.regFunction(this);
				if (value >= localmax) {
					localmax = value;
					bestDiscard = discarded;
				}
				// undo the discard
				hand.add(discardedCards.pop());
			}
			// System.out.println("local max: " + localmax);
			// System.out.println(newCard + " best discard: " + bestDiscard);
			discardCases[newCard.getId()] = bestDiscard.getId();
			average += localmax;
			// undo the draw
			hand.remove(newCard);
			unknownCards.add(newCard);
			possibleCards.add(newCard);
		}
		// System.out.println("in willDra...: " + Arrays.toString(discardCases));
		average /= possibleCards.size();
		// System.out.println("average facedown value: " + average);
		if (average >= max) {
			toDiscard = null;
			// System.out.println("Doesn't draw face up card");
			return false;
		}
		// System.out.println("Draws face up card");
		return true;

	}


	@Override
	// public void reportDraw(int playerNum, Card drawnCard) {
	// 	// Ignore other player draws.  Add to cards if playerNum is this player.
	// 	if (playerNum == this.playerNum) {
	// 		hand.add(drawnCard);
	// 		this.drawnCard = drawnCard;
	// 		if (toDiscard == null) {
	// 			// System.out.println("in reportDraw: " + Arrays.toString(discardCases));
	// 			toDiscard = Card.getCard(discardCases[drawnCard.getId()]);
	// 			// System.out.println("to discard (from player): " + toDiscard);
	// 			unknownCards.remove(drawnCard);
	// 		}
	// 		else {
	// 			discardedCards.pop();
	// 		}
	// 	}
	// 	else {
	// 		if (drawnCard != null) { // picked up face up card (known opponent hand should consist of our discards)
	// 			opponentAllHand.add(drawnCard);
	// 			opponentHand.add(drawnCard);
	// 			if (!discardedCards.isEmpty()) {
	// 				discardedCards.pop();
	// 			}
	//
	// 			//REMOVE ONCE WE ARE DONE TESTING
	// 			unknownCards.remove(drawnCard);
	// 		}
	// 		else {
	// 			opponentRejectedCards.add(discardedCards.peek());
	// 		}
	// 	}
	// }
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			hand.add(drawnCard);
			this.drawnCard = drawnCard;
			if (toDiscard == null) { //picked up facedown card
				// System.out.println("in reportDraw: " + Arrays.toString(discardCases));
				toDiscard = Card.getCard(discardCases[drawnCard.getId()]);
				// System.out.println("to discard (from player): " + toDiscard);
				if (unknownCards.contains(drawnCard)) {
					unknownCards.remove(drawnCard);
				}
				if (possibleCards.contains(drawnCard)) {
					possibleCards.remove(drawnCard);
				}
			}
			else { // we picked up faceup card
				visibleCards.add(drawnCard);
				discardedCards.pop();
			}
		}
		else {
			if (drawnCard != null) { // picked up face up card (known opponent hand should consist of our discards)
				opponentAllHand.add(drawnCard);
				opponentHand.add(drawnCard);
				if (!discardedCards.isEmpty()) {
					discardedCards.pop();
				}

				//possible cards
				possibleCards.add(drawnCard);
			}
			else {
				opponentRejectedCards.add(discardedCards.peek());
			}
		}
	}

	@Override
	public Card getDiscard() {
		// TODO : Prevent future repeat of draw, discard pair?
		Card temp = toDiscard;
		toDiscard = null;
		return temp;
	}


	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			hand.remove(discardedCard);
			if (visibleCards.contains(discardedCard)) {
				visibleCards.remove(discardedCard);
			}
		}
		else { //opponent discard
			opponentDiscards.add(discardedCard);
			opponentHand.remove(discardedCard);
			if (unknownCards.contains(discardedCard)){
				unknownCards.remove(discardedCard);
			}
			if (possibleCards.contains(discardedCard)) {
				possibleCards.remove(discardedCard);
			}

		}
		discardedCards.push(discardedCard);
		turn++;
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out.
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand) > GinRummyUtil.MAX_DEADWOOD))
			return null;
		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
	}

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		// Melds ignored by simple player, but could affect which melds to make for complex player.
		if (playerNum != this.playerNum)
			opponentKnocked = true;
	}

	@Override
	public void reportScores(int[] scores) {
		this.scores = scores.clone();
		// Ignored by simple player, but could affect strategy of more complex player.
	}

	@Override
	public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
		// Ignored by simple player, but could affect strategy of more complex player.
	}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		// Ignored by simple player, but could affect strategy of more complex player.
	}



}
