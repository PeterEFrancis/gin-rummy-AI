import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;



public class Player implements GinRummyPlayer {
	@SuppressWarnings("unused")

	public final int TYPE = BlackBox.LINEAR;
	public final int VERSION = BlackBox.ALPHA;

	public int playerNum;
	public int startingPlayerNum;

	public int[] scores;

	public boolean opponentKnocked = false;

	public Random random = new Random();

	public Card drawnCard;
	public Card toDiscard;

	public ArrayList<Card> hand = new ArrayList<Card>();
	public ArrayList<Card> unknownCards = new ArrayList<Card>();
	public ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	public ArrayList<Card> opponentDiscards = new ArrayList<Card>();
	public ArrayList<Card> opponentRejectedCards = new ArrayList<Card>();
	public ArrayList<Card> opponentAllHand = new ArrayList<Card>();   // opponent picked up cards throughout the startGame
	public ArrayList<Card> opponentHand = new ArrayList<Card>();      // opponent picked up cards at the moment

	public Stack<Card> discardedCards = new Stack<Card>();

	int[] discardCases = new int[52];

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] hand) {
		scores = new int[2];
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.hand.clear();
		Stack<Card> cards = Card.getShuffle(256);
		unknownCards = new ArrayList<Card>();
		unknownCards.addAll(cards);

		for (Card card : hand) {
			this.hand.add(card);
			unknownCards.remove(card);
		}
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
	}




	@Override
	public boolean willDrawFaceUpCard(Card card) {
      card = Utilities.transformCard(card);
		// @SuppressWarnings("unchecked")
		if (discardedCards.isEmpty()) {
			discardedCards.push(card);
		}
		hand.add(discardedCards.pop()); // draw face up

		double max = 0;
		for (int i = 0; i < 10; i++) {
			//discard the first card in the hand
			Card discarded = hand.remove(0);
			discardedCards.add(discarded);

			double value = BlackBox.regFunction(this, VERSION, TYPE);
			if (value > max) {
				max = value;
				toDiscard = discarded;
			}
			//undo the discard
			hand.add(discardedCards.pop());
		}
		discardedCards.push(hand.remove(0)); //undoes the draw

		double average = 0;
		for (int i = 0; i < unknownCards.size(); i++) {
			//draw card
			Card newCard = unknownCards.remove(i);
			hand.add(newCard);

			double localmax = -1;
			Card bestDiscard = null;
			for (int j = 0; j < 11; j++) {
				//discard the first card in the hand
				Card discarded = hand.remove(0);
				discardedCards.add(discarded);

				double value = BlackBox.regFunction(this, VERSION, TYPE);
				if (value >= localmax) {
					localmax = value;
					bestDiscard = discarded;
				}
				//undo the discard
				hand.add(discardedCards.pop());
			}
			discardCases[newCard.getId()] = bestDiscard.getId();
			average += localmax;

			//undo the draw
			hand.remove(newCard);
			unknownCards.add(newCard);
		}
		average /= unknownCards.size();

		if (average >= max) {
			toDiscard = null;
			return false;
		}
		return true;

	}


	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			hand.add(drawnCard);
			this.drawnCard = drawnCard;
			if (toDiscard == null)
				toDiscard = Card.getCard(discardCases[drawnCard.getId()]);
		}
		else {
			if (drawnCard != null) { // picked up face up card (known opponent hand should consist of our discards)
				opponentAllHand.add(drawnCard);
				opponentHand.add(drawnCard);
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
		}
		else {
			opponentDiscards.add(discardedCard);
			opponentHand.remove(discardedCard);
			if (unknownCards.contains(discardedCard)){
				unknownCards.remove(discardedCard);
			}
		}
		discardedCards.add(discardedCard);

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

	public double[] calculateFeatures() {

		double current_player_score = scores[0];
		double opponent_score = scores[1];
		double current_player_deadwood = Utilities.deadwoodCount(hand);
		double current_player_num_hit_cards = Utilities.numHitCards(unknownCards, hand);


		return new double[] {current_player_score,
				opponent_score,
				current_player_deadwood,
				current_player_num_hit_cards,
				1};
	}



























}
