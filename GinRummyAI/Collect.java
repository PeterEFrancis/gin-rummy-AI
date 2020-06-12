import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

/**
 * Collect.java - Collects play data and stores it in a csv files
 **/
public class Collect {


	/**
	 * Random number generator
	 */
	private Random RANDOM;
	private long seed;

	/**
	 * Hand size (before and after turn). After draw and before discard there is one extra card.
	 */
	private static final int HAND_SIZE = 10;

	/**
	 * Whether or not to print information during game play
	 */
	private static boolean playVerbose = false;

	private static boolean errorVerbose = true;

	/**
	 * Two Gin Rummy players numbered according to their array index.
	 */
	private GinRummyPlayer[] players;

	/**
	 * Set whether or not there is to be printed output during gameplay.
	 * @param playVerbose whether or not there is to be printed output during gameplay
	 */
	public static void setPlayVerbose(boolean playVerbose) {
		Collect.playVerbose = playVerbose;
	}

	/**
	 * Create a GinRummyGame with two given players
	 * @param player0 Player 0
	 * @param player1 Player 1
	 */
	public Collect(GinRummyPlayer player0, GinRummyPlayer player1, long seed) {
		players = new GinRummyPlayer[] {player0, player1};
		this.seed = seed;
		RANDOM = new Random(seed);
	}

	public Collect(GinRummyPlayer player0, GinRummyPlayer player1) {
		players = new GinRummyPlayer[] {player0, player1};
		this.seed = 0;
		RANDOM = new Random();
	}

	/**
	 * Play a game of Gin Rummy and return the winning player number 0 or 1.
	 * @return the winning player number 0 or 1
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<ArrayList<String>> getPlayData() {

		int line_number = 2;

		ArrayList<ArrayList<String>> csvOutput = new ArrayList<ArrayList<String>>();


		int[] scores = new int[2];
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		hands.add(new ArrayList<Card>());
		hands.add(new ArrayList<Card>());
		int startingPlayer = RANDOM.nextInt(2);

		while (scores[0] < GinRummyUtil.GOAL_SCORE && scores[1] < GinRummyUtil.GOAL_SCORE) { // while game not over

			ArrayList<String> handData = new ArrayList<String>();

			// hand winner
			double handWinner = 0.5;


			int currentPlayer = startingPlayer;
			int opponent = (currentPlayer == 0) ? 1 : 0;

			// get shuffled deck and deal cards
			Stack<Card> deck = Card.getShuffle(RANDOM.nextInt());
			hands.get(0).clear();
			hands.get(1).clear();
			for (int i = 0; i < 2 * HAND_SIZE; i++)
				hands.get(i % 2).add(deck.pop());
			for (int i = 0; i < 2; i++) {
				Card[] handArr = new Card[HAND_SIZE];
				hands.get(i).toArray(handArr);
				players[i].startGame(i, startingPlayer, handArr);
				if (playVerbose)
					System.out.printf("Player %d is dealt %s.\n", i, hands.get(i));
			}
			if (playVerbose)
				System.out.printf("Player %d starts.\n", startingPlayer);
			Stack<Card> discards = new Stack<Card>();
			discards.push(deck.pop());
			if (playVerbose)
				System.out.printf("The initial face up card is %s.\n", discards.peek());
			Card firstFaceUpCard = discards.peek();
			int turnsTaken = 0;
			ArrayList<ArrayList<Card>> knockMelds = null;

			while (deck.size() > 2) { // while the deck has more than two cards remaining, play round



// -------------------- DATA A -------------------------------------------------------------↓
// 				 // System.out.println("---------------------------------------------------------------------- line number: " + line_number++);
// 				 deck.addAll(hands.get(opponent));
// 				 // ArrayList<Card> uc = players[currentPlayer].unknownCards;
// 				 // players[currentPlayer].unknownCards = deck;
// 				 double[] features = null;
// 				 if (players[currentPlayer] instanceof SimpleGinRummyPlayer) {
// 				 	ArrayList<Card> deckArray = new ArrayList<Card>();
// 				 	deckArray.addAll(deck);
// 				 	features = OurUtilities.calculateSimpleFeatures((SimpleGinRummyPlayer) players[currentPlayer], deckArray, scores);
// 				 } else if (players[currentPlayer] instanceof Player) {
// 				 	// ArrayList<Card> deckArray = new ArrayList<Card>();
// 				 	// deckArray.addAll(deck);
// 				 	// System.out.println();
// 				 	//
// 				 	// System.out.print("A - Unknown Cards: ");
// 				 	// OurUtilities.printAsSorted(((Player) players[currentPlayer]).unknownCards);
// 				 	// System.out.println();
// 				 	//
// 				 	// System.out.print("A - Stock + hand : ");
// 				 	// OurUtilities.printAsSorted(deckArray);
// 				 	// System.out.println();
//
// 				 	features = OurUtilities.calculateFeatures((Player) players[currentPlayer]);
// 				 } else if (players[currentPlayer] instanceof SimplePlayer) {
// 				 	// System.out.println("a-hand: " + ((SimplePlayer) players[currentPlayer]).hand );
// 				 	// System.out.println("a-unknown hit cards: " + OurUtilities.numHitCards( ((SimplePlayer) players[currentPlayer]).unknownCards ,((SimplePlayer) players[currentPlayer]).hand ));
// //				 	features = OurUtilities.calcSimple2((SimplePlayer) players[currentPlayer]);
// 				 } else {
// 				 	System.err.println("You can only collect data on SimpleGinRummyPlayer or Player.");
// 				 }
// 				 // ArrayList<ArrayList<ArrayList<Card>>> best = OurUtilities.getBestHandOrganization(players[currentPlayer].hand);
// 				 // System.out.println("melds " + currentPlayer + ": " + best.get(0));
// 				 // System.out.println("combos " + currentPlayer + ": " + best.get(1));
// 				 // System.out.println("knockCash " + currentPlayer + ": " + best.get(2));
// 				 // System.out.println("load Cards " + currentPlayer + ": " + best.get(3));
// 				 // System.out.println("hand " + currentPlayer + " : " + players[currentPlayer].hand);
// 				 // System.out.println("to discard: " + players[currentPlayer].toDiscard);
// 				 // System.out.println("discarded: " + players[currentPlayer].discardedCards);
// 				  // System.out.println("unknown " + currentPlayer + " : " + ((SimplePlayer) players[currentPlayer]).unknownCards);
//
// 				 // System.out.println("features: " + Arrays.toString(features));
// 				 StringBuilder sb = new StringBuilder(currentPlayer + "");
// 				 for (int i = 0; i < features.length; i++)
// 				 	sb.append("," + features[i]);
// 				 handData.add(sb.toString());
// 				 // players[currentPlayer].unknownCards = uc;
// 				 deck.removeAll(hands.get(opponent));
// -------------------- DATA A -------------------------------------------------------------↑




				// DRAW
				boolean drawFaceUp = false;
				Card faceUpCard = discards.peek();
				// offer draw face-up iff not 3rd turn with first face up card (decline automatically in that case)




				if (!(turnsTaken == 2 && faceUpCard == firstFaceUpCard)) { // both players declined and 1st player must draw face down
					drawFaceUp = players[currentPlayer].willDrawFaceUpCard(faceUpCard);
					if (playVerbose && !drawFaceUp && faceUpCard == firstFaceUpCard && turnsTaken < 2)
						System.out.printf("Player %d declines %s.\n", currentPlayer, firstFaceUpCard);
				}




				if (!(!drawFaceUp && turnsTaken < 2 && faceUpCard == firstFaceUpCard)) { // continue with turn if not initial declined option
					Card drawCard = drawFaceUp ? discards.pop() : deck.pop();

					for (int i = 0; i < 2; i++)
						players[i].reportDraw(currentPlayer, (i == currentPlayer || drawFaceUp) ? drawCard : null);
					if (playVerbose)
						System.out.printf("Player %d draws %s.\n", currentPlayer, drawCard);
					hands.get(currentPlayer).add(drawCard);



// -------------------- DATA B -------------------------------------------------------------↓
// 					 // System.out.println("---------------------------------------------------------------------- line number: " + line_number++);
// 					 deck.addAll(hands.get(opponent));
// 					 // ArrayList<Card> uc = players[currentPlayer].unknownCards;
// 					 // players[currentPlayer].unknownCards = deck;
// 					 if (players[currentPlayer] instanceof SimpleGinRummyPlayer) {
// 					 	ArrayList<Card> deckArray = new ArrayList<Card>();
// 					 	deckArray.addAll(deck);
// 					 	features = OurUtilities.calculateSimpleFeatures((SimpleGinRummyPlayer) players[currentPlayer], deckArray, scores);
// 					 } else if (players[currentPlayer] instanceof Player) {
// 					 	// ArrayList<Card> deckArray = new ArrayList<Card>();
// 					 	// deckArray.addAll(deck);
// 					 	// System.out.println();
// 					 	//
// 					 	// System.out.print("B - Unknown Cards: ");
// 					 	// OurUtilities.printAsSorted(((Player) players[currentPlayer]).unknownCards);
// 					 	// System.out.println();
// 					 	//
// 					 	// System.out.print("B - Stock + hand : ");
// 					 	// OurUtilities.printAsSorted(deckArray);
// 					 	// System.out.println();
//
// 					 	features = OurUtilities.calculateFeatures((Player) players[currentPlayer]);
// 					 } else if (players[currentPlayer] instanceof SimplePlayer) {
// 					 	// System.out.println("b-hand: "+ ((SimplePlayer) players[currentPlayer]).hand );
// 					 	// System.out.println("b-hit count:" + OurUtilities.numHitCards( ((SimplePlayer) players[currentPlayer]).unknownCards,((SimplePlayer) players[currentPlayer]).hand ));
// 					 	// System.out.println("unknown " + currentPlayer + " : " + ((SimplePlayer) players[currentPlayer]).unknownCards);
// //					 	features = OurUtilities.calcSimple2((SimplePlayer) players[currentPlayer]);
// 					 	// System.out.println("unknown " + currentPlayer + " : " + ((SimplePlayer) players[currentPlayer]).unknownCards);
// 					 } else {
// 					 	System.err.println("You can only collect data on SimpleGinRummyPlayer or Player.");
// 					 }
// 					 sb = new StringBuilder(currentPlayer + "");
// 					 for (int i = 0; i < features.length; i++)
// 					 	sb.append("," + features[i]);
// 					 handData.add(sb.toString());
// 					 // players[currentPlayer].unknownCards = uc;
// 					 deck.removeAll(hands.get(opponent));
// -------------------- DATA B -------------------------------------------------------------↑




					// DISCARD
					Card discardCard = players[currentPlayer].getDiscard();
					if (!hands.get(currentPlayer).contains(discardCard) || discardCard == faceUpCard) {
						if (playVerbose || errorVerbose)
							System.err.printf("Player %d discards %s illegally and forfeits.\n", currentPlayer, discardCard);
							System.err.println("seed: " + seed);
							return null;
					}

					hands.get(currentPlayer).remove(discardCard);
					for (int i = 0; i < 2; i++)
						players[i].reportDiscard(currentPlayer, discardCard);
					if (playVerbose)
						System.out.printf("Player %d discards %s.\n", currentPlayer, discardCard);
					discards.push(discardCard);
					if (playVerbose) {
						ArrayList<Card> unmeldedCards = (ArrayList<Card>) hands.get(currentPlayer).clone();
						ArrayList<ArrayList<ArrayList<Card>>> bestMelds = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
						if (bestMelds.isEmpty())
							System.out.printf("Player %d has %s with %d deadwood.\n", currentPlayer, unmeldedCards, GinRummyUtil.getDeadwoodPoints(unmeldedCards));
						else {
							ArrayList<ArrayList<Card>> melds = bestMelds.get(0);
							for (ArrayList<Card> meld : melds)
								for (Card card : meld)
									unmeldedCards.remove(card);
							melds.add(unmeldedCards);
							System.out.printf("Player %d has %s with %d deadwood.\n", currentPlayer, melds, GinRummyUtil.getDeadwoodPoints(unmeldedCards));
						}
					}



// -------------------- DATA C -------------------------------------------------------------↓
					// deck.addAll(hands.get(opponent));
					// ArrayList<Card> uc = players[currentPlayer].unknownCards;
					// players[currentPlayer].unknownCards = deck;
					double[] features = null;
					if (players[currentPlayer] instanceof SimpleGinRummyPlayer) {
						ArrayList<Card> deckArray = new ArrayList<Card>();
						deckArray.addAll(deck);
						features = OurUtilities.calculateSimpleFeatures((SimpleGinRummyPlayer) players[currentPlayer], deckArray, scores);

					} else if (players[currentPlayer] instanceof Player) {
						features = OurUtilities.calculateFeatures((Player) players[currentPlayer]);
					} else {
						System.err.println("You can only collect data on SimpleGinRummyPlayer or Player.");
					}
					StringBuilder sb = new StringBuilder(currentPlayer + "");
					for (int i = 0; i < features.length; i++)
						sb.append("," + features[i]);
					handData.add(sb.toString());
					deck.removeAll(hands.get(opponent));
// -------------------- DATA C -------------------------------------------------------------↑




					if (line_number > 6) {
						System.out.println(1/0);
					}

					// CHECK FOR KNOCK
					knockMelds = players[currentPlayer].getFinalMelds();
					if (knockMelds != null)
						break; // player knocked; end of round
				}

				turnsTaken++;
				currentPlayer = (currentPlayer == 0) ? 1 : 0;
				opponent = (currentPlayer == 0) ? 1 : 0;

			}



// -------------------- DATA D -------------------------------------------------------------↓
					// System.out.println("---------------------------------------------------------------------- line number: " + line_number++);
// 					deck.addAll(hands.get(opponent));
// 					// ArrayList<Card> uc = players[currentPlayer].unknownCards;
// 					// players[currentPlayer].unknownCards = deck;
// 					double[] features = null;
// 					if (players[currentPlayer] instanceof SimpleGinRummyPlayer) {
// 						ArrayList<Card> deckArray = new ArrayList<Card>();
// 						deckArray.addAll(deck);
// 						features = OurUtilities.calculateSimpleFeatures((SimpleGinRummyPlayer) players[currentPlayer], deckArray, scores);
// 					} else if (players[currentPlayer] instanceof Player) {
// 						features = OurUtilities.calculateFeatures((Player) players[currentPlayer]);
// 					} else if (players[currentPlayer] instanceof SimplePlayer) {
// //						features = OurUtilities.calcSimple2((SimplePlayer) players[currentPlayer]);
// 					} else {
// 						System.err.println("You can only collect data on SimpleGinRummyPlayer or Player.");
// 					}
// 					StringBuilder sb = new StringBuilder(currentPlayer + "");
// 					for (int i = 0; i < features.length; i++)
// 						sb.append("," + features[i]);
// 					handData.add(sb.toString());
// 					// players[currentPlayer].unknownCards = uc;
// 					deck.removeAll(hands.get(opponent));
// -------------------- DATA D -------------------------------------------------------------↑


			int[] scores_before_addition = scores.clone();

			if (knockMelds != null) { // round didn't end due to non-knocking and 2 cards remaining in draw pile
				// check legality of knocking meld
				long handBitstring = GinRummyUtil.cardsToBitstring(hands.get(currentPlayer));
				long unmelded = handBitstring;
				for (ArrayList<Card> meld : knockMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & unmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose || errorVerbose)
							System.err.printf("Player %d melds %s illegally and forfeits.\n", currentPlayer, knockMelds);
							System.err.println("seed: " + seed);
							return null;
					}
					unmelded &= ~meldBitstring; // remove successfully melded cards from
				}
				// compute knocking deadwood
				int knockingDeadwood = GinRummyUtil.getDeadwoodPoints(knockMelds, hands.get(currentPlayer));
				if (knockingDeadwood > GinRummyUtil.MAX_DEADWOOD) {
					if (playVerbose || errorVerbose)
						System.err.printf("Player %d melds %s with greater than %d deadwood and forfeits.\n", currentPlayer, knockMelds, knockingDeadwood);
						System.err.println("seed: " + seed);
						return null;
				}

				ArrayList<ArrayList<Card>> meldsCopy = new ArrayList<ArrayList<Card>>();
				for (ArrayList<Card> meld : knockMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				for (int i = 0; i < 2; i++)
					players[i].reportFinalMelds(currentPlayer, meldsCopy);
				if (playVerbose)
					if (knockingDeadwood > 0)
						System.out.printf("Player %d melds %s with %d deadwood from %s.\n", currentPlayer, knockMelds, knockingDeadwood, GinRummyUtil.bitstringToCards(unmelded));
					else
						System.out.printf("Player %d goes gin with melds %s.\n", currentPlayer, knockMelds);

				// get [opponent meld]
				ArrayList<ArrayList<Card>> opponentMelds = players[opponent].getFinalMelds();
				meldsCopy = new ArrayList<ArrayList<Card>>();
				for (ArrayList<Card> meld : opponentMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				for (int i = 0; i < 2; i++)
					players[i].reportFinalMelds(opponent, meldsCopy);

				// check legality of opponent meld
				long opponentHandBitstring = GinRummyUtil.cardsToBitstring(hands.get(opponent));
				long opponentUnmelded = opponentHandBitstring;
				for (ArrayList<Card> meld : opponentMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & opponentUnmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose || errorVerbose)
							System.err.printf("Player %d melds %s illegally and forfeits.\n", opponent, opponentMelds);
							System.err.println("seed: " + seed);
							return null;
					}
					opponentUnmelded &= ~meldBitstring; // remove successfully melded cards from
				}
				if (playVerbose)
					System.out.printf("Player %d melds %s.\n", opponent, opponentMelds);

				// lay off on knocking meld (if not gin)
				ArrayList<Card> unmeldedCards = GinRummyUtil.bitstringToCards(opponentUnmelded);
				if (knockingDeadwood > 0) { // knocking player didn't go gin
					boolean cardWasLaidOff;
					do { // attempt to lay each card off
						cardWasLaidOff = false;
						Card layOffCard = null;
						ArrayList<Card> layOffMeld = null;
						for (Card card : unmeldedCards) {
							for (ArrayList<Card> meld : knockMelds) {
								ArrayList<Card> newMeld = (ArrayList<Card>) meld.clone();
								newMeld.add(card);
								long newMeldBitstring = GinRummyUtil.cardsToBitstring(newMeld);
								if (GinRummyUtil.getAllMeldBitstrings().contains(newMeldBitstring)) {
									layOffCard = card;
									layOffMeld = meld;
									break;
								}
							}
							if (layOffCard != null) {
								if (playVerbose)
									System.out.printf("Player %d lays off %s on %s.\n", opponent, layOffCard, layOffMeld);
								unmeldedCards.remove(layOffCard);
								layOffMeld.add(layOffCard);
								cardWasLaidOff = true;
								break;
							}

						}
					} while (cardWasLaidOff);
				}
				int opponentDeadwood = 0;

				for (Card card : unmeldedCards)
					opponentDeadwood += GinRummyUtil.getDeadwoodPoints(card);
				if (playVerbose)
					System.out.printf("Player %d has %d deadwood with %s\n", opponent, opponentDeadwood, unmeldedCards);

				// compare deadwood and compute new scores
				if (knockingDeadwood == 0) { // gin round win
					scores[currentPlayer] += GinRummyUtil.GIN_BONUS + opponentDeadwood;
					handWinner = currentPlayer;
					if (playVerbose)
						System.out.printf("Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", currentPlayer, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood);
				}
				else if (knockingDeadwood < opponentDeadwood) { // non-gin round win
					scores[currentPlayer] += opponentDeadwood - knockingDeadwood;
					handWinner = currentPlayer;
					if (playVerbose)
						System.out.printf("Player %d scores the deadwood difference of %d.\n", currentPlayer, opponentDeadwood - knockingDeadwood);
				}
				else { // undercut win for opponent
					scores[opponent] += GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood;
					handWinner = opponent;
					if (playVerbose)
						System.out.printf("Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
				}
				startingPlayer = (startingPlayer == 0) ? 1 : 0; // starting player alternates
			}
			else { // If the round ends due to a two card draw pile with no knocking, the round is cancelled.
				if (playVerbose)
					System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
			}

			handData.add("" + handWinner);
			handData.add("" + (scores[0] - scores_before_addition[0]));
			handData.add("" + (scores[1] - scores_before_addition[1]));

			// score reporting
			if (playVerbose)
				System.out.printf("Player\tScore\n0\t%d\n1\t%d\n", scores[0], scores[1]);
			for (int i = 0; i < 2; i++) {
				players[i].reportScores(scores);
			}


			csvOutput.add(handData);
		}

		if (playVerbose)
			System.out.printf("Player %s wins.\n", scores[0] > scores[1] ? 0 : 1);

		ArrayList<String> gameWinnerLine = new ArrayList<>();
		gameWinnerLine.add("" + (scores[0] >= GinRummyUtil.GOAL_SCORE ? 0 : 1));
		csvOutput.add(gameWinnerLine);

		return csvOutput;
	}




	public static String fileName = "epsilon-3.csv";
	public static File file = new File(fileName);
	public static PrintWriter pw;
	static {
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			System.err.println("No file.");
		}
	}


	public static void main(String args[]) {

		pw.print("current_player,");
		pw.print("is_current_player_hand_winner,");
		pw.print("is_current_player_game_winner,");
		pw.print("current_player_end_hand_score_advantage,");
		// this space is intentional
		// calculated features below
		pw.print("current_player_score,");
		pw.print("opponent_score,");
		pw.print("current_player_deadwood,");
		pw.print("current_player_num_hit_cards,");
		// Alpha to here
		pw.print("num_melds,");
		pw.print("point_sum_melds,");
		pw.print("num_combos,");
		pw.print("point_sum_combos,");
		pw.print("num_knock_cache,");
		pw.print("point_sum_knock_cache,");
		pw.print("num_load_cards,");
		pw.print("point_sum_load_cards,");
		pw.print("turns_taken,");
		// Beta to here
		pw.print("num_nearby_opponent_cards,");
		// Gamma to here
		pw.print("discard_danger\n");
		// Delta to here

		//pw.print("num_set_melds");
		//pw.print("num_run_melds");
		//pw.print("num_set_combos");
		//pw.print("num_run_combos\n");
		// put new features directly above me


		for (int i = 0; i < 1000000; i++) {
			setPlayVerbose(false);

			Collect game = new Collect(new Player(BlackBox.ALPHA, BlackBox.LINEAR), new Player(BlackBox.ALPHA, BlackBox.LINEAR));


			ArrayList<ArrayList<String>> csvOutput = game.getPlayData();

			double gameWinner = Double.parseDouble(csvOutput.get(csvOutput.size() - 1).get(0));

			csvOutput.remove(csvOutput.size() - 1);

			for (ArrayList<String> handData : csvOutput) {
				double handWinner = Double.parseDouble(handData.get(handData.size() - 3).toString());
				double handScore0 = Double.parseDouble(handData.get(handData.size() - 2).toString());
				double handScore1 = Double.parseDouble(handData.get(handData.size() - 1).toString());
				handData.remove(handData.size() - 1);
				handData.remove(handData.size() - 1);
				handData.remove(handData.size() - 1);
				for (String data : handData) {
					double currentPlayer = Double.parseDouble(Arrays.asList(data.split(",")).get(0));
					int comma = data.indexOf(",");
					String calculatedFeatures = data.substring(comma + 1);
					if (handWinner != 0.5) {
						boolean is_current_player_hand_winner = currentPlayer == handWinner;
						boolean is_current_player_game_winner = currentPlayer == gameWinner;
						double current_player_end_hand_score_advantage = currentPlayer == 0 ? (handScore0 - handScore1) : (handScore1 - handScore0);

						String fdata = currentPlayer + ","
													+ (is_current_player_hand_winner ? "1" : "0") + ","
													+ (is_current_player_game_winner ? "1" : "0") + ","
													+ current_player_end_hand_score_advantage + ","
													// add hand scores here
													+ calculatedFeatures;
						pw.println(fdata);
					}

				}

			}
			if (i % 100 == 0) {
				System.out.println(i);
			}
		}

		pw.close();

		System.out.println("done");
	}

}
