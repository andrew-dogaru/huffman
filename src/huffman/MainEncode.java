package huffman;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import util.Console;

public class MainEncode {

    public static void main(String[] args) throws IOException {
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // Prompt user for input file
            input = Console.getDataInput();

            // Prompt user for output file
            output = Console.getDataOutput();

            // Place a mark at the beginning of the stream so we can read it twice
            input.mark(10000000);
            
            // Build the dictionary and store it into the output file
            HuffmanDict dictionary = buildDictionary(input);
            output.write(dictionary.toBytes());

            // Go back to the marked position
            input.reset();

            // Compress data and store it into the output file
            writeData(input, dictionary, output);
        }
        catch (Console.CancelException e) {
            System.out.println("Program terminated");
        }
        finally {
            // The finally block gets executed even if the user cancels
            if (input != null)
                input.close();
            if (output != null)
                output.close();
        }
    }
    
    /**
     * Reads bytes from the input stream until the end of stream is 
     * reached and builds a HuffmanDict which contains the occurrence count 
     * of each character.
     * 
     * @param input the input character stream
     * @return a HuffmanDict instance
     * @throws IOException if a read error occurs
     */
    static HuffmanDict buildDictionary(DataInputStream input) throws IOException {
        HuffmanDict dict = new HuffmanDict();

        try {
            while(true) {
                byte c = input.readByte();
                dict.add(c);
            }
        }
        catch (EOFException e) {} // end of stream
        dict.makeTree();
        return dict;
    }

    /**
     * Encode characters read from the input reader using the given encoder
     * and write them into the specified writer.
     * 
     * @param input
     * @param dict
     * @param output
     * @throws IOException if an error occurs during reading or writing
     */
    static void writeData(DataInputStream input, HuffmanDict dict, 
            DataOutputStream output) throws IOException {

        StringBuilder encodedBits = new StringBuilder();

        for (int c = input.read(); c != -1; c = input.read()) {
            encodedBits.append(dict.encode((byte)c));
            if (encodedBits.length() > 1024) {
                output.write(encodedBits.toString().getBytes());
                encodedBits = new StringBuilder(); 
            }
        }
        
        // write the rest of the encoded bits
        output.write(encodedBits.toString().getBytes());
    }
}
