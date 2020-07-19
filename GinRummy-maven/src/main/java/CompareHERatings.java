import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import ginrummy.Card;
import ginrummy.GinRummyPlayer;
import ginrummy.GinRummyUtil;
import ginrummy.SimpleGinRummyPlayer;


public class CompareHERatings {

	private static boolean playVerbose = false;
	private static boolean saveData = false;
	private static boolean reportEvaluation = false;
	private static boolean doOurNNHE = true;
	private static boolean doToddHE = true;
	private static boolean doCNNHE = true;

	private static int startSaving = 0;

	private static int testTurn = -1;


	private static ArrayList<ArrayList<ArrayList<Double>>> nnhe_evaluationData = new ArrayList<ArrayList<ArrayList<Double>>>();
	static {
		for (int p = 0; p < 2; p++) {
			ArrayList<ArrayList<Double>> pal = new ArrayList<ArrayList<Double>>();
			for (int z = 0; z < 5; z++) {
				pal.add(new ArrayList<Double>());
			}
			nnhe_evaluationData.add(pal);
		}
	}

	private static ArrayList<ArrayList<ArrayList<Double>>> todd_evaluationData = new ArrayList<ArrayList<ArrayList<Double>>>();
	static {
		for (int p = 0; p < 2; p++) {
			ArrayList<ArrayList<Double>> pal = new ArrayList<ArrayList<Double>>();
			for (int z = 0; z < 5; z++) {
				pal.add(new ArrayList<Double>());
			}
			todd_evaluationData.add(pal);
		}
	}

	private static ArrayList<ArrayList<ArrayList<Double>>> cnn_evaluationData = new ArrayList<ArrayList<ArrayList<Double>>>();
	static {
		for (int p = 0; p < 2; p++) {
			ArrayList<ArrayList<Double>> pal = new ArrayList<ArrayList<Double>>();
			for (int z = 0; z < 5; z++) {
				pal.add(new ArrayList<Double>());
			}
			cnn_evaluationData.add(pal);
		}
	}

	private static final Random RANDOM = new Random();
	private static final int HAND_SIZE = 10;
	private GinRummyPlayer[] players;
	private File he_file = new File("data-he.csv");
	private File ph_file = new File("data-ph.csv");

	private OurNNHandEstimator[] nn_hand_estimators = new OurNNHandEstimator[2];
	private HandEstimator[] todd_hand_estimators = new HandEstimator[2];
	private ShankarArray[] shankar_arrays = new ShankarArray[2];

	// KERAS Import
	public static String keras_file = "";
	public static MultiLayerNetwork nnhe_model;

	static {
		try {
			// Sequential Model SetUp
			keras_file = "regression_models/nnhe-simple-3.h5";
			// Scanner scanner = new Scanner(new File(simpleMlp));
			nnhe_model = KerasModelImport.importKerasSequentialModelAndWeights(keras_file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// KERAS CNN Import
	public static String cnn_keras_file = "";
	public static ComputationGraph cnn_model;

	static {
		try {
			// Sequential Model SetUp
			cnn_keras_file = "regression_models/f16-128-e10b50-lr01d01-200.h5";
			// Scanner scanner = new Scanner(new File(simpleMlp));
			cnn_model = KerasModelImport.importKerasModelAndWeights(cnn_keras_file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setPlayVerbose(boolean playVerbose) {
		CompareHERatings.playVerbose = playVerbose;
	}

	public CompareHERatings(GinRummyPlayer player0, GinRummyPlayer player1) {
		players = new GinRummyPlayer[] {player0, player1};
	}

	@SuppressWarnings("unchecked")
	public int play() {
		int[] scores = new int[2];
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		hands.add(new ArrayList<Card>());
		hands.add(new ArrayList<Card>());
		int startingPlayer = RANDOM.nextInt(2);


		while (scores[0] < GinRummyUtil.GOAL_SCORE && scores[1] < GinRummyUtil.GOAL_SCORE) { // while game not over
			int currentPlayer = startingPlayer;
			int opponent = (currentPlayer == 0) ? 1 : 0;

			// get shuffled deck and deal cards
			Stack<Card> deck = Card.getShuffle(RANDOM.nextInt());
			hands.get(0).clear();
			hands.get(1).clear();
			for (int i = 0; i < 2 * HAND_SIZE; i++){
				hands.get(i % 2).add(deck.pop());
			}

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


			for (int i = 0; i < 2; i++) {
				if (doOurNNHE) {
					nn_hand_estimators[i] = new OurNNHandEstimator();
					nn_hand_estimators[i].initialize(hands.get(i), firstFaceUpCard);
				}
				if (doToddHE) {
					todd_hand_estimators[i] = new HandEstimator();
					todd_hand_estimators[i].init();
					todd_hand_estimators[i].setKnown(hands.get(i), false);
				}
				if (doCNNHE) {
					shankar_arrays[i] = new ShankarArray();
					shankar_arrays[i].playerHand(hands.get(i));
				}
			}



			while (deck.size() > 2) { // while the deck has more than two cards remaining, play round
				if (playVerbose)
					System.out.println("----------Turns Taken: " + turnsTaken);

				// DRAW
				boolean drawFaceUp = false;
				Card faceUpCard = discards.peek();
				// offer draw face-up iff not 3rd turn with first face up card (decline automatically in that case)
				if (!(turnsTaken == 2 && faceUpCard == firstFaceUpCard)) { // both players declined and 1st player must draw face down
					drawFaceUp = players[currentPlayer].willDrawFaceUpCard(faceUpCard);
					if (playVerbose && !drawFaceUp && faceUpCard == firstFaceUpCard && turnsTaken < 2) {
						System.out.printf("Player %d declines %s.\n", currentPlayer, firstFaceUpCard);
						// if (turnsTaken == 1) {
						// 	players[opponent].opponentDrawDiscard(firstFaceUpCard, firstFaceUpCard, firstFaceUpCard, false);
						// }
					}
				}

				if (!(!drawFaceUp && turnsTaken < 2 && faceUpCard == firstFaceUpCard)) { // continue with turn if not initial declined option
					Card drawCard = drawFaceUp ? discards.pop() : deck.pop();
					for (int i = 0; i < 2; i++) {
						players[i].reportDraw(currentPlayer, (i == currentPlayer || drawFaceUp) ? drawCard : null);
					}


					if (playVerbose)
						System.out.printf("Player %d draws %s.\n", currentPlayer, drawCard);
					hands.get(currentPlayer).add(drawCard);

					// DISCARD
					Card discardCard = players[currentPlayer].getDiscard();
					if (!hands.get(currentPlayer).contains(discardCard) || discardCard == faceUpCard) {
						if (playVerbose)
							System.out.printf("Player %d discards %s illegally and forfeits.\n", currentPlayer, discardCard);
						return opponent;
					}
					hands.get(currentPlayer).remove(discardCard);
					for (int i = 0; i < 2; i++)
						players[i].reportDiscard(currentPlayer, discardCard);


					// REPORT TO ESTIMATOR
					if (faceUpCard != null) {
						if (doOurNNHE) {
							nn_hand_estimators[currentPlayer].reportPlayerDecision(faceUpCard, drawCard, discardCard);
							nn_hand_estimators[opponent].opponentDrawDiscard(faceUpCard, drawCard, discardCard, drawFaceUp);
						}
						if (doToddHE) {
							todd_hand_estimators[currentPlayer].setKnown(faceUpCard, false);
							todd_hand_estimators[currentPlayer].setKnown(drawCard, false);
							todd_hand_estimators[opponent].reportDrawDiscard(faceUpCard, faceUpCard == drawCard, discardCard);
						}
						if (doCNNHE) {
							shankar_arrays[currentPlayer].playerDraw(drawCard, faceUpCard == drawCard);
							shankar_arrays[currentPlayer].playerDiscard(discardCard);
							shankar_arrays[opponent].opponentDrawFaceUpCard(faceUpCard, faceUpCard == drawCard);
							shankar_arrays[opponent].opponentDiscard(discardCard);
						}
					}

					if (playVerbose) {
						if (doOurNNHE) {
							System.out.println("Our NN Hand Estimator:");
							System.out.println("From Player " + currentPlayer + "'s perspective:");
							nn_hand_estimators[currentPlayer].print();
							System.out.println("From Player " + opponent + "'s perspective:");
							nn_hand_estimators[opponent].print();
						}
						if (doToddHE) {
							System.out.println("Todd's Hand Estimator:");
							System.out.println("From Player " + currentPlayer + "'s perspective:");
							todd_hand_estimators[currentPlayer].print();
							System.out.println("From Player " + opponent + "'s perspective:");
							todd_hand_estimators[opponent].print();
						}
						if (doCNNHE) {
							System.out.println("Todd's Hand Estimator:");
							System.out.println("From Player " + currentPlayer + "'s perspective:");
							System.out.println(Arrays.toString(shankar_arrays[currentPlayer].shankarr));
							System.out.println("From Player " + opponent + "'s perspective:");
							System.out.println(Arrays.toString(shankar_arrays[opponent].shankarr));
						}
					}


					if (saveData && turnsTaken >= startSaving) {
						try {
							FileWriter he_pw = new FileWriter(he_file, true);
							he_pw.append(nn_hand_estimators[0] + "\n");
							he_pw.append(nn_hand_estimators[1] + "\n");
							he_pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}

						try {
							FileWriter ph_pw = new FileWriter(ph_file, true);
							ph_pw.append(Arrays.deepToString(OurUtilities.handTo2DBitArray(hands.get(1))) + "\n");
							ph_pw.append(Arrays.deepToString(OurUtilities.handTo2DBitArray(hands.get(0))) + "\n");
							ph_pw.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					if (reportEvaluation){
						if (turnsTaken == testTurn || testTurn == -1) {
							if (doOurNNHE) {
								double[] reported0 = HandEstimationEvaluation.report(OurUtilities.handTo1DArray(hands.get(1)), nn_hand_estimators[0].probDistribution);
								double[] reported1 = HandEstimationEvaluation.report(OurUtilities.handTo1DArray(hands.get(0)), nn_hand_estimators[1].probDistribution);
								for (int z = 0; z < reported0.length; z++) {
									nnhe_evaluationData.get(0).get(z).add(reported0[z]);
									nnhe_evaluationData.get(1).get(z).add(reported1[z]);
								}
							}
							if (doToddHE) {
								double[] todd_reported0 = HandEstimationEvaluation.report(OurUtilities.handTo1DArray(hands.get(1)), todd_hand_estimators[0].prob);
								double[] todd_reported1 = HandEstimationEvaluation.report(OurUtilities.handTo1DArray(hands.get(0)), todd_hand_estimators[1].prob);
								for (int z = 0; z < todd_reported0.length; z++) {
									todd_evaluationData.get(0).get(z).add(todd_reported0[z]);
									todd_evaluationData.get(1).get(z).add(todd_reported1[z]);
								}
							}
							if (doCNNHE) {
								double[] hand0 = OurUtilities.handTo1DArray(hands.get(0));
								double[] hand1 = OurUtilities.handTo1DArray(hands.get(1));

								int[][] shankar0 = OurUtilities.getShankarMatrix(shankar_arrays[0].copyToExport());


								int[][][][] cardMatrix4d = new int[1][17][13][1];
								for (int i = 0; i < 17; i++) {

									int[][] beforeTranspose = new int[][] {shankar0[i]};

									int m = beforeTranspose.length;
									int n = beforeTranspose[0].length;

									int[][] afterTranspose = new int[n][m];

									for (int a = 0; a < n; a++) {
										for (int b = 0; b < m; b++) {
											afterTranspose[a][b] = beforeTranspose[b][a];
										}
									}

									cardMatrix4d[0][i] = afterTranspose;
								}
								INDArray shankar_0_4d = Nd4j.createFromArray(cardMatrix4d);
								INDArray[] out0 = cnn_model.output(shankar_0_4d);
								double[] cnn_reported0 = HandEstimationEvaluation.report(hand1, out0[0].toDoubleVector());


								int[][] shankar1 = OurUtilities.getShankarMatrix(shankar_arrays[1].copyToExport());
								cardMatrix4d = new int[1][17][13][1];
								for (int i = 0; i < 17; i++) {

									int[][] beforeTranspose = new int[][] {shankar1[i]};

									int m = beforeTranspose.length;
									int n = beforeTranspose[0].length;

									int[][] afterTranspose = new int[n][m];

									for (int a = 0; a < n; a++) {
										for (int b = 0; b < m; b++) {
											afterTranspose[a][b] = beforeTranspose[b][a];
										}
									}

									cardMatrix4d[0][i] = afterTranspose;
								}
								INDArray shankar_1_4d = Nd4j.createFromArray(cardMatrix4d);
								INDArray[] out1 = cnn_model.output(shankar_1_4d);
									// System.out.println(out1[0]);
								double[] cnn_reported1 = HandEstimationEvaluation.report(hand0, out1[0].toDoubleVector());
								for (int z = 0; z < cnn_reported1.length; z++) {
									cnn_evaluationData.get(0).get(z).add(cnn_reported0[z]);
									cnn_evaluationData.get(1).get(z).add(cnn_reported1[z]);
								}
							}
						}
					}


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

					// CHECK FOR KNOCK
					knockMelds = players[currentPlayer].getFinalMelds();
					if (knockMelds != null)
						break; // player knocked; end of round
				}

				turnsTaken++;
				currentPlayer = (currentPlayer == 0) ? 1 : 0;
				opponent = (currentPlayer == 0) ? 1 : 0;
			}

			if (knockMelds != null) { // round didn't end due to non-knocking and 2 cards remaining in draw pile
				// check legality of knocking meld
				long handBitstring = GinRummyUtil.cardsToBitstring(hands.get(currentPlayer));
				long unmelded = handBitstring;
				for (ArrayList<Card> meld : knockMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & unmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose)
							System.out.printf("Player %d melds %s illegally and forfeits.\n", currentPlayer, knockMelds);
						return opponent;
					}
					unmelded &= ~meldBitstring; // remove successfully melded cards from
				}
				// compute knocking deadwood
				int knockingDeadwood = GinRummyUtil.getDeadwoodPoints(knockMelds, hands.get(currentPlayer));
				if (knockingDeadwood > GinRummyUtil.MAX_DEADWOOD) {
					if (playVerbose)
						System.out.printf("Player %d melds %s with greater than %d deadwood and forfeits.\n", currentPlayer, knockMelds, knockingDeadwood);
					return opponent;
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

				// get opponent meld
				ArrayList<ArrayList<Card>> opponentMelds = players[opponent].getFinalMelds();
				for (ArrayList<Card> meld : opponentMelds)
					meldsCopy.add((ArrayList<Card>) meld.clone());
				meldsCopy = new ArrayList<ArrayList<Card>>();
				for (int i = 0; i < 2; i++)
					players[i].reportFinalMelds(opponent, meldsCopy);

				// check legality of opponent meld
				long opponentHandBitstring = GinRummyUtil.cardsToBitstring(hands.get(opponent));
				long opponentUnmelded = opponentHandBitstring;
				for (ArrayList<Card> meld : opponentMelds) {
					long meldBitstring = GinRummyUtil.cardsToBitstring(meld);
					if (!GinRummyUtil.getAllMeldBitstrings().contains(meldBitstring) // non-meld ...
							|| (meldBitstring & opponentUnmelded) != meldBitstring) { // ... or meld not in hand
						if (playVerbose)
							System.out.printf("Player %d melds %s illegally and forfeits.\n", opponent, opponentMelds);
						return currentPlayer;
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
					if (playVerbose)
						System.out.printf("Player %d scores the gin bonus of %d plus opponent deadwood %d for %d total points.\n", currentPlayer, GinRummyUtil.GIN_BONUS, opponentDeadwood, GinRummyUtil.GIN_BONUS + opponentDeadwood);
				}
				else if (knockingDeadwood < opponentDeadwood) { // non-gin round win
					scores[currentPlayer] += opponentDeadwood - knockingDeadwood;
					if (playVerbose)
						System.out.printf("Player %d scores the deadwood difference of %d.\n", currentPlayer, opponentDeadwood - knockingDeadwood);
				}
				else { // undercut win for opponent
					scores[opponent] += GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood;
					if (playVerbose)
						System.out.printf("Player %d undercuts and scores the undercut bonus of %d plus deadwood difference of %d for %d total points.\n", opponent, GinRummyUtil.UNDERCUT_BONUS, knockingDeadwood - opponentDeadwood, GinRummyUtil.UNDERCUT_BONUS + knockingDeadwood - opponentDeadwood);
				}
				startingPlayer = (startingPlayer == 0) ? 1 : 0; // starting player alternates
			}
			else { // If the round ends due to a two card draw pile with no knocking, the round is cancelled.
				if (playVerbose)
					System.out.println("The draw pile was reduced to two cards without knocking, so the hand is cancelled.");
			}

			// score reporting
			if (playVerbose)
				System.out.printf("Player\tScore\n0\t%d\n1\t%d\n", scores[0], scores[1]);
			for (int i = 0; i < 2; i++)
				players[i].reportScores(scores.clone());

		}
		if (playVerbose)
			System.out.printf("Player %s wins.\n", scores[0] > scores[1] ? 0 : 1);


		return scores[0] >= GinRummyUtil.GOAL_SCORE ? 0 : 1;
	}




	/**
	 * Test and demonstrate the use of the GinRummyGame class.
	 * @param args (unused)
	 */
	public static void main(String[] args) {

		CompareHERatings.playVerbose = false;
		CompareHERatings.saveData = false;
		CompareHERatings.reportEvaluation = true;

		CompareHERatings.doOurNNHE = true;
		CompareHERatings.doToddHE = true;
		CompareHERatings.doCNNHE = true;

		CompareHERatings.startSaving = 0;
		CompareHERatings.testTurn = 20;

		int numGames = 1000;
		int countStep = 100;

		int numP1Wins = 0;

		GinRummyPlayer player0 = new SimpleGinRummyPlayer();
		GinRummyPlayer player1 = new SimpleGinRummyPlayer();

		CompareHERatings game = new CompareHERatings(player0, player1);

		long startMs = System.currentTimeMillis();
		for (int i = 0; i < numGames; i++) {
			if (i % countStep == 0 && i != 0) {
				System.out.println(i);
				// System.out.printf("Games Won: P0:%d, P1:%d.\n", i - numP1Wins, numP1Wins);
			}
			numP1Wins += game.play();
		}
		long totalMs = System.currentTimeMillis() - startMs;

		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Player 0: " + player0.getClass().getName());
		System.out.println("Player 1: " + player1.getClass().getName());
		System.out.println();

		System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
		System.out.printf("Games Won: P0:%d, P1:%d.\n", numGames - numP1Wins, numP1Wins);
		System.out.println();

		System.out.println(CompareHERatings.saveData ? "Started saving data at turn " + CompareHERatings.startSaving : "Did not save data");
		System.out.println();

		if (CompareHERatings.reportEvaluation) {

			String[] heFeatures = new String[] {"mean_squared_difference", "probabilistic_integrity", "top_n_cards", "minimum_top_largest", "drift_area"};

			ArrayList<String> estimatorNames = new ArrayList<>();
			estimatorNames.add("Our NN Hand Estimator");
			estimatorNames.add("Todd's Hand Estimator");
			estimatorNames.add("Shankarr CNN Hand Estimation");

			ArrayList<ArrayList<ArrayList<ArrayList<Double>>>> evalData = new ArrayList<ArrayList<ArrayList<ArrayList<Double>>>>();
			evalData.add(nnhe_evaluationData);
			evalData.add(todd_evaluationData);
			evalData.add(cnn_evaluationData);

			ArrayList<Boolean> doFlags = new ArrayList<>();
			doFlags.add(doOurNNHE);
			doFlags.add(doToddHE);
			doFlags.add(doCNNHE);

			System.out.println(CompareHERatings.testTurn != -1 ? "Analysis only at turn " + CompareHERatings.testTurn : "analysis at every turn");
			System.out.println("\t=> number of data points: " + evalData.get(0).get(0).get(0).size());
			System.out.println();

			System.out.println("Analysis results:");

			for (int i = 0; i < doFlags.size(); i++) {
				if (doFlags.get(i)) {
					System.out.println("\t" + estimatorNames.get(i) + ":");
					for (int z = 0; z < heFeatures.length; z++) {
						System.out.println("\t\t" + heFeatures[z]);
						for (int p = 0; p < 2; p++) {
							double avg = 0;
							for (int y = 0; y < evalData.get(i).get(p).get(z).size(); y++) {
								avg += evalData.get(i).get(p).get(z).get(y);
							}
							avg /= evalData.get(i).get(p).get(z).size();
							double stdev = 0;
							for (int y = 0; y < evalData.get(i).get(p).get(z).size(); y++) {
								stdev += Math.pow((evalData.get(i).get(p).get(z).get(y) - avg), 2);
							}
							stdev = Math.sqrt(stdev / (evalData.get(i).get(p).get(z).size() - 1));
							System.out.printf("\t\t\tPlayer %d: %f +/- %f\n", p, avg, stdev  / Math.sqrt(evalData.get(i).get(p).get(z).size()));
						}

					}
					System.out.println();
				}
			}

		}
	}

}
