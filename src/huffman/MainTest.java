package huffman;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import util.Console;

/**
 * Main program for Huffman encoding and decoding. 
 */
public class MainTest {
    public static void main(String[] args) throws IOException {
        byte[] data = encode();
        
        decode(data);
    }

    static byte[] encode() throws IOException {
        DataInputStream input = null;
        DataOutputStream output = null;
        ByteArrayOutputStream bytes = null;
        try {
            // Test with a character code larger than 0x7F
            String test = "Laoco�n";
            System.out.println(test);
            input = new DataInputStream(new ByteArrayInputStream(test.getBytes("US-ASCII")));

            // output file
            bytes = new ByteArrayOutputStream();
            output = new DataOutputStream(bytes);

            // Place a mark at the beginning of the stream so we can read it twice
            // This limits the size of the file we can encode.
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
            System.out.println(bytes.toString());
        }
        return bytes.toByteArray();
    }

    /**
     * Reads bytes from the input stream.  First build a Huffman dictionary. 
     * then decodes the rest of the data using that dictionary.
     * 
     * @param data
     * @throws IOException
     */
    static void decode(byte[] data) throws IOException {
        DataInputStream input = new DataInputStream(new ByteArrayInputStream(data));
        
        HuffmanDict dictionary = HuffmanDict.read(input);
        System.out.println(dictionary.toString());
        
        try {
            while(true) {
                HuffmanDict.Decoder dec = dictionary.startDecoding();
                byte decoded = -1;
                do {
                    byte b = input.readByte();
                    decoded = dec.read(b);
                } while (decoded == -1);
                
                // we decoded one byte
                System.out.print((char)decoded);
            }
        }
        catch (EOFException e) {} // end of input stream
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
