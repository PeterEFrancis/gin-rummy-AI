import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;

import ginrummy.*;




public class SpecificCardGinRummyDataCollector {
	private static final Random RANDOM = new Random();
	private static final int HAND_SIZE = 10;
	private static boolean playVerbose = false;
	private GinRummyPlayer[] players = new GinRummyPlayer[2];

	final int DRAW_FACE_UP = 1, DRAW_FACE_DOWN = 0, UNSUITED = 0, SUITED_WITH_FACE_UP = 1, SUITED_WITH_DISCARD = 2;

	int[][][] heldVisits = new int[52][53][52];       // faceup card, drawn card (52 for face down), discarded card
	int[][][][] heldCounts = new int[52][53][52][52]; // faceup card, drawn card (52 for face down), discarded card, hand card


	public void collectData(Card faceUpCard, Card drawCard, Card discardCard, ArrayList<Card> opponentHand) {

		int faceUpCardId = faceUpCard.getId();
		int drawCardId = drawCard == null ? 52 : drawCard.getId();
		int discardCardId = discardCard.getId();

		heldVisits[faceUpCardId][drawCardId][discardCardId]++;
		for (Card c : opponentHand)
			heldCounts[faceUpCardId][drawCardId][discardCardId][c.getId()]++;

	}



	public static void setPlayVerbose(boolean playVerbose) {
		SpecificCardGinRummyDataCollector.playVerbose = playVerbose;
	}


	@SuppressWarnings("unchecked")
	public int play(int startingPlayer) {
		int[] scores = new int[2];
		ArrayList<ArrayList<Card>> hands = new ArrayList<ArrayList<Card>>();
		hands.add(new ArrayList<Card>());
		hands.add(new ArrayList<Card>());
		// int startingPlayer = RANDOM.nextInt(2);

		while (scores[0] < GinRummyUtil.GOAL_SCORE && scores[1] < GinRummyUtil.GOAL_SCORE) { // while game not over
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
					if (playVerbose)
						System.out.printf("Player %d discards %s.\n", currentPlayer, discardCard);
					discards.push(discardCard);
					collectData(faceUpCard, drawFaceUp ? drawCard : null, discardCard, hands.get(opponent));
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

	private void match(String player1Name, String player2Name, int numGames) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException, UnsupportedEncodingException {
		String filename = "match-" + numGames + "-" + player1Name + "-" + player2Name + ".txt";
		PrintWriter writer = new PrintWriter(filename, "UTF-8");

		writer.printf("Match %s vs. %s (%d games) ... ", player1Name, player2Name, numGames);
		writer.flush();
		@SuppressWarnings("unchecked")
		Class<GinRummyPlayer> playerClass1 = (Class<GinRummyPlayer>) Class.forName(player1Name);
		players[0] = playerClass1.newInstance();
		@SuppressWarnings("unchecked")
		Class<GinRummyPlayer> playerClass2 = (Class<GinRummyPlayer>) Class.forName(player2Name);
		players[1] = playerClass2.newInstance();
		long startMs = System.currentTimeMillis();
		int numP1Wins = 0;
		for (int i = 0; i < numGames; i++) {
			numP1Wins += play(i % 2);
		}
		long totalMs = System.currentTimeMillis() - startMs;
		writer.printf("%d games played in %d ms.\n", numGames, totalMs);
		writer.printf("Games Won: %s:%d, %s:%d.\n", player1Name, numGames - numP1Wins, player2Name, numP1Wins);
		System.out.printf("%d games played in %d ms.\n", numGames, totalMs);
		System.out.printf("Games Won: %s:%d, %s:%d.\n", player1Name, numGames - numP1Wins, player2Name, numP1Wins);
		writer.close();
	}


	/**
	 * Test and demonstrate the use of the GinRummyGame class.
	 * @param args (unused)
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException, FileNotFoundException, UnsupportedEncodingException {
		String[] playerNames = {"OurSimpleGinRummyPlayer", "SimpleGinRummyPlayer2"};
		int index = 0;
		int index1 = 0, index2 = 0;
		for (int i = 0; i < playerNames.length && index >= 0; i++)
			for (int j = i + 1; j < playerNames.length && index >= 0; j++) {
				if (index == 0) {
					index1 = i;
					index2 = j;
				}
				index--;
			}
		setPlayVerbose(false);
		int numGames = 5000000;
		SpecificCardGinRummyDataCollector collector = new SpecificCardGinRummyDataCollector();
		collector.match(playerNames[index1], playerNames[index2], numGames);
//		collector.displayData();
		SpecificCardHandEstimator handEst = new SpecificCardHandEstimator(collector);
		handEst.save("SpecificHandEst1-" + numGames + ".dat");
		handEst = new SpecificCardHandEstimator("SpecificHandEst1-" + numGames + ".dat");
//		handEst.test();
	}
}
