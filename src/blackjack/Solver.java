package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
//import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collector;

/**
 * The main class. Contains the current strategy as well as the file i/o.
 * 
 * @author Brandon
 *
 */
public class Solver {

	/**
	 * An array representing the table of options for a hand without a pair or an
	 * ace.
	 */
	private static final String[][] hard = new String[17][10];

	/**
	 * An array representing the table of options for a hand with an ace
	 */
	private static final String[][] soft = new String[9][10];

	/**
	 * An array representing the table of options for a hand with two cards that are
	 * the same face (ie 2 Kings)
	 */
	private static final String[][] pairs = new String[10][10];

	private static ArrayList<AtomicInteger> strat1Totals = new ArrayList<>();

	private static ArrayList<AtomicInteger> strat2Totals = new ArrayList<>();

	private static int compareStrategiesHW4(String input, int index) {
//		numTrials.addAndGet(1);
		String[] hexCards = input.split(",");
		// get the dealer's card and hand
		Card dealerCard = new Card(hexCards[2]);
		Hand dealer1 = new Hand(List.of(dealerCard));
		Hand dealer2 = new Hand(List.of(dealerCard));
		// get our hand
		Card yourCard1 = new Card(hexCards[11]);
		Card yourCard2 = new Card(hexCards[12]);
		Hand hand1 = new Hand(List.of(yourCard1, yourCard2));
		Hand hand2 = new Hand(List.of(yourCard1, yourCard2));
		// make two shuffled decks that are identical
		// first, get all the cards that are not in the decks
		ArrayList<Card> cards = new ArrayList<>();
		for (String strCard : hexCards) {
			if (!strCard.equals("")) {
				cards.add(new Card(strCard));
			}
		}
		// make the decks and remove the cards that shouldn't be in them
		Deck deck1 = new Deck(cards);
		Deck deck2 = new Deck(cards);
		// make "hands" of the other player cards
		cards.remove(dealerCard);
		cards.remove(yourCard1);
		cards.remove(yourCard2);
		Hand others1 = new Hand(cards);
		Hand others2 = new Hand(cards);
		// shuffle the decks in the same way
		Random rndSeed = new Random();
		final long seed = rndSeed.nextLong();
		Random rnd1 = new Random(seed);
		Random rnd2 = new Random(seed);
		deck1.shuffle(rnd1);
		deck2.shuffle(rnd2);
		assert deck1 == deck2;
		// test the strategies
		strat1Totals.get(index).addAndGet(strategy1(dealer1, hand1, deck1, Move.HIT));
//		strat1Totals.set(index, strat1Totals.get(index)+strategy1(dealer1, hand1, deck1, Move.HIT));
		strat2Totals.get(index).addAndGet(strategy2(dealer2, hand2, deck2, Move.HIT));
//		strat2Totals.set(index, strat1Totals.get(index)+strategy2(dealer2, hand2, deck2, Move.HIT));
//		total1.addAndGet(strategy1(dealer1, hand1, deck1, Move.HIT));
//		total2.addAndGet(strategy2(dealer2, hand2, deck2, Move.HIT));
		return 0;
	}

	private static double compareStrategiesHW5(GameState initial) {
		final Hand dealer = initial.getDealer();
		final Hand hand = initial.getHand();
		ArrayList<Card> drawnCards = new ArrayList<>(initial.getNumCards());
		drawnCards.addAll(initial.getDealer().getHand());
		drawnCards.addAll(initial.getHand().getHand());
		drawnCards.addAll(initial.getOthers().getHand());
		final Deck deck1 = new Deck(drawnCards);
		final Deck deck2 = new Deck(drawnCards);
		Random rndSeed = new Random();
		final long seed = rndSeed.nextLong();
		Random rnd1 = new Random(seed);
		Random rnd2 = new Random(seed);
		deck1.shuffle(rnd1);
		deck2.shuffle(rnd2);
		assert deck1 == deck2;
		// test the strategies
		double output = wikiStrat(dealer, hand, deck1);
		if (!accOutcomes.containsKey(initial)) {
			accOutcomes.put(initial, getDescendentScore(initial, Move.HIT));
		}
		return output;
	}
	
	/**
	 * implementing strategy 1 (the naive strategy in homework1)
	 * 
	 * @param dealer the dealer's card
	 * @param hand   cards in the hand
	 * @param deck   the list of cards
	 * @param move   the move taken by the player
	 * @return the strategy1 results of the case
	 */
	private static int strategy1(Hand dealer, Hand hand, Deck deck, Move move) {
		// condition to return if hand is final
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcomeRevised(dealer, hand, deck, move) * 10;
			int temp = outcome.intValue();
			return temp;
		}
		// logic to decide next move and recurse
		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
			return strategy1(dealer, hand, deck, Move.STAY);
		} else {
			hand.addCard(deck.draw());
			return strategy1(dealer, hand, deck, Move.HIT);
		}
	}

	/**
	 * implementing strategy 2 (the strategy used for homework 2)
	 * 
	 * @param dealer the dealer's card
	 * @param hand   the card in the hand
	 * @param deck   the list of cards
	 * @param move   the move taken by the player
	 * @return the strategy2 results of the case
	 */
	private static int strategy2(Hand dealer, Hand hand, Deck deck, Move move) {
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcomeRevised(dealer, hand, deck, move) * 10;
//			Double outcome = evaluateOutcomeRevised(dealer, hand, deck, move);
			int temp = outcome.intValue();
//			maxGain2.set(Math.max(temp, maxGain2.get()));
//			maxLoss2.set(Math.min(temp, maxLoss2.get()));
			return temp;
		}
		// logic to decide next move and recursion
		String lookup = "";
		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
			if (hand.hasAce()) {
				lookup = pairs[9][dealer.getSoftTotal() - 2];
			}
			lookup = pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftTotal() - 2];
		} else if (hand.hasAce()) {
			// contains ace = soft table
			if (hand.getSoftTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getSoftTotal() < 21) {
				lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftTotal() - 2];
			}
		}
		if (lookup == "") {
			// no ace, no pair = hard table; or with ace > 21
			if (hand.getHardTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getHardTotal() > 21) {
				lookup = "STAY";
//				throw new IllegalArgumentException();
			} else {
				lookup = hard[(hand.getHardTotal()) - 5][dealer.getSoftTotal() - 2];
			}
		}
		// for when there are multiple options
		if (lookup.contains("/")) {
			// if there are multiple options and there are 2 cards, take the first option
			if (hand.getHand().size() == 2) {
				lookup = lookup.split("/")[0];
			} else { // otherwise, take the second
				lookup = lookup.split("/")[1];
			}
		}
		Move nextMove = Move.valueOf(lookup);
		move = nextMove;
		if (nextMove == Move.DOUBLE || nextMove == Move.HIT) { // if hit or double, draw a card
			hand.addCard(deck.draw());
		} else if (nextMove == Move.SPLIT) { // if split, recurse on each new hand
			Cardtype type = hand.getHand().get(0).getType();
			Hand hand1 = new Hand(List.of(new Card(type), deck.draw()));
			Hand hand2 = new Hand(List.of(new Card(type), deck.draw()));
			// copy the dealer's hand to avoid indexing issues
			Hand dealer2 = new Hand(dealer.getHand());
			// play out the first hand
			Move nextMove1 = nextMove;
			strategy2(dealer, hand1, deck, nextMove1);
			// put back dealer cards (dealer should not go yet)
			for (int i = dealer.getHand().size() - 1; i > 0; i--) {
				deck.putBack(dealer.getHand().get(i));
			}
			// run strategy for other hand
			int outcome2 = strategy2(dealer2, hand2, deck, nextMove);
			// evaluate the first hand based on the actual, final dealer's hand
			Double outcome1 = (evaluateOutcomeRevised(dealer2, hand1, deck, nextMove1) * 10);
			int total = outcome1.intValue() + outcome2;
//			maxGain2.set(Math.max(total, maxGain2.get()));
//			maxLoss2.set(Math.min(total, maxLoss2.get()));
			return total;
		}
		return strategy2(dealer, hand, deck, nextMove);
	}
	
	private static Double wikiStrat(Hand dealer, Hand hand, Deck deck) {
		assert (dealer.getHand().size() == 1);
		// logic to decide next move and recursion
		String lookup = "";
		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
			if (hand.hasAce()) {
				lookup = pairs[9][dealer.getSoftTotal() - 2];
			}
			lookup = pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftTotal() - 2];
		} else if (hand.hasAce()) {
			// contains ace = soft table
			if (hand.getSoftTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getSoftTotal() < 21) {
				lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftTotal() - 2];
			}
		} else {
			// no ace, no pair = hard table; or with ace > 21
			if (hand.getHardTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getHardTotal() > 21) {
				// if here, last move must have been a hit
				lookup = "STAY";
//				return -1.0;
//				System.out.println("ERROR: hand total too great");
//				System.out.println(hand.toString());
//				throw new IllegalArgumentException();
			} else {
				if ((hand.getHardTotal()) - 5 > 16 || dealer.getSoftTotal() > 9) {
					System.out.println(hand);
					System.out.println(dealer);
				}
				lookup = hard[(hand.getHardTotal()) - 5][dealer.getSoftTotal() - 2];
			}
		}
		// for when there are multiple options
		if (lookup.contains("/")) {
			// if there are multiple options and there are 2 cards, take the first option
			if (hand.getHand().size() == 2) {
				lookup = lookup.split("/")[0];
			} else { // otherwise, take the second
				lookup = lookup.split("/")[1];
			}
		}
		// if busted or odd behavior, stay
		if (lookup.equals("")) {
			lookup = "STAY";
		}
		Move nextMove = Move.valueOf(lookup);
		// Once a move is decided, determine if we need to recurse
		// TODO move the below behavior to its own function (possibly) in order to make another function for splitting behavior...
		switch(nextMove) {
		case HIT:
			hand.addCard(deck.draw());
			return wikiStrat(dealer, hand, deck);
		case SPLIT:
			Hand hand1 = new Hand(List.of(hand.getHand().get(0), deck.draw()));
			Hand hand2 = new Hand(List.of(hand.getHand().get(1), deck.draw()));
			wikiStrat(dealer, hand1, deck);
			while (dealer.getHand().size() > 2) {
				deck.putBack(dealer.getHand().get(dealer.getHand().size()-1));
			}
			double output = wikiStrat(dealer, hand2, deck);
			return output + wikiStrat(dealer, hand1, deck);
		case DOUBLE:
			hand.addCard(deck.draw());
			break;
		case SURRENDER:
			return -0.5;
		case STAY:
			break;
		}
		while ((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
			dealer.addCard(deck.draw());
		}
		double output = 0.0;
		int handTotal = hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal();
		int dealerTotal = dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal();
		if(handTotal > 21) { // we busted
			output = -1.0;
		} else if(dealerTotal > handTotal && dealerTotal <= 21) { // dealer won
			output = -1.0;
		} else if (dealerTotal == handTotal) { // tie
			output = 0.0;
		} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) { // we got a blackjack
			output = 1.5;
		} else if(dealer.getHardTotal() > 21) { // dealer busted
			output = 1.0;
		} else if(dealerTotal < handTotal){ // we won
			output = 1.0;
		} else { // an outcome that should not be possible
			System.out.println("Something went wrong when evaluating outcomes:");
			System.out.println("\tHand: " + hand.toString());
			System.out.println("\tDealer: " + dealer.toString());
		}
		output = (nextMove == Move.DOUBLE ? output * 2 : output);
		return output;
//		Move nextMove = Move.valueOf(lookup);
//		return Move.valueOf(lookup);
	}

	
	private static HashMap<GameState, Double> accOutcomes = new HashMap<>();
	
	private static HashMap<GameState, Move> moveOutcomes = new HashMap<GameState, Move>();
	
	private static double strategyIdeal(Hand dealer, Hand hand, Hand others, Deck deck) {
		// lookup the next move given our position; if null, run the code to make it not null
		GameState position = new GameState(dealer, hand, others);
		Move move = moveOutcomes.get(position);
		int handTotal = hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal();
		if (move == null) {
			System.out.println("Move is null for this position: ");
			System.out.println("\tHand: " + position.getHand());
			System.out.println("\tDealer: " + position.getDealer());
			System.out.println("\tOthers: " + position.getOthers());
			System.out.println("\t" + position.hashCode());
			System.out.println(accOutcomes.containsKey(position));
			System.out.println(accOutcomes.get(position));
			System.out.println(moveOutcomes.containsKey(position));
			System.out.println(moveOutcomes.get(position));
//			if (handTotal > 21 && !accOutcomes.containsKey(position)) {
//				accOutcomes.put(position, -1.0);
//				moveOutcomes.put(position, Move.STAY);
//			} else {
				getDescendentScore(position, Move.STAY);
//			}
			move = moveOutcomes.get(position);
			System.out.println(accOutcomes.containsKey(position));
			System.out.println(accOutcomes.get(position));
			System.out.println(moveOutcomes.containsKey(position));
			System.out.println(moveOutcomes.get(position));
		}
		if (move == null) {
			System.out.println("Move is STILL null for this position: ");
			System.out.println("\tHand: " + position.getHand());
			System.out.println("\tDealer: " + position.getDealer());
			System.out.println("\tOthers: " + position.getOthers());
			System.out.println(accOutcomes.get(position));
		}
		// based on the move to make, make that move
		switch(move) {
		case HIT:
			hand.addCard(deck.draw());
			return strategyIdeal(dealer, hand, others, deck);
		case SPLIT:
			Hand hand1 = new Hand(List.of(hand.getHand().get(0), deck.draw()));
			Hand hand2 = new Hand(List.of(hand.getHand().get(1), deck.draw()));
			Hand others1 = new Hand(others.getHand());
			others1.addCard(hand2.getHand().get(1));
			Hand others2 = new Hand(others.getHand());
			others2.addCard(hand1.getHand().get(1));
			// play through the whole thing with the first hand to find out what hand1 does
			strategyIdeal(dealer, hand1, others1, deck);
			// undo the dealer's draws since they can't have happened yet
			while (dealer.getHand().size() > 2) {
				deck.putBack(dealer.getHand().get(dealer.getHand().size()-1));
			}
			// update others2 to reflect what hand1 drew when solving
			for(int i = 2; i < hand1.getHand().size(); i++) {
				others2.addCard(hand1.getHand().get(i));
			}
			// score everything (for hand1 call, hand should already be at a termingatin state
			double output = strategyIdeal(dealer, hand2, others2, deck);
			return output + strategyIdeal(dealer, hand1, others1, deck);
		case DOUBLE:
			hand.addCard(deck.draw());
			break;
		case SURRENDER:
			return -0.5;
		case STAY:
			break;
		}
		// have the dealer draw from the deck
		while ((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
			dealer.addCard(deck.draw());
		}
		double output = 0.0;
		// evaluate how this hand did
		handTotal = hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal();
		int dealerTotal = dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal();
		if(handTotal > 21) { // we busted
			output = -1;
		} else if(dealerTotal > handTotal && dealerTotal <= 21) { // dealer won
			output = -1;
		} else if (dealerTotal == handTotal) { // tie
			output = 0;
		} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) { // we got a blackjack
			output = 1.5;
		} else if(dealer.getHardTotal() > 21) { // dealer busted
			output = 1;
		} else if(dealerTotal < handTotal){ // we won
			output = 1;
		} else { // an outcome that should not be possible
			System.out.println("Something went wrong when evaluating outcomes:");
			System.out.println("\tHand: " + hand.toString());
			System.out.println("\tDealer: " + dealer.toString());
		}
		output = (move == Move.DOUBLE ? output * 2 : output);
		return output;
	}
	
	private static double getDescendentScore(GameState position, Move move) {
		// For a particular game state (hand, dealer's card, and deck/other player's cards), if we have found a solution, return that solution
		// otherwise, for each possible move, find the score for that move, and store the best outcome in the mapping, and return that solution
			// for surrendering, the outcome is always -0.5 (but this is only possible if there are 2 cards in the player's hand)
			// for staying, the dealer proceeds to draw cards according to the 17 rule, and the total returned from the result of that is the outcome
			// for the other options, we need to run the simulation for each possible card draw and the probablity for the current deck
				// for doubling, the hand gets 1 new card (try for all options in deck), then the dealer draws cards until 17 rule, eval the outcome
				// for hitting, like doubling, except a recursive call to this method for each new possible card
				// for splitting, like hitting, except there is a recursive call to this function for every possible combination of 2 hands
					// note that each hand can be evaluated independently* and the results added
					// *except for the part where both hands need to play before the dealer goes...
		if (position.getHand().getHand().size() == 2) {
			System.out.println("\tHand: " + position.getHand());
			System.out.println("\tDealer: " + position.getDealer());
			System.out.println("\tOthers: " + position.getOthers());
		}
		if (accOutcomes.get(position) != null) {
			return accOutcomes.get(position); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
		Hand hand = position.getHand();
		Hand dealer = position.getDealer();
		Hand others = position.getOthers();
		// if we have busted, there is no point in continuing since we lose
		if ((hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal()) > 21) {
//			double outcome = (move == Move.DOUBLE ? -2.0 : -1.0);
//			accOutcomes.put(position, outcome);
			double outcome = dealerPossibilities(position, (move == Move.DOUBLE ? move : Move.STAY));
			if (accOutcomes.get(position) == null || accOutcomes.get(position) < outcome) {
				accOutcomes.put(position, outcome);
				moveOutcomes.put(position, move);
			}
//			accOutcomes.put(position, outcome);
			return outcome;
		}
		double splitScore = 0.0;
		double doubleOutcome = 0.0;
		double scoreOutcome = Integer.MIN_VALUE;
		Move moveOutcome = null;
		HashMap<Integer, Integer> cardCounts = position.getCardsRemaining();
		if (hand.getHand().size() == 2) {
			scoreOutcome = -0.5;
			moveOutcome = Move.SURRENDER;
			
			// TODO: implement splitting behavior (not easy b/c have to play both possibilities before dealerPossibilities)
				// basically, want to play first hand until it doesn't hit. When that happens, play the second hand until it doesn't hit. Then dealer.
				// first, create 2 hands and 2 gameStates, where each hand has 1 card, and the other card is in the "others" section of GameState
				// will have to setup a double-nested for loop of calls so each hand has the given, split cards (can't stay or surrender on 1 card)
					// or have to add conditions where can only hit if the hand has < 2 cards in it
				// play hand 1 like this function, except when reach what is a dealerPossibilities call here, instead add the cards in hand1 to hand2's gameState
				// under "others". Then, eval hand2 just like hand1 was. When hand2 reaches what would be dealerPossibilities, instead add the cards in hand2 to
				// hand1's gameState under "others". Then, proceed to the regular dealerPossibilities call for each hand and state. dealer's cards and probabilities 
				// should be the same in both cases, but the chance of winning will vary depending on each hand
			
			if (hand.getHand().get(0) == hand.getHand().get(1)) {
				System.out.println("running split...");
//				double splitScore = 0.0;
				var tempSet = cardCounts.entrySet();
				for (var entry1 : tempSet) {
					if (entry1.getValue() == 0) {
						continue;
					}
					int tempCount1 = entry1.getValue() - 1;
					for (var entry2 : tempSet) {
						if (entry2.getValue() == 0 || (entry2.getKey().equals(entry1.getKey()) && tempCount1 == 0)) {
							continue;
						}
//						int tempCount2 = (entry2.getKey() == entry1.getKey() ? tempCount1 - 1 : entry2.getValue() - 1);
						// draw new cards for both hands, update the states to reflect the new missing cards
						Hand hand1 = new Hand(List.of(hand.getHand().get(0), new Card(entry1.getKey())));
						Hand hand2 = new Hand(List.of(hand.getHand().get(0), new Card(entry2.getKey())));
						// there is 1 split, the original one, only
						Hand dealer2 = new Hand(dealer.getHand());
						Hand others1 = new Hand(others.getHand());
						others1.addCard(hand2.getHand().get(0));
						others1.addCard(hand2.getHand().get(1));
						List<Card> origOthers = others.getHand();
						origOthers.addAll(hand1.getHand());
						Hand others2 = new Hand(origOthers);
						GameState position1 = new GameState(dealer, hand1, others1);
						GameState position2 = new GameState(dealer2, hand2, others2);
						position1.next = position2;
						splitScore = getSplitScore(position1, Move.SPLIT, position1);
					}
				}
				if (splitScore >= scoreOutcome) {
					scoreOutcome = splitScore;
					moveOutcome = Move.SPLIT;
				}
			}

			// check doubling outcomes
			System.out.println("running double...");
//			double doubleOutcome = 0.0;
			var tempSet = cardCounts.entrySet();
			// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
			for (var entry : tempSet) {
				// update hand and deck appropriately
				if (entry.getValue() == 0) {
					continue;
				}
				Card card = new Card(entry.getKey());
				doubleOutcome += (entry.getValue()/(position.getNumCards()-1) * dealerPossibilities(position.updateHand(card), Move.DOUBLE));
			}
			if (doubleOutcome >= scoreOutcome) {
				scoreOutcome = doubleOutcome;
				moveOutcome = Move.DOUBLE;
			}
		}
		
		// check staying outcomes
//		System.out.println("running stay...");
		double stayOutcome = dealerPossibilities(position, Move.STAY);
		if (stayOutcome >= scoreOutcome) {
			scoreOutcome = stayOutcome;
			moveOutcome = Move.STAY;
		} else {
			if (hand.getHand().size() == 3) {
				System.out.println("Found " + stayOutcome + " < " + scoreOutcome);
			}
		}
		
		// check hitting outcomes
//		System.out.println("running hit (recurse)...");
		double hitOutcome = 0.0;
		var tempSet = cardCounts.entrySet();
		// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
		for (var entry : tempSet) {
			if (entry.getValue() <= 0) {
				continue;
			}
//			System.out.println(position.getHand());
			// update hand and deck appropriately
			Card card = new Card(entry.getKey());
			if (card.getHardValue() == 6 && hand.getHand().size() == 2) {
				System.out.println("HERE IS A CASE THAT WORKS!!!");
			}
			// recursively call this function
			try {
				hitOutcome += (entry.getValue()/position.getNumCards() * getDescendentScore(position.updateHand(card), Move.HIT));
			} catch (NullPointerException e) {
				System.out.println("ERROR: NullPointerException");
				System.out.println("\tHand: " + position.getHand());
				System.out.println("\tCard: " + card);
				System.out.println("\tEntry: " + entry);
				e.printStackTrace();
			}
			
		}
		if (hitOutcome >= scoreOutcome) {
			scoreOutcome = hitOutcome;
			moveOutcome = Move.HIT;
		}
		if (hand.getHand().size() == 2) {
			System.out.println("position: ");
			System.out.println(position);
			System.out.println("\t" + moveOutcome);
			System.out.println("\t" + position.hashCode());
		}
		accOutcomes.put(position, scoreOutcome);
		moveOutcomes.put(position, moveOutcome);
		return scoreOutcome;
	}
	
	private static double dealerPossibilities(GameState position, Move move) {
		if (position == null) {
			return 0.0;
		}
		Hand dealer = position.getDealer();
		Hand hand = position.getHand();
		// we have been here before; just return right away
		if (accOutcomes.containsKey(position)) {
			return accOutcomes.get(position).doubleValue(); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
		int handTotal = hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal();
		double output = 0.0;
		// dealer no longer wants to take cards; evaluate the outcomes
		if (((dealer.hasAce() && dealer.getSoftTotal() > 17) || dealer.getHardTotal() >= 17)) { // dealer is done; eval outcome time
			
			int dealerTotal = dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal();
			if(handTotal > 21) { // we busted
				output = -1;
			} else if(dealerTotal > handTotal && dealerTotal <= 21) { // dealer won
				output = -1;
			} else if (dealerTotal == handTotal) { // tie
				output = 0;
			} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) { // we got a blackjack
				output = 1.5;
			} else if(dealer.getHardTotal() > 21) { // dealer busted
				output = 1;
			} else if(dealerTotal < handTotal){ // we won
				output = 1;
			} else { // an outcome that should not be possible
				System.out.println("Something went wrong when evaluating outcomes:");
				System.out.println("\tHand: " + hand.toString());
				System.out.println("\tDealer: " + dealer.toString());
			}
			output = (move == Move.DOUBLE ? output * 2 : output);
//			if (accOutcomes.get(position) == null || accOutcomes.get(position) < output) {
//				accOutcomes.put(position, output);
//				moveOutcomes.put(position, move);
//			}
//			accOutcomes.put(position, output);
			return output;
		}
		// try every possible card draw for the dealer
		HashMap<Integer, Integer> cardCounts = position.getCardsRemaining();
		var tempSet = cardCounts.entrySet();
		for (var entry : tempSet) {
			if (entry.getValue() == 0) {
				continue;
			}
			Card card = new Card(entry.getKey());
			double temp = (entry.getValue()-1) * dealerPossibilities(position.updateDealer(card), move);
			output += temp;
		}
		output = output / position.getNumCards();
		return output;
	}
	
	private static double getSplitScore(GameState position, Move move, GameState head) {
		Hand hand = position.getHand();
		Hand dealer = position.getDealer();
		Hand others = position.getOthers();
		if (accOutcomes.containsKey(position)) {
			return accOutcomes.get(position); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
		// if we have busted, there is no point in continuing since we lose
//		if ((hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal()) > 21) {
////			double outcome = (move == Move.DOUBLE ? -2.0 : -1.0);
////			accOutcomes.put(position, outcome);
//			double outcome = 0.0;
//			if (position.next == null) {
//				GameState tempPos = head;
//				position.lastMove = move;
//				while (tempPos.next != null) {
//					outcome += dealerPossibilities(tempPos, tempPos.lastMove);
//				}
////				stayOutcome += dealerPossibilities(position, Move.STAY);
//			} else {
//				position.lastMove = Move.STAY;
//				outcome += getSplitScore(position.next, Move.SPLIT, head);
//			}
//			if (accOutcomes.get(position) == null || accOutcomes.get(position) < outcome) {
//				accOutcomes.put(position, outcome);
//				moveOutcomes.put(position, move);
//			}
////			accOutcomes.put(position, outcome);
//			return outcome;
//		}
		double scoreOutcome = Double.MIN_VALUE;
		Move moveOutcome = null;
		HashMap<Integer, Integer> cardCounts = position.getCardsRemaining();
		if (hand.getHand().size() == 2) {
			scoreOutcome = -0.5 + (position.next != null ? getSplitScore(position.next, Move.SPLIT, head) : 0.0);
			moveOutcome = Move.SURRENDER;
			
			// all splitting instances created by getDescendentScore
			// above is incorrect
			// if this hand should split, try all splitting possibilities
			if (hand.getHand().get(0) == hand.getHand().get(1)) {
				double splitScore = 0.0;
				var tempSet = cardCounts.entrySet();
				for (var entry1 : tempSet) {
					if (entry1.getValue() == 0) {
						continue;
					}
					int tempCount1 = entry1.getValue() - 1;
					for (var entry2 : tempSet) {
						if (entry2.getValue() == 0 || (entry2.getKey().equals(entry1.getKey()) && tempCount1 == 0)) {
							continue;
						}
						// draw new cards for both hands, update the states to reflect the new missing cards
						Hand hand1 = new Hand(List.of(hand.getHand().get(0), new Card(entry1.getKey())));
						Hand hand2 = new Hand(List.of(hand.getHand().get(0), new Card(entry2.getKey())));
						Hand dealer1 = new Hand(dealer.getHand());
						Hand dealer2 = new Hand(dealer.getHand());
						Hand others1 = new Hand(others.getHand());
						others1.addCard(hand2.getHand().get(0));
						others1.addCard(hand2.getHand().get(1));
						List<Card> origOthers = others.getHand();
						origOthers.addAll(hand1.getHand());
						Hand others2 = new Hand(origOthers);
						GameState position1 = new GameState(dealer1, hand1, others1);
						GameState position2 = new GameState(dealer2, hand2, others2);
						// set the first new hand to point to the second
						position1.next = position2;
						// make copies of all other remaining hands to play, and add them to the chain
						GameState tempOrig = position;
						GameState tempDupe = position2;
						while (tempOrig != null) {
							Hand newOthers = tempOrig.getOthers();
							newOthers.addCard(hand1.getHand().get(1));
							newOthers.addCard(hand2.getHand().get(1));
							tempDupe.next = new GameState(tempOrig.getDealer(), tempOrig.getHand(), newOthers);
							tempOrig = tempOrig.next;
							tempDupe = tempDupe.next;
						}
						splitScore = getSplitScore(position1, Move.SPLIT, position1);
					}
				}
				if (splitScore >= scoreOutcome) {
					scoreOutcome = splitScore;
					moveOutcome = Move.SPLIT;
				}
			}
			
			// check doubling outcomes
			double doubleOutcome = 0.0;
			var tempSet = cardCounts.entrySet();
			// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
			for (var entry : tempSet) {
				// update hand and deck appropriately
				Card card = new Card(entry.getKey());
				if (position.next == null) {
					// if evaluating terminating hand, evaluate all outcomes
					GameState tempPos = head;
					while (tempPos.next != null) {
						doubleOutcome += (entry.getValue()/(position.getNumCards()-1) * dealerPossibilities(tempPos.updateOthers(card), tempPos.lastMove));
					}
					position.lastMove = Move.DOUBLE;
					doubleOutcome += (entry.getValue()/(position.getNumCards()-1) * dealerPossibilities(position.updateHand(card), Move.DOUBLE));
				} else {
					// if evaluating non-terminating hand, recurse
					position.lastMove = Move.DOUBLE;
					doubleOutcome += getSplitScore(position.next.updateOthers(card), Move.SPLIT, head);
				}
			}
			if (doubleOutcome >= scoreOutcome) {
				scoreOutcome = doubleOutcome;
				moveOutcome = Move.DOUBLE;
			}
		}
		// check staying outcomes
		double stayOutcome = 0.0;
		if (position.next == null) {
			GameState tempPos = head;
			position.lastMove = Move.STAY;
			while (tempPos.next != null) {
				stayOutcome += dealerPossibilities(tempPos, tempPos.lastMove);
			}
//			stayOutcome += dealerPossibilities(position, Move.STAY);
		} else {
			position.lastMove = Move.STAY;
			stayOutcome += getSplitScore(position.next, Move.SPLIT, head);
		}
		if (stayOutcome >= scoreOutcome) {
			scoreOutcome = stayOutcome;
			moveOutcome = Move.STAY;
		}
		// check hitting outcomes
		double hitOutcome = 0.0;
		var tempSet = cardCounts.entrySet();
		// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
		for (var entry : tempSet) {
			if (entry.getValue() == 0) {
				continue;
			}
			// update hand and deck appropriately
			Card card = new Card(entry.getKey());
			GameState newPosition = position.updateHand(card);
			// recursively call this function
			if (newPosition.next != null) {
				if ((newPosition.getHand().getSoftTotal() > 21 ? newPosition.getHand().getHardTotal() : newPosition.getHand().getSoftTotal()) > 21) {
					// scenario 1: hand 1 has busted, recurse on hand 2
					newPosition.lastMove = Move.HIT;
					hitOutcome += (entry.getValue()/(newPosition.getNumCards()) * getSplitScore(newPosition.next.updateOthers(card), Move.SPLIT, head));
				} else {
					// scenario 2: hand 1 has not busted, recurse on hand 1 again
					hitOutcome += (entry.getValue()/(newPosition.getNumCards()) * getSplitScore(newPosition, Move.HIT, head));
				}
			} else {
				if ((newPosition.getHand().getSoftTotal() > 21 ? newPosition.getHand().getHardTotal() : newPosition.getHand().getSoftTotal()) > 21) {
					// scenario 3: hand 2 has busted, calculate final scores
					GameState tempPos = head;
					while (tempPos.next != null) {
						hitOutcome += (entry.getValue()/(newPosition.getNumCards()) * dealerPossibilities(tempPos.updateOthers(card), tempPos.lastMove));
					}
					position.lastMove = Move.HIT;
					hitOutcome += (entry.getValue()/(newPosition.getNumCards()) * dealerPossibilities(newPosition, Move.HIT));
				} else {
					// scenario 4: hand 2 has not busted, recurse on hand 2 again
					hitOutcome += (entry.getValue()/(newPosition.getNumCards()) * getSplitScore(newPosition, Move.HIT, head));
				}
			}
			// each iteration of the loop adds the averaged score for that outcome, weighted based on all possible outcomes
		}
		if (hitOutcome >= scoreOutcome) {
			scoreOutcome = hitOutcome;
			moveOutcome = Move.HIT;
		}
		if (scoreOutcome > 8 || scoreOutcome < -8) {
			System.out.println("ERROR: a call to getSplitScore returned a value outside of acceptable bounds");
		}
		accOutcomes.put(position, scoreOutcome);
		moveOutcomes.put(position, moveOutcome);
		return scoreOutcome;
	}
	
	/**
	 * evaluate the outcomes of a game situation after implementing the strategy. Revised for homework 4
	 * 
	 * @param dealer the dealer's card
	 * @param hand   cards in the hand
	 * @param deck   a list of cards
	 * @param move   the move taken by the player
	 * @return the outcome of the current hand
	 */
	private static Double evaluateOutcomeRevised(Hand dealer, Hand hand, Deck deck, Move move) {
		while ((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
			dealer.addCard(deck.draw());
		}
		if (move == Move.SURRENDER) {
			return -0.5;
		}
		double output = 0.0;
		int handTotal = hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal();
		int dealerTotal = dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal();
		if(handTotal > 21) { // we busted
			output = -1;
		} else if(dealerTotal > handTotal && dealerTotal <= 21) { // dealer won
			output = -1;
		} else if (dealerTotal == handTotal) { // tie
			output = 0;
		} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) { // we got a blackjack
			output = 1.5;
		} else if(dealer.getHardTotal() > 21) { // dealer busted
			output = 1;
		} else if(dealerTotal < handTotal){ // we won
			output = 1;
		} else { // an outcome that should not be possible
			System.out.println("Something went wrong when evaluating outcomes:");
			System.out.println("\tHand: " + hand.toString());
			System.out.println("\tDealer: " + dealer.toString());
		}
		return (move == Move.DOUBLE ? output * 2 : output);
	}

	/**
	 * Reads from a csv file, uses the strategy on each game, and writes a new
	 * output file containing the results.
	 * 
	 * @param args string arguments
	 */
	public static void main(String[] args) {
		for (String arg : args) {
			System.out.println(arg);
		}
		loadTables();
		File inputCSV;
		if (args.length > 0) {
			inputCSV = new File(args[0]);
		} else {
			inputCSV = new File(System.getProperty("user.dir") + "/src/testFiles/input.csv");
		}
		// gets the current file
		if (!inputCSV.isFile()) {
			System.out.println("File passed is not a file");
			return;
		}
		BufferedReader readIn;
		try {
			readIn = new BufferedReader(new FileReader(inputCSV));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Running solver on all lines...");
		// Read all of the lines in the input and execute the strategy on them.
		// should run the comparison 1,000,000 times per input
//		System.out.println("Making tasks (this will take awhile)...");
		AtomicInteger testCount = new AtomicInteger(0);
//		String output = readIn.lines().parallel().map((elem) -> {
//			if (elem.startsWith(",,")) {
//				final int index = testCount.getAndIncrement();
//				strat1Totals.add(new AtomicInteger());
//				strat2Totals.add(new AtomicInteger());
//				for (int i = 0; i < 2000000; i++) {
//					compareStrategiesHW4(elem, index);
//				}
//				System.out.println("Iteration " + index + " complete");
//				return ((double)strat1Totals.get(index).get() / 20000000.0) + "," + ((double)strat2Totals.get(index).get() / 20000000.0)
//						+ "," + elem.substring(2, elem.length());
//			} else {
//				return elem;
//			}
//		}).collect(Collectors.joining("\r\n"));
		String output = readIn.lines().parallel().map((elem) -> {
			if (elem.startsWith(",,")) {
				testCount.set(testCount.get() + 1);
				System.out.println("init line " + testCount.get());
				final GameState initial = loadGameState(elem);
				getDescendentScore(initial, Move.HIT);
				ArrayList<Card> drawnCards = new ArrayList<>();
				drawnCards.addAll(initial.getDealer().getHand());
				drawnCards.addAll(initial.getHand().getHand());
				drawnCards.addAll(initial.getOthers().getHand());
				Random rndSeed = new Random();
				double wikiOut = 0.0;
				double idealOut = 0.0;
				System.out.println("running sim for line " + testCount.get());
				for (int i = 0; i < 100; i++) {
					long seed = rndSeed.nextLong();
//					long seed = 6126672029307901454L;
//					System.out.println(seed);
					Random rnd1 = new Random(seed);
					Random rnd2 = new Random(seed);
					Deck deck1 = new Deck(drawnCards);
					Deck deck2 = new Deck(drawnCards);
					deck1.shuffle(rnd1);
					deck2.shuffle(rnd2);
					Hand tempDealer1 = new Hand(List.of(initial.getDealer().getHand().get(0)));
					Hand tempDealer2 = new Hand(List.of(initial.getDealer().getHand().get(0)));
					Hand tempHand1 = new Hand(List.of(initial.getHand().getHand().get(0), initial.getHand().getHand().get(1)));
					Hand tempHand2 = new Hand(List.of(initial.getHand().getHand().get(0), initial.getHand().getHand().get(1)));
					ArrayList<Card> temp2 = new ArrayList<>();
					for (Card card : initial.getOthers().getHand()) {
						temp2.add(card);
					}
					Hand tempOthers2 = new Hand(temp2);
					wikiOut += wikiStrat(tempDealer1, tempHand1, deck1);
					idealOut += strategyIdeal(tempDealer2, tempHand2, tempOthers2, deck2);
				}
				idealOut /= 100;
				wikiOut /= 100;
				// accOutcomes only needs to run once since the output is always the same
				return (idealOut + "," + wikiOut + "," + elem.substring(2, elem.length()));
			} else {
				return elem;
			}
		}).collect(Collectors.joining("\r\n"));
		
		System.out.println("All lines solved");
		try {
			readIn.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		// output the results to a new file
		try {
			FileWriter writer;
			if (args.length > 1) {
				writer = new FileWriter(new File(args[1]));
			} else {
				writer = new FileWriter(new File(System.getProperty("user.dir") + "/src/testFiles/outputHW5.csv"));
			}
			System.out.println("Writing to output...");
			writer.write(output);
			System.out.println("Done writing.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static GameState loadGameState(String input) {
		String[] hexCards = input.split(",");
		// get the dealer's card and hand
		Card dealerCard = new Card(hexCards[2]);
		Hand dealer1 = new Hand(List.of(dealerCard));
		// get our hand
		Card yourCard1 = new Card(hexCards[11]);
		Card yourCard2 = new Card(hexCards[12]);
		Hand hand1 = new Hand(List.of(yourCard1, yourCard2));
		// make two shuffled decks that are identical
		// first, get all the cards that are not in the decks
		ArrayList<Card> cards = new ArrayList<>();
		for (String strCard : hexCards) {
			if (!strCard.equals("")) {
				cards.add(new Card(strCard));
			}
		}
		cards.remove(yourCard1);
		cards.remove(yourCard2);
		cards.remove(dealerCard);
//		cards.removeAll(hand1.getHand());
//		cards.removeAll(dealer1.getHand());
		// make "hands" of the other player cards
		Hand others1 = new Hand(cards);
		return new GameState(dealer1, hand1, others1);
	}

	/**
	 * Loads in the pairs, hard, and soft csv tables for the current strategy
	 */
	private static void loadTables() {
		// pairs csv
		BufferedReader pairsReader;
		try {
			pairsReader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.dir") + "/src/blackjack/tables/pairs.csv")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		var pairsStr = pairsReader.lines().toList(); // convert relevant data to arrays
		for (int i = 1; i < pairsStr.size(); i++) {
			pairs[i - 1] = Arrays.copyOfRange(pairsStr.get(i).split(","), 1, 11);
		}
		// soft csv
		BufferedReader softReader;
		try {
			softReader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.dir") + "/src/blackjack/tables/soft.csv")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		var softStr = softReader.lines().toList();
		for (int i = 1; i < softStr.size(); i++) {
			soft[i - 1] = Arrays.copyOfRange(softStr.get(i).split(","), 1, 11);
		}
		// hard csv
		BufferedReader hardReader;
		try {
			hardReader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.dir") + "/src/blackjack/tables/hard.csv")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		var hardStr = hardReader.lines().toList();
		for (int i = 1; i < hardStr.size(); i++) {
			hard[i - 1] = Arrays.copyOfRange(hardStr.get(i).split(","), 1, 11);
		}
	}

}
