package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
			if (strCard != "") {
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
				throw new IllegalArgumentException();
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
	
	private static HashMap<GameState, Double> accOutcomes = new HashMap<>();
	
	private static HashMap<GameState, Move> moveOutcomes = new HashMap<GameState, Move>();
	
	private static int strategy3(Hand dealer, Hand hand, Hand others, GameState position, Deck deck, Move move) {
		int score = 0;
		for (Move option : Move.values()) {
			// skip double and surrender if they are not available options
			if ((option == Move.DOUBLE || option == Move.SURRENDER) && hand.getHand().size() != 2) {
				continue;
			}
			double temp = getDescendentScore(dealer, hand, others, position, deck, option);
		}
	}
	
	// *** TODO: new tempDecks are not getting shuffled properly!!! ***
	private static double getDescendentScore(Hand dealer, Hand hand, Hand others, GameState position, Deck deck, Move move) {
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
		if (accOutcomes.containsKey(position)) {
			return accOutcomes.get(position); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
		// if we have busted, there is no point in continuing since we lose
		if ((hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal()) > 21) {
			double outcome = (move == Move.DOUBLE ? -2.0 : -1.0);
			accOutcomes.put(position, outcome);
			return outcome;
		}
		double scoreOutcome = -0.5;
		Move moveOutcome = Move.SURRENDER;
		HashMap<Integer, Integer> cardCounts = deck.getCardCounts();
		if (hand.getHand().size() == 2) {
			
			// TODO: implement splitting behavior (not easy b/c have to play both possibilities before dealerPossibilities)
				// basically, want to play first hand until it doesn't hit. When that happens, play the second hand until it doesn't hit. Then dealer.
				// first, create 2 hands and 2 gameStates, where each hand has 1 card, and the other card is in the "others" section of GameState
				// will have to setup a double-nested for loop of calls so each hand has the given, split cards (can't stay or surrender on 1 card)
					// or have to add conditions where can only hit if the hand has < 2 cards in it
				// play hand 1 like this function, except when reach what is a dealerPossibilities call here, instead add the cards in hand1 to hand2's gameState
				// under "others". Then, eval hand2 just like hand1 was. When hand2 reaches what would be dealerPossibilities, instead add the cards in hand2 to
				// hand1's gameState under "others". Then, proceed to the regular dealerPossibilities call for each hand and state. dealer's cards and probabilities 
				// should be the same in both cases, but the chance of winning will vary depending on each hand
			Hand hand2 = new Hand(List.of(hand.getHand().get(0)));
			Hand dealer2 = new Hand(dealer.getHand());
			List<Card> origOthers = others.getHand();
			origOthers.add(hand.getHand().get(0));
			Hand others2 = new Hand(origOthers);
			GameState position2 = new GameState(dealer2, hand2, others2);
			double splitScore = getSplitScore(dealer, hand, hand2, false, others, position, position2, deck, Move.SPLIT);
			
			
			// check doubling outcomes
			double doubleOutcome = 0.0;
			var tempSet = cardCounts.entrySet();
			// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
			for (var entry : tempSet) {
				// update hand and deck appropriately
				Card card = new Card(entry.getKey());
				Hand tempHand = new Hand(hand.getHand());
				tempHand.addCard(card);
				ArrayList<Card> allRmCards = new ArrayList<>();
				allRmCards.addAll(dealer.getHand());
				allRmCards.addAll(hand.getHand());
				allRmCards.addAll(others.getHand());
				allRmCards.add(card);
				Deck tempDeck = new Deck(allRmCards);
				doubleOutcome += (entry.getValue()/tempDeck.getNumCards() * dealerPossibilities(dealer, tempHand, others, position.updateHand(card), tempDeck, Move.DOUBLE));
			}
			if (doubleOutcome >= scoreOutcome) {
				scoreOutcome = doubleOutcome;
				moveOutcome = Move.DOUBLE;
			}
		}
		
		// check staying outcomes
		double stayOutcome = dealerPossibilities(dealer, hand, others, position, deck, Move.STAY);
		if (stayOutcome >= scoreOutcome) {
			scoreOutcome = stayOutcome;
			moveOutcome = Move.STAY;
		}
		
		// check hitting outcomes
		double hitOutcome = 0.0;
		var tempSet = cardCounts.entrySet();
		// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
		for (var entry : tempSet) {
			// update hand and deck appropriately
			Card card = new Card(entry.getKey());
			Hand tempHand = new Hand(hand.getHand());
			tempHand.addCard(card);
			ArrayList<Card> allRmCards = new ArrayList<>();
			allRmCards.addAll(dealer.getHand());
			allRmCards.addAll(hand.getHand());
			allRmCards.addAll(others.getHand());
			allRmCards.add(card);
			Deck tempDeck = new Deck(allRmCards);
			// recursively call this function
			hitOutcome += (entry.getValue()/tempDeck.getNumCards() * getDescendentScore(dealer, tempHand, others, position.updateHand(card), tempDeck, Move.HIT));
		}
		if (hitOutcome >= scoreOutcome) {
			scoreOutcome = hitOutcome;
			moveOutcome = Move.HIT;
		}
		accOutcomes.put(position, scoreOutcome);
		return scoreOutcome;
	}
	
	private static double dealerPossibilities(Hand dealer, Hand hand, Hand others, GameState position, Deck deck, Move move) {
		// we have been here before; just return right away
		if (accOutcomes.containsKey(position)) {
			return accOutcomes.get(position).doubleValue(); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
		double output = 0.0;
		// dealer no longer wants to take cards; evaluate the outcomes
		if ((dealer.hasAce() && dealer.getSoftTotal() > 17) || dealer.getHardTotal() >= 17) { // dealer is done; eval outcome time
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
			output = (move == Move.DOUBLE ? output * 2 : output);
			accOutcomes.put(position, output);
			return output;
		}
		// try every possible card draw for the dealer
		HashMap<Integer, Integer> cardCounts = deck.getCardCounts();
		var tempSet = cardCounts.entrySet();
		for (var entry : tempSet) {
			Card card = new Card(entry.getKey());
			Hand tempDealer = new Hand(dealer.getHand());
			tempDealer.addCard(card);
			ArrayList<Card> allRmCards = new ArrayList<>();
			allRmCards.addAll(dealer.getHand());
			allRmCards.addAll(hand.getHand());
			allRmCards.addAll(others.getHand());
			allRmCards.add(card);
			Deck tempDeck = new Deck(allRmCards);
//			deck.remove(card);
			output += (entry.getValue()/tempDeck.getNumCards() * dealerPossibilities(tempDealer, hand, others, position.updateDealer(card), tempDeck, move));
		}
		return output;
	}
	
	private static double getSplitScore(GameState position1, GameState position2, boolean secondHand, Deck deck, Move move) {
		Hand hand;
		Hand dealer;
		Hand others;
		GameState position;
		if (secondHand) {
			hand = position2.getHand();
			dealer = position2.getDealer();
			others = position2.getOthers();
			for (Card card : position1.getHand().getHand()) {
				others.addCard(card);
			}
			position = position2;
			position.updateOthers(hand1.getHand());
		} else {
			hand = position1.getHand();
			dealer = position1.getDealer();
			others = position2.getOthers();
			for (Card card : position2.getHand().getHand()) {
				others.addCard(card);
			}
			position = position1;
			position.updateOthers(hand2.getHand());
		}
		if (accOutcomes.containsKey(position)) {
			return accOutcomes.get(position); // NEEDS TO BE CHANGED TO REFLECT MOVE INFO TOO?
		}
//		// if we have busted, there is no point in continuing since we lose
//		if ((hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal()) > 21) {
//			double outcome = (move == Move.DOUBLE ? -2.0 : -1.0);
//			accOutcomes.put(position, outcome);
//			return outcome;
//		}
		double scoreOutcome = -0.5;
		Move moveOutcome = Move.SURRENDER;
		HashMap<Integer, Integer> cardCounts = deck.getCardCounts();
		if (hand.getHand().size() == 2) {
			// check doubling outcomes
			double doubleOutcome = 0.0;
			var tempSet = cardCounts.entrySet();
			// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
			for (var entry : tempSet) {
				// update hand and deck appropriately
				Card card = new Card(entry.getKey());
				Hand tempHand = new Hand(hand.getHand());
				tempHand.addCard(card);
				ArrayList<Card> allRmCards = new ArrayList<>();
				allRmCards.addAll(dealer.getHand());
				allRmCards.addAll(hand.getHand());
				allRmCards.addAll(others.getHand());
				allRmCards.add(card);
				Deck tempDeck = new Deck(allRmCards);
				if (secondHand) {
					Hand tempHand1 = new Hand(hand1.getHand());
					Deck tempDeck1 = new Deck(allRmCards);
					doubleOutcome += (entry.getValue()/tempDeck.getNumCards() * dealerPossibilities(dealer, tempHand1, others, position1, tempDeck1, move));
					doubleOutcome += (entry.getValue()/tempDeck.getNumCards() * dealerPossibilities(dealer, tempHand, others, position.updateHand(card), tempDeck, Move.DOUBLE));
				} else {
					getSplitScore(dealer, tempHand, hand2, true, others, position.updateHand(card), position2.updateOthers(List.of(card)), tempDeck, Move.DOUBLE);
				}
			}
			if (doubleOutcome >= scoreOutcome) {
				scoreOutcome = doubleOutcome;
				moveOutcome = Move.DOUBLE;
			}
		}
		// --- Above should be complete ---
		
		// check staying outcomes
		double stayOutcome = dealerPossibilities(dealer, hand, others, position, deck, Move.STAY);
		if (stayOutcome >= scoreOutcome) {
			scoreOutcome = stayOutcome;
			moveOutcome = Move.STAY;
		}
		// check hitting outcomes
		double hitOutcome = 0.0;
		var tempSet = cardCounts.entrySet();
		// each key is the numerical card value (1 - 10), each value is the number of cards with that value in the deck (ie 0-4 for 1-9, 0-16 for 10-K)
		for (var entry : tempSet) {
			// update hand and deck appropriately
			Card card = new Card(entry.getKey());
			Hand tempHand = new Hand(hand.getHand());
			tempHand.addCard(card);
			ArrayList<Card> allRmCards = new ArrayList<>();
			allRmCards.addAll(dealer.getHand());
			allRmCards.addAll(hand.getHand());
			allRmCards.addAll(others.getHand());
			allRmCards.add(card);
			Deck tempDeck = new Deck(allRmCards);
			// recursively call this function
			hitOutcome += (entry.getValue()/tempDeck.getNumCards() * getDescendentScore(dealer, tempHand, others, position.updateHand(card), tempDeck, Move.HIT));
		}
		if (hitOutcome >= scoreOutcome) {
			scoreOutcome = hitOutcome;
			moveOutcome = Move.HIT;
		}
		accOutcomes.put(position, scoreOutcome);
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
		String output = readIn.lines().parallel().map((elem) -> {
			if (elem.startsWith(",,")) {
				final int index = testCount.getAndIncrement();
				strat1Totals.add(new AtomicInteger());
				strat2Totals.add(new AtomicInteger());
				for (int i = 0; i < 2000000; i++) {
					compareStrategiesHW4(elem, index);
				}
				System.out.println("Iteration " + index + " complete");
				return ((double)strat1Totals.get(index).get() / 20000000.0) + "," + ((double)strat2Totals.get(index).get() / 20000000.0)
						+ "," + elem.substring(2, elem.length());
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
				writer = new FileWriter(new File(System.getProperty("user.dir") + "/src/testFiles/output4.csv"));
			}
			System.out.println("Writing to output...");
			writer.write(output);
			System.out.println("Done writing.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
