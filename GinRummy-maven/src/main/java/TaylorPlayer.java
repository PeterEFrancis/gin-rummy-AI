import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import javax.sound.midi.SysexMessage;

import ginrummy.*;


public class TaylorPlayer implements GinRummyPlayer{

	int test_turn = -1;

	int type;
	public static final int OLD = 0;
	public static final int REG = 1;
	public static final int GO_GIN = 2;
	public static final int FAST_KNOCK = 3;
	//test


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
	public ArrayList<ArrayList<Card>> opMelds;

	public TaylorPlayer() {
		this(OLD);
	}
	public TaylorPlayer(int type) {
		scores = new int[2];
		this.type = type;
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
		opMelds = new ArrayList<ArrayList<Card>>();
		discardedCards = new Stack<Card>();
		random = new Random(0);
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
		
		if (turn > 50)
			return false;

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
		}

		turn++;
	}


	@Override
	public ArrayList<ArrayList<Card>> getFinalMelds() {
		// Check if deadwood of maximal meld is low enough to go out.
		////13.0191019836,-0.0934308441,-0.1710914405,-0.2967996745,-3.6200016049 

		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		
		
		if (!opponentKnocked) {

			if (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), hand) > GinRummyUtil.MAX_DEADWOOD)
				return null;
			
			
			
			double[] feats = calcF();
			if (type == GO_GIN) {
				if (feats[2] == 0)
					return bestMeldSets.get(0);
				return null;
			}
			else if (type == FAST_KNOCK)
				return bestMeldSets.get(0);
			else if (type == OLD) {
				if (feats[2] == 0)
					return bestMeldSets.get(0);
				if (turn > 7) {
					
					//					if (feats[0] >= 75)
					//						return null; // if score is 75 or greater
					if (feats[3] > 1) {
						if (feats[3] > 2) // hit cards is 3 or more
							return null;
						if (feats[8] == 1) // knock cash is 1
							return null;
						if (feats[8] < 3 && feats[2] < 6) // knock cash is 1 or 2 and deadwood is less than 6
							return null;
						//						if (feats[13] > 0) // there are nearby cards
						//							return null;
					}
				}
			}
			else if (type == REG) {
				//13.0191019836,-0.0934308441,-0.1710914405,-0.2967996745,-3.6200016049 
				//7.9437129329,-0.1069889502,-0.0717781042,-3.6712258200 no hit cards
				//-1.5591367689,-0.0002118639,-0.0391332872,-2.3604671369 deadwood squared, no hit card
				//-1.4227071282,-0.0002004365,0.3892715114,-4.9603696757,-2.1617916261 num_opponent_drawn (deadwood squared)
				//8.2557723604,-0.0000000057,-0.6332207794,-0.0358811272 gin rating squared
				//6.8146259698,-0.0003235425,-0.5046563938,-0.0000278105 gin rating squared deadwood squared
				//-1.3628946739,0.0003137766,-0.3187430038,-0.0100138815 
				//25.8780053290,-0.1518415068,-1.1481568138,-1.2022821488 best
				//26.3352712620,-0.1628341124,-0.8705252460,-1.2085649094,-3.2397413281 num opponent drawn
				//35.3727832629,-0.1782984932,-0.7414126804,-0.9259340706,-3.4544522483,-4.0195098062,-6.0311405520 num opponent drawn, set melds, run melds
				//18.8531030616,-0.1232572344,-1.0910931483,-1.0830827132 deadwood^2, num_discarded, gin rating^2
				//26.3095479611,-0.0000000778,-0.7387795764,-2.4402438923,-6.0769791365,-7.3258541055 no gin rating
				//19.6004421437,-0.0984499221,-0.5434354834,-0.7336717683,-1.3870395981,-3.1072493891,-2.5159534065,-3.4955969876 num opponent drawn, set melds, run melds, num opponent layoff
				// 28.5664675788,-0.1268362604,-0.6350133286,-0.7464949132,-2.0527062758,-5.1421435910,-6.0656498051,0.9058453666 above with 10,000 games
				double score = 0;
				double[] reg = {25.8780053290,-0.1518415068,-1.1481568138,-1.2022821488};
				score += reg[0];
				score += feats[2] * feats[2] * reg[1];
//				score += feats[3] * reg[2];
				score += feats[12] * reg[2];
				score += feats[14] * feats[14] * reg[3];


				//System.out.println("Score: " + score);
				if (score > 0 || feats[2] == 0) {
//				if (feats[14] < -1 || feats[2] == 0) {
					return bestMeldSets.get(0);
				}
				else {
					return null;
				}

			}
			else {
				System.err.println("Type error");
			}
		}


		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
	}
	

	@Override
	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
		// Melds ignored by simple player, but could affect which melds to make for complex player.
		if (playerNum != this.playerNum) {
			opponentKnocked = true;
			opMelds = melds;
		}
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

//		Card discardedCard = discardedCards.peek();
//		double discard_danger = OurUtilities.getDangerOfDiscard(discardedCard, estimator);
		
		double gin_rating = OurUtilities.getGinRating(hand, unknownCards);
		double num_opponent_drawn = opponentAllTimeHand.size();
		
		double num_set_melds = OurUtilities.numSetMelds(organization.get(0));
		double num_run_melds = OurUtilities.numRunMelds(organization.get(0));
		
		double num_discarded = discardedCards.size();
		double num_opponent_layoff = 0;
		for (Card c : opponentHand) {
			if (OurUtilities.canBeMeldedIn(c, organization.get(0))) {
				num_opponent_layoff++;
			}
		}
		
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
				gin_rating,
				num_opponent_drawn,
				// gamma to here
//				discard_danger,									// 14
				// delta to here

				num_set_melds,
				num_run_melds,
				num_discarded,
				num_opponent_layoff
				//num_set_combos
				//num_run_combos
		};
	}

public double turnconst = 15;
public double deadconst = 1.6;
public double hitconst = 3.6;

	public double heuristic() {
		//3.9392167199,0.0146450136,-0.0154069223,-0.1766904915,0.0081401442       	-alpha with hits squared
		//6.5946068966,0.0148006395,-0.0144666241,-0.0388012138,-0.0181583350		-alpha with hits and deadwood multiplied by turn
		double[] regCoeffs = {3.9392167199,0.0146450136,-0.0154069223,-0.1766904915,0.0081401442};
		
		double[] features = calcF();
		double ourscore = features[0];
		double opscore = features[1];
		double dwood = features[2];
		double hits = features[3];
//		double mhits = modifiedHits();
		double turn = features[12];
		double nearby = features[13];
//		double ddanger = features[14];
		double numload = features[10];
		double numcash = features[9];

		
		double score = 0;
		if (type == REG) {
//			features[3] *= features[3];
////			features[2] *= features[12];
////			features[3] /= features[12];
//			for (int i = 0; i < regCoeffs.length-1; i++) {
//				score += regCoeffs[i+1]*features[i];
//			}
			score = -dwood;
			score -= deadconst*dwood*turn/(turnconst*2/3);
			score += hitconst*hits*(turnconst-turn)/turnconst;
			score -= nearby;
		}
		else {
			score = -dwood;
			score -= deadconst*dwood*turn/(turnconst*2/3);
			score += hitconst*hits*(turnconst-turn)/turnconst;
		}
//		score += mhits;						// this doesn't work well
//		score += nearby*4;					// this doesn't work well
		

		return score;

	}




}
