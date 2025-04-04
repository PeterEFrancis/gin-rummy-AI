//import java.util.ArrayList;
//import java.util.Random;
//
//import ginrummy.GinRummyPlayer;
//
//
///**
// * Implements a random dummy Gin Rummy player that has the following trivial, poor play policy:
// * Ignore opponent actions and cards no longer in play.
// * Draw face up card only if it becomes part of a meld.  Draw face down card otherwise.
// * Discard a highest ranking unmelded card without regard to breaking up pairs, etc.
// * Knock as early as possible.
// *
// * @author Todd W. Neller
// * @version 1.0
//
//Copyright (C) 2020 Todd Neller
//
//This program is free software; you can redistribute it and/or
//modify it under the terms of the GNU General Public License
//as published by the Free Software Foundation; either version 2
//of the License, or (at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//Information about the GNU General Public License is available online at:
//  http://www.gnu.org/licenses/
//To receive a copy of the GNU General Public License, write to the Free
//Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
//02111-1307, USA.
//
// */
//public class ChGinRummyPlayer implements GinRummyPlayer {
//	private int playerNum;
//	@SuppressWarnings("unused")
//	private int startingPlayerNum;
//	private ArrayList<Card> cards = new ArrayList<Card>();
//	private Random random = new Random();
//	private boolean opponentKnocked = false;
//	Card faceUpCard, drawnCard;
//	ArrayList<Long> drawDiscardBitstrings = new ArrayList<Long>();
//
//	@Override
//	public void startGame(int playerNum, int startingPlayerNum, Card[] cards) {
//		this.playerNum = playerNum;
//		this.startingPlayerNum = startingPlayerNum;
//		this.cards.clear();
//		for (Card c : cards) {
//			this.cards.add(c);
//		}
//		opponentKnocked = false;
//		drawDiscardBitstrings.clear();
//	}
//
//	@Override
//	public boolean willDrawFaceUpCard(Card card) {
//		// Return true if card would be a part of a meld, false otherwise.
//		this.faceUpCard = card;
//		@SuppressWarnings("unchecked")
//		ArrayList<Card> newCards = (ArrayList<Card>) cards.clone();
//		newCards.add(card);
//		for (ArrayList<Card> meld : GinRummyUtil.cardsToAllMelds(newCards))
//			if (meld.contains(card))
//				return true;
//		return false;
//	}
//
//	@Override
//	public void reportDraw(int playerNum, Card drawnCard) {
//		// Ignore other player draws.  Add to cards if playerNum is this player.
//		if (playerNum == this.playerNum) {
//			cards.add(drawnCard);
//			this.drawnCard = drawnCard;
//		}
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public Card getDiscard() {
//		if (true) {
//			return drawnCard;
//		}
//		// Discard a random card (not just drawn face up) leaving minimal deadwood points.
//		int minDeadwood = Integer.MAX_VALUE;
//		ArrayList<Card> candidateCards = new ArrayList<Card>();
//		for (Card card : cards) {
//			// Cannot draw and discard face up card.
//			if (card == drawnCard && drawnCard == faceUpCard)
//				continue;
//			// Disallow repeat of draw and discard.
//			ArrayList<Card> drawDiscard = new ArrayList<Card>();
//			drawDiscard.add(drawnCard);
//			drawDiscard.add(card);
//			if (drawDiscardBitstrings.contains(GinRummyUtil.cardsToBitstring(drawDiscard)))
//				continue;
//
//			ArrayList<Card> remainingCards = (ArrayList<Card>) cards.clone();
//			remainingCards.remove(card);
//			ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(remainingCards);
//			int deadwood = bestMeldSets.isEmpty() ? GinRummyUtil.getDeadwoodPoints(remainingCards) : GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), remainingCards);
//			if (deadwood <= minDeadwood) {
//				if (deadwood < minDeadwood) {
//					minDeadwood = deadwood;
//					candidateCards.clear();
//				}
//				candidateCards.add(card);
//			}
//		}
//		Card discard = candidateCards.get(random.nextInt(candidateCards.size()));
//		// Prevent future repeat of draw, discard pair.
//		ArrayList<Card> drawDiscard = new ArrayList<Card>();
//		drawDiscard.add(drawnCard);
//		drawDiscard.add(discard);
//		drawDiscardBitstrings.add(GinRummyUtil.cardsToBitstring(drawDiscard));
//		return discard;
//	}
//
//	@Override
//	public void reportDiscard(int playerNum, Card discardedCard) {
//		// Ignore other player discards.  Remove from cards if playerNum is this player.
//		if (playerNum == this.playerNum)
//			cards.remove(discardedCard);
//	}
//
//	@Override
//	public ArrayList<ArrayList<Card>> getFinalMelds() {
//		// Check if deadwood of maximal meld is low enough to go out.
//		ArrayList<ArrayList<ArrayList<Card>>> bestMeldSets = GinRummyUtil.cardsToBestMeldSets(cards);
//		if (!opponentKnocked && (bestMeldSets.isEmpty() || GinRummyUtil.getDeadwoodPoints(bestMeldSets.get(0), cards) > GinRummyUtil.MAX_DEADWOOD))
//			return null;
//		return bestMeldSets.isEmpty() ? new ArrayList<ArrayList<Card>>() : bestMeldSets.get(random.nextInt(bestMeldSets.size()));
//	}
//
//	@Override
//	public void reportFinalMelds(int playerNum, ArrayList<ArrayList<Card>> melds) {
//		// Melds ignored by simple player, but could affect which melds to make for complex player.
//		if (playerNum != this.playerNum)
//			opponentKnocked = true;
//	}
//
//	@Override
//	public void reportScores(int[] scores) {
//		scores[0] = 101;
//		// Ignored by simple player, but could affect strategy of more complex player.
//	}
//
//	@Override
//	public void reportLayoff(int playerNum, Card layoffCard, ArrayList<Card> opponentMeld) {
//		// Ignored by simple player, but could affect strategy of more complex player.
//
//	}
//
//	@Override
//	public void reportFinalHand(int playerNum, ArrayList<Card> hand) {
//		// Ignored by simple player, but could affect strategy of more complex player.
//	}
//
//}
