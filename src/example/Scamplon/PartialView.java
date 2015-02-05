package example.Scamplon;

import peersim.cdsim.CDState;
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
    //private final Node self;
    private List<Entry> out;
    private List<Node> list;

    public PartialView() {
        //this.self = self;
        this.list = new ArrayList<Node>();
        this.out = new ArrayList<Entry>();
    }


    // ============================================
    // P U B L I C
    // ============================================

    /**
     * @return
     */
    public List<Node> list() {
        this.list.clear();
        for (Entry e : this.out) {
            this.list.add(e.node);
        }
        return this.list;
    }

    /**
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
     * @param n
     * @return
     */
    public boolean contains(Node n) {
        return contains(this.out, n);
    }

    /**
     * @return size of the partial view
     */
    public int degree() {
        return this.out.size();
    }

    /**
     * Needed for the interface
     *
     * @param i
     * @return
     */
    public Node get(int i) {
        return this.out.get(i).node;
    }

    /**
     * might be needed internally
     *
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
        return (int) Math.max(1, Math.ceil(this.degree() / 2.0));
    }

    /**
     * should be used in the shuffle-response part
     *
     * @return
     */
    public List<Entry> subset() {
        return subset(this.out, this.l());
    }

    /**
     * Should be used in the shuffle code
     *
     * @param oldest
     * @return
     */
    public List<Entry> subsetMinus1(Node oldest) {
        return subset(this.out, oldest, this.l() - 1);
    }

    /**
     * @return oldest element
     */
    public Entry oldest() {
        Entry oldest = this.out.get(0);
        for (Entry e : this.out) {
            if (oldest.age > e.age) {
                oldest = e;
            }
        }
        oldest.isVolatile = true;
        return oldest;
    }

    public void merge(Node self, Node oldest, List<Entry> received, int otherSize) {
        this.out = merge(self, oldest, this.out, received, otherSize);
    }

    public boolean p() {
        return CDState.r.nextDouble() < 1.0 / (1.0 + this.out.size());
    }

    // ============================================
    // P R I V A T E
    // ============================================


    // ============================================
    // L I S T  H E L P E R
    // ============================================


    public static List<Entry> merge(Node me, Node other, List<Entry> list, List<Entry> received, int otherSize) {
        int newSize = (list.size() % 2 == 0) ?
                (int) Math.ceil((list.size() + otherSize) / 2) :
                (int) Math.floor((list.size() + otherSize) / 2);

        RemoveVolatileResult rem = removeVolatileResults(list);
        list = rem.rest;

        if (contains(received, me)) {
            List<Entry> sent = remove(rem.volatiles, me);
            received = remove(received, me);
            if (sent.size() > 0) {
                received.add(youngest(sent));
            } else {
                received.add(new Entry(other)); // introduce a new arc!
            }
        }

        if (newSize != (list.size() + received.size())) {
            throw new RuntimeException("LOSING ARCS! MUST NOT HAPPEN!");
        }

        list.addAll(received);
        return list;
    }


    /**
     * Select a random integer between max and 0
     *
     * @param max
     * @return
     */
    private static int nextInt(int max) {
        if (!TEST_ENV) {
            return CDState.r.nextInt(max);
        } else {
            return r.nextInt(max);
        }
    }

    private static Entry youngest(List<Entry> list) {
        Entry youngest = list.get(0);
        for (Entry e : list) {
            if (e.age < youngest.age) {
                youngest = e;
            }
        }
        return youngest;
    }

    /**
     * @param list
     * @param l
     * @return
     */
    public static List<Entry> subset(List<Entry> list, int l) {
        return subset(list, null, l);
    }

    /**
     * @param list
     * @param filter
     * @param l
     * @return
     */
    public static List<Entry> subset(List<Entry> list, Node filter, int l) {
        List<Entry> res = clone(list);
        if (filter != null) {
            res = remove(res, filter);
        }
        if (l >= res.size()) {
            return res;
        } else {
            HashSet<Integer> pos = new HashSet<Integer>();
            for (int i = 0; i < l; i++) {
                int p = nextInt(res.size());
                while (pos.contains(p)) {
                    p = nextInt(res.size()); // resolves eventually
                }
                pos.add(p);
            }
            List<Entry> result = new ArrayList<Entry>();
            for (int i : pos) {
                Entry current = res.get(i);
                current.isVolatile = false; // make sure we do not send "volatile" elements
                result.add(current.clone());
                current.isVolatile = true;
            }
            return result;
        }
    }

    /**
     * Checks if the list contains an entry with said node
     *
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
     *
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
     *
     * @param list
     * @param n
     * @return
     */
    public static List<Entry> remove(List<Entry> list, Node n) {
        Entry del = get(list, n);
        if (del != null) {
            list.remove(del);
        }
        return list;
    }

    /**
     * Clones a list
     *
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
     * @param list
     * @return
     */
    public static List<Entry> sort(List<Entry> list) {
        Arrays.sort(list.toArray(new Entry[0]), new Entry(null));
        return list;
    }

    /**
     * Splits up the view
     *
     * @param list
     * @return
     */
    /*public static List<Entry> removeVolatileResults(List<Entry> list) {
        List<Entry> rest = clone(list);
        for (Entry e : list) {
            if (e.isVolatile) {
                e.isVolatile = false;
                rest.remove(e);
            }
        }
        return rest;
    }*/

    /**
     * Splits up the view
     *
     * @return
     */
    private static RemoveVolatileResult removeVolatileResults(List<Entry> list) {
        List<Entry> rest = clone(list);
        List<Entry> volatiles = new ArrayList<Entry>();
        for (Entry e : list) {
            if (e.isVolatile) {
                volatiles.add(e);
                e.isVolatile = false;
                rest.remove(e);
            }
        }
        return new RemoveVolatileResult(volatiles, rest);
    }

    @Override
    public String toString() {
        return  " -> " + this.out.toString();
    }

    // ============================================
    // R E M O V E _ V O L A T I L E
    // ============================================
    private static final class RemoveVolatileResult {
        public final List<Entry> volatiles;
        public final List<Entry> rest;

        public RemoveVolatileResult(List<Entry> v, List<Entry> r) {
            this.volatiles = v;
            this.rest = r;
        }
    }

    // ============================================
    // E N T R Y
    // ============================================
    public static final class Entry implements Comparable<Entry>, Comparator<Entry> {
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
            result.isVolatile = this.isVolatile;
            return result;
        }

        @Override
        public String toString() {
            return "{" + node.getID() + "|" + age + "|" + (isVolatile ? "y" : "n") + "}";
        }

        @Override
        public int compare(Entry entry, Entry t1) {
            return Integer.compare(entry.age, t1.age);
        }

        @Override
        public int compareTo(Entry entry) {
            return ((Integer) this.age).compareTo(entry.age);
        }
    }

}
