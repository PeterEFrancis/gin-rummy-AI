import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class OurUtilities {

	// AC 2C 3C AD 2D 3D AH 2H 3H 2S  (all melded, several optimal meld combinations)
	public static int[] set1ranks = {0,0,0,1,1,1,1,2,2,2};
	public static int[] set1suits = {0,1,2,0,1,2,3,0,1,2};

	// 2C 3C 4C 5H 6H 7H 8C 8D 8H KC (2 run, 1 set, 1 deadwood card)
	public static int[] set2ranks = {1,2,3,4,5,6,7,7,7,12};
	public static int[] set2suits = {0,0,0,2,2,2,0,1,2,0};

	// AC 2D 3H 4S 6C 6D 6H 7C 10H 10S (triangle shape, 1 combination)
	public static int[] set3ranks = {0,1,2,3,5,5,5,6,9,9};
	public static int[] set3suits = {0,1,2,3,0,1,2,0,2,3};

	// AC 2D 3H 4S 5C 6D 7H 8S 9C 10D (no hit cards)
	public static int[] set4ranks = {0,1,2,3,4,5,6,7,8,9};
	public static int[] set4suits = {0,1,2,3,0,1,2,3,0,1};

	// 2C 3C 2H 3H 7C 8C 7H 8H KC KH (18 hit cards)
	public static int[] set5ranks = {1,2,1,2,6,7,6,7,12,12};
	public static int[] set5suits = {0,0,1,1,0,0,1,1,0,1};

	public static int[][] testCards;
	static {
		testCards = new int[10][10];
		testCards[0] = set1ranks;
		testCards[1] = set1suits;
		testCards[2] = set2ranks;
		testCards[3] = set2suits;
		testCards[4] = set3ranks;
		testCards[5] = set3suits;
		testCards[6] = set4ranks;
		testCards[7] = set4suits;
		testCards[8] = set5ranks;
		testCards[9] = set5suits;
	}

	/**
	 * returns the number of deadwood points in player's hand
	 * @param hand - cards in player's hand
	 * @return number of deadwood points in player's hand
	 **/
	public static int deadwoodCount(ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (bestMeldSets.isEmpty()) {
			return GinRummyUtil.getDeadwoodPoints(hand);
		}
		ArrayList<ArrayList<Card>> chosenMeldSet = bestMeldSets.get(0);
		//  System.out.println("Meld sets: " + bestMeldSets.size());
		// for(ArrayList<Card> meld : chosenMeldSet) {
		//      System.out.println("meld: " + meld);
		// }
		int deadwood = GinRummyUtil.getDeadwoodPoints(chosenMeldSet,hand);
		return deadwood;
	}

	// TODO: for now we will pass any cards even if we know the opponent has them (maybe make opponentHand variable?)
	/**
	 * returns the number of hit cards for the player's hand that can still be acquired
	 * @param possibleCards - all possible cards still available to the player
	 * @param hand - cards in player's hand
	 * @return the number of hit cards
	 **/
	public static int numHitCards(ArrayList<Card> possibleCards, ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (bestMeldSets.isEmpty()) {
			bestMeldSets.add(new ArrayList<ArrayList<Card>>());
		}
		int hitCount = 0;
		for (ArrayList<ArrayList<Card>> meldSet : bestMeldSets) {
			ArrayList<ArrayList<Card>> combos = getCombos(meldSet, hand);
			int hits = getHitCount(combos, meldSet, possibleCards);
			if (hits > hitCount) {
				hitCount = hits;
			}
		}
		return hitCount;
	}

	/**
	 * returns true if the card is a hit card for you the given hand
	 * @param c - card
	 * @param hand - cards in player's handData
	 * @return true if the card is a hit card for you the given hand; false otherwise
	 **/
	public static boolean isHitCard(Card c, ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (bestMeldSets.isEmpty()) {
			bestMeldSets.add(new ArrayList<ArrayList<Card>>());
		}
		for (ArrayList<ArrayList<Card>> meldSet : bestMeldSets) {
			ArrayList<ArrayList<Card>> combos = getCombos(meldSet, hand);
			if (isHitCard(combos,meldSet,c)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * returns true if the card is a hit card for you the given hand (forms meld from combo or adds to existing meld)
	 * @param combos - list of combos in hand
	 * @param melds - list of melds in hand
	 * @param c - card
	 * @return true if the card is a hit card for you the given hand; false otherwise
	 **/
	public static boolean isHitCard(ArrayList<ArrayList<Card>> combos, ArrayList<ArrayList<Card>> melds, Card c) {
		return meldFromCombo(c,combos) || canBeMeldedIn(c,melds);
	}

	/**
	 * returns true if the card adds to a combo to form a meld
	 * @param c - card
	 * @param combos - list of ocmbos in hand
	 * @return true if the card adds to a combo to form a meld
	 **/
	public static boolean meldFromCombo(Card c, ArrayList<ArrayList<Card>> combos) {
		ArrayList<Card> comboCards = new ArrayList<Card>();
		for (ArrayList<Card> combo : combos) {
			comboCards.addAll(combo);
		}
		//count hits for combination
		comboCards.add(c);
		ArrayList<Long> bitStrings = GinRummyUtil.cardsToAllMeldBitstrings(comboCards);
		comboCards.remove(c);
		if (bitStrings.size() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * returns the hit count of all combinations and melds
	 * @param combos - combos in hand
	 * @param melds - melds in hand
	 * @param possibleCards - possible cards available to player
	 * @return hit count
	 **/
	public static int getHitCount(ArrayList<ArrayList<Card>> combos, ArrayList<ArrayList<Card>> melds, ArrayList<Card> possibleCards) {
		int hitCount = 0;
		for (Card c : possibleCards) {
			if (isHitCard(combos,melds,c)) {
				hitCount++;
			}
		}
		return hitCount;
	}

	/**
	 * returns true if the card adds to an existing meld
	 * @param c - card
	 * @param melds - melds in hand
	 * @return true if the card adds to an existing meld
	 **/
	public static boolean canBeMeldedIn(Card c, ArrayList<ArrayList<Card>> melds) {
		for (ArrayList<Card> meld : melds) {
			int rank = c.getRank();
			if (meld.get(0).getRank() == meld.get(1).getRank()) { //meld is a set
				if (meld.get(0).getRank() == rank) {
					return true;
				}
			}
			else if (meld.get(0).getSuit() == c.getSuit()) { //meld is a run and c is similar suit
				for (Card c2 : meld) {
					if (Math.abs(c2.getRank() - rank) == 1) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * returns a list of combos given player's hands
	 * @param melds - melds in hand
	 * @param hand - list of cards in hand
	 * @return a list of combos given player's hands
	 **/
	public static ArrayList<ArrayList<Card>> getCombos(ArrayList<ArrayList<Card>> melds, ArrayList<Card> hand) {
		ArrayList<Card> unmeldedCards = cardsNotInMeld(melds,hand); //unmelded cards in hand
		ArrayList<ArrayList<Card>> combos = new ArrayList<ArrayList<Card>>();
		while (unmeldedCards.size() > 0) { //iterates through unmelded cards in hand checking for combinations
			Card c1 = unmeldedCards.get(0);
			for (int i = 1; i < unmeldedCards.size(); i++) {
				Card c2 = unmeldedCards.get(i);
				if (c1.getRank() == c2.getRank() ||
						c1.getSuit() == c2.getSuit() && Math.abs(c1.getRank() - c2.getRank()) == 1) {
					ArrayList<Card> combo = new ArrayList<Card>();
					combo.add(c1);
					combo.add(c2);
					combos.add(combo);
				}
			}
			unmeldedCards.remove(0);
		}
		return combos;
	}

	/**
	 * returns the list of unmelded cards in given hand
	 * @param melds - list of melds in given hand
	 * @param hand - list of cards in hand
	 * @return the list of unmelded cards in given hand
	 **/
	public static ArrayList<Card> cardsNotInMeld(ArrayList<ArrayList<Card>> melds, ArrayList<Card> hand) {
		ArrayList<Card> unmeldedCards = new ArrayList<Card>();
		for (int i = 0; i < hand.size(); i++) {
			unmeldedCards.add(hand.get(i));
		}
		for (ArrayList<Card> cards : melds) {
			for (Card c : cards) {
				unmeldedCards.remove(c);
			}
		}
		return unmeldedCards;
	}

	/**
	 * unfinished; returns - ?
	 **/
	public static int numOptions(ArrayList<Card> possibleCards, ArrayList<Card> hand) {
		int deadwood = deadwoodCount(hand);
		int opCount = 0;
		for (Card c : possibleCards) {
			hand.add(c);
			int newDeadwood = deadwoodCount(hand);
			if ((newDeadwood) < deadwood) {
				opCount++;
			}
			hand.remove(c);
		}
		return opCount;
	}

	/**
	* converts a card to its corresponding bitstring
* @param c - card
	*
	**/
	public static long cardToBitstring(Card c) {
		return 1L << c.getId();
	}

	public static Card bitstringToCard(long cardBits) {
		int id = 0;
		while (cardBits != 1) {
			id++;
			cardBits /= 2;
		}
		return Card.allCards[id];
	}

	public static Card transformCard(Card c) {
		return Card.allCards[c.getId()];
	}

	/**
	 * returns an ArrayList of 52 shuffled cards
	 **/
	public static ArrayList<Card> getShuffle() {
		ArrayList<Card> deck = new ArrayList<Card>();
		for (int i = 0; i < 52; i++)
			deck.add(Card.allCards[i]);
		Collections.shuffle(deck);
		return deck;
	}

	public static int getFaceValue(Card c) {
		int rank = c.getRank();
		if (rank >= 10)
			return 10;
		return rank;
	}


	//__objectively__ the best function

	public static ArrayList<ArrayList<ArrayList<Card>>> getBestHandOrganization(ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> meldSets = GinRummyUtil.cardsToBestMeldSets(hand);

		int numLoad = 11;
		ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestCombos = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestKnockCache = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestLoadCards = new ArrayList<ArrayList<Card>>(); // actually just an arraylist<Card>

		if (meldSets.isEmpty()){
			meldSets.add(new ArrayList<ArrayList<Card>>());
		}

		for (ArrayList<ArrayList<Card>> meldSet : meldSets) {
			ArrayList<Card> unmeldedCards = removeCards(meldSet, hand);
			ArrayList<Card> knockCache = new ArrayList<Card>();
			Card highest = null;
			int highestCardRankInKnock = -1;
			int currentKnockScore = 0;


			for(int i = 0; i < unmeldedCards.size(); i++){
				Card currentCard = unmeldedCards.get(i);
				int currentCardRank = currentCard.getRank() + 1;
				currentCardRank = currentCardRank >= 10 ? 10 : currentCardRank;

				// The card can be put to knock cache
				if(currentKnockScore + currentCardRank <= 10) {

					knockCache.add(currentCard);
					currentKnockScore += currentCardRank;
					// updating the highest card in knock cache
					if(currentCardRank > highestCardRankInKnock){
						highest = currentCard;
						highestCardRankInKnock = currentCardRank;
					}
				}
				// The card cannot be put to knock cache but can be replaced with the highest card in knock cache
				// if we found a lower card not in the knock cache than the highest card in knock cache
				else if(currentCardRank < highestCardRankInKnock){
					knockCache.remove(highest);
					knockCache.add(currentCard);

					currentKnockScore += (-highestCardRankInKnock + currentCardRank);
					highestCardRankInKnock = currentCardRank;
					highest = currentCard;
				}
			}

			ArrayList<ArrayList<Card>> combos = getCombos(meldSet, hand);
			ArrayList<ArrayList<Card>> knockCash$ = new ArrayList<ArrayList<Card>>();
			knockCash$.add(knockCache);
			ArrayList<Card> load = removeCards(combos, removeCards(knockCash$, removeCards(meldSet, hand)));
			if (load.size() < numLoad) {
				numLoad = load.size();
				bestMelds = meldSet;
				bestCombos = combos;
				bestKnockCache = knockCash$;
				bestLoadCards = new ArrayList<ArrayList<Card>>();
				bestLoadCards.add(load);
			}
		}

		ArrayList<ArrayList<ArrayList<Card>>> organization = new ArrayList<ArrayList<ArrayList<Card>>>();
		organization.add(bestMelds);
		organization.add(bestCombos);
		organization.add(bestKnockCache);
		organization.add(bestLoadCards);

		return organization;
	}

	//pass organization[0]
	public static int numSetMelds(ArrayList<ArrayList<Card>> melds) {
		int numSets = 0;
		for (ArrayList<Card> meld : melds) {
			if (meld.get(0).getRank() == meld.get(1).getRank()) {
				numSets++;
			}
		}
		return numSets;
	}

	//pass organization[0]
	public static int numRunMelds(ArrayList<ArrayList<Card>> melds) {
		int numRuns = 0;
		for (ArrayList<Card> meld : melds) {
			if (meld.get(0).getRank() != meld.get(1).getRank()) {
				numRuns++;
			}
		}
		return numRuns;
	}

	//pass organization[1]
	public static int numSetCombos(ArrayList<ArrayList<Card>> combos) {
		int numSets = 0;
		for (ArrayList<Card> combo : combos) {
			if (combo.get(0).getRank() == combo.get(1).getRank()) {
				numSets++;
			}
		}
		return numSets;
	}

	//pass organization[1]
	public static int numRunCombos(ArrayList<ArrayList<Card>> combos) {
		int numRuns = 0;
		for (ArrayList<Card> combo : combos) {
			if (combo.get(0).getRank() != combo.get(1).getRank()) {
				numRuns++;
			}
		}
		return numRuns;
	}

	public static ArrayList<Card> removeCards(ArrayList<ArrayList<Card>> set, ArrayList<Card> cards) {
		ArrayList<Card> returnCards = new ArrayList<Card>();
		for (Card c : cards) {
			returnCards.add(c);
		}
		for (ArrayList<Card> subset : set) {
			for (Card c : subset) {
				returnCards.remove(c);
			}
		}
		return returnCards;
	}

	public static int getPoints(ArrayList<ArrayList<Card>> set) {
		int points = 0;
		for (ArrayList<Card> subset : set) {
			for (Card c : subset) {
				points += getFaceValue(c);
			}
		}
		return points;
	}

	public static void testUtils() {

		Card c = new Card(10,3);
		System.out.println(c);
		System.out.println(transformCard(c));

		for (int r = 0; r < testCards.length; r+=2) {
			System.out.println("------------------CARD SET "+ r/2 +"------------------------");
			ArrayList<Card> hand = new ArrayList<>();
			for (int i = 0; i < testCards[r].length; i++) {
				hand.add(new Card(testCards[r][i], testCards[r+1][i]));
			}
			long handbits = GinRummyUtil.cardsToBitstring(hand);
			hand = GinRummyUtil.bitstringToCards(handbits);

			System.out.println("Hand: " + hand);

			// Test Deadwood
			System.out.println("Deadwood Count: " + deadwoodCount(hand));

			// Test getBestHandOrganization
			ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(hand);
			System.out.println("Best Organization: " + organization);
			System.out.println("\tBest Melds: " + organization.get(0));
			System.out.println("\tBest Combos: " + organization.get(1));
			System.out.println("\tBest KnockCash$: " + organization.get(2));
			System.out.println("\tBest Loads: " + organization.get(3));
			//System.out.println("\tNumber of set melds: " + numSetMelds(organization.get(0)));
			//System.out.println("\tNumber of run melds: " + numRunMelds(organization.get(0)));
			//System.out.println("\tNumber of set combos: " + numSetCombos(organization.get(1)));
			//System.out.println("\tNumber of run combos: " + numRunCombos(organization.get(1)));

			ArrayList<Card> deck = new ArrayList<>();
			for (int i = 0; i < 52; i++) {
				deck.add(new Card(i % 13, i / 13));
			}

			long deckbits = GinRummyUtil.cardsToBitstring(deck);
			deck = GinRummyUtil.bitstringToCards(deckbits);

			deck.removeAll(hand);


			System.out.println("Hit Count: " + numHitCards(deck, hand));
			// System.out.println("Number of options: " + numOptions(deck, hand));
		}

	}


	/**
	 * calculates all possible features about a player's current state
	**/
	public static double[] calculateFeatures(Player player) {

		double current_player_score = player.scores[player.playerNum];

		double opponent_score = player.scores[1 - player.playerNum];

		double current_player_deadwood = deadwoodCount(player.hand);

		double current_player_num_hit_cards = numHitCards(player.unknownCards, player.hand);

		double turns_taken = player.turn;

		ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(player.hand);

		double num_melds = organization.get(0).size();
		double point_sum_melds = getPoints(organization.get(0));

		double num_combos = organization.get(1).size();
		double point_sum_combos = getPoints(organization.get(1));

		double num_knock_cache = organization.get(2).get(0).size();
		double point_sum_knock_cache = getPoints(organization.get(2));

		double num_load_cards = organization.get(3).get(0).size();
		double point_sum_load_cards = getPoints(organization.get(3));

		return new double[] {
							current_player_score,
							opponent_score,
							current_player_deadwood,
							current_player_num_hit_cards,
							num_melds,
							point_sum_melds,
							num_combos,
							point_sum_combos,
							num_knock_cache,
							point_sum_knock_cache,
							num_load_cards,
							point_sum_load_cards,
							turns_taken
		};
	}



	public static void main(String[] args) {

//		testUtils();

		// find distributions

		int[] deadwoodCounter = new int[98 + 1];
		int[] hitCardCounter = new int[18 + 1];
		int[] numMeldCounter = new int[3 + 1];
		int[] pointSumMeldCounter = new int[100 + 1];
		int[] numCombosCounter = new int[50 + 1];
		int[] pointSumCombosCounter = new int[250 + 1];
		int[] numKnockCacheCounter = new int[5 + 1];
		int[] pointSumKnockCacheCounter = new int[10 + 1];
		int[] numLoadCardsCounter = new int[8 + 1];
		int[] pointSumLoadCardsCounter = new int[100 + 1];

		int[][] counts = {deadwoodCounter, hitCardCounter, numMeldCounter,
			pointSumMeldCounter, numCombosCounter, pointSumCombosCounter,
			numKnockCacheCounter, pointSumKnockCacheCounter, numLoadCardsCounter,
			pointSumLoadCardsCounter};

		int NUMBER_OF_HANDS = 100000;
		for (int i = 0; i < NUMBER_OF_HANDS; i++) {
			Stack<Card> deck = Card.getShuffle(256);
			ArrayList<Card> hand = new ArrayList<>();
			for (int j = 0; j < 10; j++) {
				hand.add(deck.pop());
			}
			deadwoodCounter[deadwoodCount(hand)]++;
			hitCardCounter[numHitCards(new ArrayList<Card>(deck), hand)]++;
			ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(hand);
			numMeldCounter[organization.get(0).size()]++;
			pointSumMeldCounter[getPoints(organization.get(0))]++;
			numCombosCounter[organization.get(1).size()]++;
			pointSumCombosCounter[getPoints(organization.get(1))]++;
			numKnockCacheCounter[organization.get(2).get(0).size()]++;
			pointSumKnockCacheCounter[getPoints(organization.get(2))]++;
			numLoadCardsCounter[organization.get(3).get(0).size()]++;
			pointSumLoadCardsCounter[getPoints(organization.get(3))]++;

		}

		String fileName = "distributions-0.txt";
		File file = new File(fileName);
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			for (int[] map : counts) {
				String mapString = Arrays.toString(map);
				pw.println(mapString.substring(1, mapString.length() - 1));
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.err.println("No file.");
		}
		System.out.println("done.");
	}
}
