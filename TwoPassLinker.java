import java.util.Scanner;
import java.io.*;
public class TwoPassLinker {
	public static void main(String args[]) {
		

	}
	public static Scanner newScanner(String inputName) {

		try {
			Scanner inputScanner = new Scanner(new BufferedReader(new FileReader(inputName)));
			return inputScanner;
		}

		catch(Exception ex) {
			System.out.println("Error reading " + inputName);
			System.exit(0);
		}
		
		return null;
	}
}