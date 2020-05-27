import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class SimplePlayer implements GinRummyPlayer{

   public Random random = new Random(0);
	public int type;
	public int version;

	public int playerNum;
	public int startingPlayerNum;
	public int turn;

	public int[] scores = new int[2];

	public boolean opponentKnocked = false;

	public Card drawnCard;
	public Card toDiscard;

    public Card faceUpCard;

	public ArrayList<Card> hand;
	public ArrayList<Card> unknownCards;
	public ArrayList<Long> drawDiscardBitstrings;
	public ArrayList<Card> opponentDiscards;
	public ArrayList<Card> opponentRejectedCards;
	public ArrayList<Card> opponentAllHand;   // opponent picked up cards throughout the startGame
	public ArrayList<Card> opponentHand;      // opponent picked up cards at the moment

	public Stack<Card> discardedCards;

	int[] discardCases = new int[52];




	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] hand) {

		// Initialize all arrayLists
		this.hand = new ArrayList<Card>();
		unknownCards = new ArrayList<Card>();
		drawDiscardBitstrings = new ArrayList<Long>();
		opponentDiscards = new ArrayList<Card>();
		opponentRejectedCards = new ArrayList<Card>();
		opponentAllHand = new ArrayList<Card>();
		opponentHand = new ArrayList<Card>();
		discardedCards = new Stack<Card>();

		drawnCard = null;
		toDiscard = null;

		turn = 0;
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.hand.clear();
		unknownCards.addAll(Arrays.asList(Card.allCards));

		for (Card card : hand) {
			this.hand.add(card);
			unknownCards.remove(card);
		}
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
	}



   @Override
	public boolean willDrawFaceUpCard(Card card) {

      if (discardedCards.isEmpty()) {
			discardedCards.push(card);
			unknownCards.remove(card);
		}

		// Return true if card would be a part of a meld, false otherwise.
		this.faceUpCard = card;
		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) hand.clone();
		newCards.add(card);
		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
			if (meld.contains(card))
				return true;
		return false;
	}

	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		// Ignore other player draws.  Add to cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			hand.add(drawnCard);
			this.drawnCard = drawnCard;
			// if (toDiscard == null) {
			// 	// System.out.println("in reportDraw: " + Arrays.toString(discardCases));
			// 	toDiscard = Card.getCard(discardCases[drawnCard.getId()]);
         //    discardedCards.pop();
         //
			// 	// System.out.println("to discard (from player): " + toDiscard);
			// }
			// else {
         if (unknownCards.contains(drawnCard)) {
            unknownCards.remove(drawnCard);
         }
			// }
		}
		else {
			if (drawnCard != null) { // picked up face up card (known opponent hand should consist of our discards)
				opponentAllHand.add(drawnCard);
				opponentHand.add(drawnCard);
				if (!discardedCards.isEmpty()) {
					discardedCards.pop();
				}

				//REMOVE ONCE WE ARE DONE TESTING
				unknownCards.add(drawnCard);
			}
			else {
				opponentRejectedCards.add(discardedCards.peek());
			}
		}
	}


   @SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
		int minDeadwood = Integer.MAX_VALUE;
		ArrayList<Card> candidateCards = new ArrayList<Card>();
		for (Card card : hand) {
			// Cannot draw and discard face up card.
			if (card == drawnCard && drawnCard == faceUpCard)
				continue;
			// Disallow repeat of draw and discard.
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(card);
			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
				continue;

			ArrayList<Card> remainingCards = (ArrayList<Card>) hand.clone();
			remainingCards.remove(card);
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
			int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
			if (deadwood <= minDeadwood) {
				if (deadwood < minDeadwood) {
					minDeadwood = deadwood;
					candidateCards.clear();
				}
				candidateCards.add(card);
			}
		}
		Card discard = candidateCards.get(random.nextInt(candidateCards.size()));
		// Prevent future repeat of draw, discard pair.
		ArrayList<Card> drawDiscard = new ArrayList<Card>();
		drawDiscard.add(drawnCard);
		drawDiscard.add(discard);
		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
		return discard;
	}


	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		// Ignore other player discards.  Remove from cards if playerNum is this player.
		if (playerNum == this.playerNum) {
			hand.remove(discardedCard);
			turn++;
		}
		else {
			opponentDiscards.add(discardedCard);
			opponentHand.remove(discardedCard);
			if (unknownCards.contains(discardedCard)){
				unknownCards.remove(discardedCard);
			}
		}
		discardedCards.push(discardedCard);

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


	public void resetDiscarded() {
		discardedCards.clear();
		discardedCards = new Stack<Card>();
	}





}
