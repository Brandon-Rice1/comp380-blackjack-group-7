package blackjack;

import java.util.ArrayList;

/**
 * 
 * A representation of a hand of cards in blackjack.
 * @author Brandon
 *
 */
public class Hand {
	
	private ArrayList<Card> hand = new ArrayList<>();
	
	public Hand(ArrayList<Card> input) {
		hand = input;
	}
	
	public ArrayList<Card> getHand() {
		return hand;
	}

	public void setHand(ArrayList<Card> hand) {
		this.hand = hand;
	}
	
	public void addCard(Card card) {
		hand.add(card);
	}

	public int getSoftTotal() {
		int total = 0;
		for(Card card:hand) {
			total += card.getSoftValue();
		}
		return total;
	}
	
	public int getHardTotal() {
		int total = 0;
		for(Card card:hand) {
			total += card.getHardValue();
		}
		return total;
	}

}
