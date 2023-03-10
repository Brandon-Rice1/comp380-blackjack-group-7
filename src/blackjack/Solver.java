package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

//	private static AtomicInteger maxGain1 = new AtomicInteger();
//	
//	private static AtomicInteger maxLoss1 = new AtomicInteger();
//	
//	/**
//	 * NOTE: will be 10x larger than expected since this is an integer
//	 */
//	private static AtomicInteger total1 = new AtomicInteger();
//	
//	private static AtomicInteger maxGain2 = new AtomicInteger();
//	
//	private static AtomicInteger maxLoss2 = new AtomicInteger();
//	
//	/**
//	 * NOTE: will be 10x larger than expected since this is an integer
//	 */
//	private static AtomicInteger total2 = new AtomicInteger();
//	
//	private static AtomicInteger numTrials = new AtomicInteger();

	private static ArrayList<AtomicInteger> strat1Totals = new ArrayList<>();
//	private static ArrayList<Double> strat1Totals = new ArrayList<>();

	private static ArrayList<AtomicInteger> strat2Totals = new ArrayList<>();
//	private static ArrayList<Double> strat2Totals = new ArrayList<>();

//	/**
//	 * The naive strategy for determining if we should hit or stay
//	 * 
//	 * @param input the raw csv input line
//	 * @return a properly formatted for csv line that includes our decision
//	 */
//	private static String strategy(String input) {
//		// ignore the 'comment' lines in the input
//		if (input.contains("==>")) {
//			return input;
//		}
//		// initializes the card objects
//		String[] hexCards = input.split(",");
//		Card dealer;
//		if (hexCards[1].length() > 0) {
//			dealer = new Card(hexCards[1]);
//		} else {
//			dealer = null;
//		}
//		ArrayList<Card> cards = new ArrayList<>();
//		for (int i = 8; i < hexCards.length; i++) {
//			cards.add(new Card(hexCards[i]));
//		}
//		Hand hand = new Hand(hexCards);
//		// homework 2 strategy
////		if (hand.getHardTotal() > 21) {
////			System.out.println("ERROR: hand's hard total was greater than 21, which is not covered under the tables");
////			System.exit(-2);
////		}
//		// pair of same card = pairs table, which only has single options
//		// alternate idea: instead of arrays, use nested dictionaries (hand, dealer,
//		// hasAce)
//		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
//			if (hand.hasAce()) {
//				return pairs[9][dealer.getSoftValue() - 2] + input;
//			}
//			return pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftValue() - 2] + input;
//		}
//		String lookup = "";
//		if (hand.hasAce()) {
//			// contains ace = soft table
//			if (hand.getSoftTotal() == 21) {
//				lookup = "STAY";
//			} else if (hand.getSoftTotal() < 21) {
//				lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftValue() - 2];
//			}
//		}
//		// if no ace OR ace as 11 was too big, use the hard table (ace is 1)
//		if (lookup.equals("")) {
//			// no ace, no pair = hard table
//			if (hand.getHardTotal() == 21) {
//				lookup = "STAY";
//			} else if (hand.getHardTotal() > 21) {
//				throw new IllegalArgumentException();
//			}
//			else {
//				lookup = hard[(hand.getHardTotal()) - 5][dealer.getSoftValue() - 2];
//			}
//		}
//		// alternate idea: reverse lookup, split on '/', reverse each half of the
//		// string; only 1 condition (if 2 cards and 2nd is non-empty)
//		if (lookup.contains("/")) {
//			// if there are multiple options and there are 2 cards, take the first option
//			if (hand.getHand().size() == 2) {
//				lookup =  lookup.split("/")[0];
//			} else { // otherwise, take the second
//				lookup = lookup.split("/")[1];
//			}
//		}
//		return lookup + input;
////		// homework 1 strategy below
////		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
////			return "STAY" + input;
////		} else {
////			return "HIT" + input;
////		}
//
//	}

//	/**
//	 *
//	 * @param input an input line of the csv file
//	 * @return 0
//	 */
//	private static int compareStrategies() {
//		numTrials.addAndGet(1);
//		// make two shuffled decks that are identical
//		Deck deck1 = new Deck();
//		Deck deck2 = new Deck();
//		Random rndSeed = new Random();
//		final long seed = rndSeed.nextLong();
//		Random rnd1 = new Random(seed);
//		Random rnd2 = new Random(seed);
//		deck1.shuffle(rnd1);
//		deck2.shuffle(rnd2); // NOTE: probably need to test that these are equal
//		assert deck1 == deck2;
//		// make the dealer's hand
//		Card dealerCard = deck1.draw();
//		Hand dealer1 = new Hand(List.of(dealerCard));
//		Hand dealer2 = new Hand(List.of(dealerCard));
//		deck2.draw();
//		// make your hand
//		Card yourCard1 = deck1.draw();
//		Card yourCard2 = deck1.draw();
//		Hand hand1 = new Hand(List.of(yourCard1, yourCard2));
//		Hand hand2 = new Hand(List.of(yourCard1, yourCard2));
//		deck2.draw();
//		deck2.draw();
//		// remove cards for between 0 and 3 other players
//		final int players = rndSeed.nextInt(4);
//		for (int i = 0; i < players * 2; i++) {
//			deck1.draw();
//			deck2.draw();
//		}
//		// test the strategies
//		total1.addAndGet(strategy1(dealer1, hand1, deck1, Move.HIT));
//		total2.addAndGet(strategy2(dealer2, hand2, deck2, Move.HIT));
//		return 0;
//	}

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

//	/**
//	 * evaluate the outcomes of a game situation after implementing the strategy
//	 * 
//	 * @param dealer the dealer's card
//	 * @param hand   cards in the hand
//	 * @param deck   a list of cards
//	 * @param move   the move taken by the player
//	 * @return the outcome of the current hand
//	 */
//	private static Double evaluateOutcome(Hand dealer, Hand hand, Deck deck, Move move) {
//		// dealer makes the move(s)
//		while ((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
//			dealer.addCard(deck.draw());
//		}
//		// you have blackjack
//		if (hand.getSoftTotal() == 21 && hand.getHand().size() == 2) {
//			// dealer has blackjack
//			if (dealer.getSoftTotal() == 21 && dealer.getHand().size() == 2) {
//				return 0.0;
//			} else { // dealer doesn't have blackjack
//				// you doubled or didn't
//				if (move == Move.DOUBLE) {
//					return 3.0;
//				} else {
//					return 1.5;
//				}
//			}
//			// if the dealer has blackjack and you don't
//		} else if (dealer.getSoftTotal() == 21 && dealer.getHand().size() == 2) {
//			return -1.0;
//			// if you busted
//		} else if (hand.getHardTotal() > 21) {
//			if (move == Move.DOUBLE) {
//				return -2.0;
//			} else {
//				return -1.0;
//			}
//			// if you surrendered
//		} else if (move == Move.SURRENDER) {
//			return -0.5;
//			// Comparing non-blackjack hands
//			// hands are the same
//		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal()
//				: dealer.getSoftTotal()) == (hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
//			return 0.0;
//			// dealer wins
//		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal()
//				: dealer.getSoftTotal()) > (hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
//			if (move == Move.DOUBLE) {
//				return -2.0;
//			} else {
//				return -1.0;
//			}
//			// you win
//		} else {
//			if (move == Move.DOUBLE) {
//				return 2.0;
//			} else {
//				return 1.0;
//			}
//		}
//	}
	
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
		if((hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal()) > 21) {
			output = -1;
		} else if((dealer.getSoftTotal() > 21 ? dealer.getHardTotal()
				: dealer.getSoftTotal()) > (hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
			output = -1;
		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal()
				: dealer.getSoftTotal()) == (hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
			output = 0;
		} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) {
			output = 1.5;
		} else if(dealer.getHardTotal() > 21) {
			output = 1;
		} else if((dealer.getSoftTotal() > 21 ? dealer.getHardTotal()
				: dealer.getSoftTotal()) < (hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())){
			output = 1;
		} else {
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
//		String output = readIn.lines().parallel().map(elem -> strategy(elem)).collect(Collectors.joining("\r\n"));
//		double total = readIn.lines().parallel().map(elem -> compareStrategies(elem)).collect(Collectors.summingDouble((elem)->elem));
//		readIn.lines().parallel().forEachOrdered(elem -> compareStrategies(elem));
		// should run the comparison 100,000,000 times
//		ExecutorService tasks = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		System.out.println("Making tasks (this will take awhile)...");
//		for (int i = 0; i < 100000000; i++) {
////			System.out.println("Running iteration: " + i);
//			tasks.execute(() -> compareStrategies());
//		}
		AtomicInteger testCount = new AtomicInteger(0);
		String output = readIn.lines().parallel().map((elem) -> {
			if (elem.startsWith(",,")) {
				final int index = testCount.getAndIncrement();
				strat1Totals.add(new AtomicInteger());
//				strat1Totals.add(0.0);
				strat2Totals.add(new AtomicInteger());
//				strat2Totals.add(0.0);
				for (int i = 0; i < 2000000; i++) {
//					tasks.execute(() -> compareStrategiesHW4(elem, index));
					compareStrategiesHW4(elem, index);
				}
				System.out.println("Iteration " + index + " complete");
				return ((double)strat1Totals.get(index).get() / 20000000.0) + "," + ((double)strat2Totals.get(index).get() / 20000000.0)
						+ "," + elem.substring(2, elem.length());
			} else {
				return elem;
			}
		}).collect(Collectors.joining("\r\n"));
		// ALTERNATIVE IDEA: not sure if it works/is faster, but could have the stream
		// stick everything in the pool and then do the whole pool.
//		readIn.lines().parallel().filter((elem) -> elem.startsWith(",,")).forEachOrdered((elem) -> {
//			final int index = testCount.getAndIncrement();		
//			for (int i = 0; i < 2000000; i++) {
//				tasks.execute(() -> compareStrategiesHW4(elem, index));
//			}
//			});
//		tasks.shutdown();
//		try {
//			System.out.println("Waiting on tasks to complete...");
//			if (!tasks.awaitTermination(5, TimeUnit.MINUTES)) {
//				System.out.println("Timed out waiting for things to finish");
//			}
//		} catch (InterruptedException e1) {
//			e1.printStackTrace();
//		}
//		String output = ",Strategy 1,Strategy2\r\nSingle Round Outcome,"+((double)total1.get() / 10)/numTrials.get()+","+((double)total2.get() / 10)/numTrials.get()+"\r\nMax Gain,"+(double)maxGain1.get()/10+","+(double)maxGain2.get()/10+"\r\nMax Loss,"+(double)maxLoss1.get()/10+","+(double)maxLoss2.get()/10+"\r\n";
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
