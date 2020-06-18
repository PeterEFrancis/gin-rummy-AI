import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import org.nd4j.common.util.ArrayUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class Test {

	public static void main(String[] args) throws IOException {
		//		Scanner fileIn = new Scanner(new File("epsilon-2.csv"));
		//		ArrayList<ArrayList<Double>> testCases = new ArrayList<ArrayList<Double>>();
		//		fileIn.nextLine();
		//		while (fileIn.hasNext()) {
		//			String line[] = fileIn.nextLine().split(",");
		//			ArrayList<Double> lineAL = new ArrayList<>();
		//			for (int i = 0; i < line.length; i++) {
		//				lineAL.add(Double.parseDouble(line[i]));
		//			}
		////			if (Math.random() < .01) {
		//				testCases.add(lineAL);
		////			}
		//
		//
		//		}
		//		// data line: 4.6,3.6,1.0,0.2,2
		//		xgboost_0849a1cf_9e9a_4851_aaa0_17514eb90764 xgb = new xgboost_0849a1cf_9e9a_4851_aaa0_17514eb90764();
		//		for (ArrayList<Double> d : testCases) {
		//			double[] red_features = new double[3];
		//			red_features[0] = d.get(6);
		//			red_features[1] = d.get(8);
		//			red_features[2] = d.get(16);
		//
		//			// double[] red_features = new double[] {d[0],d[1],d[2],d[3]};
		//			double[] val = xgb.score0(red_features, new double[10]);
		//			System.out.println(val[0] + "," + d.get(3));
		//		}

		double[][] myDoubleArray = new double[][] {{1,2,3},{4,5,6}};

		double[] flat = ArrayUtil.flattenDoubleArray(myDoubleArray);
		int[] shape = new int[]{2,3};    // Array shape here
		INDArray myArr = Nd4j.create(flat,shape,'c');
		System.out.println(myArr);
		int nRows = 2;
		int nColumns = 2;
		// Create INDArray of zeros
		INDArray zeros = Nd4j.zeros(nRows, nColumns);
		// Create one of all ones
		INDArray ones = Nd4j.ones(nRows, nColumns);

		//hstack
		INDArray hstack = Nd4j.hstack(ones,zeros);
		System.out.println("### HSTACK ####");
		System.out.println(hstack);


		//vstack
		INDArray vstack = Nd4j.vstack(ones,zeros);
		System.out.println("### VSTACK ####");
		System.out.println(vstack);

		System.out.println("Saving arrays...");
		for (int i = 0; i < 5; i++) {
			FileWriter fw = new FileWriter(new File("save_test_1"), true);
			fw.write(hstack.toString().replace("\n", "").replace(" ",  "") + "\n");
			fw.close();
		}
//		for (int i = 0; i < 5; i++) {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			Nd4j.write(baos, hstack);
//			String s = baos.toString();
//			FileWriter fw = new FileWriter(new File("save_test_1"), true);
//			fw.write(s.replace("\n", " ") + "\n");
//			fw.close();
//		}
		
		System.out.println("Done.");
		
		
		
		System.out.println("Reading Arrays...");
		
		Scanner in = new Scanner(new File("save_test_1"));
		for (int i = 0; i < 5; i++) {
			String s = in.nextLine().replace(" ", "");
			if (s.length() > 4) {
				String[] sa = s.substring(2,s.length() - 2).split("\\],\\[");
				double[][] dad = new double[sa.length][sa[0].split(",").length];
				String[] sad;
				for (int r = 0; r < dad.length; r++) {
					sad = sa[r].split(",");
					for (int c = 0; c < dad[0].length; c++) {
						dad[r][c] = Double.parseDouble(sad[c].trim());
					}
				}
				double[] flat_arr = ArrayUtil.flattenDoubleArray(dad);
				int[] shape_arr = new int[]{dad.length, dad[0].length};
				INDArray arr = Nd4j.create(flat_arr,shape_arr,'c');
				System.out.println(arr);
			}
			
		}
		
		System.out.println("Done.");

	}

}
