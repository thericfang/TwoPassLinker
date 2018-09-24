import java.util.Scanner;
import java.io.*;
import java.util.*;
public class TwoPassLinker {
	static int modNum;
	static HashMap<String, Integer> table = new HashMap<String, Integer>(); // Hash Map for symbol table
	static ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>(); // Lines of lines
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
				// if(!"".equals(readIn[uu])) {
				
				tokens.add(readIn[j]);
				
				// }
				
			}
		}
		modNum = Integer.parseInt((String)tokens.get(0)); // Number of modules 

		int[] baseAddress = new int[modNum]; // Each module's base address array
		baseAddress[0] = 0;
		int count = 0;
		int curMod = 1;
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

			else if (((lineCount-2)% 3) == 0) { // if program text
				lineSize = first + 1; // 6 tokens in line
				if (lineSize == 0) {
					lines.get(lineCount).add("0");
					j++;
				}
				else {
					while (lineSize > 0) {
						temp = (String)tokens.get(j);
						lines.get(lineCount).add(temp);
						lineSize--;
						j++;
					}
				}
			}
			lineCount++;
		}
		System.out.println(Arrays.toString(lines.toArray()));

		
	}
	public static boolean isNumeric(String str) { // Method to check if number 
  		try {  
   			double d = Double.parseDouble(str);  
  		}  
  		catch(NumberFormatException nfe) {  
   			return false;  
 		}  
  		return true;  
	}

}