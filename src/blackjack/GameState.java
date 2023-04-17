package blackjack;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * represents a state of the game
 */
public class GameState {
	/**
	 * represents the hand of the game
	 */
	private Hand hand;
	/**
	 * represents the dealer of the game
	 */
	private Hand dealer;
	/**
	 * represent other players of the game
	 */
	private Hand others;
	/**
	 * represents the number of card
	 */
	private int numCards;
	/**
	 * represents the map of the counts of different types of remaining cards
	 */
	private HashMap<Integer, Integer> cardsRemaining = new HashMap<>(13);
	/**
	 * a list of all cards
	 */
	private ArrayList<Card> combinedCardState = new ArrayList<>();

	/**
	 * initialize next game state to null
	 */
	public GameState next = null;
	/**
	 * initialize previous game state to null
	 */
	public GameState prev = null;
	/**
	 * initialize last move to null
	 */
	public Move lastMove = null;

	/**
	 * construct a game state
	 * @param dealer cards on the hand of the dealer of the game
	 * @param hand cards on the hand of the game
	 * @param others cards on the hand of other players of the game
	 */
	public GameState(Hand dealer, Hand hand, Hand others) {
		for (int j = 1; j <= 14; j++) {
			if (j == 12) {
				continue;
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

	/**
	 * update the cards of the hand of the game
	 * @param card the card that needs to be added to the hand
	 * @return the game state after the updation
	 */
	public GameState updateHand(Card card) {
		Hand tempHand = new Hand(this.hand.getHand());
		tempHand.addCard(card);
//		this.hand.addCard(card);
//		this.combinedCardState.add(card);
		GameState temp = new GameState(this.dealer, tempHand, this.others);
		temp.lastMove = this.lastMove;
		temp.next = this.next;
		return temp;
	}

	/**
	 * update the cards of the dealer of the game
	 * @param card the card that needs to be added to the dealer
	 * @return the game state after the updation
	 */
	public GameState updateDealer(Card card) {
		Hand tempDealer = new Hand(this.dealer.getHand());
		tempDealer.addCard(card);
//		this.dealer.addCard(card);
//		this.combinedCardState.add(card);
		GameState temp = new GameState(tempDealer, this.hand, this.others);
		temp.lastMove = this.lastMove;
		temp.next = this.next;
		return temp;
	}

	/**
	 * update the cards of others of the game
	 * @param card the card that needs to be added to others
	 * @return the game state after the updation
	 */
	public GameState updateOthers(Card card) {
		Hand tempOthers = new Hand(this.others.getHand());
		tempOthers.addCard(card);
		GameState temp = new GameState(this.dealer, this.hand, tempOthers);
		temp.lastMove = this.lastMove;
		temp.next = this.next;
		return temp;
	}

	/**
	 * get cards on the hand
	 * @return cards on the hand
	 */
	public Hand getHand() {
		return hand;
	}

	/**
	 * get cards of the dealer
	 * @return cards of the dealer
	 */
	public Hand getDealer() {
		return dealer;
	}

	/**
	 * get the cards of others
	 * @return the cards of others
	 */
	public Hand getOthers() {
		return others;
	}

	/**
	 * get the number of cards
	 * @return the number of cards
	 */
	public int getNumCards() {
		return numCards;
	}

	/**
	 * get the map of counts of remaining cards
	 * @return the map of counts of remaining cards
	 */
	public HashMap<Integer, Integer> getCardsRemaining() {
		return cardsRemaining;
	}

	/**
	 * compare whether two game states are equal
	 * @param o a given object to compare
	 * @return whether two game states are equal
	 */
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

	/**
	 * get the hash code of the combine card state
	 * @return the hash code of the combine card state
	 */
	@Override
	public int hashCode() {
//		var temp = new ArrayList<Card>();
		return this.combinedCardState.hashCode();
	}

}
