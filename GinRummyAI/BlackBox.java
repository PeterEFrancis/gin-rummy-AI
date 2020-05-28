import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

public class BlackBox {

	static final String[] FILENAMES = new String[] {"linear_coef.csv", "quadratic_coef.csv", "logistic_coef.csv"};
	static final int SIMPLE = -1, ALPHA = 0, BETA = 1, GAMMA = 2, DELTA = 3;
	static final int LINEAR = 0, QUADRATIC = 1, LOGISTIC = 2;

	static final int[] VERSIONS = {ALPHA, BETA};
	static final int[] TYPES = {LINEAR, QUADRATIC, LOGISTIC};

	static ArrayList<ArrayList<ArrayList<Double>>> coefficients = new ArrayList<ArrayList<ArrayList<Double>>>();

	static {
		try {
			for (int type : TYPES) {
				ArrayList<ArrayList<Double>> typeAL = new ArrayList<ArrayList<Double>>();
				String fileName = FILENAMES[type];
				Scanner fileIn = new Scanner(new File(fileName));
				while (fileIn.hasNext()) {
					String line[] = fileIn.nextLine().split(",");
					ArrayList<Double> lineAL = new ArrayList<>();
					for (int i = 0; i < line.length; i++) {
						lineAL.add(Double.parseDouble(line[i]));
					}
					typeAL.add(lineAL);
				}
				fileIn.close();
				coefficients.add(typeAL);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns the probability of winning the game
	**/
	static public double regFunction(Player player) {

		if (player.version == SIMPLE) {
			return OurUtilities.calculateFeatures(player)[2] / -10.0 + 5;
		}

		if (player.type == LINEAR) {
			ArrayList<Double> coefArr = coefficients.get(player.type).get(player.version);
			double[] features = OurUtilities.calculateFeatures(player);
			double linear_combination = 0;
			for (int i = 1; i < coefArr.size(); i++) {
				linear_combination += coefArr.get(i) * features[i - 1];
			}
			return linear_combination + coefArr.get(0);
		}

		if (player.type == QUADRATIC) {
			ArrayList<Double> coefArr = coefficients.get(player.type).get(player.version);
			double[] features = OurUtilities.calculateFeatures(player);
			double[] quadratic_features = new double[features.length + (int) (features.length * (features.length + 1) / 2)];
			for (int i = 0; i < features.length; i++) {
				quadratic_features[i] = features[i];
			}
			int k = features.length;
			for (int i = 0; i < features.length; i++) {
				for (int j = i; j < features.length; j++) {
					quadratic_features[k] = features[i] * features[j];
					k++;
				}
			}
			double linear_combination = 0;
			// System.out.println("features len: " + quadratic_features.length);
			// System.out.println("Coeef size: " + coefArr.size());
			for (int i = 1; i < coefArr.size(); i++) {
				// System.out.println("i: " + i);
				// System.out.println("Coeef: " + coefArr.get(i));
				// System.out.println("Quad: " + quadratic_features[i - 1]);
				// System.out.println("---------------------------------------------------");
				linear_combination += coefArr.get(i) * quadratic_features[i - 1];
			}
			return linear_combination + coefArr.get(0);
		}

		else {
			return -100000;
		}
	}


}
