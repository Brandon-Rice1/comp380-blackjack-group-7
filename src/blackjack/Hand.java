package blackjack;

import java.util.ArrayList;

/**
 * 
 * A representation of cards of out hand in blackjack.
 * 
 * @author Brandon
 *
 */
public class Hand {

	/**
	 * a list of cards that represents the cards in our hand
	 */
	private ArrayList<Card> hand = new ArrayList<>();

	/**
	 * Constructor that takes in an already made arraylist of cards
	 * 
	 * @param input an arraylist of card objects
	 */
	public Hand(ArrayList<Card> input) {
		hand = input;
	}

	/**
	 * Constructor that takes in the hexadecimal input from a csv line that has been
	 * separated into individual cards. Only creates our (player 1's) hand of cards.
	 * 
	 * @param hexCards an array of hexadecimal representations of cards
	 */
	public Hand(String[] hexCards) {
		for (int i = 8; i < hexCards.length; i++) {
			hand.add(new Card(hexCards[i]));
		}
	}

	/**
	 * 
	 * @return an arraylist of cards in this hand
	 */
	public ArrayList<Card> getHand() {
		return hand;
	}

	/**
	 * updates this hand to be a new hand containing new cards
	 * @param hand an arraylist of cards (the new hand of cards)
	 */
	public void setHand(ArrayList<Card> hand) {
		this.hand = hand;
	}

	/**
	 * adds a single card to this hand (ie the card that was drawn from a deck of cards)
	 * @param card the card object to add to the hand
	 */
	public void addCard(Card card) {
		hand.add(card);
	}

	/**
	 * gets the value of all cards in the hand and sums them together. Treats aces as having a value of 1.
	 * @return the sum of the soft values of all cards in this hand
	 */
	public int getSoftTotal() {
		int total = 0;
		for (Card card : hand) {
			total += card.getSoftValue();
		}
		return total;
	}

	/**
	 * gets the value of all cards in the hand and sums them together. Treats aces as having a value of 11.
	 * @return the sum of the hard values of all cards in this hand
	 */
	public int getHardTotal() {
		int total = 0;
		for (Card card : hand) {
			total += card.getHardValue();
		}
		return total;
	}

}
