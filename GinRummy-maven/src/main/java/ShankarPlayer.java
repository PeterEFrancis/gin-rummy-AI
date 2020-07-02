
/*//////////////////////////////////////////////////////////////////////////////

Shankar's 10 rules:

	1. Make all plays with the single goal of getting below the knock cache count, and as soon as possible.
	2. Knock at first opportunity, if you are playing for a knock.
	3. Always pick up a card that makes a meld in your hand.
	4. Never pick up a card that does not make a meld, or adds to an existing meld in your hand.
	5. Do not throw cards that the opponent can use.
	6. Do no throw cards that the opponent may use.
	7. Holding four or more 10-point cards at the deal, start discarding these no matter how promising the high card combination.
	8. Pick up A,2,3,4 to build knock cache, if you have safer card to discard (knock cache is like a meld).
	9. Play for a tie by going to the bottom of the stock instead for a doubtful win.
	10. Prefer to be undercut by knocking than Ginning the opponent by making a dubious discard.


*///////////////////////////////////////////////////////////////////////////////



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ginrummy.Card;
import ginrummy.GinRummyPlayer;

public class ShankarPlayer implements GinRummyPlayer {


	boolean debug = false;


	public int playerNum;
	public int startingPlayerNum;
	public int[] scores;
	public boolean opponentKnocked;
	public Random random;
	public ArrayList<Card> hand;
	public ArrayList<Card> opponentHand;
	public boolean heldFourOrMoreTensAtStart;
	public Card faceUpCard;
	public boolean drewFaceUp;
	public ArrayList<Card> discardedCards;

	public ShankarArray shankarr;

	double adjRateConst;
	double sudoAdjRateConst;
	double meldRateConst;
	double discardedAdjCardsConst;
	double discardedSudoAdjConst;

	public ShankarPlayer() {
		reset();
		this.scores = new int[2];
		shankarr = new ShankarArray();
	}

	public ShankarPlayer(long seed) {
		reset();
		this.random.setSeed(seed);
		this.scores = new int[2];
		shankarr = new ShankarArray();
	}

	public void reset() {
		drewFaceUp = false; // just to be safe
		faceUpCard = null;
		playerNum = -1;
		startingPlayerNum = -1;
		hand = new ArrayList<Card>();
		opponentHand = new ArrayList<Card>();
		random = new Random();
		heldFourOrMoreTensAtStart = false;
		opponentKnocked = false;
		discardedCards = new ArrayList<Card>();
		shankarr = new ShankarArray();
	}


	public void start_collect() {
		adjRateConst = 0; // random.nextDouble();
		sudoAdjRateConst = 1; // random.nextDouble();
		meldRateConst = 1; // random.nextDouble();
		discardedAdjCardsConst = 1; // random.nextDouble();
		discardedSudoAdjConst = 1; // random.nextDouble();

		File f = new File("data.csv");
		try {
			FileWriter pw = new FileWriter(f, true);
			pw.write(adjRateConst + "," + sudoAdjRateConst + "," + meldRateConst + "," + discardedAdjCardsConst + "," + discardedSudoAdjConst + ",");
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] hand) {
		reset();
		int tenCount = 0;
		for (Card card : hand) {
			this.hand.add(card);
			if (ginrummy.GinRummyUtil.getDeadwoodPoints(card) >= 10) {
				tenCount++;
			}
		}

		shankarr.playerHand(this.hand);

		ArrayList<ArrayList<ArrayList<Card>>> bestMeldsSets = ginrummy.GinRummyUtil.cardsToBestMeldSets(this.hand);
		if (!bestMeldsSets.isEmpty()) {
			ArrayList<ArrayList<Card>> bestMeld = bestMeldsSets.get(0);
			for (ArrayList<Card> meld : bestMeld) {
				for (Card card : meld) {
					if (card.getRank() > 8) {
						tenCount--;
					}
				}
			}
		}
		this.heldFourOrMoreTensAtStart = (tenCount >= 4);

		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;


	}





	@Override
	public boolean willDrawFaceUpCard(Card faceUpCard) {
		/**
		1. Make all plays with the single goal of getting below the knock cache count, and as soon as possible.
		3. Always pick up a card that makes a meld in your hand.
		4. Never pick up a card that does not (1) make a meld or (2) adds to an existing meld in your hand.
		8. Pick up A,2,3,4 to build knock cache, if you have safer card to discard (knock cache is like a meld).
		**/

		hand.add(faceUpCard);
		ArrayList<ArrayList<ArrayList<Card>>> bestHandOrganization = OurUtilities.getBestHandOrganization(hand);
		ArrayList<ArrayList<Card>> bestMeldSet = bestHandOrganization.get(0);
		hand.remove(faceUpCard);

		for (ArrayList<Card> meld : bestMeldSet) {
			if (meld.contains(faceUpCard)) {
				drewFaceUp = true;
				return true;
			}
		}

		ArrayList<Card> knockCache = bestHandOrganization.get(2).get(0);
		if (knockCache.contains(faceUpCard)) {
			drewFaceUp = true;
			return true;
		}
		drewFaceUp = false;
		return false;
	}





	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		if (drawnCard != null) {
			faceUpCard = drawnCard;
		}
		if (this.playerNum == playerNum) {
			hand.add(drawnCard);
			if (drewFaceUp) {
				faceUpCard = drawnCard;
				discardedCards.remove(drawnCard);
			}
			shankarr.playerDraw(drawnCard, drewFaceUp);
		}
		else {
			if (drawnCard != null) {
				opponentHand.add(drawnCard);
				discardedCards.remove(drawnCard);
				shankarr.opponentDrawFaceUpCard(drawnCard, true);
			} else {
				shankarr.opponentDrawFaceUpCard(faceUpCard, false);
			}
		}
	}




	public double getOpponentUsefulness(Card card) {

		if (opponentHand.size() == 0) {
			return 0;
		}

		int numAdj = 0;
		for (Card c : opponentHand) {
			if (c.getRank() ==  card.getRank())
				numAdj++;
			if (c.getSuit() == card.getSuit() && Math.abs(c.getRank() - card.getRank()) < 2 )
				numAdj++;
		}

		int numSudoAdj = 0;
		for (Card c : opponentHand) {
			if (c.getSuit() == card.getSuit() && Math.abs(c.getRank() - card.getRank()) == 2 )
				numSudoAdj++;
		}

		int numMeldAdditions = 0;
		opponentHand.add(card);
		ArrayList<ArrayList<Card>> allOpponentMelds = ginrummy.GinRummyUtil.cardsToAllMelds(opponentHand);
		for (ArrayList<Card> opponentMeld : allOpponentMelds) {
			if (opponentMeld.contains(card)) {
				numMeldAdditions++;
			}
		}
		opponentHand.remove(card);

		int numAdjCardsInDiscarded = 0;
		for (Card c : discardedCards) {
			if (c.getRank() ==  card.getRank())
				numAdjCardsInDiscarded++;
			if (c.getSuit() == card.getSuit() && Math.abs(c.getRank() - card.getRank()) < 2 )
				numAdjCardsInDiscarded++;
		}

		int numSudoAdjInDiscarded = 0;
		for (Card c : opponentHand) {
			if (c.getSuit() == card.getSuit() && Math.abs(c.getRank() - card.getRank()) == 2 )
				numSudoAdjInDiscarded++;
		}


		double usefulness = 0;
		usefulness += numAdj * adjRateConst;
		usefulness += numSudoAdj * sudoAdjRateConst;
		usefulness += numMeldAdditions * meldRateConst;
		usefulness -= numAdjCardsInDiscarded * discardedAdjCardsConst;
		usefulness -= numSudoAdjInDiscarded * discardedSudoAdjConst;
		return usefulness;
	}



	@Override
	public Card getDiscard() {
		/*
		5. Do not throw cards that the opponent can use.
		6. Do not throw cards that the opponent may use.
		7. Holding four or more 10-point cards at the deal, start discarding these no matter how promising the high card combination.
		*/

		double usefulnessCutOff = 0.54;

		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = ginrummy.GinRummyUtil.cardsToBestMeldSets(hand);
		ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
		if (!bestMeldSets.isEmpty()){
			bestMelds = bestMeldSets.get(0);
		}
		ArrayList<Card> possibleDiscards = OurUtilities.cardsNotInMeld(bestMelds, hand);
		if (drewFaceUp) {
			possibleDiscards.remove(faceUpCard);
		}

		// if our hand is a gin
		if (possibleDiscards.isEmpty()) {
			for (ArrayList<Card> meld : bestMelds) {
				if (meld.size() > 3) {
					// if it's a set
					if (meld.get(0).getRank() == meld.get(1).getRank()) {
						return meld.get(0);
					}
					// if it's a run
					else {
						Card highestRankCard = null;
						int highestRank = -1;
						for (Card c : meld) {
							if (c.getRank() > highestRank) {
								highestRank = c.getRank();
								highestRankCard = c;
							}
						}
						return highestRankCard;
					}
				}
			}
		}

		ArrayList<Card> minOUDiscard = new ArrayList<Card>();
		ArrayList<Card> toRemove = new ArrayList<Card>();

		do {
			possibleDiscards.addAll(toRemove);
			usefulnessCutOff +=0.05;
			if (debug) {
				System.out.println("Usefulness cut off: " + usefulnessCutOff);
			}
			double min = Double.MAX_VALUE;
			HashMap<Card, Double> mapToUsefulness = new HashMap<Card, Double>();
			toRemove = new ArrayList<Card>();
			for (Card c : possibleDiscards) {
				double opponentUsefulness = getOpponentUsefulness(c);
				mapToUsefulness.put(c, opponentUsefulness);
				if (opponentUsefulness < min) {
					min = opponentUsefulness;
					minOUDiscard.clear();
					minOUDiscard.add(c);
				} else if (opponentUsefulness == min) {
					minOUDiscard.add(c);
				}
				if (opponentUsefulness >= usefulnessCutOff) {
					toRemove.add(c);
				}
			}
			possibleDiscards.removeAll(toRemove);
			if (debug) {
				System.out.println("map to usefuleness: " + mapToUsefulness);
			}
			Card bestTenDiscard = null;
			double minOU = Double.MAX_VALUE;
			for (Card c : possibleDiscards) {
				if (c.getRank() > 8) {
					if (mapToUsefulness.get(c) < minOU) {
						bestTenDiscard = c;
					}
				}
			}
			if (bestTenDiscard != null) {
				return bestTenDiscard;
			}

		} while (possibleDiscards.size() == 0);

		Card minDeadwoodDiscard = null;
		int maxDeadwood = -Integer.MAX_VALUE;
		for (Card c : minOUDiscard) {
			int dw = ginrummy.GinRummyUtil.getDeadwoodPoints(c);
			if (dw > maxDeadwood) {
				maxDeadwood = dw;
				minDeadwoodDiscard = c;
			}
		}

		return minDeadwoodDiscard;
		}





	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {

		faceUpCard = discardedCard;

		// System.out.println("before discard player " + this.playerNum + " hand: " + hand);
		if (playerNum == this.playerNum) {
			hand.remove(discardedCard);
		} else {
			opponentHand.remove(discardedCard);
		}
		discardedCards.add(discardedCard);
		// System.out.println(" after discard player " + this.playerNum + " hand: " + hand);
	}



	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		/*
		2. Knock at first opportunity, if you are playing for a knock.
		9. Play for a tie by going to the bottom of the stock instead for a doubtful win.
		10. Prefer to be undercut by knocking than Ginning the opponent by making a dubious discard.
		*/

		// Check if deadwood of maximal meld is low enough to go out.
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = ginrummy.GinRummyUtil.cardsToBestMeldSets(hand);

		if (!opponentKnocked && (bestMeldSets.isEmpty() || ginrummy.GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand) > ginrummy.GinRummyUtil.MAX_DEADWOOD))
			return null;

		// System.out.println("ginrummy.GinRummyUtil.MAX_DEADWOOD: " + ginrummy.GinRummyUtil.MAX_DEADWOOD);
		// System.out.println("Hand: " + hand);
		// System.out.println("Best Meld: " + bestMeldSets.get(0));
		// System.out.println("ginrummy.GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand): " + ginrummy.GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand));
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
		this.scores = scores;
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
