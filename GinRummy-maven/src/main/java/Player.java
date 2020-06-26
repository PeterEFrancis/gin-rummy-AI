import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import ginrummy.Card;
import ginrummy.GinRummyPlayer;
import ginrummy.GinRummyUtil;

public class Player implements GinRummyPlayer{

	int test_turn = -1;


	public int type;
	public int version;
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
	public int[] cardMat;

	// for hand estimation
	public HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();

//
//	public Player() {
//		reset();
//		this.version = BlackBox.ALPHA;
//		this.type = BlackBox.LINEAR;
//		this.scores = new int[2];
//	}


	public Player(int version, int type) {
		reset();
		this.version = version;
		this.type = type;
		this.scores = new int[2];
	}

	public Player(int version, int type, long seed) {
		reset();
		this.version = version;
		this.type = type;
		this.random.setSeed(seed);
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
		cardMat = new int[Card.NUM_CARDS];
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
		for (Card c : hand) {
			handAL.add(c);
			// update card matrix
			cardMat[c.getId()] = 1;
		}
		estimator.setKnown(handAL, false);

		//		  estimator.print();
	}


	public static String disp_arr(double[] arr, int precision, int spacing) {
		StringBuilder sb = new StringBuilder("[");
		for (double d : arr) {
			String toAdd = (d + "               ").substring(0, precision);
			toAdd = (toAdd.trim() + ",                ").substring(0,precision + 1);
			sb.append(toAdd);
			for (int i = 0; i < spacing; i++) {
				sb.append(" ");
			}
		}
		sb.append("]");
		return sb.toString();
	}



	// TODO: change datastructure to better remove and add?
	public double[] getBestDiscardAndProb(Card faceUpCard) {
		if (turn == test_turn) {
			System.out.println("\tFinding the best discard ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ v ");
		}
		LinkedList<Card> possibleDiscards = new LinkedList<Card>();
		possibleDiscards.addAll(hand);
		if (faceUpCard != null) {
			possibleDiscards.remove(faceUpCard);
		}
		if (turn == test_turn) {
			System.out.println("\t|\tpossible discards: " + possibleDiscards);
		}
		Card bestDiscard = null;
		double bestProbOfWinning = -Double.MAX_VALUE;

		if (turn == test_turn) {
			System.out.println("\t|");
			System.out.println("\t|\t\ttest Discard       CPSc  OPSc  DeWo  nHCa  nMel  psMe  nCom  psCo  nKCa  psKC  nLoC  psLC  TuTa  nNOC  DisDan     Prob. of Winning");
			System.out.println("\t|\t\t                                *           *                                               *                                     ");
		}

		for (Card possDiscard : possibleDiscards) {
			hand.remove(possDiscard);
			discardedCards.add(possDiscard);
			double probOfWinning = BlackBox.regFunction(this);
			if (turn == test_turn) {
				System.out.println("\t|\t\t\t" + possDiscard + "    ->  " + disp_arr(OurUtilities.calculateFeatures(this), 5, 0) + " ->  " + probOfWinning);
			}
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
		if (turn == test_turn) {
			System.out.println("\t|\n\t|\tbest discard: " + Card.allCards[(int) ret[0]]);
			System.out.println("\t|\tbest discard prob: " + ret[1]);
		}
		if (turn == test_turn) {
			System.out.println("\t~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ^ ");
		}
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

		if (turn == test_turn) {
			System.out.println("==================================================================================== Begin Extensive Verbose Turn: Player " + playerNum);
			System.out.println("Hand estimation matrix:");
			double[][] matrix = OurUtilities.getCardProbImageMatrix(this);
			for (int i = 0; i < matrix.length; i++) {
				System.out.println("\t" + disp_arr(matrix[i], 5, 4));
			}
			System.out.println("\nestimator:\n");
			estimator.print();
			System.out.println();
			System.out.println("Finding best draw option for player " + playerNum);
			System.out.println("#############################");
			System.out.println("draw face up card option:");
		}

		// find probability of winning if draw face up
		Card fuc = discardedCards.pop();
		hand.add(fuc);
		if (turn == test_turn) {
			System.out.println("\tface up card: " + faceUpCard);
			// System.out.println("\tfuc: " + fuc);

		}
		double[] bestDiscardAndProb = getBestDiscardAndProb(faceUpCard);
		double drawFaceUpCardWinProb = bestDiscardAndProb[1];
		hand.remove(faceUpCard);
		discardedCards.push(faceUpCard);

		if (turn == test_turn) {
			System.out.println("draw face up win prob: " + drawFaceUpCardWinProb);
			System.out.println("#############################");
			System.out.println("draw face down card options:");
		}

		// find average probability of winning if you draw from face down card
		double drawFaceDownWinProbAvg = 0;
		for (int i = 0; i < unknownCards.size(); i++) {
			Card card = unknownCards.remove(0);
			if (turn == test_turn) {
				System.out.println("\tpossible draw card: " + card);
			}
			hand.add(card);
			drawFaceDownWinProbAvg += getBestDiscardAndProb(null)[1];
			hand.remove(card);
			unknownCards.add(card);
		}
		drawFaceDownWinProbAvg /= unknownCards.size();

		if (turn == test_turn) {
			System.out.println("draw face down average win prob: " + drawFaceDownWinProbAvg);
			System.out.println("#############################");
		}

		boolean retBool = false;
		if (drawFaceDownWinProbAvg < drawFaceUpCardWinProb){
			retBool = true;
		}

		if (turn == test_turn) {
			System.out.println("Player decides to draw " + (retBool ? "face up card (" + faceUpCard + ")" : "face down card"));
			System.out.println("#############################");
		}

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
			// update card matrix
			cardMat[drawnCard.getId()] = 1;
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

			cardMat[discardedCard.getId()] = 0;
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

		if (test_turn != -1 && turn == test_turn + 1) {
			System.out.println("==================================================================================== End Extensive Verbose Turn");
//			System.out.println(1/0);
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

	// added v


}
