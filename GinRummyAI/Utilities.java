import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Utilities {

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

   public static int deadwoodCount(ArrayList<Card> hand) {
      ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
      if (bestMeldSets.isEmpty()) {
         return GinRummyUtil.getDeadwoodPoints(hand);
      }
      ArrayList<ArrayList<Card>> chosenMeldSet = bestMeldSets.get(0);
      // System.out.println("Meld sets: " + bestMeldSets.size());
//      for(ArrayList<Card> meld : chosenMeldSet) {
//           System.out.println("meld: " + meld);
//      }
      int deadwood = GinRummyUtil.getDeadwoodPoints(chosenMeldSet,hand);
      return deadwood;
   }

   // TODO: for now we will pass any cards even if we know the opponent has them
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

   public static boolean isHitCard(Card c, ArrayList<Card> hand, ArrayList<Card> possibleCards) {
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
   public static boolean isHitCard(ArrayList<ArrayList<Card>> combos, ArrayList<ArrayList<Card>> melds, Card c) {
      return meldFromCombo(c,combos) || canBeMeldedIn(c,melds);
   }
   
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

   // given combinations and melds in the hand, and possible cards in the deck
   // returns the hit count of all combinations and melds
   public static int getHitCount(ArrayList<ArrayList<Card>> combos, ArrayList<ArrayList<Card>> melds, ArrayList<Card> possibleCards) {
	  int hitCount = 0;
      for (Card c : possibleCards) {
         if (isHitCard(combos,melds,c)) {
            hitCount++;
         }
      }
      return hitCount;
   }

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

   public static ArrayList<ArrayList<Card>> getCombos(ArrayList<ArrayList<Card>> melds, ArrayList<Card> hand) {
	  ArrayList<Card> otherCards = cardsNotInSet(melds,hand); //unmelded cards in hand
      ArrayList<ArrayList<Card>> combos = new ArrayList<ArrayList<Card>>();
      while (otherCards.size() > 0) { //iterates through unmelded cards in hand checking for combinations
         Card c1 = otherCards.get(0);
         for (int i = 1; i < otherCards.size(); i++) {
            Card c2 = otherCards.get(i);
            if (c1.getRank() == c2.getRank() ||
                  c1.getSuit() == c2.getSuit() && Math.abs(c1.getRank() - c2.getRank()) == 1) {
               ArrayList<Card> combo = new ArrayList<Card>();
               combo.add(c1);
               combo.add(c2);
               combos.add(combo);
            }
         }
         otherCards.remove(0);
      }
      return combos;
   }

   public static ArrayList<Card> cardsNotInSet(ArrayList<ArrayList<Card>> set, ArrayList<Card> hand) {
      ArrayList<Card> copy = new ArrayList<Card>();
      for (int i = 0; i < hand.size(); i++) {
         copy.add(hand.get(i));
      }
      for (ArrayList<Card> cards : set) {
         for (Card c : cards) {
            copy.remove(c);
         }
      }
      return copy;
   }


   public static int numOptions(ArrayList<Card> possibleCards, ArrayList<Card> hand) {
      int deadwood = deadwoodCount(hand);
      int opCount = 0;
      for (Card c : possibleCards) {
         hand.add(c);
         int newdeadwood = deadwoodCount(hand);
         if ((newdeadwood) < deadwood) {
            opCount++;
         }
         hand.remove(c);
      }
      return opCount;
   }

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


         //Test deadwoodCount
         System.out.println("Deadwood Count: " + deadwoodCount(hand));

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

   public static void main(String[] args) {

      testUtils();

      // Guesses:
      // taylor - 5 (integer value)
      // peter  - 6
      // ryz - 7 (expectation value)
      // hoang  - 8


      // find average number of hit cards of a hand
      int NUMBER_OF_HANDS = 1000000;
      double average = 0;
      for (int i = 0; i < NUMBER_OF_HANDS; i++) {
         Stack<Card> deck = Card.getShuffle(256);
         ArrayList<Card> hand = new ArrayList<>();
         for (int j = 0; j < 10; j++) {
            hand.add(deck.pop());
         }
         int numHitCards = numHitCards(new ArrayList(deck), hand);
         average += numHitCards;
      }
      average /= NUMBER_OF_HANDS;


      System.out.println("\n\nAverage: " + average);



   }
}
