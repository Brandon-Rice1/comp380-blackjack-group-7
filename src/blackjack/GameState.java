package blackjack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameState {

	private Hand hand;

	private Hand dealer;

	private Hand others;
	
	private int numCards = 0;
	
	private HashMap<Integer, Integer> cardsRemaining = new HashMap<Integer, Integer>(10); 

	private ArrayList<Card> combinedCardState = new ArrayList<>();

	public GameState(Hand dealer, Hand hand, Hand others) {
		for (int j = 1; j <= 10; j++) {
			if (j == 10) {
				cardsRemaining.put(j, 16);
			} else {
				cardsRemaining.put(j, 4);
			}
		}
		hand.getHand().sort((a, b) -> {
			return a.getSoftValue() - b.getSoftValue();
		});
		dealer.getHand().sort((a, b) -> {
			return a.getSoftValue() - b.getSoftValue();
		});
		others.getHand().sort((a, b) -> {
			return a.getSoftValue() - b.getSoftValue();
		});
		this.hand = hand;
		this.dealer = dealer;
		this.others = others;
		combinedCardState.addAll(hand.getHand());
		combinedCardState.add(null);
		combinedCardState.addAll(dealer.getHand());
		combinedCardState.add(null);
		combinedCardState.addAll(others.getHand());
		numCards = 52 - (combinedCardState.size() - 2);
		for (Card card : combinedCardState) {
			if (card != null) {
				cardsRemaining.put(card.getHardValue(), cardsRemaining.get(card.getHardValue()) - 1);
			}
		}
	}
	
	public GameState updateHand(Card card) {
		Hand tempHand = new Hand(this.hand.getHand());
		tempHand.addCard(card);
//		this.hand.addCard(card);
//		this.combinedCardState.add(card);
		return new GameState(this.dealer, tempHand, this.others);
	}
	
	public GameState updateDealer(Card card) {
		Hand tempDealer = new Hand(this.dealer.getHand());
		tempDealer.addCard(card);
//		this.dealer.addCard(card);
//		this.combinedCardState.add(card);
		return new GameState(tempDealer, this.hand, this.others);
	}
	
	public GameState updateOthers(List<Card> Cards) {
		Hand tempOthers = new Hand(this.others.getHand());
		for (Card card : Cards) {
			tempOthers.addCard(card);
		}
		return new GameState(this.dealer, this.hand, tempOthers);
	}
	
	public Hand getHand() {
		return hand;
	}

	public Hand getDealer() {
		return dealer;
	}

	public Hand getOthers() {
		return others;
	}
	
	public int getNumCards() {
		return numCards;
	}

	public HashMap<Integer, Integer> getCardsRemaining() {
		return cardsRemaining;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof GameState)) {
			return false;
		}
		GameState state2 = (GameState) o;
		return (this.hand == state2.hand && this.dealer == state2.dealer && this.others == state2.others);
	}

	@Override
	public int hashCode() {
//		var temp = new ArrayList<Card>();
		return this.combinedCardState.hashCode();
	}

}
