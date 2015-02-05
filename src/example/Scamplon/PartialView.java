package example.Scamplon;

import peersim.core.Node;

import java.util.*;

/**
 * Created by julian on 2/5/15.
 */
public class PartialView {

    public static boolean TEST_ENV = false;
    private static final Random r = new Random(42);

    // ============================================
    // E N T I T Y
    // ============================================

    // pointer to the own node
    private final Node self;
    private final List<Entry> out;

    public PartialView(Node self) {
        this.self = self;
        this.out = new ArrayList<Entry>();
    }


    // ============================================
    // P U B L I C
    // ============================================

    /**
     *
     * @param n
     * @return
     */
    public boolean add(Node n) {
        if (this.contains(n)) {
            return false;
        }
        this.out.add(new Entry(n));
        return true;
    }

    /**
     *
     * @param n
     * @return
     */
    public boolean contains(Node n) {
        return contains(this.out, n);
    }

    /**
     * @return size of the partial view
     */
    public int degree(){
        return this.out.size();
    }

    /**
     * Needed for the interface
     * @param i
     * @return
     */
    public Node get(int i) {
        return this.out.get(i).node;
    }

    /**
     * might be needed internally
     * @param n
     * @return
     */
    public Entry get(Node n) {
        return get(this.out, n);
    }

    /**
     * Of all entries
     */
    public void incrementAge() {
        for (Entry e : this.out) {
            e.age += 1;
        }
    }

    /**
     * @return subset-size
     */
    public int l() {
        return (int) Math.max(1, Math.ceil(this.degree()/2.0));
    }


    // ============================================
    // P R I V A T E
    // ============================================



    // ============================================
    // L I S T  H E L P E R
    // ============================================




    /**
     * Checks if the list contains an entry with said node
     * @param list
     * @param n
     * @return
     */
    public static boolean contains(List<Entry> list, Node n) {
        for (Entry e : list) {
            if (e.node.getID() == n.getID()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delivers the node or null
     * @param list
     * @param n
     * @return
     */
    public static Entry get(List<Entry> list, Node n) {
        for (Entry e : list) {
            if (n.getID() == e.node.getID()) {
                return e;
            }
        }
        return null;
    }

    /**
     * Remove an element
     * @param list
     * @param n
     * @return
     */
    public static List<Entry> remove(List<Entry> list, Node n){
        Entry del = get(list, n);
        if (del != null) {
            list.remove(del);
        }
        return list;
    }

    /**
     * Clones a list
     * @param list
     * @return
     */
    public static List<Entry> clone(List<Entry> list) {
        List<Entry> result = new ArrayList<Entry>();
        for (Entry e : list) {
            result.add(e);
        }
        return result;
    }

    /**
     *
     * @param list
     * @return
     */
    public static List<Entry> sort(List<Entry> list) {
        Arrays.sort(list.toArray(new Entry[0]), new Entry(null));
        return list;
    }

    // ============================================
    // E N T R Y
    // ============================================
    public static final class Entry implements Comparable<Entry>, Comparator<Entry>{
        public final Node node;
        public int age;
        public boolean isVolatile;
        public Entry(Node n) {
            this.node = n;
            this.age = 0;
            this.isVolatile = false;
        }
        public final Entry clone() {
            Entry result = new Entry(this.node);
            result.age = this.age;
            return result;
        }

        @Override
        public int compare(Entry entry, Entry t1) {
            return Integer.compare(entry.age, t1.age);
        }

        @Override
        public int compareTo(Entry entry) {
            return ((Integer)this.age).compareTo(entry.age);
        }
    }

}
