import java.util.Arrays;
import java.util.LinkedList;


public class HandEstimationEvaluation {

	public static double mean_squared_difference(double[] actualHand, double[] handProbEstimation) {
		double s = 0;
		for (int i = 0; i < 52; i++) {
			s += Math.pow(actualHand[i] - handProbEstimation[i], 2);
		}
		return Math.sqrt(s);
	}

	public static double probabilistic_integrity(double[] handProbEstimation) {
		// closeness of sum of probabilities to 10
		double s = 0;
		for (double d : handProbEstimation) {
			s += d;
		}
		return Math.abs(10 - s) / 10;
	}

	public static int top_n_cards(int n, double[] actualHand, double[] handProbEstimation) {
		// count how many out of n top probabilities in handProbEstimation are cards in the actualHand
		LinkedList<Integer> topNCardsIndices = new LinkedList<>();

		for (int i = 0; i < handProbEstimation.length; i++) {
			boolean foundGreater = false;
			double currentCardProb = handProbEstimation[i];
			for (int cardIndex = 0; cardIndex < topNCardsIndices.size() && !foundGreater; cardIndex++) {
				if (currentCardProb < handProbEstimation[topNCardsIndices.get(cardIndex)]) {
					topNCardsIndices.add(cardIndex, i);
					foundGreater = true;
				}
			}
			if (!foundGreater) {
				topNCardsIndices.add(topNCardsIndices.size(), i);
			}

			if(topNCardsIndices.size() > n) {
				topNCardsIndices.remove(0);
			}
		}

		int cardMatches = 0;

		for (Integer cardIndex : topNCardsIndices) {
			if (actualHand[cardIndex] == 1) {
				cardMatches++;
			}
		}
		return cardMatches;
	}

	public static int minimum_top_largest(double[] actualHand, double[] handProbEstimation) {
		for (int n = 10; n < 52; n++) {
			if (top_n_cards(n, actualHand, handProbEstimation) == 10) {
				return n;
			}
		}
		return -1;
	}

	public static void print(double[] probArr) {
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
				System.out.print((String.format("%.8f", probArr[13 * i + j]) + "             ").substring(0, 5) + "  ");
			}
			System.out.println(" *");
		}
		for (int k = 0; k < 7 * 13 + 3 + 5; k++) {
			System.out.print("*");
		}
		System.out.println();
	}

	public static double[] report(double[] actualHand, double[] handProbEstimation) {
		return report(actualHand, handProbEstimation, false);
	}

	public static double[] report(double[] actualHand, double[] handProbEstimation, boolean verbose) {

		double msd = mean_squared_difference(actualHand, handProbEstimation);
		double pi = probabilistic_integrity(handProbEstimation);
		double tn = top_n_cards(10, actualHand, handProbEstimation);
		double mtl = minimum_top_largest(actualHand, handProbEstimation);

		if (verbose) {
			System.out.println("~Evaluation of Hand Estimation~\n");
			System.out.println("Actual Hand:");
			print(actualHand);
			System.out.println(Arrays.toString(actualHand));
			System.out.println("Estimated probabilities:");
			print(handProbEstimation);
			System.out.println(Arrays.toString(handProbEstimation));
			System.out.println();
			System.out.println("Mean Squared Difference: " + msd);
			System.out.println("Probabilistic Integrity: " + pi);
			System.out.println("Top 10 Probability Card Matches: " + tn);
			System.out.println("Top 15 Probability Card Matches: " + top_n_cards(15, actualHand, handProbEstimation));
			System.out.println("Top 20 Probability Card Matches: " + top_n_cards(20, actualHand, handProbEstimation));
			System.out.println("Minimum Top Largest: " + mtl);
		}

		return new double[] {msd, pi, tn, mtl};
	}



	public static void main(String[] args) {

		double[] pred = {0.40916005, 0.40250874, 0.00874296, 0.36802202, 0.02365682, 0.5881919, 0.03828397, 0.0560064, 0.02094573, 0.02421007, 0.01756173, 0.03232729, 0.01160747, 0.05872476, 0.448012, 0.00048691, 0.4608109 , 0.32126898, 0.6437781 , 0.26481766, 0.21892133, 0.00814983, 0.04827061, 0.04452127, 0.05923635, 0.01306537, 0.4359278, 0.45163217, 0.00777471, 0.00112414, 0.34566593, 0.02940187, 0.31676704, 0.31624818, 0.01047912, 0.05472204, 0.05141705, 0.05441523, 0.01030844, 0.4304061 , 0.39367098, 0.36558068, 0.39602366, 0.0013971, 0.8937961, 0.6018296, 0.3676975, 0.01770863, 0.01533322, 0.01683715, 0.03115228, 0.00699045};

		double[] actual = {0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0 ,0 ,0 , 0};

		pretty_report(actual, pred);


	}


}
