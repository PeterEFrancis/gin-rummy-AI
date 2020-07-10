import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TestTestTest {

	public static void main(String[] args) throws IOException {

		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec("python3 src/main/python/test.py [1,2,3,4]");

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(pr.getInputStream()));

	 	BufferedReader stdError = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

	 	String s = null;
//	 	int i = 0;
//	 	double[] ds = new double[4];
		while ((s = stdInput.readLine()) != null) {
//			ds[i++] = Double.parseDouble(s);
			System.out.println(s);
		}

		while ((s = stdError.readLine()) != null) {
    		System.err.println(s);
		}


//		System.out.println("doubles: " + Arrays.toString(ds));

	}

}
