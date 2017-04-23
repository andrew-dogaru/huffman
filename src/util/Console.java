package util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper methods to get input and write output.
 */
public class Console {
    /**
     * Prompts the user then returns his input. If the user types "cancel"
     * in any combination of lower case and upper case letters then it 
     * throws a Console.CancelException.
     * 
     * @param prompt string to prompt the user on System out 
     * @return the line that the user types
     * @throws IOException, CancelException
     */
    public static String getLineFromUser(String prompt) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(prompt);
        String line = br.readLine();
        line = line.trim();
        if (line.equalsIgnoreCase("cancel")) {
            throw new CancelException(); // user canceled program
        }
        return line;
    }
    
    /**
     * Prompts user for a file name and returns a reader for the file.
     * Repeats the prompts if the file does not exist or it cannot open 
     * the file.
     * 
     * @return BufferedReader used for reading the file
     * @throws IOException
     */
    public static BufferedReader getReader() throws IOException {
        while (true) {
            // Prompt user for input file name
            String fileName = getLineFromUser("Enter the name of the input file:");
            try {
                return new BufferedReader(new FileReader(fileName));
            } catch (IOException e) {
                System.err.println("File " + fileName + " does not exist or cannot be opened.");
            }
        }
    }

    /**
     * Prompts user for a file name and returns a DataInputStream for the file.
     * Repeats the prompts if the file does not exist or it cannot open 
     * the file.
     * 
     * @return DataInputStream used for reading the file
     * @throws IOException
     */
    public static DataInputStream getDataInput() throws IOException {
        while (true) {
            // Prompt user for input file name
            String fileName = getLineFromUser("Enter the name of the input file:");
            try {
                return new DataInputStream(new BufferedInputStream(
                        new FileInputStream(fileName)));
            } catch (IOException e) {
                System.err.println("File " + fileName + " does not exist or cannot be opened.");
            }
        }
    }

    /**
     * Prompts user for a file name and returns a writer to the file.
     * 
     * @return Buffered writer used for writing into the output file
     * @throws IOException
     */
    public static BufferedWriter getWriter() throws IOException {
        while (true) {
            String fileName = getLineFromUser("Enter the name of the output file:");
            try {
                return new BufferedWriter(new FileWriter(fileName));
            } catch (IOException e) {
                System.err.println("File " + fileName + " cannot be opened to write in it.");
            }
        }
    }
    
    /**
     * Prompts user for a file name and returns a DataOutputStream to the file.
     * 
     * @return DataOutputStream for writing into the output file
     * @throws IOException
     */
    public static DataOutputStream getDataOutput() throws IOException {
        while (true) {
            String fileName = getLineFromUser("Enter the name of the output file:");
            try {
                return new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(fileName)));
            } catch (IOException e) {
                System.err.println("File " + fileName + " cannot be opened to write in it.");
            }
        }
    }

    public static class CancelException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
