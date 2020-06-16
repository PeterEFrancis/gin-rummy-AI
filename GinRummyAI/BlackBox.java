import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;

import java.util.Arrays;

public class BlackBox {

	static final String GBMName = "xgboost_817c4595_1ef8_4d35_aa14_3c1c731d4b88";
	static final String[] FILENAMES = new String[] {"linear_coef.csv", "quadratic_coef.csv"};
	static final int ALPHA = 0, BETA = 1, GAMMA = 2, DELTA = 3, EPSILON = 4;
	static final int LINEAR = 0, QUADRATIC = 1, NETWORK = 2, XGBOOST = 3;

	static final int[] VERSIONS = {ALPHA, BETA, GAMMA, DELTA};
	static final String[] STRING_VERSIONS = {"alpha", "beta", "gamma", "delta", "epsilon"};
	static final int[] COEFFICIENT_TYPES = {LINEAR, QUADRATIC};
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

			double[] features = OurUtilities.calculateFeatures(player);
			// System.out.println("\t" + Arrays.toString(features));

			deeplearning_faf3fb3c_87a1_445e_9332_e95539ce38bc dl = new deeplearning_faf3fb3c_87a1_445e_9332_e95539ce38bc();
			double[] red_features = new double[] {features[2], features[4], features[12]};

			double[] val = dl.score0(red_features, new double[10]);
			// System.out.println("\tval= " + val[0]);
			return val[0];


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
			// System.out.println("\t" + Arrays.toString(features));

			xgboost_8eda59da_c567_472f_9f17_6fa62fe9f11d xgb = new xgboost_8eda59da_c567_472f_9f17_6fa62fe9f11d();
			double[] red_features = new double[] {features[2], features[4], features[12]};

			double[] val = xgb.score0(red_features, new double[1]);
			// System.out.println("\tval= " + val[0]);
			return val[0];








			// xgboost_ea6fe23c_5dd1_4e5c_bed4_d01cb74709ae p = new xgboost_ea6fe23c_5dd1_4e5c_bed4_d01cb74709ae();
			// double[] red_features = new double[] {features[0], features[1], features[2], features[3], features[12], features[13], features[14]};
			// double[] preds = new double[p.getPredsSize()];
			// p.score0(red_features, preds);
			// return preds[0];


			// String modelClassName = "xgboost_0373d649_c119_4ca5_a67d_c22d2c258b81"; //insert class name here
			// hex.genmodel.GenModel rawModel;
   		// try {
			// rawModel = (hex.genmodel.GenModel) Class.forName(modelClassName).newInstance();
			//
			// EasyPredictModelWrapper model = new EasyPredictModelWrapper(rawModel);
			//
			// String[] feature_names = {"current_player_score",
			// 													"opponent_score",
			// 													"current_player_deadwood",
			// 													"current_player_num_hit_cards",
			// 													"num_melds",
			// 													"point_sum_melds",
			// 													"num_combos",
			// 													"point_sum_combos",
			// 													"num_knock_cache",
			// 													"point_sum_knock_cache",
			// 													"num_load_cards",
			// 													"point_sum_load_cards",
			// 													"turns_taken",
			// 													"num_nearby_opponent_cards"};
			//
			//
    	// 	RowData row = new RowData();
	 		// for (int i = 0; i < features.length; i++) {
		 	// 	row.put(feature_names[i],features[i]);
	 		// }
    	// 	BinomialModelPrediction p = model.predictBinomial(row);
    	// 	// System.out.println("Label (aka prediction) is_current_player_game_winner: " + p.label);
    	// 	// System.out.print("Class probabilities: ");
    	// 	// for (int i = 0; i < p.classProbabilities.length; i++) {
      // 	// 	if (i > 0) {
      //   	// 		System.out.print(",");
      // 	// 	}
      // 	// 	System.out.print(p.classProbabilities[i]);
    	// 	// }
    	// 	// System.out.println("");
			//
			// System.out.println("taylor's thing: " + p.classProbabilities[0]);
			//
			// return p.classProbabilities[0];


//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		// System.out.println(1/0);
		}

		return -1000000;
	}


	public static void main(String[] args) throws IOException {

	}

}
