package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * represents a deck of cards
 */
public class Deck {
	/**
	 * the deck of cards
	 */
	private final ArrayList<Card> deck;
	/**
	 * the map of the counts of different types of cards
	 */
	private HashMap<Integer, Integer> cardCounts = new HashMap<>(10);
	/**
	 * number of cards
	 */
	private int numCards;

	/**
	 * get the number of cards
	 * @return the number of cards
	 */
	public int getNumCards() {
		return numCards;
	}

	/**
	 * constructor of the deck
	 */
	public Deck() {
		deck = new ArrayList<Card>();
		for (Cardtype i : Cardtype.values()) {
			deck.add(new Card(i));
			deck.add(new Card(i));
			deck.add(new Card(i));
			deck.add(new Card(i));
		}
		for (int j = 1; j <= 10; j++) {
			if (j == 10) {
				cardCounts.put(j, 16);
			} else {
				cardCounts.put(j, 4);
			}
		}
		numCards = 52;
	}

	/**
	 *
	 * @param cards
	 */
	public Deck(List<Card> cards) {
		this();
		deck.removeAll(cards);
		for (Card card : cards) {
			if (card == null) {
				continue;
			}
			cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) - 1);
			numCards--;
		}
	}

	/**
	 * get the map of the counts of different types of cards
	 * @return the map of the counts of different types of cards
	 */
	public HashMap<Integer, Integer> getCardCounts() {
		return cardCounts;
	}

	/**
	 * set the card counts to the given map
	 * @param cardCounts a map of the counts of different types of cards
	 */
	public void setCardCounts(HashMap<Integer, Integer> cardCounts) {
		this.cardCounts = cardCounts;
	}

	/**
	 * shuffle the deck
	 */
	public void shuffle() {
		Collections.shuffle(deck);
	}

	/**
	 * shuffle the deck with a random number
	 * @param rnd a random number
	 */
	public void shuffle(Random rnd) {
		Collections.shuffle(deck, rnd);
	}

	/**
	 * draw the first card out of the deck
	 * @return the card that is drawn
	 */
	public Card draw() {
		if (deck.size() > 0) {
			Card removed = this.deck.remove(0);
			cardCounts.put(removed.getHardValue(), cardCounts.get(removed.getHardValue()) - 1);
			numCards--;
			return removed;
		} else {
			return null;
		}
	}

	/**
	 * remove a given card from the deck
	 * @param card the given card that need to be removed
	 * @return whether the card is moved successfully
	 */
	public boolean remove(Card card) {
		if(deck.remove(card)) { // remove the card from deck and counts if available
			cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) - 1);
			numCards--;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * check whether two decks are the same
	 * @param o the given object
	 * @return whether two decks are the same
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) { // if the same object, are equal
			return true;
		}
//		if (o == null) {
//			return false;
//		}
		if (!(o instanceof Deck)) { // if not a Deck object, then not equal
			return false;
		}
		Deck deck2 = (Deck) o;
		return this.deck == deck2.deck;
	}

	/**
	 * put a given card back to the deck
	 * @param card the given card that needs to be put back
	 */
	public void putBack(Card card) {
		deck.add(0, card);
		cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) + 1);
		numCards++;
	}

}
