import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		
		File f = new File("data.csv");
		ArrayList<String> dataLines = new ArrayList<String>();

		try {
			Scanner s = new Scanner(f);

			s.nextLine();
			while (s.hasNext()) {
				String str = s.nextLine();
				dataLines.add(str);
			}
			
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		double diffCounter = 0;
		
		for (int i = 0; i < dataLines.size(); i++) {
			String[] pieces = dataLines.get(i).split(",");

			double actualValue = Double.parseDouble(pieces[0]);

			double[] features = new double[3];
			for (int j = 0; j < features.length; j++) {
				features[j] = Double.parseDouble(pieces[j + 1]);
			}

			xgboost_85928cc1_bc04_4b25_a40b_e6ae90c7e8ed xgb = new xgboost_85928cc1_bc04_4b25_a40b_e6ae90c7e8ed();
			
			double[] val = xgb.score0(features, new double[3]);

			double gbmPred = val[0];
			
			System.out.println(gbmPred + " =?= " + actualValue);

			diffCounter += Math.pow(gbmPred - actualValue, 2);
			
		}
		
		
		System.out.println("\naverage difference from accurate: " + Math.sqrt(diffCounter) / dataLines.size());


	}

}
