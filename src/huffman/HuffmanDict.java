package huffman;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Huffman dictionary.
 * 
 * Call add() repeatedly to build the character histogram, then
 * call makeTree() to build the Huffman tree.
 * Use encode() to encode each byte and use HuffmanDict.Decoder to decode
 * an input stream into bytes.
 */
public class HuffmanDict {
    /**
     * Huffman tree node. 
     */
    static class Node implements Comparable<Node> {
        public byte c;      // character (ASCII)
        public int f;       // frequency
        public Node left;   // reference to left child
        public Node right;  // reference to right child
        
        public Node(byte c, int f) {
            this.c = c;
            this.f = f;
        }

        public Node(int f) {
            this.c = 0;
            this.f = f;
        }

        @Override
        public int compareTo(Node o) {
            // The "maximum" node is the one with the lowest f
            if (this.f < o.f) return +1;
            if (this.f > o.f) return -1;
            else return 0;
        }
        
        /**
         * Join this node with the other node and return a
         * root node which has the two arguments as children.
         * 
         * @param other the other node
         * @return new parent node
         */
        public Node join(Node other) {
            Node root = new Node(this.f + other.f);
            root.left = this;
            root.right = other;
            return root;
        }
        
        /**
         * Returns true if the node is leaf (contains a character) otherwise false.
         */
        public boolean isLeaf() {
            return c != 0;
        }
    }

    /**
     * Class used for decoding from a bit set to a character.
     */
    public class Decoder {
        private Node curNode;
    
        /**
         * Construct a decoder with the root of a Huffman tree.
         * @param n the Huffman tree root
         */
        private Decoder() {
            curNode = tree;
        }

        /**
         * Given a stream of '0's and '1's, return the character 
         * associated with the code entered, or -1 if the code is incomplete. 
         * @param code
         * @return decoded character, or -1 if no character decoded yet
         */
        public byte read(byte bit) {
            if (bit == '0') {
                if (curNode.left == null) {
                    // Can't invoke this method
                    throw new IllegalStateException();
                }
                curNode = curNode.left;
                return (curNode.left == null) ? curNode.c : (byte) -1;
            }
            if (bit == '1') {
                if (curNode.right == null) {
                    // Can't invoke this method
                    throw new IllegalStateException();
                }
                curNode = curNode.right;
                return (curNode.right == null) ? curNode.c : (byte) -1;
            }
            // Can't invoke with this argument
            throw new IllegalArgumentException(String.valueOf(bit));
        }
    }

    /**
     * Create a new decoder which we can use to read bits until a
     * character is decoded.
     * @return a new decoder
     */
    public Decoder startDecoding() {
        return new Decoder();
    }

    private int freqs[];
    private Node tree;
    private String codes[];
    
    public HuffmanDict() {
        this.freqs = new int[256];
        this.codes = new String[256];
    }

    // Used when reading the huffman tree from a file
    private HuffmanDict(Node node) {
        this.tree = node;
    }

    /**
     * Increments the count of the given byte c.
     * 
     * @param c the byte
     */
    public void add(byte c) {
        freqs[c + 128]++;
    }

    /**
     * Builds the Huffman tree.
     */
    public void makeTree() {
        PriorityQueue<Node> pqueue = new PriorityQueue<>();
        
        // Insert all non-zero frequency nodes into the priority queue 
        for (int c = 0; c < 256; c++) {
            int f = freqs[c];
            if (f != 0) {
                pqueue.insert(new Node((byte)(c - 128), f));
            }
        }

        // Build the Huffman tree
        while (pqueue.size() > 1) {
            Node n1 = pqueue.remove();
            Node n2 = pqueue.remove();
            pqueue.insert(n1.join(n2));
        }
        tree = pqueue.remove();

        // Store the codes for each byte in an array indexed by the byte value
        // so to find the code associated with each byte from the file we just
        // access the array.
        buildCodes(tree, "");
    }

    private void buildCodes(Node n, String code) {
        if (n.left == null) {
            // terminal node, assign code to its character
            codes[n.c + 128] = code;
            return;
        }
        else {
            buildCodes(n.left, code + "0");
            buildCodes(n.right, code + "1");
        }
    }
    
    /**
     * Encode the specified byte b.
     * 
     * @param b a data byte
     * @return a string of '0's and '1's corresponding to the code
     * associated with the specified character
     */
    public String encode(byte b) {
        if (codes[b+128] == null)
            // we don't expect to have characters for which we build no code
            throw new IllegalStateException("No code found for byte " + String.valueOf(b));
        
        return codes[b+128];
    }

    /**
     * Returns a text representation of the Huffman tree with the following
     * format:
     *  - non-leaf node: (0, frequency, lefttree, righttree) 
     *  - leaf node: (1, frequency, byte_to_encode)
     *  Note that the tree is represented with ASCII codes for easy reading 
     *  by a human. In a real case, the encoding can be 
     */
    public byte[] toBytes() {
        ByteArrayOutputStream sb = new ByteArrayOutputStream();
        try {
            toByteArray(tree, sb);
            return sb.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return a String representation of the Huffman dictionary.
     */
    public String toString() {
        try {
            return new String(toBytes(), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a Huffman dictionary from its representation in an input stream. 
     * 
     * @param input the input stream
     * @return param tree the tree where values are inserted
     * @throws IOException 
     */
    public static HuffmanDict read(DataInputStream input) throws IOException {
        Node node = readNode(input);
        return new HuffmanDict(node);
    }

    private void toByteArray(Node n, ByteArrayOutputStream sb) throws IOException {
        sb.write('(');
        if (n.isLeaf()) {
            sb.write('1');
            sb.write(',');
            sb.write(Integer.toString(n.f).getBytes());
            sb.write(',');
            sb.write(n.c);
        } else {
            sb.write('0');
            sb.write(',');
            sb.write(Integer.toString(n.f).getBytes());
            sb.write(',');
            toByteArray(n.left, sb);
            sb.write(',');
            toByteArray(n.right, sb);
        }
        sb.write(')');
    }

    private static Node readNode(DataInputStream input) throws IOException {
        readBeginNode(input);
        if (isLeafNode(input))
            return readLeafNode(input);
        else
            return readNonleafNode(input);
    }
    
    private static Node readNonleafNode(DataInputStream input) throws IOException {
        int freq = readInteger(input);
        Node n = new Node(freq); // new non-leaf node
        n.left = readNode(input);
        readComma(input);
        n.right = readNode(input);
        readEndNode(input);
        return n;
    }

    private static Node readLeafNode(DataInputStream input) throws IOException {
        int freq = readInteger(input);
        byte c = input.readByte();
        Node n = new Node(c, freq); // new leaf node
        readEndNode(input);
        return n;
    }

    // Reads up to and including ','
    private static int readInteger(DataInputStream input) throws IOException {
        byte[] data = new byte[20];
        for (int i = 0; i < 20; i++) {
            data[i] = input.readByte();
            if (data[i] == ',') {
                String s = new String(data, 0, i);
                return Integer.valueOf(s).intValue();
            }
        }
        throw new IllegalArgumentException("Integer too long: " + new String(data));
    }

    private static void readComma(DataInputStream input) throws IOException {
        byte b = input.readByte(); // must be ','
        if (b != ',')
            throw new IllegalArgumentException("Unexpected delimiter: " + (char)b);
    }
    
    private static void readBeginNode(DataInputStream input) throws IOException {
        byte b = input.readByte(); // must be '('
        if (b != '(')
            throw new IllegalArgumentException("Unexpected node start: " + (char)b);
    }
    
    private static void readEndNode(DataInputStream input) throws IOException {
        byte b = input.readByte(); // must be ')'
        if (b != ')')
            throw new IllegalArgumentException("Unexpected node end: " + (char)b);
    }

    private static boolean isLeafNode(DataInputStream input) throws IOException {
        byte b = input.readByte();
        if (b == '0') {
            readComma(input);
            return false;
        }
        else if (b == '1') {
            readComma(input);
            return true;
        }
        else
            throw new IllegalArgumentException("Unexpected node type: " + (char)b);
    }
}