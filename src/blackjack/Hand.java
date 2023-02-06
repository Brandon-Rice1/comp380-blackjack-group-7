package blackjack;

import java.util.ArrayList;

/**
 * 
 * A representation of cards of our hand in blackjack.
 * 
 * @author Brandon
 *
 */
public class Hand {

	/**
	 * a list of cards that represents the cards in our hand
	 * should never be modified once it is set
	 */
	private ArrayList<Card> hand = new ArrayList<>();

	/**
	 * true if this hand contains an Ace, false if it does not
	 */
	private boolean hasAce = false;

	/**
	 * The sum of the hard values of all the cards in this hand
	 */
	private int hardTotal = 0;

	/**
	 * Constructor that takes in an already made arraylist of cards
	 * 
	 * @param input an arraylist of card objects
	 */
	public Hand(ArrayList<Card> input) {
		hand = input;
		for (Card card : hand) {
			if (card.getType() == Cardtype.Ace) {
				this.hasAce = true;
			}
			this.hardTotal += card.getHardValue();
		}
	}

	/**
	 * Constructor that takes in the hexadecimal input from a csv line that has been
	 * separated into individual cards. Only creates our (player 1's) hand of cards.
	 * 
	 * @param hexCards an array of hexadecimal representations of cards
	 */
	public Hand(String[] hexCards) {
		for (int i = 8; i < hexCards.length; i++) {
			Card temp = new Card(hexCards[i]);
			this.hand.add(temp);
			if (temp.getType() == Cardtype.Ace) {
				this.hasAce = true;
			}
			this.hardTotal += temp.getHardValue();
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
	 * 
	 * @param hand an arraylist of cards (the new hand of cards)
	 */
	public void setHand(ArrayList<Card> hand, boolean hasAce) {
		this.hand = hand;
		this.hasAce = hasAce;
		for (Card card : hand) {
			this.hardTotal += card.getHardValue();
		}
	}

	/**
	 * adds a single card to this hand (ie the card that was drawn from a deck of
	 * cards)
	 * 
	 * @param card the card object to add to the hand
	 */
	public void addCard(Card card) {
		this.hand.add(card);
		this.hardTotal += card.getHardValue();
		if (card.getType() == Cardtype.Ace) {
			this.hasAce = true;
		}
	}

	/**
	 * gets the value of all cards in the hand and sums them together. Same value as
	 * getHardTotal unless there is 1 or more Aces, then this has that value + 10.
	 * 
	 * @return the sum of the soft values of all cards in this hand
	 */
	public int getSoftTotal() {
		return (hasAce ? this.getHardTotal() + 10 : this.getHardTotal());
	}

	/**
	 * gets the value of all cards in the hand and sums them together. Treats aces
	 * as having a value of 1.
	 * 
	 * @return the sum of the hard values of all cards in this hand
	 */
	public int getHardTotal() {
//		int total = 0;
//		for (Card card : hand) {
//			total += card.getHardValue();
//		}
//		return total;
		return this.hardTotal;
	}

	/**
	 * gets whether or not this hand contains an Ace
	 * 
	 * @return true if this hand contains an Ace, false otherwise
	 */
	public boolean hasAce() {
		return this.hasAce;
	}
	
	@Override
	public String toString() {
		return this.hand.toString();
	}

}
