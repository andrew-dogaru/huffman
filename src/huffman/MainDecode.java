package huffman;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import util.Console;

public class MainDecode {

    public static void main(String[] args) throws IOException {
        DataInputStream input = null;
        DataOutputStream output = null;

        try {
            // Prompt user for input file
            input = Console.getDataInput();

            // Prompt user for output file
            output = Console.getDataOutput();
        
            // Read the dictionary
            HuffmanDict dictionary = HuffmanDict.read(input);
            
            // Decode the data from the rest of the input file and write
            // it to the output file
        try {
            while(true) {
                HuffmanDict.Decoder decoder = dictionary.startDecoding();
                byte decoded = -1;
                do {
                    byte b = input.readByte();
                    
                    for (int i = 0; i < 8; i++) {
                        byte bit = (((1 << i) & b) != 0) ? (byte)'1' : (byte)'0'; 
                        decoded = decoder.read(bit);
                        
                        if (decoded == -1)
                            continue;

                        // we decoded one byte
                        output.write(decoded);

                        // start with new decoder
                        decoder = dictionary.startDecoding();
                        decoded = -1;
                    }
                } while (decoded == -1);

                // we decoded one byte
                output.write(decoded);
            }
        }
        catch (EOFException e) {} // end of input stream
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
}
