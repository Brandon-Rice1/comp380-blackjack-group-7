package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

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
	private static final String[][] soft = new String[8][10];

	/**
	 * An array representing the table of options for a hand with two cards that are
	 * the same face (ie 2 Kings)
	 */
	private static final String[][] pairs = new String[10][10];

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
		// current strategy implementation
		// pair of same card = pairs table, which only has single options
		if (hand.getHand().size() == 2 && hand.getHand().get(0).getType() == hand.getHand().get(1).getType()) {
			return pairs[(hand.getSoftTotal() / 2) - 2][dealer.getSoftValue() - 2];
		}
		String lookup;
		if (hand.hasAce()) {
			// contains ace = soft table
			lookup = soft[(hand.getSoftTotal() - 11) - 2][dealer.getSoftValue() - 2];
		} else {
			// no ace, no pair = hard table
			lookup = hard[(hand.getHardTotal()) - 5][dealer.getSoftValue() - 2];
		}
		if (lookup.contains("/")) {
			// if there are multiple options and there are 2 cards, take the first option
			if (hand.getHand().size() == 2) {
				return lookup.split("/")[0];
			} else { // otherwise, take the second
				return lookup.split("/")[1];
			}
		} else { // if there is only one option, take it
			return lookup;
		}
//		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
//			return "STAY" + input;
//		} else {
//			return "HIT" + input;
//		}

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
			inputCSV = new File(System.getProperty("user.dir") + "/src/testFiles/blackjack_table_samples-V3.csv");
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
		String output = readIn.lines().parallel().map(elem -> strategy(elem)).collect(Collectors.joining("\r\n"));
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
				writer = new FileWriter(new File(System.getProperty("user.dir") + "/src/testFiles/hw1Output.csv"));
			}
			System.out.println("Writing to output...");
			writer.write(output);
			System.out.println("Done writing.");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
		var pairsStr = pairsReader.lines().toList();
		for (int i = 1; i < pairsStr.size(); i++) {
			pairs[i] = Arrays.copyOfRange(pairsStr.get(i).split(","), 1, 11);
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
		for (int i = 1; i < pairsStr.size(); i++) {
			soft[i] = Arrays.copyOfRange(softStr.get(i).split(","), 1, 11);
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
		for (int i = 1; i < pairsStr.size(); i++) {
			hard[i] = Arrays.copyOfRange(hardStr.get(i).split(","), 1, 11);
		}
	}

}
