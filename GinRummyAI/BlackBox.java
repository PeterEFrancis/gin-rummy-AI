import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Arrays;

public class BlackBox {

	static final String[] FILENAMES = new String[] {"linear_coef.csv", "quadratic_coef.csv", "logistic_coef.csv"};
	static final int SIMPLE = -1, ALPHA = 0, BETA = 1, GAMMA = 2, DELTA = 3;
	static final int LINEAR = 0, QUADRATIC = 1, LOGISTIC = 2, NETWORK = 3, XGBOOST = 4;

	static final int[] VERSIONS = {ALPHA, BETA, GAMMA, DELTA};
	static final String[] STRING_VERSIONS = {"alpha", "beta", "gamma", "delta"};
	static final int[] COEFFICIENT_TYPES = {LINEAR, QUADRATIC, LOGISTIC};
	static String base_post_url = "http://127.0.0.1:4201/";

	static ArrayList<ArrayList<ArrayList<Double>>> coefficients = new ArrayList<ArrayList<ArrayList<Double>>>();

	static {
		try {
			for (int type : COEFFICIENT_TYPES) {
				ArrayList<ArrayList<Double>> typeAL = new ArrayList<ArrayList<Double>>();
				String fileName = "regression_models/" + FILENAMES[type];
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
	 * @throws IOException
	 * @throws UnsupportedKerasConfigurationException
	 * @throws InvalidKerasConfigurationException
	**/
	public static double regFunction(Player player) {

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
			for (int i = 1; i < coefArr.size(); i++) {
				linear_combination += coefArr.get(i) * quadratic_features[i - 1];
			}
			return linear_combination + coefArr.get(0);

		}

		if (player.type == NETWORK) {

			// double[] features = OurUtilities.calculateFeatures(player);
			// features = new double[] {features[0], features[1], features[2], features[3], features[13]};
			// String featuresStr1 = Arrays.toString(features);
			// String featuresStr = featuresStr1.substring(1,featuresStr1.length() -1);
			//
			// Request request = new Request(base_post_url + "calculate_network/version=" + player.version);
			//
			// try {
			// 	return request.sendFeatures(featuresStr);
			// } catch (IOException e) {
			// 	e.printStackTrace();
			// }

		}

		if (player.type == XGBOOST) {

			double[] features = OurUtilities.calculateFeatures(player);

			xgboost_087630fb_4f1c_4de4_b265_932fcf311387 xgb = new xgboost_087630fb_4f1c_4de4_b265_932fcf311387();
			double[] val = xgb.score0(features, new double[1]);
			// System.out.println(Arrays.toString(val));
			return val[0];

		}



		return -1000000;
	}


	public static void main(String[] args) throws IOException {

	}

}
