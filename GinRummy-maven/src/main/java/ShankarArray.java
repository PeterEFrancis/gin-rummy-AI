
import java.util.ArrayList;
import java.util.Arrays;

import ginrummy.*;

public class ShankarArray {

	// BLANK- unknown card
	// DOT - card in our hand
	// x - safe discard
	// X - oponnent does not have a card
	// v - oponnent might make a meld with that card
	// V - oponnent surely contains a card

	public final int BLANK = 0, DOT = -2, COMMA = -1, x = 1, X = 2, v = 3, V = 4;
	public final int CHARS_OFFSET = 2;
	public final String[] CHARS = {".", ",", " ", "x", "X", "v", "V"};
	public final String RANKS = "A23456789TJQK";
	public final String SUITS = "CHSD";

	int[][] shankarr;


	public ShankarArray() {
		shankarr = new int[4][13];
	}



	public int[] getSurroundings(int rank, int suit) {
		int numOpenAbove = 0;
		int numOpenBelow = 0;
		int numOpenRow = 0;
		int i = 1;
		while (rank - i >= 0 && (shankarr[suit][rank - i] == BLANK || shankarr[suit][rank - i] == v || shankarr[suit][rank - i] == V)) {
			numOpenAbove++;
			i++;
		}
		i = 1;
		while (rank + i < 13 && (shankarr[suit][rank + i] == BLANK || shankarr[suit][rank + i] == v || shankarr[suit][rank + i] == V)) {
			numOpenBelow++;
			i++;
		}
		for (int j = 0; j < 4; j++) {
			if ((shankarr[j][rank] == BLANK || shankarr[j][rank] == v || shankarr[j][rank] == V) && j != suit) {
				numOpenRow++;
			}
		}
		return new int[] {numOpenAbove, numOpenBelow, numOpenRow};
	}



	public int getRunSurroundings(int rank, int suit) {
		int numOpenAbove = 0;
		int numOpenBelow = 0;

		int i = 1;
		while (rank - i >= 0 && (shankarr[suit][rank - i] == BLANK || shankarr[suit][rank - i] == v || shankarr[suit][rank - i] == V)) {
			numOpenAbove++;
			i++;
		}
		i = 1;
		while (rank + i < 13 && (shankarr[suit][rank + i] == BLANK || shankarr[suit][rank + i] == v || shankarr[suit][rank + i] == V)) {
			numOpenBelow++;
			i++;
		}
		return numOpenAbove + numOpenBelow;
	}

	public int getSetSurroundings(int rank, int suit) {
		int numOpenRow = 0;
		for (int j = 0; j < 4; j++) {
			if ((shankarr[j][rank] == BLANK || shankarr[j][rank] == v || shankarr[j][rank] == V) && j != suit) {
				numOpenRow++;
			}
		}
		return numOpenRow;
	}




	public void playerHand(ArrayList<Card> hand) {
		for (Card card : hand) {
			playerDraw(card, false, false);
		}
		plug();
	}


	public void playerDiscard(Card discardCard) {
		int suit = discardCard.getSuit();
		int rank = discardCard.getRank();

		shankarr[suit][rank] = BLANK;

		plug();
	}

	public void playerDraw(Card drawnCard, boolean drewFaceUp) {
		playerDraw(drawnCard, drewFaceUp, true);
	}

	public void playerDraw(Card drawnCard, boolean drewFaceUp, boolean shouldPlug) {
		int suit = drawnCard.getSuit();
		int rank = drawnCard.getRank();

		shankarr[suit][rank] = drewFaceUp ? COMMA : DOT;

		if (shouldPlug) {
			plug();
		}
	}


	public void opponentDiscard(Card discardCard) {
		int suit = discardCard.getSuit();
		int rank = discardCard.getRank();
		shankarr[suit][rank] = X;

		plug();
	}


	public void opponentDrawFaceUpCard(Card faceUpCard, boolean accepted) {
		int suit = faceUpCard.getSuit();
		int rank = faceUpCard.getRank();
		shankarr[suit][rank] = accepted ? V : X;

		plug();
	}


	// uses Shankar's "super induction" to fill in super intuitive
	// info about the opponent hand (only v and x)
	public void plug() {

		for (int suit = 0; suit < 4; suit++) {
			for (int rank = 0; rank < 13; rank++) {
				if (shankarr[suit][rank] == x || shankarr[suit][rank] == v) {
					shankarr[suit][rank] = BLANK;
				}
			}
		}

		boolean passOverAgain = true;
		while (passOverAgain) {
			passOverAgain = false;
			for (int suit = 0; suit < 4; suit++) {
				for (int rank = 0; rank < 13; rank++) {

					int numOpenCol = getRunSurroundings(rank, suit);
					int numOpenRow = getSetSurroundings(rank, suit);


					// if a BLANK cell has fewer than 2 neighbors in both directions,
					//     then it can't be melded, so mark it with an x as a safe discard
					if (shankarr[suit][rank] == BLANK) {
						if (numOpenCol < 2 && numOpenRow < 2) {
							shankarr[suit][rank] = x;
							passOverAgain = true;
						}
					}


					// if a cell was drawn face up (V)
					//     if it is blocked from being in a run and it could be in a set,
					//         assume that it was picked up to use in a set, so mark the other
					//         cards in the potential set (v)
					//     else if it is blocked from being in a set but could be in a run
					//         (and the assumed run is size 3) then mark each card in the potentia meld (v)
					else if (shankarr[suit][rank] == V) {
						// System.out.println("Suit: " + suit +" Rank: " + rank );
						if (numOpenCol < 2 && numOpenRow >= 2) {
							for (int j = 0; j < 4; j++) {
								if (shankarr[j][rank] == BLANK && j != suit) {
									shankarr[j][rank] = v;
									passOverAgain = true;
								}
							}
						} else if (numOpenCol == 2 && numOpenRow < 2) {
							// this "knows" which ones to set, since they are the ones that are counted with num...
							int i = 1;
							while (rank - i >= 0 && (shankarr[suit][rank - i] == BLANK)) {
								shankarr[suit][rank - i] = v;
								i++;
								passOverAgain = true;
							}
							i = 1;
							while (rank + i < 13 && (shankarr[suit][rank + i] == BLANK)) {
								shankarr[suit][rank + i] = v;
								i++;
								passOverAgain = true;
							}
						}
					}


					// if a cell is marked (X), then look at the cards of the same rank
					// (we assume they are not being used for a set, since they were either discarded or rejected)
					// if one of the other cards is blocked from forming a run, mark it (x) as a safe discard
					else if (shankarr[suit][rank] == X || shankarr[suit][rank] == COMMA) {

						// System.out.println("Suit: " + suit + " Rank: " + rank );
						for (int s = 0; s < 4; s++) {
							if (shankarr[s][rank] == BLANK) {
								int numOpenCol_s = getRunSurroundings(rank, s);
								if (numOpenCol_s < 2) {
									shankarr[s][rank] = x;
									passOverAgain = true;
								}
							}
						}
					}

				}
			}

		}

	}




	public String toString() {
		return Arrays.toString(shankarr);
	}

	public String fancyToString() {
		StringBuilder sb = new StringBuilder("+---+---+---+---+---+\n|  ");
		for (int suit = 0; suit < 4; suit ++) {
			sb.append(" | " + "CDHS".charAt(suit));
		}
		sb.append(" |\n+---+---+---+---+---+\n");

		for (int rank = 12; rank >= 0; rank--) {
			sb.append("| " + RANKS.charAt(rank) + " |");
			// for (int suit = 0; suit < 4; suit ++) {
			sb.append(" " + CHARS[CHARS_OFFSET + shankarr[0][rank]] + " |");
			sb.append(" " + CHARS[CHARS_OFFSET + shankarr[3][rank]] + " |");
			sb.append(" " + CHARS[CHARS_OFFSET + shankarr[1][rank]] + " |");
			sb.append(" " + CHARS[CHARS_OFFSET + shankarr[2][rank]] + " |");
			// }
			sb.append("\n+---+---+---+---+---+\n");
		}

		return sb.toString();
	}


	// BLANK = 0, DOT = -2, COMMA = -1, x = 1, X = 2, v = 3, V = 4;
	// Export => (COMMA, DOT) = -2, (x,X)=-1, BLANK = 0, (v,V)=1
	public String export() {
		int[][] copied = new int[4][13];
		for (int i = 0; i < shankarr.length; i++) {
		 	copied[i] = (int[]) shankarr[i].clone();
	 	}
		for (int suit = 0; suit < 4; suit++) {
			for (int rank = 0; rank < 13; rank++) {
				if (shankarr[suit][rank] == COMMA || shankarr[suit][rank] == DOT) {
					copied[suit][rank] = 0;
				} else if (shankarr[suit][rank] == x || shankarr[suit][rank] == X) {
					copied[suit][rank] = 1;
				} else if (shankarr[suit][rank] == BLANK) {
					copied[suit][rank] = 2;
				} else if (shankarr[suit][rank] == v || shankarr[suit][rank] == V) {
					copied[suit][rank] = 3;
				}
			}
		}
		return Arrays.deepToString(copied);
	}


	public static Card c(String cardString) {
		return ginrummy.Card.allCards["A23456789TJQK".indexOf(cardString.charAt(0)) + 13 * ("CHSD".indexOf(cardString.charAt(1)))];
	}



	public static void main(String[] args) {

		ShankarArray sa = new ShankarArray();
		ArrayList<Card> hand = new ArrayList<>();
		hand.add(c("AC"));
		hand.add(c("AH"));
		hand.add(c("5H"));
		hand.add(c("5S"));
		hand.add(c("6C"));
		hand.add(c("8C"));
		hand.add(c("8H"));
		hand.add(c("9H"));
		hand.add(c("JC"));
		hand.add(c("QS"));

		System.out.println("Player's hand: " + hand);
		sa.playerHand(hand);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("KH"));
		sa.opponentDiscard(c("KH"));
		System.out.println(sa.fancyToString());


		System.out.println("Player drew: " + c("KC"));
		sa.playerDraw(c("KC"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("KC"));
		sa.playerDiscard(c("KC"));
		System.out.println(sa.fancyToString());


		System.out.println("Opponent rejected: " + c("KC"));
		sa.opponentDrawFaceUpCard(c("KC"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("JS"));
		sa.opponentDiscard(c("JS"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("9S"));
		sa.playerDraw(c("9S"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("JC"));
		sa.playerDiscard(c("JC"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("JC"));
		sa.opponentDrawFaceUpCard(c("JC"), false);
		System.out.println(sa.fancyToString());


		System.out.println("Opponent discarded: " + c("7H"));
		sa.opponentDiscard(c("7H"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("7H"));
		sa.playerDraw(c("7H"), true, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("QS"));
		sa.playerDiscard(c("QS"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("QS"));
		sa.opponentDrawFaceUpCard(c("QS"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("QC"));
		sa.opponentDiscard(c("QC"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("QH"));
		sa.playerDraw(c("QH"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("QH"));
		sa.playerDiscard(c("QH"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("QH"));
		sa.opponentDrawFaceUpCard(c("QH"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("QD"));
		sa.opponentDiscard(c("QD"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("AD"));
		sa.playerDraw(c("AD"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("9S"));
		sa.playerDiscard(c("9S"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("9S"));
		sa.opponentDrawFaceUpCard(c("9S"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("TH"));
		sa.opponentDiscard(c("TH"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("AS"));
		sa.playerDraw(c("AS"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("8C"));
		sa.playerDiscard(c("8C"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent accepted: " + c("8C"));
		sa.opponentDrawFaceUpCard(c("8C"), true);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("6S"));
		sa.opponentDiscard(c("6S"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("6H"));
		sa.playerDraw(c("6H"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("6C"));
		sa.playerDiscard(c("6C"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("6C"));
		sa.opponentDrawFaceUpCard(c("6C"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("TD"));
		sa.opponentDiscard(c("TD"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("3C"));
		sa.playerDraw(c("3C"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("3C"));
		sa.playerDiscard(c("3C"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent accepted: " + c("3C"));
		sa.opponentDrawFaceUpCard(c("3C"), true);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("4D"));
		sa.opponentDiscard(c("4D"));
		System.out.println(sa.fancyToString());

		System.out.println("Player drew: " + c("7S"));
		sa.playerDraw(c("7S"), false, true);
		System.out.println(sa.fancyToString());

		System.out.println("Player discarded: " + c("7S"));
		sa.playerDiscard(c("7S"));
		System.out.println(sa.fancyToString());

		System.out.println("Opponent rejected: " + c("7S"));
		sa.opponentDrawFaceUpCard(c("7S"), false);
		System.out.println(sa.fancyToString());

		System.out.println("Opponent discarded: " + c("4C"));
		sa.opponentDiscard(c("4C"));
		System.out.println(sa.fancyToString());


		System.out.println(sa.export());

	}


}
