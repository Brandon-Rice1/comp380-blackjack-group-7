package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Deck {
	
	private final ArrayList<Card> deck;
	
	private HashMap<Integer, Integer> cardCounts = new HashMap<Integer, Integer>(10);
	
	private int numCards = 0;

	public int getNumCards() {
		return numCards;
	}

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
	
	public Deck(List<Card> cards) {
		this();
		deck.removeAll(cards);
		for (Card card : cards) {
			cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) - 1);
			numCards--;
		}
	}
	
	public HashMap<Integer, Integer> getCardCounts() {
		return cardCounts;
	}

	public void setCardCounts(HashMap<Integer, Integer> cardCounts) {
		this.cardCounts = cardCounts;
	}
	
	public void shuffle() {
		Collections.shuffle(deck);
	}
	
	public void shuffle(Random rnd) {
		Collections.shuffle(deck, rnd);
	}
	
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
	
	public boolean remove(Card card) {
		if(deck.remove(card)) { // remove the card from deck and counts if available
			cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) - 1);
			numCards--;
			return true;
		} else {
			return false;
		}
	}
	
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

	public void putBack(Card card) {
		deck.add(0, card);
		cardCounts.put(card.getHardValue(), cardCounts.get(card.getHardValue()) + 1);
		numCards++;
	}

}
