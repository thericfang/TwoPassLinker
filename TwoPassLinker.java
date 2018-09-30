import java.util.Scanner;
import java.io.*;
import java.util.*;

/*
* An implementation of Operating Systems Lab 1. 
* Two Pass Linker which links 
*/
public class TwoPassLinker {
	static ArrayList tokens = new ArrayList<String>();
	static ArrayList<String> totalUses = new ArrayList<String>(); 
	static int modNum;
	static int useCount;
	static LinkedHashMap<String, ArrayList<Integer>> table = new LinkedHashMap<String, ArrayList<Integer>>(); // Hash Map for symbol table
	static ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>(); // Lines of lines
	static ArrayList<Integer> baseAddress = new ArrayList<Integer>(); // Each module's base address 
	static ArrayList<ArrayList<Integer>> modAddress = new ArrayList<ArrayList<Integer>>(); // ArrayList of ArrayList in each module
	static ArrayList<Integer> countNT = new ArrayList<Integer>(); // ArrayList for number of NT uses
	// static HashMap<Integer, String> uses = new HashMap<Integer, String>(); // Hashmap for external symbol uses
	static ArrayList<String[]> modUses = new ArrayList<String[]>(); // list of hashmaps

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
			parseScanner(newScanner(inputName));
			firstPass();
			secondPass();
			kbScanner.close();
		}
		else if (inputType == 2) {
			keyboardScanner();
			firstPass();
			secondPass();
			System.exit(0);
			
		}
		else {
			System.out.println("Error: Not a valid input!");
			System.exit(0);
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

	public static void keyboardScanner() {
		System.out.println("Please type the input. Type exit to finish reading.");
		Scanner inputScanner = new Scanner(System.in);
		String[] readIn;
		String current = "";
		while (inputScanner.hasNext()) { // Read through input and split into token array
			current = inputScanner.nextLine();
			if(current.equals("exit")) {
				inputScanner.close();
			}
			else {
				readIn = current.split("\\s+");
				for(int j = 0; j < readIn.length; j++) {
					if(!(readIn[j]).equals("")) {
						
						tokens.add(readIn[j]);
						
					
					}		
					
				}
			}	
			
		}
	}

	public static void firstPass() { // Pass one determines the base address for each module and the absolute address for each external symbol, storing the latter in the symbol table it produces
		
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
			for (int z = 1; z < numOfSymbols; z+=2) {
				ArrayList currentList = lines.get(k);
				if (table.containsKey((String)currentList.get(z))) {
					table.get((String)currentList.get(z)).add((baseAddress.get(curMod)) + (Integer.parseInt((String)(currentList.get(z+1)))));
				}
				// if ((baseAddress.get(curMod) + Integer.parseInt((String)(currentList.get(z+1)))) >= useCount) {
				// 	ArrayList temp = new ArrayList<Integer>();
				// 	temp.add(useCount-1);
				// 	table.put((String)currentList.get(z), temp);
				// }
				else {
					ArrayList temp = new ArrayList<Integer>();
					temp.add((baseAddress.get(curMod)) + (Integer.parseInt((String)(currentList.get(z+1)))));
					table.put((String)currentList.get(z), temp); //hash map adding
				}
		
			}
			curMod++;
			
			
		}

		
	}
	/* Pass two uses the base addresses and the symbol table computed in pass one to generate the actual output
	by relocating relative addresses and resolving external references.*/

	public static void secondPass () { 
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
					useCount++;
				}
			}
			curMod++;
		}
	
		curMod = 0;
		String s = "";
		boolean[] duplicate = new boolean[useCount];
		for (int i = 1; i < lines.size(); i+=3) { // Add uses of external symbols to a list with indices corresponding to module number
			String[] uses = new String[modAddress.get(i/3).size()]; 
			int index = 0;
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
					index = Integer.parseInt(lines.get(i).get(j));
					if (uses[index] != null) {
						duplicate[index] = true;
					}
					else {
						duplicate[index] = false;
					}
					uses[index] = s;
				}

			}
			modUses.add(uses);
			curMod++;
		}
		for (int i = 0; i < modUses.size(); i++) {
			for (int j = 0; j < modUses.get(i).length; j++) {
				totalUses.add(modUses.get(i)[j]);
			}
		}
		System.out.println("Symbol Table");
		ArrayList<String> notUsed = new ArrayList<String>();
		if (!table.isEmpty()) {
			for (Map.Entry<String, ArrayList<Integer>> entry : table.entrySet()) {
				if (entry.getValue().get(0) >= useCount) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.add(useCount-1);
					table.put(entry.getKey(), temp);
					System.out.println(entry.getKey()+"="+(useCount-1)+ " Error: Definition exceeds module size; last word in module used.");
				}
				else if (entry.getValue().size()>1) {
					System.out.println(entry.getKey()+"="+entry.getValue().get(entry.getValue().size()-1) + " Error: This variable is multiply defined; last value used."); 
				}
				else System.out.println(entry.getKey()+"="+entry.getValue().get(0));
				if (!totalUses.contains(entry.getKey())) {
					notUsed.add(entry.getKey());
					
				}
				
				
				
			}
		}	
		
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
				if (table.get(modUses.get(i)[j]) == null) {
					temp = resolveAddress(modAddress.get(i).get(j)/10, 111);
				}
				else {
					temp = resolveAddress(modAddress.get(i).get(j)/10, table.get(modUses.get(i)[j]).get(table.get(modUses.get(i)[j]).size()-1));
				}
				counter++;
				// System.out.println("moduses" + modUses.get(0)[1]);
				// for (int k = 0; i < modUses.size(); i++) {
				// 	for (int l = 0; j < modUses.get(k).length; j++) {
				// 		System.out.println(modUses.get(k)[l]);
				// 	}
				// }
				
				}
				resolvedAddress.add(temp);
				
			}
		}
		System.out.println("\n" + "Memory Map");
		for (int i = 0; i < resolvedAddress.size(); i++) {
			if (!table.containsKey(totalUses.get(i)) && totalUses.get(i)!= null) {
				System.out.println(i+": " + resolvedAddress.get(i) + " Error: "+ totalUses.get(i) + " is not defined; 111 used.");
			}
			else if (duplicate[i]) {
				System.out.println(i+": " + resolvedAddress.get(i) + " Error: Multiple variables used in instruction; all but last ignored.");
			}
			else if (resolvedAddress.get(i)/1000 == 2 && resolvedAddress.get(i)%1000 > 300) {
				System.out.println(i+": " + (resolvedAddress.get(i)/1000*1000+299) + " Error: Absolute address exceeds machine size; largest legal value used.");
			}
			else {
				System.out.println(i+": " + resolvedAddress.get(i));
			}
			
		}

		for (int i = 0; i < notUsed.size(); i++) {
			System.out.println("Warning: " + notUsed.get(i) + " was defined but never used.");
		}
 

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
		target /= 1000;
		target *= 1000;
		target += temp;
		return target;
	}
	
	public static void parseScanner(Scanner inputScanner) {
		String[] readIn;
		String current = "";
		while (inputScanner.hasNext()) { // Read through input and split into token array
			current = inputScanner.nextLine();
			if(current.equals("exit")) {
				inputScanner.close();
				System.exit(0);
			}
			else {
				readIn = current.split("\\s+");
				for(int j = 0; j < readIn.length; j++) {
					if(!(readIn[j]).equals("")) {
						
						tokens.add(readIn[j]);
						
					
					}		
					
				}
			}	
			
		}
	}

	

}