import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class Motor_Analysis {
	static String[] toTextArray = new String[40]; // holds the array that is going to be printed. Written of size 40 because 
												 // that's roughly the size of the array to be exported to the text file
	static String[] toTextArrayholder = new String[40]; // holds a copy of the array to be printed that can be manipulated
	static int arrayTextCount;

	final static int NUMNUMS = 1000;
	
	private static String[] readText() {
		String[] array = new String[NUMNUMS];
		Path file = Paths.get("Logger.csv");
		String line;
		try (BufferedReader reader = Files.newBufferedReader(file)) {
			for (int i = 0; i < NUMNUMS; i++) {
				line = reader.readLine();
				array[i] = line;
			}
		} catch (IOException err) {
			System.err.println(err.getMessage());
		} catch (NumberFormatException err) {
			System.err.println(err.getMessage());			
		}
		return array;
	} // end readText
	
    
	private static double[] splitArray(String inputArray[], int servosMotor){
		String[] clonedInputArray = inputArray.clone(); //clones the input array so they are not aliased
		double[] dataArray = new double[NUMNUMS];
		
			for (int i = 0; i < NUMNUMS; i++) {
				String[] parts = clonedInputArray[i].split(","); //splits the current line of the array into seconds then all the servos motor data
				dataArray[i] = Double.parseDouble(parts[servosMotor].trim()); //set the seconds to dataArray 
				}
			return dataArray;
			}
		
	private static void currentAnalysis(double[] inputArray, int servosMotor) throws FileNotFoundException{
		String st = new String();
		int startTime = 0; // initializes the end time of the burst
		int endTime = 0; //initializes the end time of the burst
		int count = 0; // this counts how many times the motors ran during the 1000 seconds
		double sum = 0; 
		double avgCur = 0;
		String avgCurrent;
		st = "";
		toTextArrayholder = createArray(st, toTextArray, arrayTextCount++); 
		toTextArray = toTextArrayholder.clone(); //takes the clone to avoid aliasing
		st = "Motor " + servosMotor + ":";
		System.out.println(st);
		toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
		toTextArray = toTextArrayholder.clone();
		
		for (int i = 0; i < NUMNUMS; i++){
			if (inputArray[i] > 1 && inputArray[i] < 8 && inputArray[i-1] <= 1){ //this is the first second of current usage
				startTime = i-1; //the -1 is because the seconds is the same as the array index - 1.
				count++;
				sum = inputArray[i];
			}
			else if (inputArray[i] > 1 && inputArray[i] < 8 && inputArray[i-1] > 1 && inputArray[i+1] > 1){ //these are the middle sections of current usage
				sum += inputArray[i];
			}
			else if (inputArray[i] > 1 && inputArray[i] < 8 && inputArray[i+1] <= 1){ // this is the last second of current usage
				endTime = i-1;
				sum += inputArray[i];
				avgCur = sum/(endTime - startTime);
				DecimalFormat df = new DecimalFormat("#.###");
				avgCurrent = df.format(avgCur); 
				st = "Burst " + count + ": " + avgCurrent + " amps, starting at " + startTime + ", ending at " + endTime;
				System.out.println(st);
				toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
				toTextArray = toTextArrayholder.clone();
			}
			if (inputArray[i] >= 8 && inputArray[i-1] < 8) //this is the first second of current exceeding
				startTime = i-1;
			else if (inputArray[i] > 8 && inputArray[i+1] < 8){ // this is the last second of current exceeding
				endTime = i-1;
				st = "***CURRENT EXCEEDED!: started at " + startTime + ", and ended at " + endTime;
				System.out.println(st);
				toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
				toTextArray = toTextArrayholder.clone();
			}
		}
		if (count == 0){
			st = "Motor did not run at all!";
			System.out.println(st);
			toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
			toTextArray = toTextArrayholder.clone();
		}
	}
		
	
	private static void readStringArray(String[] inputArray){ // test prints the first 5 lines of a string array to make sure the array has content
															// used to make sure that the file import worked correctly
		for (int i = 0; i < 5; i++)
			System.out.println(inputArray[i]);
	}
	private static void readDoubleArray(double[] inputArray){ // test prints the first 5 lines of a double array to make sure the array has content
															// used to make sure the contents of each servos motor were parsed correctly
		for (int i = 0; i < 5; i++)							// isn't used in the main code, but was used for original testing
			System.out.println(inputArray[i]);
	}

	private static String[] createArray(String inputString, String[] finalArray, int arrCount){ //this method takes a string and prints it into
		String[] clonedfinalArray = finalArray.clone(); // the next line of the array
		clonedfinalArray[arrCount] = inputString; 
		return clonedfinalArray;
	}

	private static void writeText(String[] inputString) throws FileNotFoundException{//this method couldn't take one string at a time and print them
		PrintWriter pw = new PrintWriter(new File("Motor_Analysis_Output.txt")); // individually into a text file, as each iteration, a new text file would
		int i = 0;  // be made that would overwrite the old one.  So i modified it to print an array into 1 text file and i put all the strings to be printed 
		while (inputString[i] != null){ // into 1 array
			  pw.println(inputString[i]);
			  i++;}
			pw.close();
	}

	public static void main(String[] args) throws IOException {
		String st = new String();
		st = "Motor Use Summary: ";
		toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
		toTextArray = toTextArrayholder.clone();
		st = ""; // prints a gap to make it look nicer
		toTextArrayholder = createArray(st, toTextArray, arrayTextCount++);
		toTextArray = toTextArrayholder.clone();
		
		String[] testArray = new String[NUMNUMS];
		
		testArray = readText();
		readStringArray(testArray); //tests to make sure that the file was imported correctly
		for (int i = 1; i <= 7; i++){
		double[] servosMotori = new double[NUMNUMS];
		servosMotori = splitArray(testArray, i);
		currentAnalysis(servosMotori, i);
		}
		writeText(toTextArray);
	}
}
// I never have to use a 2D array in my methods because I import the data and put it in the array as a string
// then I parse the array into a double when I split the strings by servos motor.  I don't need a 2D array for 
// either of these as the string array imported comes in as a 1D string, then when I split the data up by servos 
// motor, each motor only needs 1 column of data, as the "seconds" column of data is the same as the array index - 1.
