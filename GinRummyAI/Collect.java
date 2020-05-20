import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * Collect.java - Collects play data and stores it in a csv files
 */
public class Collect {

	public static String fileName = "alpha-2.csv";
	public static File file = new File(fileName);
	public static PrintWriter pw;
	static {
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			System.err.println("No file.");
		}
	}


	public static void main(String args[]) {
		pw.println("current_player,current_player_score,opponent_score,current_player_deadwood,current_player_num_hit_cards,is_current_player_hand_winner,is_current_player_game_winner");

		for (int i = 0; i < 50; i++) {
			Game game = new Game(new SimpleGinRummyPlayer(), new SimpleGinRummyPlayer());

			ArrayList<ArrayList<String>> csvOutput = game.getPlayData();
			double gameWinner = Double.parseDouble(csvOutput.get(csvOutput.size() - 1).get(0));
			csvOutput.remove(csvOutput.size() - 1);

			for (ArrayList<String> handData : csvOutput) {
				double handWinner = Double.parseDouble(handData.get(handData.size() - 1).toString());
				handData.remove(handData.size() - 1);
				for (String data : handData) {
					double currentPlayer = Double.parseDouble(Arrays.asList(data.split(",")).get(0));
					if (handWinner != 0.5) {
						pw.println(data
						 		  + ((currentPlayer == handWinner) ? "1" : "0") + ","
								  + ((currentPlayer == gameWinner) ? 1 : 0));
					}

				}

			}

		}

		pw.close();

		System.out.println("done");
	}

}
