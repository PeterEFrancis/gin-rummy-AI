import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ginrummy.*;

public class SpecificCardHandEstimator {
	private static final int HAND_SIZE = 10;
	private static final int MIN_VISITS = 50;
	private static final double EPS = .1 / MIN_VISITS; // to make non-events rare but not impossible
	int[][][] heldVisits = new int[52][53][52];       // turn number, faceup card, drawn card (52 for face down), discarded card
	int[][][][] heldCounts = new int[52][53][52][52]; // turn number, faceup card, drawn card (52 for face down), discarded card, hand card

	boolean[] known = new boolean[Card.NUM_CARDS];
	double[] prob = new double[Card.NUM_CARDS];
	int numUnknownInHand = HAND_SIZE;

	public SpecificCardHandEstimator(SpecificCardGinRummyDataCollector collector) {
		heldVisits = collector.heldVisits;
		heldCounts = collector.heldCounts;
	}

	public SpecificCardHandEstimator(String filename) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			heldVisits = (int[][][]) in.readObject();
			heldCounts = (int[][][][]) in.readObject();
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
//		System.out.println(Arrays.deepToString(heldCounts));
		for (int[][] arr : heldVisits) {
			System.out.println(Arrays.deepToString(arr));
		}
//		System.out.println(Arrays.deepToString(heldVisits));
//		System.out.println(heldVisits[0][0][4]);
	}

	public SpecificCardHandEstimator() {
		this("SpecificHandEst1-10500000.dat");
	}

	public void init() {
		known = new boolean[Card.NUM_CARDS];
		prob = new double[Card.NUM_CARDS];
		int numUnknown = 0;
		for (int i = 0; i < prob.length; i++)
			if (!known[i])
				numUnknown++;
		for (int i = 0; i < prob.length; i++)
			prob[i] = (double) numUnknownInHand / numUnknown;
	}

	public void save(String filename) {
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(heldVisits);
			out.writeObject(heldCounts);
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void print() {
		System.out.print("Rank");
		for (int i = 0; i < Card.NUM_RANKS; i++)
			System.out.print("\t" + Card.rankNames[i]);
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			if (i % Card.NUM_RANKS == 0)
				System.out.printf("\n%s", Card.suitNames[i / Card.NUM_RANKS]);
			System.out.print("\t");
			if (known[i])
				System.out.print(prob[i] == 1 ? "*TRUE*" : "FALSE");
			else
				System.out.printf("%.4f", prob[i]);
		}
		System.out.println();
	}

	public void setKnown(Card card, boolean held) {
		ArrayList<Card> cards = new ArrayList<Card>();
		cards.add(card);
		setKnown(cards, held);
	}

	public void setKnown(ArrayList<Card> cards, boolean held) {
		for (Card card : cards) {
			known[card.getId()] = true;
			prob[card.getId()] = held ? 1 : 0;
		}
	}

	public void reportDrawDiscard(int turnsTaken, Card faceUpCard, boolean faceUpDrawn, Card discardCard) {
		// System.out.printf("*** %s %s %s\n", faceUpCard, faceUpDrawn, discardCard);
		int faceUpCardId = faceUpCard.getId();
		int discardCardId = discardCard.getId();

		known[faceUpCardId] = true;
		prob[faceUpCardId] = faceUpDrawn ? 1 : 0;
		known[discardCardId] = true;
		prob[discardCardId] = 0;

		double denom = heldVisits[faceUpCardId][faceUpDrawn ? faceUpCardId : 52][discardCardId];
		for (int i = 0; i < 52; i++) {
			if (!known[i]){
				double new_val = 0;
				if (denom == 0) {
					new_val = ((turnsTaken - 2) + 10) / 42.0; // prob[i];
				} else {
					double num = heldCounts[faceUpCardId][faceUpDrawn ? faceUpCardId : 52][discardCardId][i];
					new_val =  num / denom;
					System.out.println("fuc: " + Card.allCards[faceUpCardId] + "\tdrawn: " + faceUpDrawn + "\tdiscard: " + Card.allCards[discardCardId] + "\ti: " + Card.allCards[i] + "\tnum: " + num + "\tdenom:" + denom  + "\tfrac: " + (num/denom));
				}
				prob[i] = (prob[i] * (turnsTaken) + new_val) / (turnsTaken + 1);
			}
		}

	}

	public void test() {
		init();
		print();
	}


	public static void main(String[] args) {
		new SpecificCardHandEstimator().test();
	}

}
