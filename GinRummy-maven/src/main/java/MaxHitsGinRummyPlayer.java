import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import ginrummy.Card;
import ginrummy.GinRummyPlayer;
import ginrummy.GinRummyUtil;


/**
 * Player that makes decisions based on maximizing hit cards and minimizing deadwoods
 */
public class MaxHitsGinRummyPlayer implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private ArrayList<Card> deck = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	private int hitCounts;
	private int deadwoodPts;
	Card faceUpCard, drawnCard;
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();

		//initializing the deck
		for (int i = 0; i < 52; i++) {
			 deck.add(new Card(i % 13, i / 13));
		}
		//deck without our hand
		long deckbits = GinRummyUtil.cardsToBitstring(deck);
		deck = GinRummyUtil.bitstringToCards(deckbits);
		deck.removeAll(Arrays.asList(cards));

		// //counting the hit cards
		// this.hitCounts = OurUtilities.numHitCards(deck, cards);
		//
		// //counting the deadwood points
		// this.deadwoodPts = OurUtilities.deadwoodCount(cards);
	}

	@Override
	public boolean willDrawFaceUpCard(Card card) {
		// Return true if card will
		// 1) create a meld
		// 2) increases number of hit cards
		// 3) decreases deadwood
		this.faceUpCard = card;
		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
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
			drawnCard = OurUtilities.transformCard(drawnCard);
			cards.add(drawnCard);
			this.drawnCard = drawnCard;
			deck.remove(drawnCard);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {
		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
		int minDeadwood = Integer.MAX_VALUE;
		int maxHitCards = Integer.MIN_VALUE;
		ArrayList<Card> candidateCards = new ArrayList<Card>();
		for (Card card : cards) {
			// Cannot draw and discard face up card.
			if (card == drawnCard && drawnCard == faceUpCard)
				continue;
			// Disallow repeat of draw and discard.
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(card);
			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
				continue;

			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
			remainingCards.remove(card);
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
			int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
			int hitCount = OurUtilities.numHitCards(deck, remainingCards);
			// boolean meldable = false;
			// for (ArrayList<ArrayList<Card>> melds : bestMeldSets) {
			// 	if (GinRummyUtil.canBeMeldedIn(card, melds)) {
			// 		meldable = true;
			// 		break;
			// 	}
			// }
			if (!OurUtilities.isHitCard(card,deck)) {
				if (hitCount >= maxHitCards && deadwood <= minDeadwood) {
					if (hitCount > maxHitCards) {
						maxHitCards = hitCount;
						minDeadwood = deadwood;
						candidateCards.clear();
					}
					if (deadwood < minDeadwood) {
						minDeadwood = deadwood;
						candidateCards.clear();
					}
					candidateCards.add(card);
				}
			}
		}

		//if no candidates card, pick a random card to
		Card discard = cards.get(random.nextInt(cards.size()));

		if(!candidateCards.isEmpty()){ //if we have some candidate cards
			discard = candidateCards.get(random.nextInt(candidateCards.size()));
		}



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
		if (playerNum == this.playerNum)
			cards.remove(discardedCard);
		else
			deck.remove(drawnCard);
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out.
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
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
