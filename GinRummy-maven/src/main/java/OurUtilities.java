import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ginrummy.*;

//import hex.genmodel.easy.EasyPredictModelWrapper;
//import hex.genmodel.easy.RowData;
//import hex.genmodel.easy.prediction.BinomialModelPrediction;
//import hex.genmodel.easy.prediction.RegressionModelPrediction;

@SuppressWarnings("unchecked")
public class OurUtilities {

	static long[] cardBitstrings = new long[Card.NUM_CARDS];

	static ArrayList<ArrayList<Long>> meldBitstrings;

	static HashMap<Long, ArrayList<Card>> meldBitstringToCardsMap;

	static {
		// initialize cardBitStrings
		long bitstring = 1L;
		for (int i = 0; i < Card.NUM_CARDS; i++) {
			cardBitstrings[i] = bitstring;
			bitstring <<= 1;
		}

		// build list of lists of meld bitstring where each subsequent meld bitstring in the list is a superset of previous meld bitstrings
		meldBitstrings = new ArrayList<ArrayList<Long>>();
		meldBitstringToCardsMap = new HashMap<Long, ArrayList<Card>>();

		// build run meld lists
		for (int suit = 0; suit < Card.NUM_SUITS; suit++) {
			for (int runRankStart = 0; runRankStart < Card.NUM_RANKS - 2; runRankStart++) {
				ArrayList<Long> bitstringList = new ArrayList<Long>();
				ArrayList<Card> cards = new ArrayList<Card>();
				Card c = Card.getCard(runRankStart, suit);
				cards.add(c);
				long meldBitstring = cardBitstrings[c.getId()];
				c = Card.getCard(runRankStart + 1, suit);
				cards.add(c);
				meldBitstring |= cardBitstrings[c.getId()];
				for (int rank = runRankStart + 2; rank < Card.NUM_RANKS; rank++) {
					c = Card.getCard(rank, suit);
					cards.add(c);
					meldBitstring |= cardBitstrings[c.getId()];
					bitstringList.add(meldBitstring);
					meldBitstringToCardsMap.put(meldBitstring, (ArrayList<Card>) cards.clone());
				}
				meldBitstrings.add(bitstringList);
			}
		}

		// build set meld lists
		for (int rank = 0; rank < Card.NUM_RANKS; rank++) {
			ArrayList<Card> cards = new ArrayList<Card>();
			for (int suit = 0; suit < Card.NUM_SUITS; suit++)
				cards.add(Card.getCard(rank,  suit));
			for (int suit = 0; suit <= Card.NUM_SUITS; suit++) {
				ArrayList<Card> cardSet = (ArrayList<Card>) cards.clone();
				if (suit < Card.NUM_SUITS)
					cardSet.remove(Card.getCard(rank,  suit));
				ArrayList<Long> bitstringList = new ArrayList<Long>();
				long meldBitstring = 0L;
				for (Card card : cardSet)
					meldBitstring |= cardBitstrings[card.getId()];
				bitstringList.add(meldBitstring);
				meldBitstringToCardsMap.put(meldBitstring, cardSet);
				meldBitstrings.add(bitstringList);
			}
		}
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
		ArrayList<Long> bitStrings = GinRummyUtil.cardsToAllMeldBitstrings(comboCards); // doesn't this have duplicates
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
		if (rank >= 9)
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

	public static ArrayList<Card> nearbyCardsInHand(ArrayList<Card> cards, ArrayList<Card> possibleCards) { // counting multiplicity, also our hand would be passed in for possibleCards if we want a feature blocking their play
		ArrayList<Card> nearby = new ArrayList<Card>();
		for (Card c : cards) {
			int rank = c.getRank();
			int suit = c.getSuit();
			int id = c.getId();
			for (int i = 0; i < 4; i++) { // adds all cards of same rank
				Card toAdd = Card.allCards[id+(i-suit)*13];
				if (!cards.contains(toAdd) && possibleCards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
			if (rank != 0) { // adds card lower in rank
				Card toAdd = Card.allCards[id-1];
				if (!cards.contains(toAdd) && possibleCards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
			if (rank != 12) { //adds card higher in rank
				Card toAdd = Card.allCards[id+1];
				if (!cards.contains(toAdd) && possibleCards.contains(toAdd)) {
					nearby.add(toAdd);
				}
			}
		}
		return nearby;
	}


	// public static ArrayList<Integer> nearbyCards(int c) {
	// 	ArrayList<Integer> nearby = new ArrayList<Integer>();
	// 	int rank = c.getRank();
	// 	int suit = c.getSuit();
	// 	int id = c.getId();
	// 	for (int i = 0; i < 4; i++) { // adds all cards of same rank
	// 		nearby.add(id+(i-suit)*13);
	// 	}
	// 	if (rank != 0) { // adds card lower in rank
	// 		nearby.add(id-1);
	// 	}
	// 	if (rank != 12) { //adds card higher in rank
	// 		nearby.add(id+1);
	// 	}
	// 	return nearby;
	// }



	public static double discardDanger(Card c, HandEstimator est) {
		double[] prob = est.prob;
		double danger = 0;
		int id = c.getId();
		int rank = c.getRank();
		int suit = c.getSuit();
		int from = id;
		int to = id;
		for (int i = id-1; i >= id-rank; i--) { // sets the lower bound for the run (inclusive)
			if (prob[i] == 0) {
				from = i+1;
				break;
			}
			if (i == id-rank) {
				from = 0;
			}
		}
		for (int i = id+1; i < id-rank+Card.NUM_RANKS; i++) { // sets the upper bound for the run (inclusive)
			if (prob[i] == 0) {
				to = i-1;
				break;
			}
			if (i == id - rank + Card.NUM_RANKS - 1) {
				to = i;
			}
		}
		ArrayList<Card> possibleRunCards = new ArrayList<Card>();
		for (int i = from; i <= to; i++) {
			possibleRunCards.add(Card.allCards[i]);
		}
		// possible run melds
		ArrayList<ArrayList<Card>> meldPossibilities = GinRummyUtil.cardsToAllMelds(possibleRunCards);
		for (ArrayList<Card> meld : meldPossibilities) {
			if (meld.contains(c)) {
				double addedDanger = 1;
				for (Card c1 : meld) {
					if (c1 != c)
						addedDanger *= prob[c1.getId()];
				}
				// addedDanger *= meld.size(); //whatever weight we assign to the probability of that size run occurring
				danger += addedDanger;
			}
		}

		ArrayList<Card> possibleSetCards = new ArrayList<Card>();
		for (int i = 0; i < Card.NUM_SUITS; i++) {
			int idNum = rank + i * Card.NUM_RANKS;
			if (id == idNum || prob[idNum] != 0) {
				possibleSetCards.add(Card.allCards[idNum]);
			}
		}
		// possible set melds
		meldPossibilities = GinRummyUtil.cardsToAllMelds(possibleSetCards);
		for (ArrayList<Card> meld : meldPossibilities) {
			if (meld.contains(c)) {
				double addedDanger = 1;
				for (Card c1 : meld) {
					if (c1 != c)
						addedDanger *= prob[c1.getId()];
				}
				// addedDanger *= meld.size(); //whatever weight we assign to the probability of that size set occurring
				danger += addedDanger;
			}
		}

		return danger;
	}


	public static double getDangerOfDiscard(Card c, HandEstimator est) {
		// get possible meld bit strings
		ArrayList<Long> possibleMeldBitstrings = new ArrayList<Long>();
		for (ArrayList<Long> meldBitstringList : meldBitstrings) {
			for (long meldBitstring : meldBitstringList) {
				long cardBitstring = cardBitstrings[c.getId()];
				if ((meldBitstring & cardBitstring) == cardBitstring) {
					possibleMeldBitstrings.add(meldBitstring);
				}
			}
		}
		// find danger of discard
		double danger = 0;
		for (long meldBitstring : possibleMeldBitstrings) {
			double meldDanger = 1;
			int cardID = 0;
			while (meldBitstring > 0) {
				if (cardID != c.getId())
					meldDanger *= (meldBitstring & 1) == 1 ? est.prob[cardID] : 1;
				meldBitstring >>= 1;
				cardID++;
			}
			danger += meldDanger;
		}
		return danger;
	}







	/**
	 * calculates all possible features about a player's current state
	 **/
	public static double[] calculateFeatures(Player player) {

		double current_player_score = player.scores[player.playerNum];

		double opponent_score = player.scores[1 - player.playerNum];
		double current_player_deadwood = deadwoodCount(player.hand);

		ArrayList<Card> possibleCards = new ArrayList<>(player.unknownCards);
		possibleCards.addAll(player.opponentHand);
		double current_player_num_hit_cards = numHitCards(possibleCards, player.hand);

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

		//System.out.println("op hand: "+player.opponentHand);
		//System.out.println("our hand: "+player.hand);
		ArrayList<Card> nearby = nearbyCardsInHand(player.opponentHand, player.hand);
		//System.out.println("nearby: "+nearby);
		double num_nearby_opponent_cards = nearby.size();

		Card discardedCard = player.discardedCards.peek();
		double discard_danger = getDangerOfDiscard(discardedCard, player.estimator);

		// double pleaseputagoodnamehere =
		//double num_set_melds = numSetMelds(organization.get(0));
		//double num_run_melds = numRunMelds(organization.get(0));
		//double num_set_combos = numSetCombos(organization.get(1));
		//double num_run_combos = numRunCombos(organization.get(1));

		//list of possible features to add
		// num_drawn, op_num_drawn   (face up drawn cards for both players)
		// num_

		return new double[] {
				current_player_score,           // 0
				opponent_score,                 // 1
				current_player_deadwood,        // 2
				current_player_num_hit_cards,   // 3
				// alpha to here
				num_melds,                      // 4
				point_sum_melds,                // 5
				num_combos,                     // 6
				point_sum_combos,               // 7
				num_knock_cache,                // 8
				point_sum_knock_cache,          // 9
				num_load_cards,                 // 10
				point_sum_load_cards,           // 11
				turns_taken,                    // 12
				// beta to here
				num_nearby_opponent_cards,      // 13
				// gamma to here
				discard_danger,                 // 14
				// delta to here

				//num_set_melds
				//num_run_melds
				//num_set_combos
				//num_run_combos
		};
	}

	public static double[] calculateSimpleFeatures(OurSimpleGinRummyPlayer players, ArrayList<Card> deck, int[] scores) {

		double current_player_score = scores[players.playerNum];

		double opponent_score = scores[1 - players.playerNum];

		double current_player_deadwood = deadwoodCount(players.cards);

		double current_player_num_hit_cards = numHitCards(deck, players.cards);

		return new double[] {
				current_player_score,
				opponent_score,
				current_player_deadwood,
				current_player_num_hit_cards
		};
	}

	public static double[][] getCardProbImageMatrix(Player player) {
		HandEstimator est = player.estimator;
		double[][] ret = new double[17][13];
		int[] order = {0, 1, 2, 3, 0, 1, 2, 0, 3, 1, 2, 0, 1, 3, 2, 0, 1};
		for (int i = 0; i < 17; i++) {
			double[] row;
			if (i < 4) {
				row = new double[13];
				for (int j = 0; j < 13; j++) {
					row[j] = est.prob[order[i] * 13 + j];
				}
			} else {
				row = (double[]) ret[order[i]].clone();
			}
			ret[i] = row;
		}
		return ret;
	}



	public static int[][] getCardMatImageMatrix(Player player) {
		int[][] ret = new int[17][13];
		int[] order = {0, 1, 2, 3, 0, 1, 2, 0, 3, 1, 2, 0, 1, 3, 2, 0, 1};
		for (int i = 0; i < 17; i++) {
			int[] row;
			if (i < 4) {
				row = new int[13];
				for (int j = 0; j < 13; j++) {
					row[j] = player.cardMat[order[i] * 13 + j];
				}
			} else {
				row = (int[]) ret[order[i]].clone();
			}
			ret[i] = row;
		}
		return ret;
	}

	public static int[][] getShankarMatrix(int[][] shankarr) {
		int[][] ret = new int[17][13];
		int[] order = {0, 1, 2, 3, 0, 1, 2, 0, 3, 1, 2, 0, 1, 3, 2, 0, 1};
		for (int i = 0; i < 17; i++) {
			int[] row;
			if (i < 4) {
				row = shankarr[i].clone();
			} else {
				row = (int[]) ret[order[i]].clone();
			}
			ret[i] = row;
		}
		return ret;
	}



	public static int[][] handTo2DBitArray(ArrayList<Card> hand) {
		int[][] handArr = new int[4][13];
		for (Card card : hand) {
			handArr[card.getSuit()][card.getRank()] = 1;
		}
		return handArr;
	}


	public static double[] handTo1DArray(ArrayList<Card> hand) {
		double[] handDouble = new double[52];

		for (Card card : hand) {
			handDouble[card.getId()] = 1;
		}
		return handDouble;
	}


	public static int numGinHits(ArrayList<Card> possibleCards, ArrayList<Card> hand) {
		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
		if (bestMeldSets.isEmpty()) {
			bestMeldSets.add(new ArrayList<ArrayList<Card>>());
		}
		int ghitCount = 0;
		for (ArrayList<ArrayList<Card>> meldSet : bestMeldSets) {
			ArrayList<ArrayList<Card>> combos = getCombos(meldSet, hand);
			ArrayList<Card> ghits = getGinHits(hand, combos, meldSet, possibleCards);
			if (ghits.size() > ghitCount) {
				ghitCount = ghits.size();
			}
		}
		return ghitCount;
	}

	public static ArrayList<Card> getGinHits(ArrayList<Card> hand, ArrayList<ArrayList<Card>> combos, ArrayList<ArrayList<Card>> melds, ArrayList<Card> possibleCards) {
		ArrayList<Card> ghits = new ArrayList<Card>();
		for (Card c : possibleCards) {
			if (isHitCard(combos,melds,c)) {
				boolean gin = false;
				hand.add(c);
				for (int i = 0; i < hand.size()-1; i++) {
					Card c1 = hand.remove(0);
					ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
					if (bestMeldSets.isEmpty()) {
						hand.add(c1);
						break;
					}
					ArrayList<ArrayList<Card>> set1 = bestMeldSets.get(0);
					int count = 0;
					for (ArrayList<Card> meld : set1)
						count+=meld.size();
					if (count == 10) {
						gin = true;
						hand.add(c1);
						break;
					}
					hand.add(c1);
				}
				hand.remove(c);
				if (gin) {
					ghits.add(c);
				}
			}
		}
		return ghits;
	}

	public static int getGinRating(ArrayList<Card> hand, ArrayList<Card> unknownCards) {
			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
			ArrayList<ArrayList<Card>> melds;
			if (bestMeldSets.isEmpty()) {
				return 0;
			}
			else {
				melds = bestMeldSets.get(0);
			}

			ArrayList<Card> unmeldedCards = cardsNotInMeld(melds,hand);
			if (unmeldedCards.size() == 1) {
				return numHitCards(unknownCards, hand);
			}
			ArrayList<Card> ginHits = new ArrayList<Card>();
			for (int i = 0; i < unknownCards.size(); i++) {
				Card possHit = unknownCards.get(i);
				unmeldedCards.add(possHit);
				ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets2 = GinRummyUtil.cardsToBestMeldSets(unmeldedCards);
				ArrayList<ArrayList<Card>> melds2;
				if (!bestMeldSets2.isEmpty()) {
					melds2 = bestMeldSets2.get(0);
					ArrayList<Card> unmeldedCards2 = cardsNotInMeld(melds2,unmeldedCards);
					if (unmeldedCards2.size() <= 1) {
						ginHits.add(unknownCards.get(i));
					}
				}
				unmeldedCards.remove(possHit);
			}
			return ginHits.size();
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

//	// C=0, H=1, S=2, D=3
//	// AC 2C 3C AD 2D 3D AH 2H 3H 2S  (all melded, several optimal meld combinations)
//	public static int[] set1ranks = {0,0,0,1,1,1,1,2,2,2};
//	public static int[] set1suits = {0,0,0,3,2,3,1,1,1,2};
//
//	// 2C 3C 4C 5H 6H 7H 8C 8D 8H KC (2 run, 1 set, 1 deadwood card)
//	public static int[] set2ranks = {1,2,3,4,5,6,7,7,7,12};
//	public static int[] set2suits = {0,0,0,1,1,1,0,3,1,0};
//
//	// AC 2D 3H 4S 6C 6D 6H 7C 10H 10S (triangle shape, 1 combination)
//	public static int[] set3ranks = {0,1,2,3,5,5,5,6,9,9};
//	public static int[] set3suits = {0,3,1,2,0,3,1,0,1,2};
//
//	// AC 2D 3H 4S 5C 6D 7H 8S 9C 10D (no hit cards)
//	public static int[] set4ranks = {0,1,2,3,4,5,6,7,8,9};
//	public static int[] set4suits = {0,3,1,2,0,1,1,2,0,3};
//
//	// 2C 3C 2H 3H 7C 8C 7H 8H KC KH (18 hit cards)
//	public static int[] set5ranks = {1,2,1,2,6,7,6,7,12,12};
//	public static int[] set5suits = {0,0,1,1,0,0,1,1,0,1};
//
//	// 4C 5C 6C 3S 4S 5S 2H 3H AS 2D
//	// 1C 8C KC
//	public static int[] set6ranks = {0,7,12};
//	public static int[] set6suits = {0,0,0};
//
//	// ArrayList of generated player states bunched in groups of two. The first state should be
//	//   better than the second state by an amount substantial enough to superceed bias
//	// Things that need to be added to each Player (used in calculateFeatures):
//	// 	-
//	// pair 1:
//	public static ArrayList<Player> testPlayers = new ArrayList<Player>();
//
//	public static int[][] testCards;
//	static {
//		testCards = new int[14][10];
//		testCards[0] = set1ranks;
//		testCards[1] = set1suits;
//		testCards[2] = set2ranks;
//		testCards[3] = set2suits;
//		testCards[4] = set3ranks;
//		testCards[5] = set3suits;
//		testCards[6] = set4ranks;
//		testCards[7] = set4suits;
//		testCards[8] = set5ranks;
//		testCards[9] = set5suits;
//		testCards[10] = set6ranks;
//		testCards[11] = set6suits;
//
//		int testVersion = BlackBox.GAMMA;
//		int testType = BlackBox.KERAS;
//
//
//		String[] handOnlyTests = {
//			"AC 2C 3C 4H 5S 6D 8C 8H 9S TD", // +1 combo +1 deadwood
//			"AC 2C 3C 4H 5S 6D 7C 8H 9S TD",
//
//			"AC 2H 3S 4D 5C 6H TS 8D 9C TH", // +1 combo +3 deadwood
//			"AC 2H 3S 4D 5C 6H 7S 8D 9C TH",
//
//			"AC AH AD 4C 6H 6S 8C 8D KC 4D", // -6 deadwood
//			"AC AH AD 4C 6H 6S 8C 8D KC KD",
//
//			"AC 2C 2S 4H 5H 7S 8C 9H TS JD", //-1 deadwood +2 combos
//			"AC 3D 2S 4H 5H 7S 8C 9H TS JD",
//
//			"2C 2H 2S 4D 5C 6H 7S 8D 9C TH", // +1 meld -1 combo
//			"AC 2H AS 4D 5C 6H 7S 8D 9C TH",
//
//			"AC 2H 3S 4D 4C 6H 7S 8D 9C TH", // +1 combo -1 deadwood
//			"AC 2H 3S 4D 5C 6H 7S 8D 9C TH",
//
//			"AC 2C 2D 3D 5C 6H 7S 8D 9C TH", // +2 combo -1 deadwood
//			"AC 2C 3S DD 5C 6H 7S 8D 9C TH",
//
//			"AC 2H 3S 3D 5C 6H 7S 8D 9C TH", // +1 combo -7 deadwood
//			"AC 2H 3S JS 5C 6H 7S 8D 9C TH"
//		};
//
//		for (String s : handOnlyTests) {
//			Player p = new Player(testVersion, testType);
//			p.hand = stringToHand(s);
//			setupGenericPlayer(p);
//			testPlayers.add(p);
//		}
//
//	}
//
//	public static void setupGenericPlayer(Player p) {
//		if (p.hand == null)
//			p.hand = stringToHand("AC 2H 3S 4D 5C 6H 7S 8D 9C TH"); // trash hand
//		Card[] handArr = new Card[10];
//		int i = 0;
//		for (Card c : p.hand) {
//			handArr[i] = c;
//			i++;
//		}
//		p.startGame(0, 0, handArr);
//		p.playerNum = 0;
//		p.scores = new int[]{0,0}; // a few hands in
//		if (p.opponentHand == null)
//			p.opponentHand = new ArrayList<Card>();
//		p.turn = 1; // a few turns in
//		if (p.discardedCards.isEmpty()) {
//			p.discardedCards.push(Card.allCards[38]); //KS
//		}
//
//	}

//	public static void testRegressionFit() {
//		String data = "alpha-81.csv";
//		int numFeatures = 4;
//
//
//		File f = new File(data);
//		ArrayList<String> dataLines = new ArrayList<String>();
//
//		try {
//			Scanner s = new Scanner(f);
//
//			s.nextLine();
//			while (s.hasNext()) {
//				String str = s.nextLine();
//				if (Math.random() < .1) {
//					dataLines.add(str);
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		double linCounter = 0;
//		double gbmCounter = 0;
//		double halfCounter = 0;
//		for (int i = 0; i < dataLines.size(); i++) {
//			String[] pieces = dataLines.get(i).split(",");
//			// System.out.println(pieces.length);
//			double[] dataline = new double[pieces.length];
//			// System.out.println(dataline.length);
//			for (int j = 0; j < pieces.length; j++) {
//				dataline[j] = Double.parseDouble(pieces[j]);
//			}
//
//			double[] features = new double[dataline.length-3];
//			for (int j = 0; j < features.length; j++) {
//				features[j] = dataline[j+3];
//			}
//
//			//----------------------------------------------------------------------------------------------------------------------------------
//			xgboost_817c4595_1ef8_4d35_aa14_3c1c731d4b88 xgb = new xgboost_817c4595_1ef8_4d35_aa14_3c1c731d4b88();
//			double[] val = xgb.score0(features, new double[3]);
//			// System.out.println("val= " + val[0]);
//
//			double gbmPred = val[0];
//
//
//			//--------------------------------------------------------------------------------------------------------------------------------
//			ArrayList<Double> coefArr = BlackBox.coefficients.get(BlackBox.LINEAR).get(BlackBox.GAMMA);
//			double linear_combination = 0;
//			// System.out.println(features.length);
//			for (int j = 1; j < numFeatures+1; j++) {
//				linear_combination += coefArr.get(j) * features[j - 1];
//			}
//			double linPred = linear_combination + coefArr.get(0);
//
//			double actualValue = dataline[2];
//
//			double half = .5;
//
//			halfCounter += Math.abs(half - actualValue) * Math.abs(half - actualValue);
//			linCounter += Math.abs(linPred - actualValue) * Math.abs(linPred - actualValue);
//			gbmCounter += Math.abs(gbmPred - actualValue) * Math.abs(gbmPred - actualValue);
//			// if (Math.abs(linPred - actualValue) > Math.abs(gbmPred - actualValue)) {
//			// 	linCounter++;
//			// }
//			// else {
//			// 	gbmCounter++;
//			// }
//
//		}
//		double n = dataLines.size();
//		double gbm = Math.sqrt(gbmCounter)/n;
//		double lin = Math.sqrt(linCounter)/n;
//		double halfVal = Math.sqrt(halfCounter)/n;
//		System.out.println("   GBM: " + gbm);
//		System.out.println("LINEAR: " + lin);
//		System.out.println("    .5: " + halfVal);
//
//
//
//
//	}

//
//	public static void testDecisions() {
//		for (int i = 0; i < testPlayers.size(); i+=2) {
//			double reg1 = BlackBox.regFunction(testPlayers.get(i));
//			double reg2 = BlackBox.regFunction(testPlayers.get(i+1));
//			System.out.println("reg1: "+reg1);
//			System.out.println("reg2: "+reg2);
//			System.out.println("reg1 > reg2: "+(reg1 > reg2));
//			System.out.println();
//
//		}
//	}

//	public static void testUtils() {
//
//		Card c = new Card(10,3);
//		System.out.println(c);
//		System.out.println(transformCard(c));
//
//		for (int r = 0; r < testCards.length; r+=2) {
//			System.out.println("------------------CARD SET "+ r/2 +"------------------------");
//			ArrayList<Card> hand = new ArrayList<>();
//			for (int i = 0; i < testCards[r].length; i++) {
//				hand.add(new Card(testCards[r][i], testCards[r+1][i]));
//			}
//			long handbits = GinRummyUtil.cardsToBitstring(hand);
//			hand = GinRummyUtil.bitstringToCards(handbits);
//
//			System.out.println("Hand: " + hand);
//
//			// Test Deadwood
//			System.out.println("Deadwood Count: " + deadwoodCount(hand));
//
//			// Test getBestHandOrganization
//			ArrayList<ArrayList<ArrayList<Card>>> organization = getBestHandOrganization(hand);
//			System.out.println("Best Organization: " + organization);
//			System.out.println("\tBest Melds: " + organization.get(0));
//			System.out.println("\tBest Combos: " + organization.get(1));
//			System.out.println("\tBest KnockCash$: " + organization.get(2));
//			System.out.println("\tBest Loads: " + organization.get(3));
//
//			// ArrayList<Card> nearby = nearbyCards(hand);
//			// System.out.println("\tNearby Cards: " + nearby);
//			// System.out.println("\tNum nearby Cards: " + nearby.size());
//
//			System.out.println("\tNumber of set melds: " + numSetMelds(organization.get(0)));
//			System.out.println("\tNumber of run melds: " + numRunMelds(organization.get(0)));
//			System.out.println("\tNumber of set combos: " + numSetCombos(organization.get(1)));
//			System.out.println("\tNumber of run combos: " + numRunCombos(organization.get(1)));
//
//			ArrayList<Card> deck = new ArrayList<>();
//			for (int i = 0; i < 52; i++) {
//				deck.add(new Card(i % 13, i / 13));
//			}
//
//			long deckbits = GinRummyUtil.cardsToBitstring(deck);
//			deck = GinRummyUtil.bitstringToCards(deckbits);
//
//			deck.removeAll(hand);
//
//			System.out.println("Hit Count: " + numHitCards(deck, hand));
//			System.out.println("Alpha was playing solitaire, but Gamma... Gamma is playing Gin Rummy.");
//			// System.out.println("Number of options: " + numOptions(deck, hand));
//		}
//
//
//	}
//
//



	public static void main(String[] args) {

		// testRegressionFit();
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
