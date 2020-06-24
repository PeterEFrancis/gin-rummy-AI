import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

import ginrummy.Card;
import ginrummy.GinRummyPlayer;
import ginrummy.GinRummyUtil;

public class Player implements GinRummyPlayer{

	int test_turn = -2;


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


	public Player() {
		reset();
		this.version = BlackBox.ALPHA;
		this.type = BlackBox.LINEAR;
		this.scores = new int[2];
	}


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

		for (Card card : hand) {
			this.hand.add(card);
		}

		this.playerNum = playerNum;
		this.startingPlayerNum = startingPlayerNum;

	}




	// TODO: change datastructure to better remove and add?
	public double[] getBestDiscardAndProb(Card faceUpCard) {

		return ;
	}



	@Override
	public boolean willDrawFaceUpCard(Card faceUpCard) {
		return false;
	}


	@Override
	public void reportDraw(int playerNum, Card drawnCard) {

	}


	@Override
	public Card getDiscard() {

	}


	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {

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

}
