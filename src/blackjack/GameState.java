package blackjack;

import java.util.ArrayList;

public class GameState {

	private Hand hand;

	private Hand dealer;

	private Hand others;
	
	private ArrayList<Card> combinedCardState = new ArrayList<>();

	public GameState(Hand hand, Hand dealer, Hand others) {
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
