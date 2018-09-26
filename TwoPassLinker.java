import java.util.Scanner;
import java.io.*;
import java.util.*;
public class TwoPassLinker {
	static int modNum;
	static HashMap<String, Integer> table = new HashMap<String, Integer>(); // Hash Map for symbol table
	static ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>(); // Lines of lines
	static ArrayList<Integer> baseAddress = new ArrayList<Integer>(); // Each module's base address 
	static ArrayList<ArrayList<Integer>> modAddress = new ArrayList<ArrayList<Integer>>(); // ArrayList of ArrayList in each module
	static ArrayList<Integer> countNT = new ArrayList<Integer>(); // ArrayList for number of NT uses
	// static HashMap<Integer, String> uses = new HashMap<Integer, String>(); // Hashmap for external symbol uses
	static ArrayList<ArrayList<String>> modUses = new ArrayList<ArrayList<String>>(); // list of hashmaps
	public static void main(String args[]) {
		System.out.println("Please state the type of input:"); // First determine what type of input
		System.out.println("1. File");
		System.out.println("2. Typed");
		Scanner kbScanner = new Scanner(System.in); 
		int inputType = kbScanner.nextInt(); // Get input type
		kbScanner.nextLine();
		if (inputType == 1) { // If file, then ask for filename and proceed to run linker
			System.out.println("What is the file name?");
			String inputName = kbScanner.nextLine();
			firstPass(newScanner(inputName));
			secondPass(newScanner(inputName));
		}


	}
	public static Scanner newScanner(String inputName) {

		try {
			Scanner inputScanner = new Scanner(new FileInputStream((inputName)));
			return inputScanner;
		}

		catch(Exception ex) {
			System.out.println("Error reading " + inputName);
			System.exit(0);
		}
		
		return null;
	}

	public static void firstPass(Scanner inputScanner) { // Pass one determines the base address for each module and the absolute address for each external symbol, storing the latter in the symbol table it produces
		String[] readIn;
		ArrayList tokens = new ArrayList<String>();
		while (inputScanner.hasNext()) { // Read through input and split into token array
			String current = inputScanner.nextLine();	
			readIn = current.split("\\s+");
			for(int j = 0; j < readIn.length; j++) {
				if(!(readIn[j]).equals("")) {
				
				tokens.add(readIn[j]);
				
				}
				
			}
		}
		modNum = Integer.parseInt((String)tokens.get(0)); // Number of modules 

		
		int curMod = 0;
		int lineCount = 0;
		int lineType = 1; // 0, 1, 2, or 3 
		for (int j = 1; j < tokens.size();) { // Start from second token
			String temp = (String)tokens.get(j); 
			int first = Integer.parseInt(temp);
			lines.add(new ArrayList<String>());
			int lineSize = 0;
			if ((lineCount % 3) == 0) { // if definition list
				lineSize = 2 * first + 1;
				if (lineSize == 0) {
					lines.get(lineCount).add("0");
					j++;
				}
				else {
					while (lineSize > 0) { // add tokens to first arraylist in line arraylist
						temp = (String)tokens.get(j);
						lines.get(lineCount).add(temp);
						lineSize--;
						j++;
					}
				}
			}
			else if (((lineCount-1) % 3) == 0) { // if use list
				lineSize = first;
				if (lineSize == 0) {
					lines.get(lineCount).add("0");
					j++;
				}
				else {
					while (lineSize > 0) { // line size is determined by first, and each unit is determined by when -1
						temp = (String)tokens.get(j); // lineSize ≠ # of tokens
						lines.get(lineCount).add(temp);
						if (tokens.get(j).equals("-1")) {
							lineSize--;
						}
						j++;
					}
				}
			}

			else if (((lineCount-2) % 3) == 0) { // if program text
				lineSize = first + 1; // Total # of tokens in line
				if (lineSize == 0) { // if 0, add 0 to the lines stack
					lines.get(lineCount).add("0"); 
					j++;
				}
				else {
					while (lineSize > 0) { //add all tokens to arraylist
						temp = (String)tokens.get(j);
						lines.get(lineCount).add(temp);
						lineSize--;
						j++;
					}
				}
			}
			lineCount++;
		}
		// System.out.println(Arrays.toString(lines.toArray()));
		int increment = 0;
		for (int i = 0; i < lineCount; i++) { // Find base addresses
			if (i == 0) {
				baseAddress.add(increment);
			}
			else if ((i % 3) == 0) { // When reaching new module
				increment += Integer.parseInt(lines.get(i-1).get(0));
				baseAddress.add(increment); 
			}

			
		}
		
		for (int k = 0; k < lines.size(); k+=3) { // Find the absolute address for external symbol
			
			int numOfSymbols = lines.get(k).size(); 
			// System.out.println(Integer.parseInt(lines.get(k).get(0)));
			for (int z = 1; z < numOfSymbols; z+=2) {
				ArrayList currentList = lines.get(k);
				if (table.get((String)currentList.get(z)!= null)) {
					System.out.println("Error: This variable is multiply defined; last value used.")
				}
				table.put((String)currentList.get(z), (baseAddress.get(curMod)) + (Integer.parseInt((String)(currentList.get(z+1))))); //hash map adding
				// System.out.println(table);
				// System.out.println((baseAddress.get(curMod)) + (Integer.parseInt((String)(currentList.get(z+1)))));
			}
			curMod++;
			
			
		}

		System.out.println(table.get("X22"));
		
	}
	/* Pass two uses the base addresses and the symbol table computed in pass one to generate the actual output
	by relocating relative addresses and resolving external references.*/

	public static void secondPass (Scanner inputScanner) { 
		// ArrayList<Integer> tempAddress = new ArrayList<Integer>();
		int curMod = 0;
		
		for (int i = 2; i < lines.size(); i+=3) { // Add instructions to an arraylist with corresponding module numbers as indices
			modAddress.add(new ArrayList<Integer>());
			for (int j = 0; j < lines.get(i).size(); j++) {
				if (j == 0) {
					if (curMod == 0) {
						countNT.add(0);
					}
					else {
						countNT.add(Integer.parseInt(lines.get(i-3).get(j)) + countNT.get(curMod-1));
					}
				}
				else {
					modAddress.get(curMod).add(Integer.parseInt(lines.get(i).get(j)));	
				}
			}
			curMod++;
		}
		// System.out.println(countNT);
		// System.out.println(modAddress);
		curMod = 0;
		String s = "";
		
		for (int i = 1; i < lines.size(); i+=3) { // Add uses of external symbols to a hash map with indices corresponding to module number
			ArrayList<String> uses = new ArrayList<String>(); 
			for (int j = 1; j < lines.get(i).size(); j++) {
				if (j == 1) {
					s = lines.get(i).get(j);
				}
				else if (isInteger((String)(lines.get(i).get(j-1))) && (Integer.parseInt(lines.get(i).get(j-1)) == -1)) {
					s = lines.get(i).get(j);
				}
				else if (isInteger(lines.get(i).get(j)) && Integer.parseInt(lines.get(i).get(j)) == -1) {
					s = "";
				}
				else {
					uses.add(s);
				}
				// System.out.println(uses);

			}
			modUses.add(uses);
			curMod++;
		}
		// System.out.println(modUses);
		ArrayList<Integer> resolvedAddress = new ArrayList<Integer>();
		
		for (int i = 0; i < modAddress.size(); i++) {
			int counter = 0;
			for (int j = 0; j < modAddress.get(i).size(); j++) {
				int code = modAddress.get(i).get(j) % 10;
			
				int temp = 0;
				switch (code) { // Assuming all instructions have either 1, 2, 3, 4 as rightmost digit
				case 1: 
				temp = modAddress.get(i).get(j)/10; //unchanged
				break;
				case 2:
				temp = modAddress.get(i).get(j)/10; //unchanged
				break;
				case 3:
				curMod = modAddress.indexOf(modAddress.get(i));
				int offset = countNT.get(curMod);
				temp = modAddress.get(i).get(j)/10 + offset;
				break;
				default:
				//TODO resolve for 4 temp = modAddress.get(i).get(j)
				
				// int index = modUses.get(i).get(counter);
				temp = resolveAddress(modAddress.get(i).get(j)/10, table.get(modUses.get(i).get(counter)));
				counter++;
				// System.out.println("moduses" + modUses);
				// System.out.println("mod address" + modAddress);
				// System.out.println("table" + table);
				
				
				
				
				
				
				}
				resolvedAddress.add(temp);
			}
		}
		// System.out.println(resolvedAddress);
		


	}

	public static boolean isInteger(String str) { 
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}

	public static Integer resolveAddress(Integer Target, Integer Dummy) { // for resolving
		int dummy = Dummy;
		int temp = Dummy;
		int numOfDigits = 0;
		int target = Target;
		while (dummy > 1) {
			numOfDigits++;
			dummy/=10;
		}
		target /= Math.pow(10.0, numOfDigits);
		target *= Math.pow(10.0, numOfDigits);
		target += temp;
		return target;
	}
	

	

}