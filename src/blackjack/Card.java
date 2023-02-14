package blackjack;

import java.util.HexFormat;
import java.util.Map;

/**
 * Represents the relevant aspects of a playing card in the game of blackjack.
 * 
 * @author Brandon
 *
 */
public class Card {

	/**
	 * The face of the card (ie Ace, 1, ...)
	 */
	private final Cardtype type;

	/**
	 * The black jack value of the card. Uses 1 for Ace.
	 */
	private final int value;

	/**
	 * A map used to convert from card types to their values.
	 */
	private static final Map<Cardtype, Integer> type2value = Map.ofEntries(Map.entry(Cardtype.Ace, 1),
			Map.entry(Cardtype.Two, 2), Map.entry(Cardtype.Three, 3), Map.entry(Cardtype.Four, 4),
			Map.entry(Cardtype.Five, 5), Map.entry(Cardtype.Six, 6), Map.entry(Cardtype.Seven, 7),
			Map.entry(Cardtype.Eight, 8), Map.entry(Cardtype.Nine, 9), Map.entry(Cardtype.Ten, 10),
			Map.entry(Cardtype.Jack, 10), Map.entry(Cardtype.Queen, 10), Map.entry(Cardtype.King, 10));

	/**
	 * A map used to convert from hex notation integer value to card type.
	 */
	private static final Map<Integer, Cardtype> hex2type = Map.ofEntries(Map.entry(1, Cardtype.Ace),
			Map.entry(2, Cardtype.Two), Map.entry(3, Cardtype.Three), Map.entry(4, Cardtype.Four),
			Map.entry(5, Cardtype.Five), Map.entry(6, Cardtype.Six), Map.entry(7, Cardtype.Seven),
			Map.entry(8, Cardtype.Eight), Map.entry(9, Cardtype.Nine), Map.entry(10, Cardtype.Ten),
			Map.entry(11, Cardtype.Jack), Map.entry(13, Cardtype.Queen), Map.entry(14, Cardtype.King));

	/**
	 * Creates a new card based off of the standard hex format for the class. For
	 * example, the input 1F0B9 would create a card with the type Nine and value 9.
	 * 
	 * @param cardData the raw hexidecimal input as a string
	 */
	public Card(String cardData) {
		// HexFormat converts the hex string for the card value to an integer
		type = hex2type.get(HexFormat.fromHexDigits(cardData, 4, 5));
		value = type2value.get(type);
	}
	
	public Card(Cardtype type) {
		this.type = type;
		this.value = type2value.get(type);
	}

	/**
	 * 
	 * @return the card's type
	 */
	public Cardtype getType() {
		return type;
	}

	/**
	 * 
	 * @return the card's hard value
	 */
	public int getHardValue() {
		return value;
	}

	/**
	 * 
	 * @return the card's soft value
	 */
	public int getSoftValue() {
		if (this.type != Cardtype.Ace) {
			return this.value;
		} else {
			return 11;
		}
	}
	
	@Override
	public String toString() {
		return this.getType().toString();
	}

}
