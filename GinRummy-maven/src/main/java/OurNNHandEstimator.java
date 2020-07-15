import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import ginrummy.Card;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class OurNNHandEstimator {

	/* outline
	 *
	 * [prob dist (52)] + [discarded one hot (52)] + [faceup card one hot (52)] + [drew face up ? 1 : -1]
	 *
	 */

	public static final int KNOWN_IN_OPPONENT_HAND = 1;
	public static final int KNOWN_NOT_IN_OPPONENT_HAND = -1;
	public static final int UNKNOWN = 0;

	public double[] probDistribution = new double[52];
	public double[] defProbDistribution = new double[52];
	public Card discardedCard;
	public Card faceUpCard;
	public boolean drewFaceUp;

	public static final int NUM_ENTRIES = 52 * 3 + 1;




	public void initialize(ArrayList<Card> hand, Card firstFaceUpCard) {
		for (int i = 0; i < 52; i++) {
			probDistribution[i] = 10.0 / 42;
		}
		for (Card card : hand) {
			probDistribution[card.getId()] = 0;
			defProbDistribution[card.getId()] = KNOWN_NOT_IN_OPPONENT_HAND;
		}
		faceUpCard = firstFaceUpCard;
	}

	public void opponentDrawDiscard(Card faceUpCard, Card drawnCard, Card discardedCard, boolean drewFaceUp) {
		this.drewFaceUp = drewFaceUp;
		if (this.drewFaceUp) {
			defProbDistribution[drawnCard.getId()] = KNOWN_IN_OPPONENT_HAND;
		}
		this.faceUpCard = faceUpCard;
		this.discardedCard = discardedCard;
		defProbDistribution[discardedCard.getId()] = KNOWN_NOT_IN_OPPONENT_HAND;

		// remake the probDistribution
		networkStep();
	}

	public void reportPlayerDecision(Card faceUpCard, Card drawnCard, Card discardedCard) {
		// whether we pick face up or not, they dont have the face up card
		defProbDistribution[faceUpCard.getId()] = KNOWN_NOT_IN_OPPONENT_HAND;
		if (drawnCard != faceUpCard) {
			defProbDistribution[drawnCard.getId()] = KNOWN_NOT_IN_OPPONENT_HAND;
		}
		this.faceUpCard = discardedCard;
	}

	public void networkStep() {
		// take the current meta array, and use the NN to find the new `probDistribution`

		// get ndarray input
		INDArray fullInput = Nd4j.createFromArray(new double[][] {getMetaArray()});

		// run the keras model
		INDArray out = CompareHERatings.model.output(fullInput);

		// set the result
		probDistribution = out.toDoubleVector();

		// Runtime rt = Runtime.getRuntime();
		// Process pr;
		// try {
		// 	// ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/network_step.py"); /*Or python3*/
		// 	// Process process = pb.start();
		// 	pr = rt.exec("/usr/local/bin/python3 /Users/peterfrancis/Programming/gin-rummy-AI/python/network_step.py " + Arrays.toString(getMetaArray()).replace(" ", ""));
		//
		// 	BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		//  	BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		//
		//  	String s = null;
		// 	int i = 0;
		// 	while ((s = stdInput.readLine()) != null) {
		// 		probDistribution[i++] = Double.parseDouble(s);
		// 	}
		// 	while ((s = stdError.readLine()) != null) {
		// 		System.err.println(s);
		// 	}
		//
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }
	}


	public double[] getMetaArray() {
		double[] meta = new double[NUM_ENTRIES];

		// prob distribution
		for (int i = 0; i < 52; i++) {
			meta[i] = getProb(i);
		}

		// discarded
		if (discardedCard != null) {
			meta[52 + discardedCard.getId()] = 1;
		}

		if (faceUpCard != null) {
			// faceUp
			meta[52 * 2 + faceUpCard.getId()] = 1;

			// drew faceup
			meta[52 * 3] = drewFaceUp ? 1 : -1;
		}



		return meta;
	}

	public String toString() {
		return Arrays.toString(getMetaArray());
	}

	public String getPrintProb(int c) {
		if (defProbDistribution[c] == KNOWN_IN_OPPONENT_HAND) {
			return "TRUE";
		} else if (defProbDistribution[c] == KNOWN_NOT_IN_OPPONENT_HAND) {
			return "FALSE";
		} else {
			return probDistribution[c] + "";
		}
	}

	public void print() {
		for (int k = 0; k < 7 * 13 + 3 + 5; k++) {
			System.out.print("*");
		}
		System.out.println();
		System.out.print("*     ");
		for (int j = 0; j < 13; j++) {
			System.out.print("A23456789TJQK".charAt(j) + "      ");
		}
		System.out.println(" *");
		for (int i = 0; i < 4; i++) {
			System.out.print("*  " + "CHSD".charAt(i) +  "  ");
			for (int j = 0; j < 13; j++) {
				System.out.print((getPrintProb(13 * i + j) + "             ").substring(0, 5) + "  ");
			}
			System.out.println(" *");
		}
		for (int k = 0; k < 7 * 13 + 3 + 5; k++) {
			System.out.print("*");
		}
		System.out.println();
	}

	public double getProb(Card c) {
		return getProb(c.getId());
	}

	public double getProb(int c) {
		if (defProbDistribution[c] == KNOWN_IN_OPPONENT_HAND) {
			return 1;
		} else if (defProbDistribution[c] == KNOWN_NOT_IN_OPPONENT_HAND) {
			return 0;
		} else {
			return probDistribution[c];
		}
	}




	public static Card c(String cardString) {
		return ginrummy.Card.allCards["A23456789TJQK".indexOf(cardString.charAt(0)) + 13 * ("CHSD".indexOf(cardString.charAt(1)))];
	}

	public static void main(String[] args) {

		OurNNHandEstimator nn = new OurNNHandEstimator();

		ArrayList<Card> hand = new ArrayList<Card>();
		hand.add(c("AC"));
		hand.add(c("2C"));
		hand.add(c("3C"));
		hand.add(c("4C"));
		hand.add(c("5C"));
		hand.add(c("6C"));
		hand.add(c("7C"));
		hand.add(c("8C"));
		hand.add(c("9C"));
		hand.add(c("TC"));

		nn.initialize(hand, c("JH"));

		System.out.println(nn);

		nn.opponentDrawDiscard(c("JC"), c("JC"), c("AC"), false);

		System.out.println(nn);


	}


}
