import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class Combine {

	public static void main(String[] args) {
		String filename1 = "SpecificHandEst1-5500000.dat";
		String filename2 = "SpecificHandEst1-5000000.dat";

		int[][][] heldVisits1 = null;
		int[][][][] heldCounts1 = null;
		int[][][] heldVisits2 = null;
		int[][][][] heldCounts2 = null;


		try {
			ObjectInputStream in1 = new ObjectInputStream(new FileInputStream(filename1));
			heldVisits1 = (int[][][]) in1.readObject();
			heldCounts1 = (int[][][][]) in1.readObject();
			in1.close();

			ObjectInputStream in2 = new ObjectInputStream(new FileInputStream(filename2));
			heldVisits2 = (int[][][]) in2.readObject();
			heldCounts2 = (int[][][][]) in2.readObject();
			in1.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}


		
		for (int a = 0; a < heldVisits1.length; a++) {
			for (int b = 0; b < heldVisits1[a].length; b++) {
				for (int c = 0; c < heldVisits1[a][b].length; c++) {
					heldVisits1[a][b][c] += heldVisits2[a][b][c];
					for (int d = 0; d < heldCounts1[a][b][c].length; d++) {
						heldCounts1[a][b][c][d] += heldCounts2[a][b][c][d];
					}
				}
			}
		}

		
		String filename = "SpecificHandEst1-10500000.dat";
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
			out.writeObject(heldVisits1);
			out.writeObject(heldCounts1);
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

}
