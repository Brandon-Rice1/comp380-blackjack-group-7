package blackjack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck {
	
	private final ArrayList<Card> deck;
	
	public Deck() {
		deck = new ArrayList<Card>();
		for (Cardtype i : Cardtype.values()) {
			deck.add(new Card(i));
			deck.add(new Card(i));
			deck.add(new Card(i));
			deck.add(new Card(i));
		}
	}
	
	public Deck(List<Card> cards) {
		this();
		deck.removeAll(cards);
	}
	
	public void shuffle() {
		Collections.shuffle(deck);
	}
	
	public void shuffle(Random rnd) {
		Collections.shuffle(deck, rnd);
	}
	
	public Card draw() {
		if (deck.size() > 0) {
			return this.deck.remove(0);
		} else {
			return null;
		}
	}

}
