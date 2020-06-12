import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Test {

	public static void main(String[] args) throws FileNotFoundException {
		Scanner fileIn = new Scanner(new File("epsilon-2.csv"));
		ArrayList<ArrayList<Double>> testCases = new ArrayList<ArrayList<Double>>();
		fileIn.nextLine();
		while (fileIn.hasNext()) {
			String line[] = fileIn.nextLine().split(",");
			ArrayList<Double> lineAL = new ArrayList<>();
			for (int i = 0; i < line.length; i++) {
				lineAL.add(Double.parseDouble(line[i]));
			}
//			if (Math.random() < .01) {
				testCases.add(lineAL);
//			}
		
				
		}
		// data line: 4.6,3.6,1.0,0.2,2
		xgboost_0849a1cf_9e9a_4851_aaa0_17514eb90764 xgb = new xgboost_0849a1cf_9e9a_4851_aaa0_17514eb90764();
		for (ArrayList<Double> d : testCases) {
			double[] red_features = new double[3];
			red_features[0] = d.get(6);
			red_features[1] = d.get(8);
			red_features[2] = d.get(16);

			// double[] red_features = new double[] {d[0],d[1],d[2],d[3]};
			double[] val = xgb.score0(red_features, new double[10]);
			System.out.println(val[0] + "," + d.get(3));
		}

	}

}
