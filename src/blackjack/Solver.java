/**
 * 
 */
package blackjack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * The main class. Contains the current strategy as well as the file i/o.
 * 
 * @author Brandon
 *
 */
public class Solver {
	
	/**
	 * The current strategy for determining if we should hit or stay
	 * @param input the raw csv input line
	 * @return a properly formatted for csv line that includes our decision
	 */
	private static String strategy(String input) {
		// initializes the card objects
		String[] hexCards = input.split(",");
//		Card dealer;
//		if (temp[1].length() > 0) {
//			dealer = new Card(temp[1]);
//		} else {
//			dealer = null;
//		}
		ArrayList<Card> cards = new ArrayList<>();
		for(int i = 8; i < hexCards.length; i++) {
			cards.add(new Card(hexCards[i]));
		}
		Hand hand = new Hand(cards);
		// cuurent strategy implementation
		if (hand.getHardTotal() > 11 || hand.getSoftTotal() > 17) {
			return "STAY" + input;
		} else {
			return "HIT" + input;
		}
	}

	/**
	 * Reads from a csv file, uses the strategy on each game, and writes a new
	 * output file containing the results.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir") + "/src/testFiles/input.csv");
		File inputCSV = new File(System.getProperty("user.dir") + "/src/testFiles/input.csv");
		if (!inputCSV.isFile()) {
			System.out.println("File passed is not a file");
			return;
		}
		BufferedReader readIn;
		try {
			 readIn = new BufferedReader(new FileReader(inputCSV));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		String output = readIn.lines().parallel().map(elem -> strategy(elem)).collect(Collectors.joining("\r\n"));
		try {
			readIn.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		try {
			FileWriter writer = new FileWriter(new File(System.getProperty("user.dir") + "/src/testFiles/output.csv"));
			writer.write(output);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
