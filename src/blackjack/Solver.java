package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
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
	
	private static AtomicInteger maxGain1 = new AtomicInteger();
	
	private static AtomicInteger maxLoss1 = new AtomicInteger();
	
	/**
	 * NOTE: will be 10x larger than expected since this is an integer
	 */
	private static AtomicInteger total1 = new AtomicInteger();
	
	private static AtomicInteger maxGain2 = new AtomicInteger();
	
	private static AtomicInteger maxLoss2 = new AtomicInteger();
	
	/**
	 * NOTE: will be 10x larger than expected since this is an integer
	 */
	private static AtomicInteger total2 = new AtomicInteger();
	
	private static AtomicInteger numTrials = new AtomicInteger();

	/**
	 *
	 * @param input an input line of the csv file
	 * @return 0
	 */
	private static int compareStrategies() {
		numTrials.addAndGet(1);
		// make two shuffled decks that are identical
		Deck deck1 = new Deck();
		Deck deck2 = new Deck();
		Random rndSeed = new Random();
		final long seed = rndSeed.nextLong();
		Random rnd1 = new Random(seed);
		Random rnd2 = new Random(seed);
		deck1.shuffle(rnd1);
		deck2.shuffle(rnd2); // NOTE: probably need to test that these are equal
		// make the dealer's hand
		Card dealerCard = deck1.draw();
		Hand dealer1 = new Hand(List.of(dealerCard));
		Hand dealer2 = new Hand(List.of(dealerCard));
		deck2.draw();
		// make your hand
		Card yourCard1 = deck1.draw();
		Card yourCard2 = deck1.draw();
		Hand hand1 = new Hand(List.of(yourCard1, yourCard2));
		Hand hand2 = new Hand(List.of(yourCard1, yourCard2));
		deck2.draw();
		deck2.draw();
		// remove cards for between 0 and 3 other players
		final int players = rndSeed.nextInt(4);
		for (int i = 0; i < players * 2; i++) {
			deck1.draw();
			deck2.draw();
		}
		// test the strategies
		total1.addAndGet(strategy1(dealer1, hand1, deck1, Move.HIT, seed));
		total2.addAndGet(strategy2(dealer2, hand2, deck2, Move.HIT));
		return 0;
	}

	/**
	 * implementing strategy 1 (the naive strategy in homework1)
	 * @param dealer the dealer's card
	 * @param hand cards in the hand
	 * @param deck the list of cards
	 * @param move the move taken by the player
	 * @return the strategy1 results of the case
	 */
	private static int strategy1(Hand dealer, Hand hand, Deck deck, Move move, Long seed) {
		// condition to return if hand is final
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER) {// || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcomeRevised(dealer, hand, deck, move) * 10;
			int temp = outcome.intValue();
			if (temp > 80 || temp < -80) {
				System.out.println("ERROR: invlid outcome total");
				System.out.println("\toutcome: " + temp);
				System.out.println("\thand: " + hand.getHand());
				System.out.println("\tdealer: " + dealer.getHand());
			}
			if (temp > maxGain1.get()) {
//				maxGain1.addAndGet(temp);
				maxGain1.set(temp);
			}
			if (temp < maxLoss1.get()) {
//				maxLoss1.addAndGet(temp);
				maxLoss1.set(Math.min(maxLoss1.get(), temp));
				if (temp > 80 || temp < -80) {
					System.out.println("ERROR: invlid outcome total");
					System.out.println("\toutcome: " + temp);
					System.out.println("\thand: " + hand.getHand());
					System.out.println("\tdealer: " + dealer.getHand());
				}
			}
			return temp;
		}
		// logic to decide next move and recurse
		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
			return strategy1(dealer, hand, deck, Move.STAY, seed);
		} else {
			hand.addCard(deck.draw());
			return strategy1(dealer, hand, deck, Move.HIT, seed);
		}
	}

	/**
	 * implementing strategy 2 (the strategy used for homework 2)
	 * @param dealer the dealer's card
	 * @param hand the card in the hand
	 * @param deck the list of cards
	 * @param move the move taken by the player
	 * @return the strategy2 results of the case
	 */
	private static int strategy2(Hand dealer, Hand hand, Deck deck, Move move) {
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcomeRevised(dealer, hand, deck, move) * 10;
			int temp = outcome.intValue();
			if (temp > maxGain2.get()) {
//				maxGain2.addAndGet(temp);
				maxGain2.set(temp);
			}
			if (temp < maxLoss2.get()) {
//				maxLoss2.addAndGet(temp);
				maxLoss2.set(temp);
			}
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
			}
			else {
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
		if (nextMove == Move.DOUBLE || nextMove == Move.HIT) { // if hit or double, draw a card
			hand.addCard(deck.draw());
		} else if (nextMove == Move.SPLIT) { // if split, recurse on each new hand
			Cardtype type = hand.getHand().get(0).getType();
			Hand hand1 = new Hand(List.of(new Card(type), deck.draw()));
			Hand hand2 = new Hand(List.of(new Card(type), deck.draw()));
			// copy the dealer's hand to avoid indexing issues
			Hand dealer2 = new Hand(dealer.getHand());
			// play out the first hand
			strategy2(dealer, hand1, deck, nextMove);
			// put back dealer cards (dealer should not go yet)
			for (int i = dealer.getHand().size()-1; i > 0; i--) {
				deck.putBack(dealer.getHand().get(i));
			}
			// run strategy for other hand
			int outcome2 = strategy2(dealer2, hand2, deck, nextMove);
			// evaluate the first hand based on the actual, final dealer's hand
			Double outcome1 = (evaluateOutcomeRevised(dealer2, hand, deck, move) * 10);
			int total = outcome1.intValue() + outcome2;
			if (total > maxGain2.get()) {
				maxGain2.addAndGet(total);
			}
			if (total < maxLoss2.get()) {
				maxLoss2.addAndGet(total);
			}
			return total;
		}
		return strategy2(dealer, hand, deck, nextMove);
	}

//	/**
//	 * evaluate the outcomes of a game situation after implementing the strategy
//	 * @param dealer the dealer's card
//	 * @param hand cards in the hand
//	 * @param deck a list of cards
//	 * @param move the move taken by the player
//	 * @return the outcome of the current hand
//	 */
//	private static Double evaluateOutcome(Hand dealer, Hand hand, Deck deck, Move move) {
//		// dealer makes the move(s)
//		while((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
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
//		// if the dealer has blackjack and you don't
//		} else if(dealer.getSoftTotal() == 21 && dealer.getHand().size() == 2) {
//			return -1.0;
//		// if you busted
//		} else if(hand.getHardTotal() > 21) {
//			if (move == Move.DOUBLE) {
//				return -2.0;
//			} else {
//				return -1.0;
//			}
//		// if you surrendered
//		} else if (move == Move.SURRENDER) {
//			return -0.5;
//		// Comparing non-blackjack hands
//		// hands are the same
//		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal()) == 
//				(hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
//			return 0.0;
//		// dealer wins
//		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal()) > 
//				(hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
//			if (move == Move.DOUBLE) {
//				return -2.0;
//			} else {
//				return -1.0;
//			}
//		// you win
//		} else {
//			if (move == Move.DOUBLE) {
//				return 2.0;
//			} else {
//				return 1.0;
//			}
//		}
//	}

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
//		if(handTotal > 21) { // we busted
//			output = -1;
//		} else if(dealerTotal > handTotal && dealerTotal <= 21) { // dealer won
//			output = -1;
//		} else if (dealerTotal == handTotal) { // tie
//			output = 0;
//		} else if(hand.getSoftTotal() == 21 && hand.getHand().size() == 2) { // we got a blackjack
//			output = 1.5;
//		} else if(dealer.getHardTotal() > 21) { // dealer busted
//			output = 1;
//		} else if(dealerTotal < handTotal){ // we won
//			output = 1;
//		} else { // an outcome that should not be possible
//			System.out.println("Something went wrong when evaluating outcomes:");
//			System.out.println("\tHand: " + hand.toString());
//			System.out.println("\tDealer: " + dealer.toString());
//		}
		
		if (handTotal == dealerTotal && handTotal == 21 && dealer.getHand().size() == 2 && hand.getHand().size() == 2) {
			output = 0.0;
		} else if (handTotal == 21 && hand.getHand().size() == 2 && (dealer.getHand().size() != 2 || dealerTotal != 21)) {
			output = (move == Move.DOUBLE ? 3 : 1.5);
		} else if (dealerTotal == 21 && dealer.getHand().size() == 2 && (hand.getHand().size() != 2 || handTotal != 21)) {
			output = -1.0;
//			output = (move == Move.DOUBLE ? -2.0 : -1.0);
		} else if (handTotal > 21) {
			output = (move == Move.DOUBLE ? -2.0 : -1.0);
		} else if (move == Move.SURRENDER) {
			output = -0.5;
		} else if (dealerTotal == handTotal) {
			output = 0.0;
		} else if (dealerTotal > handTotal && dealerTotal <= 21) {
			output = (move == Move.DOUBLE ? -2.0 : -1.0);
		} else if (dealerTotal < handTotal) {
			output = (move == Move.DOUBLE ? 2.0 : 1.0);
		} else if(dealerTotal > 21 && handTotal <= 21) {
			output = (move == Move.DOUBLE ? 2.0 : 1.0);
		} else { // an outcome that should not be possible
			System.out.println("Something went wrong when evaluating outcomes:");
			System.out.println("\tHand: " + hand.toString());
			System.out.println("\tDealer: " + dealer.toString());
		}
		return output;
		
//		return (move == Move.DOUBLE ? output * 2 : output);
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
		ExecutorService tasks = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
		System.out.println("Making tasks (this will take awhile)...");
////		Random why = new Random();
////		Long sigh = why.nextLong();
//		Long sigh = -3869327141277476901L;
//		Random rndSeed = new Random(sigh);
//		System.out.println("seed: " + sigh);
		for (int i = 0; i < 100000000; i++) {
//			System.out.println("Running iteration: " + i);
			tasks.execute(() -> compareStrategies());
		}
		tasks.shutdown();
		try {
			System.out.println("Waiting on tasks to complete...");
			if(!tasks.awaitTermination(10, TimeUnit.MINUTES)) {
				System.out.println("ERROR: took too long");
			};
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		String output = ",Strategy 1,Strategy2\r\nSingle Round Outcome,"+((double)total1.get() / 10)/numTrials.get()+","+((double)total2.get() / 10)/numTrials.get()+"\r\nMax Gain,"+(double)maxGain1.get()/10+","+(double)maxGain2.get()/10+"\r\nMax Loss,"+(double)maxLoss1.get()/10+","+(double)maxLoss2.get()/10+"\r\n";
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
				writer = new FileWriter(new File(System.getProperty("user.dir") + "/src/testFiles/output2.csv"));
			}
			System.out.println("Writing to output...");
			writer.write(output);
			System.out.println("Done writing.");
			writer.close();
			System.out.println("Done closing.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
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
			pairs[i-1] = Arrays.copyOfRange(pairsStr.get(i).split(","), 1, 11);
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
			soft[i-1] = Arrays.copyOfRange(softStr.get(i).split(","), 1, 11);
		}
		// hard csv
		BufferedReader hardReader;
		try {
			hardReader = new BufferedReader(new FileReader(new File(System.getProperty("user.dir") + "/src/blackjack/tables/hard.csv")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
			return;
		}
		var hardStr = hardReader.lines().toList();
		for (int i = 1; i < hardStr.size(); i++) {
			hard[i-1] = Arrays.copyOfRange(hardStr.get(i).split(","), 1, 11);
		}
	}

}
