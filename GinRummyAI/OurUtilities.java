import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;

public class OurUtilities {

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
		// System.out.print("hit cards: ");
		for (Card c : possibleCards) {
			if (isHitCard(combos,melds,c)) {
				// System.out.print(c + " ");
				hitCount++;
			}
		}
		// System.out.println();
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
	 * @return bitstring
	 **/
	public static long cardToBitstring(Card c) {
		return 1L << c.getId();
	}

	/**
	 * converts a bitstring to its corresponding card
	 * @param cardBits - bitstring
	 * @return card
	 **/
	public static Card bitstringToCard(long cardBits) {
		int id = 0;
		while (cardBits != 1) {
			id++;
			cardBits /= 2;
		}
		return Card.allCards[id];
	}

	/**
	 * gets the actual card object from the Card class, based on the given card's ID
	 * @param c - card
	 * @return card object from the Card class
	 **/
	public static Card transformCard(Card c) {
		return Card.allCards[c.getId()];
	}

	/**
	 * returns an ArrayList of 52 shuffled cards
	 * @return an ArrayList of 52 shuffled cards
	 **/
	public static ArrayList<Card> getShuffle() {
		ArrayList<Card> deck = new ArrayList<Card>();
		for (int i = 0; i < 52; i++)
			deck.add(Card.allCards[i]);
		Collections.shuffle(deck);
		return deck;
	}

	/**
	 * gets the face (point) value of the given card. All face cards are worth 10 points
	 * @param c - current card
	 * @return the face (point) value of the given card
	 **/
	public static int getFaceValue(Card c) {
		int rank = c.getRank();
		if (rank >= 10)
			return 10;
		return rank;
	}


	//__objectively__ the best function
	/**
	 * returns the best orgnanization of a given hand
	 * 		index 0 - list of all best melds
	 * 		index 1 - list of all possible combos
	 * 		index 2 - list of all load cards
	 * 		index 3 - list of cards in the largest knock cash
	 * @param hand - cards in the hand
	 * @return the best hand organization
	 **/
	public static ArrayList<ArrayList<ArrayList<Card>>> getBestHandOrganization(ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> meldSets = GinRummyUtil.cardsToBestMeldSets(hand);

		int numLoad = 11;
		ArrayList<ArrayList<Card>> bestMelds = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestCombos = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestKnockCache = new ArrayList<ArrayList<Card>>();
		ArrayList<ArrayList<Card>> bestLoadCards = new ArrayList<ArrayList<Card>>(); // actually just an ArrayList<Card>

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

	/**
	 * returns the number of melds that are comprised of cards of the same rank
	 * @param melds - list of best melds in the player's hand
	 * @return the number of melds that are comprised of cards of the same rank
	 **/
	public static int numSetMelds(ArrayList<ArrayList<Card>> melds) {
		int numSets = 0;
		for (ArrayList<Card> meld : melds) {
			if (meld.get(0).getRank() == meld.get(1).getRank()) {
				numSets++;
			}
		}
		return numSets;
	}

	/**
	 * returns the number of melds that are comprised of cards of the same suit
	 * @param melds - list of best melds in the player's hand
	 * @return the number of melds that are comprised of cards of the same suit
	 **/
	public static int numRunMelds(ArrayList<ArrayList<Card>> melds) {
		int numRuns = 0;
		for (ArrayList<Card> meld : melds) {
			if (meld.get(0).getRank() != meld.get(1).getRank()) {
				numRuns++;
			}
		}
		return numRuns;
	}

	/**
	 * returns the number of combos that are comprised of cards of the same rank
	 * @param combos - list of best combos in the player's hand
	 * @return the number of combos that are comprised of cards of the same rank
	 **/
	public static int numSetCombos(ArrayList<ArrayList<Card>> combos) {
		int numSets = 0;
		for (ArrayList<Card> combo : combos) {
			if (combo.get(0).getRank() == combo.get(1).getRank()) {
				numSets++;
			}
		}
		return numSets;
	}

	/**
	 * returns the number of combos that are comprised of cards of the same suit
	 * @param combos - list of best combos in the player's hand
	 * @return the number of combos that are comprised of cards of the same suit
	 **/
	public static int numRunCombos(ArrayList<ArrayList<Card>> combos) {
		int numRuns = 0;
		for (ArrayList<Card> combo : combos) {
			if (combo.get(0).getRank() != combo.get(1).getRank()) {
				numRuns++;
			}
		}
		return numRuns;
	}

	/**
	 *
	 **/
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

	public static ArrayList<Card> nearbyCards(ArrayList<Card> cards) { // counting multiplicity
		ArrayList<Card> nearby = new ArrayList<Card>();
		for (Card c : cards) {
			int rank = c.getRank();
			int suit = c.getSuit();
			int id = c.getId();
			for (int i = 0; i < 4; i++) { // adds all cards of same rank
				Card toAdd = Card.allCards[id+(i-suit)*13];
				if (!cards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
			if (rank != 0) { // adds card lower in rank
				Card toAdd = Card.allCards[id-1];
				if (!cards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
			if (rank != 12) { //adds card highr in rank
				Card toAdd = Card.allCards[id+1];
				if (!cards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
		}
		return nearby;
	}

	public static ArrayList<Card> nearbyCards(Card c) {
		ArrayList<Card> nearby = new ArrayList<Card>();
		int rank = c.getRank();
		int suit = c.getSuit();
		int id = c.getId();
		for (int i = 0; i < 4; i++) { // adds all cards of same rank
			nearby.add(Card.allCards[id+(i-suit)*13]);
		}
		if (rank != 0) { // adds card lower in rank
			nearby.add(Card.allCards[id-1]);
		}
		if (rank != 12) { //adds card highr in rank
			nearby.add(Card.allCards[id+1]);
		}
		return nearby;
	}


	//   AS   (2S)   3S
	//  [AD]   2D    3D


	/**
	 * calculates all possible features about a player's current state
	 **/
	public static double[] calculateFeatures(Player player) {

		double current_player_score = player.scores[player.playerNum];

		double opponent_score = player.scores[1 - player.playerNum];

		double current_player_deadwood = deadwoodCount(player.hand);

		double current_player_num_hit_cards = numHitCards(player.possibleCards, player.hand);

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

		ArrayList<Card> nearby = nearbyCards(player.opponentHand);
		double num_nearby_opponent_cards = nearby.size();

		// double num_vis_cards_to_opponent = player.visibleCards.size();

		//double num_set_melds = numSetMelds(organization.get(0));
		//double num_run_melds = numRunMelds(organization.get(0));
		//double num_set_combos = numSetCombos(organization.get(1));
		//double num_run_combos = numRunCombos(organization.get(1));

		//list of possible features to add
		// num_drawn, op_num_drawn   (face up drawn cards for both players)
		// num_

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
				turns_taken,
				num_nearby_opponent_cards,
				// num_vis_cards_to_opponent,
				//num_set_melds
				//num_run_melds
				//num_set_combos
				//num_run_combos
		};
	}

	// public static double[] calcSimple2(SimplePlayer player) {
	//
	// 	double current_player_score = player.scores[player.playerNum];
	//
	// 	double opponent_score = player.scores[1 - player.playerNum];
	//
	// 	double current_player_deadwood = deadwoodCount(player.hand);
	//
	// 	double current_player_num_hit_cards = numHitCards(player.unknownCards, player.hand);
	//
	// 	double turns_taken = player.turn;
	//
	// 	ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(player.hand);
	//
	// 	double num_melds = organization.get(0).size();
	// 	double point_sum_melds = getPoints(organization.get(0));
	//
	// 	double num_combos = organization.get(1).size();
	// 	double point_sum_combos = getPoints(organization.get(1));
	//
	// 	double num_knock_cache = organization.get(2).get(0).size();
	// 	double point_sum_knock_cache = getPoints(organization.get(2));
	//
	// 	double num_load_cards = organization.get(3).get(0).size();
	// 	double point_sum_load_cards = getPoints(organization.get(3));
	//
	// 	return new double[] {
	// 						current_player_score,
	// 						opponent_score,
	// 						current_player_deadwood,
	// 						current_player_num_hit_cards,
	// 						num_melds,
	// 						point_sum_melds,
	// 						num_combos,
	// 						point_sum_combos,
	// 						num_knock_cache,
	// 						point_sum_knock_cache,
	// 						num_load_cards,
	// 						point_sum_load_cards,
	// 						turns_taken,
	// 	};
	// }

	public static double[] calculateSimpleFeatures(SimpleGinRummyPlayer player, ArrayList<Card> deck, int[] scores) {

		double current_player_score = scores[player.playerNum];

		double opponent_score = scores[1 - player.playerNum];

		double current_player_deadwood = deadwoodCount(player.cards);

		double current_player_num_hit_cards = numHitCards(deck, player.cards);

		return new double[] {
				current_player_score,
				opponent_score,
				current_player_deadwood,
				current_player_num_hit_cards
		};
	}

	public static void printAsSorted(ArrayList<Card> cards) {
		for (int i = 0; i < 52; i++) {
			for (Card c : cards) {
				if (c.getId() == i) {
					System.out.print(c+" ");
				}
			}
		}
	}

	// no whitespace at end of line
	public static ArrayList<Card> stringToHand(String hand) {
		ArrayList<Card> newHand = new ArrayList<Card>();
		String[] cards = hand.split(" ");
		for (String card : cards) {
			int rank = getIndex(Card.rankNames,card.substring(0,1));
			int suit = getIndex(Card.suitNames,card.substring(1));
			newHand.add(Card.allCards[Card.getId(rank,suit)]);
		}
		return newHand;
	}

	public static int getIndex(String[] array, String s) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(s)) {
				return i;
			}
		}
		return -1;
	}

	// C=0, H=1, S=2, D=3
	// AC 2C 3C AD 2D 3D AH 2H 3H 2S  (all melded, several optimal meld combinations)
	public static int[] set1ranks = {0,0,0,1,1,1,1,2,2,2};
	public static int[] set1suits = {0,0,0,3,2,3,1,1,1,2};

	// 2C 3C 4C 5H 6H 7H 8C 8D 8H KC (2 run, 1 set, 1 deadwood card)
	public static int[] set2ranks = {1,2,3,4,5,6,7,7,7,12};
	public static int[] set2suits = {0,0,0,1,1,1,0,3,1,0};

	// AC 2D 3H 4S 6C 6D 6H 7C 10H 10S (triangle shape, 1 combination)
	public static int[] set3ranks = {0,1,2,3,5,5,5,6,9,9};
	public static int[] set3suits = {0,3,1,2,0,3,1,0,1,2};

	// AC 2D 3H 4S 5C 6D 7H 8S 9C 10D (no hit cards)
	public static int[] set4ranks = {0,1,2,3,4,5,6,7,8,9};
	public static int[] set4suits = {0,3,1,2,0,1,1,2,0,3};

	// 2C 3C 2H 3H 7C 8C 7H 8H KC KH (18 hit cards)
	public static int[] set5ranks = {1,2,1,2,6,7,6,7,12,12};
	public static int[] set5suits = {0,0,1,1,0,0,1,1,0,1};

	// 4C 5C 6C 3S 4S 5S 2H 3H AS 2D
	// 1C 8C KC
	public static int[] set6ranks = {0,7,12};
	public static int[] set6suits = {0,0,0};

	// ArrayList of generated player states bunched in groups of two. The first state should be
	//   better than the second state by an amount substantial enough to superceed bias
	// Things that need to be added to each Player (used in calculateFeatures):
	// 	-
	// pair 1:
	public static ArrayList<Player> testPlayers = new ArrayList<Player>();

	public static int[][] testCards;
	static {
		testCards = new int[14][10];
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
		testCards[10] = set6ranks;
		testCards[11] = set6suits;

		int testVersion = BlackBox.ALPHA;
		int testType = BlackBox.LINEAR;

		Player p1 = new Player(testVersion, testType);
		p1.hand = stringToHand("AC 2C 3C 4H 5S 6D 8C 8H 9S TD");
		setupGenericPlayer(p1);

		Player p2 = new Player(testVersion, testType);
		p2.hand = stringToHand("AC 2C 3C 4H 5S 6D 7C 8H 9S TD");
		setupGenericPlayer(p2);

		//p1 state better because +1 combo +1 deadwood
		testPlayers.add(p1);
		testPlayers.add(p2);


		Player p3 = new Player(testVersion, testType);
		p3.hand = stringToHand("AC 2H 3S 4D 5C 6H TS 8D 9C TH");
		setupGenericPlayer(p3);

		Player p4 = new Player(testVersion, testType);
		setupGenericPlayer(p4);

		// p3 hand is probably better? p3 hand has +1 combo +3 deadwood
		testPlayers.add(p3);
		testPlayers.add(p4);



		Player p5 = new Player(testVersion, testType);
		p5.hand = stringToHand("AC AH AD 4C 6H 6S 8C 8D KC 4D");
		setupGenericPlayer(p5);

		Player p6 = new Player(testVersion, testType);
		p6.hand = stringToHand("AC AH AD 4C 6H 6S 8C 8D KC KD");
		setupGenericPlayer(p6);

		// p5 hand better than p6 because p5 has -6 deadwood
		testPlayers.add(p5);
		testPlayers.add(p6);


	}

	public static void setupGenericPlayer(Player p) {
		if (p.hand == null)
			p.hand = stringToHand("AC 2H 3S 4D 5C 6H 7S 8D 9C TH"); // trash hand
		p.playerNum = 0;
		p.scores = new int[]{10,10}; // a few hands in
		if (p.opponentHand == null)
			p.opponentHand = new ArrayList<Card>();
		ArrayList<Card> possible = new ArrayList<Card>();
		for (Card c : Card.allCards) {
			possible.add(c);
		}
		possible.removeAll(p.hand);
		possible.removeAll(p.opponentHand);
		p.possibleCards = possible;
		p.visibleCards = new ArrayList<Card>();
		p.turn = 3; // a few turns in
	}

	public static void testRegressionFit() {
		String data = "alpha-81.csv";
		int numFeatures = 4;


		File f = new File(data);
		ArrayList<String> dataLines = new ArrayList<String>();

		try {
			Scanner s = new Scanner(f);

			s.nextLine();
			while (s.hasNext()) {
				String str = s.nextLine();
				if (Math.random() < .1) {
					dataLines.add(str);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		double linCounter = 0;
		double gbmCounter = 0;
		double halfCounter = 0;
		for (int i = 0; i < dataLines.size(); i++) {
			String[] pieces = dataLines.get(i).split(",");
			// System.out.println(pieces.length);
			double[] dataline = new double[pieces.length];
			// System.out.println(dataline.length);
			for (int j = 0; j < pieces.length; j++) {
				dataline[j] = Double.parseDouble(pieces[j]);
			}

			double[] features = new double[dataline.length-3];
			for (int j = 0; j < features.length; j++) {
				features[j] = dataline[j+3];
			}

			//----------------------------------------------------------------------------------------------------------------------------------
			xgboost_817c4595_1ef8_4d35_aa14_3c1c731d4b88 xgb = new xgboost_817c4595_1ef8_4d35_aa14_3c1c731d4b88();
			double[] val = xgb.score0(features, new double[3]);
			// System.out.println("val= " + val[0]);

			double gbmPred = val[0];


			//--------------------------------------------------------------------------------------------------------------------------------
			ArrayList<Double> coefArr = BlackBox.coefficients.get(BlackBox.LINEAR).get(BlackBox.GAMMA);
			double linear_combination = 0;
			// System.out.println(features.length);
			for (int j = 1; j < numFeatures+1; j++) {
				linear_combination += coefArr.get(j) * features[j - 1];
			}
			double linPred = linear_combination + coefArr.get(0);

			double actualValue = dataline[2];

			double half = .5;

			halfCounter += Math.abs(half - actualValue) * Math.abs(half - actualValue);
			linCounter += Math.abs(linPred - actualValue) * Math.abs(linPred - actualValue);
			gbmCounter += Math.abs(gbmPred - actualValue) * Math.abs(gbmPred - actualValue);
			// if (Math.abs(linPred - actualValue) > Math.abs(gbmPred - actualValue)) {
			// 	linCounter++;
			// }
			// else {
			// 	gbmCounter++;
			// }

		}
		double n = dataLines.size();
		double gbm = Math.sqrt(gbmCounter)/n;
		double lin = Math.sqrt(linCounter)/n;
		double halfVal = Math.sqrt(halfCounter)/n;
		System.out.println("   GBM: " + gbm);
		System.out.println("LINEAR: " + lin);
		System.out.println("    .5: " + halfVal);




	}



	public static void testDecisions() {
		for (int i = 0; i < testPlayers.size(); i+=2) {
			double reg1 = BlackBox.regFunction(testPlayers.get(i));
			double reg2 = BlackBox.regFunction(testPlayers.get(i+1));
			System.out.println("reg1: "+reg1);
			System.out.println("reg2: "+reg2);
			System.out.println("reg1 > reg2: "+(reg1 > reg2));
			System.out.println();

		}
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

			ArrayList<Card> nearby = nearbyCards(hand);
			System.out.println("\tNearby Cards: " + nearby);
			System.out.println("\tNum nearby Cards: " + nearby.size());

			System.out.println("\tNumber of set melds: " + numSetMelds(organization.get(0)));
			System.out.println("\tNumber of run melds: " + numRunMelds(organization.get(0)));
			System.out.println("\tNumber of set combos: " + numSetCombos(organization.get(1)));
			System.out.println("\tNumber of run combos: " + numRunCombos(organization.get(1)));

			ArrayList<Card> deck = new ArrayList<>();
			for (int i = 0; i < 52; i++) {
				deck.add(new Card(i % 13, i / 13));
			}

			long deckbits = GinRummyUtil.cardsToBitstring(deck);
			deck = GinRummyUtil.bitstringToCards(deckbits);

			deck.removeAll(hand);

			System.out.println("Hit Count: " + numHitCards(deck, hand));
			System.out.println("Alpha was playing solitaire, but Gamma... Gamma is playing Gin Rummy.");
			// System.out.println("Number of options: " + numOptions(deck, hand));
		}


	}





	public static void main(String[] args) {

		testRegressionFit();
		// testDecisions();
		// testUtils();

		// find distributions

		// int[] deadwoodCounter = new int[98 + 1];
		// int[] hitCardCounter = new int[18 + 1];
		// int[] numMeldCounter = new int[3 + 1];
		// int[] pointSumMeldCounter = new int[100 + 1];
		// int[] numCombosCounter = new int[50 + 1];
		// int[] pointSumCombosCounter = new int[250 + 1];
		// int[] numKnockCacheCounter = new int[5 + 1];
		// int[] pointSumKnockCacheCounter = new int[10 + 1];
		// int[] numLoadCardsCounter = new int[8 + 1];
		// int[] pointSumLoadCardsCounter = new int[100 + 1];
		//
		// int[][] counts = {deadwoodCounter, hitCardCounter, numMeldCounter,
		// 	pointSumMeldCounter, numCombosCounter, pointSumCombosCounter,
		// 	numKnockCacheCounter, pointSumKnockCacheCounter, numLoadCardsCounter,
		// 	pointSumLoadCardsCounter};
		//
		// int NUMBER_OF_HANDS = 10000000;
		// for (int i = 0; i < NUMBER_OF_HANDS; i++) {
		// 	Stack<Card> deck = Card.getShuffle(256);
		// 	ArrayList<Card> hand = new ArrayList<>();
		// 	for (int j = 0; j < 10; j++) {
		// 		hand.add(deck.pop());
		// 	}
		// 	deadwoodCounter[deadwoodCount(hand)]++;
		// 	hitCardCounter[numHitCards(new ArrayList<Card>(deck), hand)]++;
		// 	ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(hand);
		// 	numMeldCounter[organization.get(0).size()]++;
		// 	pointSumMeldCounter[getPoints(organization.get(0))]++;
		// 	numCombosCounter[organization.get(1).size()]++;
		// 	pointSumCombosCounter[getPoints(organization.get(1))]++;
		// 	numKnockCacheCounter[organization.get(2).get(0).size()]++;
		// 	pointSumKnockCacheCounter[getPoints(organization.get(2))]++;
		// 	numLoadCardsCounter[organization.get(3).get(0).size()]++;
		// 	pointSumLoadCardsCounter[getPoints(organization.get(3))]++;
		//
		// }
		//
		// String fileName = "distributions-10000000.txt";
		// File file = new File(fileName);
		// PrintWriter pw;
		// try {
		// 	pw = new PrintWriter(file);
		// 	for (int[] map : counts) {
		// 		String mapString = Arrays.toString(map);
		// 		pw.println(mapString.substring(1, mapString.length() - 1));
		// 	}
		// 	pw.close();
		// } catch (FileNotFoundException e) {
		// 	System.err.println("No file.");
		// }
		// System.out.println("done.");
	}
}
