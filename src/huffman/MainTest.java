package huffman;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import util.Console;

/**
 * Test program for Huffman encoding and decoding.
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
                        System.out.print((char)decoded);

                        // start with new decoder
                        decoder = dictionary.startDecoding();
                        decoded = -1;
                    }
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
     * Encode bytes read from the input reader using the given encoder
     * and write them into the specified output stream.  Code bits are 
     * written as '0's or '1's as characters to help with debugging.
     * 
     * @param input input stream contains data to encode
     * @param dict Huffman dictionary
     * @param output output stream, contains encoded data
     * @throws IOException if an error occurs during reading or writing
     */
    static void writeAsString(DataInputStream input, HuffmanDict dict, 
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
        final int BIT_SIZE = 2 * 8;
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
    
//    /**
//     * Appends bits from the given String into the bits BitSet, starting
//     * with position pos.  Return the number of bits appended.
//     * partial appends are not allowed.  If the String cannot be appended
//     * in its entirety then don't append anything and return 0.
//     * 
//     * The String s contains '1' for a one bit and '0' for zero bit.
//     * 
//     * @param bits the bit set where bits are appended
//     * @param pos the position where to append bits at
//     * @param s 
//     * @return the number of bits appended
//     */
//    static int appendBits(BitSet bits, int pos, String s) {
//        if ((bits.size() - pos) < s.length())
//            return 0; // not enough room to store bits
//        
//        for (int i = 0; i < s.length(); i++) {
//            if (s.charAt(i) == '0')
//                bits.clear(pos);
//            else if (s.charAt(i) == '1')
//                bits.set(pos);
//            else
//                // s must contain only 1s and 0s 
//                throw new IllegalArgumentException(s);
//            pos++;
//        }
//        return s.length();
//    }

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
