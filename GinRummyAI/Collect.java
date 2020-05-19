import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/*
 * Collect.java - Collects play data and stores it in a csv files
 */
public class Collect {

	public static String fileName = "alpha.csv";
	public static File file = new File(fileName);
	public static PrintWriter pw;
	static {
		try {
			pw = new PrintWriter(file);
		} catch (FileNotFoundException e) {
			System.err.println("No file.");
		}
	}



	/*
	 * play game and write the necessary information to a CSV file in data
	 * format:
	 *
	 *
	 *
	 */
	public static void playGame() {

		//TODO
		//provide Players to the Game(player1, player2)
		Game game = new Game(new SimpleGinRummyPlayer(), new SimpleGinRummyPlayer());

		// current_player_score,
		// opponent_score,
		// current_player_deadwood,
		// current_player_num_options,
		// is_current_player_hand_winner

		// is_current_player_game_winner


		ArrayList<StringBuilder> toCSV = game.getPlayData();
		String is_current_player_game_winner = toCSV.get(toCSV.size() - 1).toString();
		toCSV.remove(toCSV.size() - 1);
		for (StringBuilder sb : toCSV) {
//			System.out.println(sb.toString() + "," + is_current_player_game_winner);
			pw.println(sb.toString() + "," + is_current_player_game_winner);
		}
		
	}

	public static void main(String args[]) {
		pw.println("current_player_score,opponent_score,current_player_deadwood,current_player_num_options,is_current_player_hand_winner,is_current_player_game_winner");

		for (int i = 0; i < 1000; i++) {
			playGame();
		}
		System.out.println("done");
		pw.close();
	}

}
