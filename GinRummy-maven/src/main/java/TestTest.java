import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import ginrummy.Card;

public class TestTest {

	public static void main(String[] args) {
		int[][][] heldVisits = null;
		int[][][][] heldCounts = null;
		String filename = "SpecificHandEst1-10.dat";
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


		int faceUpCard = 12;
		int drawnCard = 52;
		int discardCard = 9;
		
		double denom = heldVisits[faceUpCard][drawnCard][discardCard];

		System.out.printf("FaceUpCard: \t%d (%s)\n", faceUpCard, Card.allCards[faceUpCard]);
		System.out.printf("drawnCard: \t%d (%s)\n", drawnCard, drawnCard == 52 ? "--" : Card.allCards[drawnCard]);
		System.out.printf("discardCard: \t%d (%s)\n", discardCard, Card.allCards[discardCard]);
		System.out.printf("times visited: \t%f\n", denom);

		System.out.println("---------------------------------");
		System.out.println("---------------------------------");
		System.out.println("frequency of cards in hand: " + Arrays.toString(heldCounts[faceUpCard][drawnCard][discardCard]));
		System.out.println("---------------------------------");

		int[] cardsInHand = new int[] {3, 4};

		for (int card : cardsInHand){
			double num = heldCounts[faceUpCard][drawnCard][discardCard][card];

			System.out.printf("cardInHand: \t%d (%s)\n", card, Card.allCards[card]);

			System.out.printf("(sub)times visited: \t%f\n", num);
			System.out.printf("prob: \t%f\n", num/denom);
			System.out.println("---------------------------------");
		}


	}



}
