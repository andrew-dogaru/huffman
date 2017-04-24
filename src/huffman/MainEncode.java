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
     * Encode bytes read from the input reader using the given encoder
     * and write them into the specified output stream.  Code bits are 
     * written as bits, not characters.
     * 
     * @param input input stream contains data to encode
     * @param dict Huffman dictionary
     * @param output output stream, contains encoded data
     * @throws IOException if an error occurs during reading or writing
     */
    static void writeData(DataInputStream input, HuffmanDict dict, 
            DataOutputStream output) throws IOException {
        // size of the bit array used to store encoded data, must be large
        // enough to store at least two of the longest codes
        final int BIT_SIZE = 1024 * 8;
        byte[] encodedBits = new byte[BIT_SIZE/8];
        int bitCount = 0; // number of bits stored in encodedBits

        for (int c = input.read(); c != -1; c = input.read()) {
            String code = dict.encode((byte)c);
            
            int count = appendBits(encodedBits, bitCount, code);
            bitCount += count;
            if (count == 0) {
                // must write the encodedBits to output to make room for more bits
                byte[] bytes = encodedBits;
                int byteCount = bitCount / 8;
                
                // write only the full bytes
                output.write(bytes, 0, byteCount);
                
                // write the last incomplete byte into encodedBits, starting
                // from position 0
                int restBits = bitCount % 8;
                byte lastByte = (restBits != 0) ? bytes[byteCount] : 0;
                for (int i = 0; i < restBits; i++) {
                    if ((lastByte & (1 << i)) != 0)
                        bitSet(encodedBits, i);
                    else
                        bitClear(encodedBits, i);
                }
                bitCount = restBits;
                
                // we made room, try again to append the bits
                count = appendBits(encodedBits, bitCount, code);
                bitCount += count;
                if (count == 0)
                    throw new IllegalStateException("BitSet must be large enough to read one code");
            }            
        }
        
        // we reached the end of input, write the rest of the encoded bits
        // (the last byte may contain extra bits)
        byte[] bytes = encodedBits;
        int byteCount = (bitCount + 7) / 8;
        output.write(bytes, 0, byteCount);
    }
    
    /**
     * Appends bits from the given String into the bits BitSet, starting
     * with position pos.  Return the number of bits appended.
     * partial appends are not allowed.  If the String cannot be appended
     * in its entirety then don't append anything and return 0.
     * 
     * The String s contains '1' for a one bit and '0' for zero bit.
     * 
     * @param bits the bit set where bits are appended
     * @param pos the position where to append bits at
     * @param s 
     * @return the number of bits appended
     */
    static int appendBits(byte[] bits, int pos, String s) {
        if ((bits.length*8 - pos) < s.length())
            return 0; // not enough room to store bits
        
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '0')
                bitClear(bits, pos);
            else if (s.charAt(i) == '1')
                bitSet(bits, pos);
            else
                // s must contain only 1s and 0s 
                throw new IllegalArgumentException(s);
            pos++;
        }
        return s.length();
    }
    
    private static void bitClear(byte[] bits, int pos) {
        int bytePos = pos / 8;
        bits[bytePos] &= ~(1 << (pos % 8));
    }

    private static void bitSet(byte[] bits, int pos) {
        int bytePos = pos / 8;
        bits[bytePos] |= (1 << (pos % 8));
    }
}
