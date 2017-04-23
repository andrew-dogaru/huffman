# Huffman encoding
Example of Huffman encoder and decoder using a priority queue.

To build and run you have to import the Eclipse project into your workspace.

After building the project you can run the following executables from within Eclipse:

 * Huffman encoder: execute **huffman.MainEncode**
 * Huffman decoder: execute **huffman.MainDecode**
 * Test encode/decode methods: execute **huffman.MainTest**

The encoder and decoder programs prompt the user for an input and an output file name. If the input file does not exist, the program prompts the user again. The output file is created or overwritten if it already exists.
The programs exit if the user enters “cancel”.

The encoder program writes first a text representation of the Huffman tree used for encoding, followed by the encoded data.  Note that this is done to allow the user to examine the encoding tree.  Normally the tree representation is binary to achieve a slightly higher compression.

The tree text representation is:
 * non-leaf node: (0, frequency, leftTree, rightTree) 
 * leaf node: (1, frequency, byteToEncode)

where **byteToEncode** is one byte of data read from the input file and **frequency** is the number of occurrences of the byte in the entire file.

For example, the Huffman tree for input data "aaabbc" is:

```
          (0,6)
        ___/ \___
       /         \
   (1,3,a)      (0,3)
              ___/ \___
             /         \
          (1,1,c)    (1,2,b)

Represented as:                
(0,6,(1,3,a),(0,3,(1,1,c),(1,2,b)))
```
