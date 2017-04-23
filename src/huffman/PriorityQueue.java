package huffman;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * The implementation of a priority queue based on a max heap.
 * 
 * We use a one-based indexing to simplify calculations of parent and 
 * child indexes.
 *
 * @param <T>
 */
public class PriorityQueue<T extends Comparable<T>> {

    private ArrayList<T> heapArray;

    public PriorityQueue() {
        this.heapArray = new ArrayList<>();
        
        // Start storage from index 1. This simplifies the expressions
        // used for finding parent and child indexes
        heapArray.add(null);
    }

    /**
     * Insert value into the priority queue.
     * After inserting the node we heapify up to keep the heap valid.
     * 
     * @param data 
     */
    public void insert(T data) {
        heapArray.add(data);

        // Heapify up until the curr's parent's key is greater than curr's key
        int curr = heapArray.size() - 1;
        int parent = getParent(curr);
        while ((curr > 1) && less(parent, curr)) {
            swap(curr, parent);
            curr = getParent(curr);
            parent = getParent(curr);
        }
    }

    /**
     * Removes the largest value from this priority queue.
     *
     * @return the largest value from this priority queue
     * @throws NoSuchElementException if the priority queue is empty
     */
    public T remove() {
        if (heapArray.isEmpty())
            throw new NoSuchElementException();
        
        swap(1, heapArray.size() - 1);
        T max = heapArray.remove(heapArray.size() - 1);

        // Heapify down from parent
        int parent = 1;
        int curr = getLeftChild(parent);
        while (curr < heapArray.size()) {
            // int j = 2*k;
            // if left child smaller than right child, use the right child's index 
            if (curr < (heapArray.size()-1) && less(curr, curr + 1))
                curr++;
            if (!less(parent, curr))
                break;
            swap(parent, curr);
            parent = curr;
            curr = getLeftChild(parent);
        }
        return max;
    }
    
    /**
     * Returns the number of elements in the priority queue.
     * @return
     */
    public int size() {
        return heapArray.size() - 1;
    }

    // Returns the parent's position
    private int getParent(int pos) {
        return pos / 2;
    }

    // Returns the left child's position
    private int getLeftChild(int pos) {
        return pos * 2;
    }

    // Return whether the key at pos1 is less than the key at pos2
    private boolean less(int pos1, int pos2) {
        return heapArray.get(pos1).compareTo(heapArray.get(pos2)) < 0;
    }
    
    // Swap nodes at pos1 and pos2
    private void swap(int pos1, int pos2) {
        T temp = heapArray.get(pos1);
        heapArray.set(pos1, heapArray.get(pos2));
        heapArray.set(pos2, temp);
    }
    
    // Verify that the heap starting at the given position satisfies 
    // the binary heap requirements. This is used for testing.
    private boolean isValidHeap(int parent) {
        if (parent >= heapArray.size())
            return true;
        
        // left and right subtrees
        int left = getLeftChild(parent);
        int right = left + 1;
        
        // invalid heap if parent is less than left or right
        if (left < heapArray.size() && less(parent, left))
            return false;
        if (right < heapArray.size() && less(parent, right))
            return false;
        
        // check child subtrees
        return isValidHeap(left) && isValidHeap(right);
    }
    
    /**
     * Test program - insert and remove values and validate the heap
     * at each step.
     */
    public static void main(String[] args) {
        int data[] = {6, 3, 0, 1, 8, 2, 9, 3};
        
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        
        System.out.print("Insert: ");
        for (int i : data) {
            pq.insert(new Integer(i));
            System.out.print(String.valueOf(i) + " ");
            if (!pq.isValidHeap(1)) {
                System.err.println("Invalid heap after inserting " + i);
                return;
            }
        }
        System.out.println();
        
        System.out.print("Remove: ");
        while (pq.size() > 0) {
            Integer i = pq.remove();
            System.out.print(i.toString() + " ");
            if (!pq.isValidHeap(1)) {
                System.err.println("Invalid heap after removing " + i);
                return;
            }
        }
        System.out.println();
    }
}
