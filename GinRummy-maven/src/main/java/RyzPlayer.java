import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import ginrummy.*;

public class RyzPlayer implements GinRummyPlayer{

	public int playerNum;
	public int startingPlayerNum;
	public int turn;
	public int[] scores;
	public boolean opponentKnocked;
	public Random random;
	public Card drawnCard;
	public ArrayList<Card> hand;
	public ArrayList<Card> unknownCards;
	public ArrayList<Card> opponentDiscardedCards;
	public ArrayList<Card> opponentPassedCards;
	public ArrayList<Card> opponentAllTimeHand;   // opponent picked up cards throughout the startGame
	public ArrayList<Card> opponentHand;          // opponent picked up cards at the moment
	public Stack<Card> discardedCards;
	public boolean drewFaceUp;

	public int[] opponentCardMatrix;
	public int[] safeDiscardMatrix;


	public RyzPlayer() {
		reset();
		this.scores = new int[2];
	}


	public void reset() {
		opponentDiscardedCards = new ArrayList<Card>();
		opponentPassedCards = new ArrayList<Card>();
		opponentAllTimeHand = new ArrayList<Card>();
		opponentHand = new ArrayList<Card>();
		drawnCard = null;
		playerNum = -1;
		startingPlayerNum = -1;
		turn = 0;
		opponentKnocked = false;
		drewFaceUp = false;
		hand = new ArrayList<Card>();
		unknownCards = new ArrayList<Card>();
		discardedCards = new Stack<Card>();
		random = new Random();
		opponentCardMatrix = new int[52];
		safeDiscardMatrix = new int[52];
	}

	@Override
	public void startGame(int playerNum, int startingPlayerNum, Card[] hand) {
		reset();
		ArrayList<Card> alHand = new ArrayList<>();
		alHand.addAll(Arrays.asList(hand));

		for (Card card : Card.allCards) {
			if (alHand.contains(card)) {
				this.hand.add(card);
			} else {
				unknownCards.add(card);
			}
		}

		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;
	}


	public double[] getBestDiscardAndProb(Card faceUpCard) {
		LinkedList<Card> possibleDiscards = new LinkedList<Card>();
		possibleDiscards.addAll(hand);
		if (faceUpCard != null) {
			possibleDiscards.remove(faceUpCard);
		}
		Card bestDiscard = null;
		double bestProbOfWinning = -Double.MAX_VALUE;

//		 if (turn < 2) {
//			 ArrayList<Card> toRemove = new ArrayList<>();
//			 for (Card card : possibleDiscards) {
//				 if (OurUtilitiesOriginal.getFaceValue(card) == 10) {
//					 toRemove.add(card);
//				 }
//			 }
//			 possibleDiscards.removeAll(toRemove);
//		 }

		for (Card possDiscard : possibleDiscards) {
			hand.remove(possDiscard);
			discardedCards.add(possDiscard);
			double probOfWinning = regFunction(this, possDiscard);
			if (probOfWinning > bestProbOfWinning) {
				bestProbOfWinning = probOfWinning;
				bestDiscard = possDiscard;
			}
			discardedCards.remove(possDiscard);
			hand.add(possDiscard);
		}
		double[] ret = new double[2];
		ret[0] = bestDiscard.getId();
		ret[1] = bestProbOfWinning;
		return ret;
	}



	@Override
	public boolean willDrawFaceUpCard(Card faceUpCard) {

		// first turn -- this... this is how we find out what the initial face up card is.
		if (discardedCards.isEmpty()) {
			discardedCards.push(faceUpCard);
			unknownCards.remove(faceUpCard);
		}

		@SuppressWarnings("unchecked")
		ArrayList<Card> newCards = (ArrayList<Card>) hand.clone();
		newCards.add(faceUpCard);
		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
			if (meld.contains(faceUpCard))
				return true;

//		if (turn < 2 && OurUtilitiesOriginal.getFaceValue(faceUpCard) == 10) {
//			return true;
//		}

		// find probability of winning if draw face up
		Card fuc = discardedCards.pop();
		hand.add(fuc);

		//		System.out.println("Face up card: " + faceUpCard);
		double[] bestDiscardAndProb = getBestDiscardAndProb(faceUpCard);
		double drawFaceUpCardWinProb = bestDiscardAndProb[1];
		hand.remove(faceUpCard);
		discardedCards.push(faceUpCard);

		// find average probability of winning if you draw from face down card
		double drawFaceDownWinProbAvg = 0;
		for (int i = 0; i < unknownCards.size(); i++) {
			Card card = unknownCards.remove(0);
			hand.add(card);
			drawFaceDownWinProbAvg += getBestDiscardAndProb(null)[1];
			hand.remove(card);
			unknownCards.add(card);
		}
		drawFaceDownWinProbAvg /= unknownCards.size();


		boolean retBool = false;
		if (drawFaceDownWinProbAvg < drawFaceUpCardWinProb){
			retBool = true;
		}

		return retBool;
	}


	@Override
	public void reportDraw(int playerNum, Card drawnCard) {

		// our draw
		if (playerNum == this.playerNum) {

			hand.add(drawnCard);
			this.drawnCard = drawnCard;
			// we draw face-up card
			if (drawnCard.getId() == discardedCards.peek().getId()) {
				discardedCards.remove(drawnCard);
				drewFaceUp = true;
			}
			// we draw face down card
			else {
				drewFaceUp = false;
				unknownCards.remove(drawnCard);
			}
		}
		// opponent draw
		else {

			// first turn -- this... this is how we find out what the initial face up card is.
			if (discardedCards.isEmpty()) {
				discardedCards.push(drawnCard);
				unknownCards.remove(drawnCard);
			}

			// opponent draws face up card
			Card faceUpCard = discardedCards.peek();
			if (drawnCard != null){
				opponentHand.add(discardedCards.pop());
				opponentAllTimeHand.add(faceUpCard);
				this.drawnCard = drawnCard;
				drewFaceUp = true;
				opponentCardMatrix[drawnCard.getId()] = 1;
			}
			// opponent draws face down card
			else {
				opponentPassedCards.add(faceUpCard);
				drewFaceUp = false;
				safeDiscardMatrix[faceUpCard.getId()] = 1;
			}
		}

	}


	@Override
	public Card getDiscard() {
		if (drewFaceUp) {
			return Card.getCard((int) getBestDiscardAndProb(drawnCard)[0]);
		}
		return Card.getCard((int) getBestDiscardAndProb(null)[0]);
	}


	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		Card pastFaceUpCard = null;
		if (!discardedCards.isEmpty()) {
			pastFaceUpCard = discardedCards.peek();
		}

		discardedCards.push(discardedCard);

		// our discard
		if (playerNum == this.playerNum) {
			hand.remove(discardedCard);

//			System.out.println(Arrays.toString(opponentCardMatrix));
//			System.out.println(Arrays.toString(RyzUtilities.getAdj(opponentCardMatrix)));
//			System.out.println(Arrays.toString(safeDiscardMatrix));
//			System.out.println(Arrays.toString(RyzUtilities.getAdj(safeDiscardMatrix)));
//			cardMat[discardedCard.getId()] = 0;
		}
		// opponent discard
		else {
			opponentHand.remove(discardedCard);
			opponentDiscardedCards.add(discardedCard);
			unknownCards.remove(discardedCard);

			// hand estimator
			if (drewFaceUp) {
				pastFaceUpCard = drawnCard;
			}

			opponentCardMatrix[discardedCard.getId()] = 0;
			safeDiscardMatrix[discardedCard.getId()] = 1;
		}
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

	public static double regFunction(RyzPlayer ryzPlayer2, Card possDiscard) {
		double regValue = 0;
		double[] features = calculateFeatures(ryzPlayer2);

		double deadWoodImpact = (ryzPlayer2.turn * - 3) * (features[2] / 50);
		double numMeldsImpact = 2.7 * features[4];
		double numCombosImpact = .9 * features[6];

		regValue = deadWoodImpact + numMeldsImpact + numCombosImpact;
		return regValue;
	}

	public static double[] calculateFeatures(RyzPlayer player) {

		double current_player_score = player.scores[player.playerNum];

		double opponent_score = player.scores[1 - player.playerNum];
		double current_player_deadwood = OurUtilities.deadwoodCount(player.hand);

		ArrayList<Card> possibleCards = new ArrayList<>(player.unknownCards);
		possibleCards.addAll(player.opponentHand);
		double current_player_num_hit_cards = OurUtilities.numHitCards(possibleCards, player.hand);

		double turns_taken = player.turn;

		ArrayList<ArrayList<ArrayList<Card>>> organization = OurUtilities.getBestHandOrganization(player.hand);

		double num_melds = organization.get(0).size();
		double point_sum_melds = OurUtilities.getPoints(organization.get(0));

		double num_combos = organization.get(1).size();
		double point_sum_combos = OurUtilities.getPoints(organization.get(1));

		double num_knock_cache = organization.get(2).get(0).size();
		double point_sum_knock_cache = OurUtilities.getPoints(organization.get(2));

		double num_load_cards = organization.get(3).get(0).size();
		double point_sum_load_cards = OurUtilities.getPoints(organization.get(3));


		return new double[] {
				current_player_score,						// 0
				opponent_score, 								// 1
				current_player_deadwood, 				// 2
				current_player_num_hit_cards,		// 3
				// alpha to here
				num_melds,											// 4
				point_sum_melds,								// 5
				num_combos,											// 6
				point_sum_combos,								// 7
				num_knock_cache,								// 8
				point_sum_knock_cache,					// 9
				num_load_cards,									// 10
				point_sum_load_cards,						// 11
				turns_taken,										// 12
				// beta to here
				// gamma to here
				// delta to here

				//num_set_melds
				//num_run_melds
				//num_set_combos
				//num_run_combos
		};
	}


}
