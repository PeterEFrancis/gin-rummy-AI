import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.Stack;

public class Player implements GinRummyPlayer{

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
	public double[] discardProbabilities;

//	for hand estimation
	Card faceUpCard;
	public HandEstimator estimator = new HandEstimator();
	private int totalDiscarded = 0;
	ArrayList<Double> ratios = new ArrayList<Double>();

	public Player(int version, int type) {
		reset();
		this.version = version;
		this.type = type;
	}

	public Player(int version, int type, long seed) {
		reset();
		this.version = version;
		this.type = type;
		random.setSeed(seed);
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
		scores = new int[2];
		discardProbabilities = new double[52];
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
		 for (Card c : cards)
		 	handAL.add(c);
		 estimator.setKnown(handAL, false);
		 estimator.print();
		 totalDiscarded = 0;
	}


	/**
	 * Sets the discard probabilities and returns the card object corresponding
	 * to the discard with highest winning probability.
	**/
	// TODO: change datastructure to better remove and add?
	public Card setDiscardProbabilities(Card faceUpCard) {
		LinkedList<Card> possibleDiscards = new LinkedList<Card>();
		possibleDiscards.addAll(hand);
		if (faceUpCard != null) {
			possibleDiscards.remove(faceUpCard);
		}
		Card bestDiscard = null;
		double bestProbOfWinning = Double.MIN_VALUE;
		for (Card possDiscard : possibleDiscards) {
			hand.remove(possDiscard);
			discardedCards.add(possDiscard);
			double probOfWinning = BlackBox.regFunction(this);
			discardProbabilities[possDiscard.getId()] = probOfWinning;
			if (probOfWinning > bestProbOfWinning) {
				bestProbOfWinning = probOfWinning;
				bestDiscard = possDiscard;
			}
			discardedCards.remove(possDiscard);
			hand.add(possDiscard);
		}
		return bestDiscard;
	}


	@Override
	public boolean willDrawFaceUpCard(Card faceUpCard) {
		faceUpCard = OurUtilities.transformCard(faceUpCard);

		 estimator.setKnown(card, false);
		 this.faceUpCard = card;

		// first turn -- this... this is how we find out what the initial face up card is.
		if (discardedCards.isEmpty()) {
			discardedCards.push(faceUpCard);
			unknownCards.remove(faceUpCard);
		}

		discardProbabilities = new double[52];

		// find probability of winning if draw face up
		hand.add(discardedCards.pop());
		Card bestDiscard = setDiscardProbabilities(faceUpCard);
		double drawFaceUpCardWinProb = discardProbabilities[bestDiscard.getId()];
		hand.remove(faceUpCard);
		discardedCards.push(faceUpCard);

		// find average probability of winning if you draw from face down card
		double drawFaceDownWinProbAvg = 0;
		for (int i = 0; i < unknownCards.size(); i++) {
			Card card = unknownCards.remove(0);
			hand.add(card);
			Card bestDiscard1 = setDiscardProbabilities(null);
			drawFaceDownWinProbAvg += discardProbabilities[bestDiscard1.getId()];
			hand.remove(card);
			unknownCards.add(card);
		}
		drawFaceDownWinProbAvg /= unknownCards.size();

		if (drawFaceDownWinProbAvg < drawFaceUpCardWinProb){
			return true;
		}
		return false;
	}


	@Override
	public void reportDraw(int playerNum, Card drawnCard) {
		try {
			drawnCard = OurUtilities.transformCard(drawnCard);
		} catch (Exception e) {
			// lol. Yeah, we really need a Card class, don't we?
			// this sure is making EVERYONE's lives easier.       (@)/(@)
		}
		
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
			}
			// opponent draws face down card
			else {
				opponentPassedCards.add(faceUpCard);
			}
		}

	}


	@Override
	public Card getDiscard() {
		// TODO : Prevent future repeat of draw, discard pair?
		Card bestDiscard = null;
		double bestProb = Double.MIN_VALUE;
		for (int i = 0; i < hand.size(); i++) {
			Card currCard = hand.get(i);
			double discardProb = discardProbabilities[currCard.getId()];
			if (discardProb > bestProb) {
				bestProb = discardProb;
				bestDiscard = currCard;
			}
		}
		return bestDiscard;
	}


	@Override
	public void reportDiscard(int playerNum, Card discardedCard) {
		discardedCard = OurUtilities.transformCard(discardedCard);

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
		this.scores = scores.clone();
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
