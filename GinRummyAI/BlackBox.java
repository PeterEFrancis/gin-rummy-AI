import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

public class BlackBox {

	static final String[] FILENAMES = new String[] {"linear_coef.csv", "logistic_coef.csv"};
	static final int ALPHA = 0, BETA = 1, GAMMA = 2, DELTA = 3;
	static final int LINEAR = 0, LOGISTIC = 1;

	static final int[] VERSIONS = {ALPHA};
	static final int[] TYPES = {LINEAR, LOGISTIC};

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



	static public double regFunction(Player player) {
		if (player.type == LINEAR) {
			ArrayList<Double> coefArr = coefficients.get(player.type).get(player.version);
			double[] features = OurUtilities.calculateFeatures(player);

			// System.out.println("features: " + Arrays.toString(features));

			double linear_combination = 0;
			for (int i = 1; i < coefArr.size(); i++) {
				linear_combination += coefArr.get(i) * features[i];
			}
			return linear_combination + coefArr.get(0);
		}
		else {
			return -100000;
		}
	}






}
