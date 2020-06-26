import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ginrummy.*;


public class HoangPlayer implements GinRummyPlayer {
	private int playerNum;
	@SuppressWarnings("unused")
	private int startingPlayerNum;
	private ArrayList<Card> cards = new ArrayList<Card>();
	private Random random = new Random();
	private boolean opponentKnocked = false;
	Card faceUpCard, drawnCard;
	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
	HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();
	private int turn;
	private boolean printEst = false;

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
		this.cards.clear();
		for (Card card : cards)
			this.cards.add(card);
		opponentKnocked = false;
		drawDiscardBitstrings.clear();
		estimator.init();
		ArrayList<Card> hand = new ArrayList<Card>();
		for (Card c : cards)
			hand.add(c);
		estimator.setKnown(hand, false);
		if(printEst)
			estimator.print();
		totalDiscarded = 0;
		turn = 0;
	}

	@Override
	public boolean willDrawFaceUpCard(Card card) {
		// Return true if card would be a part of a meld, false otherwise.
		estimator.setKnown(card, false);
		this.faceUpCard = card;

		int deadwoods = Integer.MAX_VALUE;

		//check deadwoods BEFORE
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		ArrayList<ArrayList<Card>> bestMelds = null;
		ArrayList<Card> unmeldedCards = null;

		if(bestMeldSets.isEmpty()) {
			deadwoods = GinRummyUtil.getDeadwoodPoints(cards);
		}
		else {
			bestMelds = bestMeldSets.get(0);
			deadwoods = GinRummyUtil.getDeadwoodPoints(bestMelds, cards);
		}

		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
		newCards.add(card);

		bestMeldSets = GinRummyUtil.cardsToBestMeldSets(newCards);

		if(!bestMeldSets.isEmpty()) {
			bestMelds = bestMeldSets.get(0);

			for(ArrayList<Card> meld : bestMelds) {
				if (meld.contains(card)) {
					return true;
				}
			}
		}

		bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if(!bestMeldSets.isEmpty()) {
			bestMelds = bestMeldSets.get(0);
			unmeldedCards = OurUtilities.cardsNotInMeld(bestMelds, cards);
		}
		else {
			unmeldedCards = cards;
		}
		boolean higher4 = false;
		for (Card c : unmeldedCards) {
			if (c.getRank() >= 4) {
				higher4 = true;
				break;
			}
		}

		if(card.getRank() < 4 && !higher4) {
			return true;
		}

		return false;
	}

	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		this.drawnCard = drawnCard;
		if (playerNum == this.playerNum) {
			cards.add(drawnCard);
			estimator.setKnown(drawnCard, false);
		}
		else {
			if (drawnCard != null) {
				faceUpCard = drawnCard;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Card getDiscard() {

		//First 8 turns
		if (turn < 8) {
			ArrayList<Card> copyHand = (ArrayList<Card>) cards.clone();

			ArrayList<ArrayList<ArrayList<Card>>>bestMeldSets =  GinRummyUtil.cardsToBestMeldSets(copyHand);
			ArrayList<ArrayList<Card>> combos = null;
			ArrayList<Card> unmeldedCards = new ArrayList<Card>(); //unmelded cards in hand
			ArrayList<ArrayList<Card>> bestMelds = null;

			if (bestMeldSets.isEmpty()) {
				combos = OurUtilities.getCombos(new ArrayList<ArrayList<Card>>(), copyHand);
				unmeldedCards = OurUtilities.cardsNotInMeld(new ArrayList<ArrayList<Card>>(), cards); //unmelded cards in hand

			}
			else {
				bestMelds = GinRummyUtil.cardsToBestMeldSets(copyHand).get(0);
				unmeldedCards = OurUtilities.cardsNotInMeld(bestMelds, cards); //unmelded cards in hand
				combos = OurUtilities.getCombos( bestMelds, copyHand);
			}

			HashMap<Card, Integer> cardInCombs = new HashMap<>();
			for (ArrayList<Card> combo : combos) {
				for (Card c : combo) {
					if(cardInCombs.containsKey(c)) {
						cardInCombs.put(c, cardInCombs.get(c)+1);
					}
					else {
						cardInCombs.put(c, 1);
					}
				}
			}

			Card lowestInCombsCard = null;
			int lowestInCombs = Integer.MAX_VALUE;
			double lowestDang = Double.MAX_VALUE;
			int rankOfLowest = -1;

			for (Card card : unmeldedCards) {

				if (card == drawnCard && drawnCard == faceUpCard)
					continue;
				// Disallow repeat of draw and discard.
				ArrayList<Card> drawDiscard = new ArrayList<Card>();
				drawDiscard.add(drawnCard);
				drawDiscard.add(card);
				if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
					continue;

				double currDang = OurUtilities.discardDanger(card, estimator);
				int currInCombCnt = 0;
				int currRank = card.getRank();

				if (cardInCombs.containsKey(card)) {
					currInCombCnt = cardInCombs.get(card);
				}

				//System.out.println(currInCombCnt);
				if (currInCombCnt <= lowestInCombs) {
					if (unmeldedCards.size() >= 3 && currRank > 2) {
						if (currInCombCnt < lowestInCombs) {
							lowestInCombsCard = card;
							lowestInCombs = currInCombCnt;
							lowestDang = currDang;
							rankOfLowest = currRank;
						}
						else{
							if (currRank > rankOfLowest) {
								lowestInCombsCard = card;
								lowestInCombs = currInCombCnt;
								lowestDang = currDang;
								rankOfLowest = currRank;
							}
						}
					}
				}
			}

			if (lowestInCombsCard == null) {
				if (unmeldedCards.isEmpty()) {
					for (ArrayList<Card> meld : bestMelds) {
						if (meld.size() > 3) {
							lowestInCombsCard = meld.get(0);
							if (lowestInCombsCard == drawnCard && drawnCard == faceUpCard) {
								lowestInCombsCard = meld.get(meld.size()-1);
							}
							break;
						}
					}
				}
				else {
					lowestInCombsCard = unmeldedCards.get(0);
					for (Card card : unmeldedCards) {
						if (card.getRank() > lowestInCombsCard.getRank()) {
							lowestInCombsCard = card;
						}
					}
				}
			}

			return lowestInCombsCard;


		}
		else { //if(turn <= 2 || turn >= 8) {
			//After 8 turns:
			// Discard a random card (not just drawn face up) leaving minimal deadwood points.
			double lowestDanger = Double.MAX_VALUE;
			Card safestCard = null;
			int lowestPtn = Integer.MAX_VALUE;

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
				//remainingCards.remove(card);
				ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);

				ArrayList<ArrayList<Card>> bestMelds = null;
				if (!bestMeldSets.isEmpty()) {
					bestMelds = bestMeldSets.get(0);
				}

				double currentDanger = OurUtilities.discardDanger(card, estimator);
				//System.out.println(card + " : " + currentDanger);
				boolean inMeld = false;
				if (currentDanger <= lowestDanger) {
					if (bestMelds != null) {
						//System.out.println("BestMElds" + bestMelds);
						for (ArrayList<Card> meld : bestMelds) {
							if (meld.contains(card)) {
								inMeld = true;
								break;
							}
						}
					}
					int currentPtn = GinRummyUtil.getDeadwoodPoints(card);
					if(inMeld || card.rank == 0) {
						continue;
					}
					else {
						if(currentDanger < lowestDanger) {
							lowestDanger = currentDanger;
							safestCard = card;
							lowestPtn = currentPtn;
						}
						else {
							if (currentPtn >= lowestPtn) {
								lowestDanger = currentDanger;
								safestCard = card;
								lowestPtn = currentPtn;
							}
						}
					}
				}
			}

			Card discard  = safestCard;
			if (discard == null) {
				ArrayList<Card> copyHand = (ArrayList<Card>) cards.clone();
				ArrayList<ArrayList<Card>> bestMelds = GinRummyUtil.cardsToBestMeldSets(copyHand).get(0);
				ArrayList<Card> unmeldedCards = OurUtilities.cardsNotInMeld(bestMelds, cards);

				if(!unmeldedCards.isEmpty()) {
					discard = unmeldedCards.get(0);
					for (Card card : unmeldedCards) {
						if (card.getRank() > discard.getRank()) {
							discard = card;
						}
					}
					return discard;
				}

				for (ArrayList<Card> meld : bestMelds) {
					if (meld.size() > 3) {
						discard = meld.get(0);
						if (discard == drawnCard && drawnCard == faceUpCard) {
							discard = meld.get(meld.size()-1);
						}
						break;
					}
				}
			}
			// Prevent future repeat of draw, discard pair.
			ArrayList<Card> drawDiscard = new ArrayList<Card>();
			drawDiscard.add(drawnCard);
			drawDiscard.add(discard);
			drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
			return discard;
		}
	}

	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		totalDiscarded++;
		if (playerNum == this.playerNum) {
			cards.remove(discardedCard);
		}
		else {
			estimator.reportDrawDiscard(faceUpCard, faceUpCard == drawnCard, discardedCard);
		}
		faceUpCard = discardedCard;
		if(printEst)
			estimator.print();
		turn++;
	}

	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out.
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD || turn > 14))
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

	}

	@Override
	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
		if (playerNum != this.playerNum) { // opponent hand
			// Record est. likelihood of actual opponent hand
			int numCards = 0;
			double estProb = 1;
			for (Card card : hand) {
				numCards++;
				if (!estimator.known[card.getId()])
					estProb *= estimator.prob[card.getId()];
			}
			// Record uniform likelihood of actual opponent hand
			double uniformProb = 1;
			// Compute the number of possible cards that may be those unknown in the opponent's hand
			//System.out.println("Number of opponent cards known: " + (hand.size() - estimator.numUnknownInHand));
			//System.out.println("Number discarded: " + totalDiscarded);
			double numCandidates = Card.NUM_CARDS - totalDiscarded - hand.size() - (hand.size() - estimator.numUnknownInHand);
			//System.out.println("Number of candidates: " + numCandidates);
			double singleCardProb = (double) estimator.numUnknownInHand / numCandidates;
			for (int i = 0; i < estimator.numUnknownInHand; i++)
				uniformProb *= singleCardProb;

			//System.out.println(">>>> est. " + estProb + " unif. " + uniformProb + " ratio " + (estProb / uniformProb));
			ratios.add((estProb / uniformProb));
			//System.out.println(ratios);
			double sum = 0;
			for (double ratio : ratios)
				sum += ratio;
			//System.out.println("Average ratio: " + sum / ratios.size());
		}
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException, UnsupportedEncodingException {
		String[] playerNames = {"HoangPlayer", "SimpleGinRummyPlayer2"};
		GinRummyTournament.setPlayVerbose(true);
		int numGames = 100;
		new GinRummyTournament().match(playerNames[0], playerNames[1], numGames);
	}

	//count how probable that combination can happen, e.g. if we are waiting for one card, it's low probability, one already gone
}
