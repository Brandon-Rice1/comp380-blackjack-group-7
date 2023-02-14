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
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicInteger;

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
	 * The naive strategy for determining if we should hit or stay
	 * 
	 * @param input the raw csv input line
	 * @return a properly formatted for csv line that includes our decision
	 */
	private static String strategy(String input) {
		// ignore the 'comment' lines in the input
		if (input.contains("==>")) {
			return input;
		}
		// initializes the card objects
		String[] hexCards = input.split(",");
		Card dealer;
		if (hexCards[1].length() > 0) {
			dealer = new Card(hexCards[1]);
		} else {
			dealer = null;
		}
		ArrayList<Card> cards = new ArrayList<>();
		for (int i = 8; i < hexCards.length; i++) {
			cards.add(new Card(hexCards[i]));
		}
		Hand hand = new Hand(hexCards);
		// homework 2 strategy
//		if (hand.getHardTotal() > 21) {
//			System.out.println("ERROR: hand's hard total was greater than 21, which is not covered under the tables");
//			System.exit(-2);
//		}
		// pair of same card = pairs table, which only has single options
		// alternate idea: instead of arrays, use nested dictionaries (hand, dealer,
		// hasAce)
		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
			return pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftValue() - 2] + input;
		}
		String lookup = "";
		if (hand.hasAce()) {
			// contains ace = soft table
			if (hand.getSoftTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getSoftTotal() < 21) {
				lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftValue() - 2];
			}
		}
		// if no ace OR ace as 11 was too big, use the hard table (ace is 1)
		if (lookup.equals("")) {
			// no ace, no pair = hard table
			if (hand.getHardTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getHardTotal() > 21) {
				throw new IllegalArgumentException();
			}
			else {
				lookup = hard[(hand.getHardTotal()) - 5][dealer.getSoftValue() - 2];
			}
		}
		// alternate idea: reverse lookup, split on '/', reverse each half of the
		// string; only 1 condition (if 2 cards and 2nd is non-empty)
		if (lookup.contains("/")) {
			// if there are multiple options and there are 2 cards, take the first option
			if (hand.getHand().size() == 2) {
				lookup =  lookup.split("/")[0];
			} else { // otherwise, take the second
				lookup = lookup.split("/")[1];
			}
		}
		return lookup + input;
//		// homework 1 strategy below
//		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
//			return "STAY" + input;
//		} else {
//			return "HIT" + input;
//		}

	}
	
	private static int compareStrategies(String input) {
		// ignore the 'comment' lines in the input
		if (input.contains("==>")) {
			return 0;
		}
		numTrials.addAndGet(1);
		String[] hexCards = input.split(",");
		// make the dealer's hand
		Card dealerCard = new Card(hexCards[1]);
		Hand dealer = new Hand(List.of(dealerCard));
		// make your hand
		Hand hand = new Hand(hexCards);
		// get all of the cards to be removed from the deck
		ArrayList<Card> cards = new ArrayList<>();
		for (String card : hexCards) {
			if (card != "") {
				cards.add(new Card(card));
			}
		}
		
		// make two shuffled decks that are identical
		Deck deck1 = new Deck(cards);
		Deck deck2 = new Deck(cards);
		long seed = new Random().nextLong();
		Random rnd1 = new Random(seed);
		Random rnd2 = new Random(seed);
		deck1.shuffle(rnd1);
		deck2.shuffle(rnd2);
		// test the strategies
		total1.addAndGet(strategy1(dealer, hand, deck1, Move.HIT));
		total2.addAndGet(strategy2(dealer, hand, deck2, Move.HIT));
		return 0;
	}
	
	private static int strategy1(Hand dealer, Hand hand, Deck deck, Move move) {
		// condition to return if hand is final
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcome(dealer, hand, deck, move) * 10;
			int temp = outcome.intValue();
			if (temp > maxGain1.get()) {
				maxGain1.addAndGet(temp);
			}
			if (temp < maxLoss1.get()) {
				maxLoss1.addAndGet(temp);
			}
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
	
	private static int strategy2(Hand dealer, Hand hand, Deck deck, Move move) {
		if (move == Move.DOUBLE || move == Move.STAY || move == Move.SURRENDER || hand.getHardTotal() >= 21) {
			// logic to determine value of hand to return
			Double outcome = evaluateOutcome(dealer, hand, deck, move) * 10;
			int temp = outcome.intValue();
			if (temp > maxGain1.get()) {
				maxGain1.addAndGet(temp);
			}
			if (temp < maxLoss1.get()) {
				maxLoss1.addAndGet(temp);
			}
			return temp;
		}
		// logic to decide next move and recursion
		String lookup = "";
		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
			lookup = pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftTotal() - 2];
		} else if (hand.hasAce()) {
			// contains ace = soft table
			if (hand.getSoftTotal() == 21) {
				lookup = "STAY";
			} else if (hand.getSoftTotal() < 21) {
				lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftTotal() - 2];
			}
		} else {
			// no ace, no pair = hard table
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
				lookup =  lookup.split("/")[0];
			} else { // otherwise, take the second
				lookup = lookup.split("/")[1];
			}
		}
		Move nextMove = Move.valueOf(lookup);
		if (nextMove == Move.DOUBLE || nextMove == Move.HIT) {
			hand.addCard(deck.draw());
		}
		return strategy2(dealer, hand, deck, nextMove);
	}
	
	private static Double evaluateOutcome(Hand dealer, Hand hand, Deck deck, Move move) {
		// dealer makes the move(s)
		while((dealer.hasAce() && dealer.getSoftTotal() <= 17) || dealer.getHardTotal() < 17) {
			dealer.addCard(deck.draw());
		}
		// you have blackjack
		if (hand.getSoftTotal() == 21 && hand.getHand().size() == 2) {
			// dealer has blackjack
			if (dealer.getSoftTotal() == 21 && dealer.getHand().size() == 2) {
				return 0.0;
			} else { // dealer doesn't have blackjack
				// you doubled or didn't
				if (move == Move.DOUBLE) {
					return 3.0;
				} else {
					return 1.5;
				}
			}
		// if the dealer has blackjack and you don't
		} else if(dealer.getSoftTotal() == 21 && dealer.getHand().size() == 2) {
			return -1.0;
		// if you busted
		} else if(hand.getHardTotal() > 21) {
			if (move == Move.DOUBLE) {
				return -2.0;
			} else {
				return -1.0;
			}
		// if you surrendered
		} else if (move == Move.SURRENDER) {
			return -0.5;
		// Comparing non-blackjack hands
		// hands are the same
		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal()) == 
				(hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
			return 0.0;
		// dealer wins
		} else if ((dealer.getSoftTotal() > 21 ? dealer.getHardTotal() : dealer.getSoftTotal()) > 
				(hand.getSoftTotal() > 21 ? hand.getHardTotal() : hand.getSoftTotal())) {
			if (move == Move.DOUBLE) {
				return -2.0;
			} else {
				return -1.0;
			}
		// you win
		} else {
			if (move == Move.DOUBLE) {
				return 2.0;
			} else {
				return 1.0;
			}
		}
	}

	
	/**
	 * Reads from a csv file, uses the strategy on each game, and writes a new
	 * output file containing the results.
	 * 
	 * @param args
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
		readIn.lines().parallel().map(elem -> compareStrategies(elem));
		String output = ",Strategy 1,Strategy2\r\nSingle Round Outcome,"+(total1.get() / 10)/numTrials.get()+","+(total2.get() / 10)/numTrials.get()+"\r\nMax Gain,"+maxGain1.get()/10+","+maxGain2.get()/10+"\r\nMax Loss,"+maxLoss1.get()/10+","+maxLoss2.get()/10+"\r\n";
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
			hardReader = new BufferedReader(
					new FileReader(new File(System.getProperty("user.dir") + "/src/blackjack/tables/hard.csv")));
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
