
public class IrisTest {

	static double[][] testCases = new double[][] {
		{5.1,3.5,1.4,0.2,2},
		{4.9,3.0,1.4,0.2,2},
		{4.7,3.2,1.3,0.2,2},
		{4.6,3.1,1.5,0.2,2},
		{5.0,3.6,1.4,0.2,2},
		{7.0,3.2,4.7,1.4,1},
		{6.4,3.2,4.5,1.5,1},
		{6.9,3.1,4.9,1.5,1},
		{5.5,2.3,4.0,1.3,1},
		{6.5,2.8,4.6,1.5,1},
		{6.7,3.0,5.2,2.3,0},
		{6.3,2.5,5.0,1.9,0},
		{6.5,3.0,5.2,2.0,0},
		{6.2,3.4,5.4,2.3,0},
		{5.9,3.0,5.1,1.8,0}
	};

	public static void main(String[] args) {
		// data line: 4.6,3.6,1.0,0.2,2
		xgboost_083c4083_1ae0_426d_b65a_2444df6724a8 xgb = new xgboost_083c4083_1ae0_426d_b65a_2444df6724a8();
		for (double[] d : testCases) {
			double[] red_features = new double[] {d[0],d[1],d[2],d[3]};
			double[] val = xgb.score0(red_features, new double[10]);
			System.out.println("predicted: " + val[0]);
			System.out.println("actual: " + d[4]);
		}

	}

}
