import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import ginrummy.*;

public class TaylorPlayer implements GinRummyPlayer{

	int test_turn = -1;


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

	// for hand estimation
	public HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();


	public TaylorPlayer() {
		scores = new int[2];
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

		estimator.init();
		ArrayList<Card> handAL = new ArrayList<Card>();
		for (Card c : hand)
			handAL.add(c);
		estimator.setKnown(handAL, false);
		//		  estimator.print();
	}

	// TODO: change datastructure to better remove and add?
	public double[] getBestDiscardAndProb(Card faceUpCard) {

		LinkedList<Card> possibleDiscards = new LinkedList<Card>();
		possibleDiscards.addAll(hand);
		if (faceUpCard != null) {
			possibleDiscards.remove(faceUpCard);
		}

		Card bestDiscard = null;
		double bestProbOfWinning = -Double.MAX_VALUE;


		for (Card possDiscard : possibleDiscards) {
			hand.remove(possDiscard);
			discardedCards.add(possDiscard);
			double probOfWinning = heuristic();

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

		// hand estimator
		estimator.setKnown(faceUpCard, false);

		// first turn -- this... this is how we find out what the initial face up card is.
		if (discardedCards.isEmpty()) {
			discardedCards.push(faceUpCard);
			unknownCards.remove(faceUpCard);
		}

//		// find probability of winning if draw face up
		// Card fuc = discardedCards.pop();
		// hand.add(fuc);
		//
		// double[] bestDiscardAndProb = getBestDiscardAndProb(faceUpCard);
		// double drawFaceUpCardWinProb = bestDiscardAndProb[1];
		// hand.remove(faceUpCard);
		// discardedCards.push(faceUpCard);
		//
		//
		// // find average probability of winning if you draw from face down card
		// double drawFaceDownWinProbAvg = 0;
		// for (int i = 0; i < unknownCards.size(); i++) {
		// 	Card card = unknownCards.remove(0);
		// 	hand.add(card);
		// 	drawFaceDownWinProbAvg += getBestDiscardAndProb(null)[1];
		// 	hand.remove(card);
		// 	unknownCards.add(card);
		// }
		// drawFaceDownWinProbAvg /= unknownCards.size();


		boolean retBool = false;
		// if (drawFaceDownWinProbAvg < drawFaceUpCardWinProb){
		// 	retBool = true;
		// }

		ArrayList<Card> newCards = (ArrayList<Card>) hand.clone();
		newCards.add(faceUpCard);
		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
			if (meld.contains(faceUpCard))
				return retBool = true;

		return retBool;
	}


	@Override
	public void reportDraw(int playerNum, Card drawnCard) {

		// our draw
		if (playerNum == this.playerNum) {
			// hand estimator
			estimator.setKnown(drawnCard, false);

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
			}
			// opponent draws face down card
			else {
				opponentPassedCards.add(faceUpCard);
				drewFaceUp = false;
			}
		}

	}


	@Override
	public Card getDiscard() {
		// TODO : Prevent future repeat of draw, discard pair?

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
			estimator.reportDrawDiscard(pastFaceUpCard, drewFaceUp, discardedCard);
		}

		turn++;
	}


	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out.
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand) > GinRummyUtil.MAX_DEADWOOD))
			return null;

		if (!opponentKnocked) {
			if (turn > 7) {
				double[] feats = calcF();
//				if (feats[0] >= 75)
//					return null; // if score is 75 or greater
				if (feats[3] > 1) {
					if (feats[3] > 2) // hit cards is 3 or more
						return null;
					if (feats[8] == 1) // knock cash is 1
						return null;
					if (feats[8] < 3 && feats[2] < 6) // knock cash is 1 or 2 and deadwood is less than 6
						return null;
//					if (feats[13] > 0) // there are nearby cards
//						return null;
				}
			}

		}

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

	public double[] calcF() {
		double current_player_score = scores[playerNum];

		double opponent_score = scores[1 - playerNum];
		double current_player_deadwood = OurUtilities.deadwoodCount(hand);

		ArrayList<Card> possibleCards = new ArrayList<>(unknownCards);
		possibleCards.addAll(opponentHand);
		double current_player_num_hit_cards = OurUtilities.numHitCards(possibleCards, hand);

		double turns_taken = turn;

		ArrayList<ArrayList<ArrayList<Card>>> organization = OurUtilities.getBestHandOrganization(hand);

		double num_melds = organization.get(0).size();
		double point_sum_melds = OurUtilities.getPoints(organization.get(0));

		double num_combos = organization.get(1).size();
		double point_sum_combos = OurUtilities.getPoints(organization.get(1));

		double num_knock_cache = organization.get(2).get(0).size();
		double point_sum_knock_cache = OurUtilities.getPoints(organization.get(2));

		double num_load_cards = organization.get(3).get(0).size();
		double point_sum_load_cards = OurUtilities.getPoints(organization.get(3));

		//System.out.println("op hand: "+player.opponentHand);
		//System.out.println("our hand: "+player.hand);
		ArrayList<Card> nearby = OurUtilities.nearbyCardsInHand(opponentHand, hand);
		//System.out.println("nearby: "+nearby);
		double num_nearby_opponent_cards = nearby.size();

		Card discardedCard = discardedCards.peek();
		double discard_danger = OurUtilities.getDangerOfDiscard(discardedCard, estimator);

		// double pleaseputagoodnamehere =
		//double num_set_melds = numSetMelds(organization.get(0));
		//double num_run_melds = numRunMelds(organization.get(0));
		//double num_set_combos = numSetCombos(organization.get(1));
		//double num_run_combos = numRunCombos(organization.get(1));

		//list of possible features to add
		// num_drawn, op_num_drawn   (face up drawn cards for both players)
		// num_

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
				num_nearby_opponent_cards,			// 13
				// gamma to here
				discard_danger,									// 14
				// delta to here

				//num_set_melds
				//num_run_melds
				//num_set_combos
				//num_run_combos
		};
	}

public double turnconst = 15;
public double deadconst = 1.6;
public double hitconst = 3.6;

	public double heuristic() {
		double[] features = calcF();
		double dwood = features[2];
		double hits = features[3];
//		double mhits = modifiedHits();
		double turn = features[12];
		double nearby = features[13];
		double ddanger = features[14];
		double numload = features[10];
		double numcash = features[9];

		double score = -dwood;
		score -= deadconst*dwood*turn/(turnconst*2/3);
		score += hitconst*hits*(turnconst-turn)/turnconst;
//		score += mhits;						// this doesn't work well
//		score += nearby*4;					// this doesn't work well

		return score;

	}




}
