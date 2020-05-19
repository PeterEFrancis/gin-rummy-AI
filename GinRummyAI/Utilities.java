import java.util.ArrayList;

public class Utilities {

   // AC 2C 3C AD 2D 3D AH 2H 3H 2S  (all melded, several optimal meld combinations)
   public static int[] set1ranks = {0,0,0,1,1,1,1,2,2,2};
   public static int[] set1suits = {0,1,2,0,1,2,3,0,1,2};

   // 2C 3C 4C 5H 6H 7H 8C 8D 8H KC (2 run, 1 set, 1 deadwood card)
   public static int[] set2ranks = {0,0,0,2,2,2,0,1,2,0};
   public static int[] set2suits = {1,2,3,4,5,6,7,7,7,12};

   // AC 2D 3H 4S 6C 6D 6H 7C 10H 10S (triangle shape, 1 combination)
   public static int[] set3ranks = {0,1,2,3,0,1,2,0,2,3};
   public static int[] set3suits = {0,1,2,3,5,5,5,6,9,9};


   public static int deadwoodCount(ArrayList<Card> hand) {
      ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
      if (bestMeldSets.isEmpty()) {
         return GinRummyUtil.getDeadwoodPoints(hand);
      }
      ArrayList<ArrayList<Card>> chosenMeldSet = bestMeldSets.get(0);
      // System.out.println("Meld sets: " + bestMeldSets.size());
      for(ArrayList<Card> meld : chosenMeldSet) {
           // System.out.println("meld: " + meld);
      }
      int deadwood = GinRummyUtil.getDeadwoodPoints(chosenMeldSet,hand);
      return deadwood;
   }

   // TODO: for now we will pass any cards even if we know the opponent has them
   public static int numHitCards(ArrayList<Card> possibleCards, ArrayList<Card> hand) {
      //TODO: we can edit what meld sets are "best" sets
      //ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(hand);
      ArrayList<Long> bitStrings = GinRummyUtil.cardsToAllMeldBitstrings(hand);
      int hitCount = 0;
      for (Card c : possibleCards) {
         hand.add(c);
         ArrayList<Long> newbitStrings = GinRummyUtil.cardsToAllMeldBitstrings(hand);
         if (newbitStrings.size() > bitStrings.size()) {
            hitCount++;
         }
         hand.remove(hand.size()-1);
      }
      return hitCount;
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

   public static void testUtils() {

      // AC 2C 3C AH 2H 3H AS 2S 3S 2D
      int[] ranks = {0,0,0,1,1,1,1,2,2,2};
      int[] suits = {0,1,2,0,1,2,3,0,1,2};

      ArrayList<Card> hand = new ArrayList<>();
      for (int i = 0; i < ranks.length; i++) {
         hand.add(new Card(ranks[i], suits[i]));
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


      // System.out.println("Hit Count: " + numHitCards(deck, hand));
      System.out.println("Number of options: " + numOptions(deck, hand));

   }

   public static void main(String[] args) {

      testUtils();

   }
}
