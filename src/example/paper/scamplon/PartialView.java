package example.paper.scamplon;

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
    public List<Entry> out;
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

    public void addMultiset(Node n) {
        this.out.add(new Entry(n));
    }

    public boolean delete(Entry e) {
        int i = 0;
        boolean found = false;
        for (; i < this.out.size(); i++) {
            if (this.out.get(i) == e) {
                found = true;
                break;
            }
        }
        if (found) {
            this.out.remove(i);
            return true;
        }
        return false;
    }

    public int deleteAll(Node n) {
        int count = 0;
        while (this.contains(n)) {
            int i = 0;
            for (; i<this.degree();i++) {
                if (this.get(i).getID() == n.getID()) {
                    this.out.remove(i);
                    count += 1;
                    break;
                }
            }
        }
        return count;
    }

    public void clear() {
        this.out.clear();
    }

    public int count(Node n) {
        int i = 0;
        for (Entry e : this.out) {
            if (e.node.getID() == n.getID()) {
                i++;
            }
        }
        return i;
    }

    public int switchNode(Node oldNode, Node newNode) {
        int count = 0;
        for (Entry e : this.out) {
            if (e.node.getID() == oldNode.getID()) {
                e.node = newNode;
                count += 1;
            }
        }
        return count;
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
    public List<Entry> get(Node n) {
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
        this.freeze();
        return subset(this.out, this.l());
    }

    /**
     * Should be used in the shuffle code
     *
     * @param oldest
     * @return
     */
    public List<Entry> subsetMinus1(Entry oldest) {
        return subset(this.out, oldest, this.l() - 1);
    }

    /**
     * @return oldest element
     */
    public Entry oldest() {
        if (this.out.size() == 0) return null;
        Entry oldest = this.out.get(0);
        for (Entry e : this.out) {
            if (oldest.age < e.age) {
                oldest = e;
            }
        }
        oldest.isVolatile = true;
        return oldest;
    }

    public void freeze() {
        for (Entry e : this.out) {
            e.isVolatile = false;
        }
    }

    public void merge(Node self, Node oldest, List<Entry> received, int otherSize) {
        this.out = merge(self, oldest, this.out, received, otherSize);
    }

    public void merge(Node self, Node oldest, List<Entry> received, int otherSize, final boolean FROM_SENDER) {
        this.out = merge(self, oldest, this.out, received, otherSize, FROM_SENDER);
    }

    public boolean p() {
        return CDState.r.nextDouble() < 1.0 / (1.0 + this.out.size());
    }

    public void unhassle() {
        for (Entry e : this.out) {
            e.isVolatile = false;
        }
    }

    // ============================================
    // P R I V A T E
    // ============================================


    // ============================================
    // L I S T  H E L P E R
    // ============================================

    public static List<Entry> merge(
            final Node me,
            final Node other,
            final List<Entry> List,
            List<Entry> received,
            final int otherSize) {
        return merge(me, other, List, received, otherSize, true);
    }

    public static List<Entry> merge(
            final Node me,
            final Node other,
            final List<Entry> List,
            List<Entry> received,
            int otherSize,
            final boolean FROM_SENDER) {

        //System.err.println("@" + me.getID() + " <- " + other.getID() + " pv:" + list + " rec:" + received + " othersize:" + otherSize);

        //Scamplon culprit = (Scamplon) other.getProtocol(Scamplon.pid);
        //System.err.println("culprit " +other.getID()+ " :" + culprit);

        //System.err.println("list size:" + list.size() + " (@" + me.getID() + ")");

        int newSize = (List.size() % 2 == 0) ?
                (int) Math.ceil((List.size() + otherSize) / 2.0) :
                (int) Math.floor((List.size() + otherSize) / 2.0);

        //System.err.println("(" + list.size() + " + " + otherSize + ")/ 2 = " + newSize);

        Parent qq = (Parent) me.getProtocol(example.Scamplon.ScamplonProtocol.pid);

        //System.err.println("from " + other.getID() + " get " + received +
        //        " @" + me.getID() + " = " + qq.debug() + " sender:" + FROM_SENDER + " otherSize:" + otherSize);

        //System.err.println("NOW:" + List + " @" + me.getID() + " from " + other.getID());

        RemoveVolatileResult rem = removeVolatileResults(List);
        List<Entry> list = rem.rest;

        if (contains(received, me)) {
            List<Entry> sent = removeAll(rem.volatiles, me); // here will never remove any element!
            //if (FROM_SENDER) {
                sent = removeAll(sent, other);
            //}
            int itemsRemoved = 0;
            int sizeBefore = received.size();
            received = removeAll(received, me); // here we might remove possibly more elements..
            itemsRemoved += (sizeBefore - received.size());
            for (int i = 0; i < itemsRemoved; i++) {
                if (sent.size() > 0) {
                    received.add(popYoungest(sent));
                } else {
                    received.add(new Entry(other)); // introduce a new arc! Because we removed one arc before!
                }
            }
        }

        if (newSize != (list.size() + received.size())) {
            System.err.println("Error @" + me.getID() + " receiving from " + other.getID() + " is sender:" + FROM_SENDER);
            System.err.println("@" + me.getID() + " = " + qq.debug());
            System.err.println("orig " + List + " =vs= " + list + " otherSize:" + otherSize);
            System.err.println(newSize + " vs " + list.size() + " + " + received.size());
            //System.err.println("from " + other.getID() + " rec:" + received + " isup:" +
                    //((Scamplon)other.getProtocol(Scamplon.pid)).isUp());
            throw new RuntimeException("@" + me.getID() +":LOSING ARCS! MUST NOT HAPPEN!");
        }

        list.addAll(received);
        for (Entry e : list) {
            e.isVolatile = false;
        }
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

    private static Entry popYoungest(List<Entry> list) {
        if (list.size() == 0) {
            return null;
        }

        Entry youngest = youngest(list);
        list.remove(youngest);
        return youngest;
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
    public static List<Entry> subset(List<Entry> list, Entry filter, int l) {
        List<Entry> res = clone(list);
        if (filter != null) {
            res = remove(res, filter);
        }
        if (l >= res.size()) {
            List<Entry> result = new ArrayList<Entry>();
            for (Entry e : res) {
                Entry newE = e.clone();
                e.isVolatile = true;
                result.add(newE);
            }
            return result;
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
    public static List<Entry> get(List<Entry> list, Node n) {
        List<Entry> result = new ArrayList<Entry>();
        for (Entry e : list) {
            if (n.getID() == e.node.getID()) {
                result.add(e);
            }
        }
        return result;
    }


    public static List<Entry> removeAll(List<Entry> list, Node n) {
        List<Entry> del = get(list, n);
        for (Entry e : del) {
            list.remove(e);
        }
        return list;
    }

    /**
     * Remove an element
     *
     * @param list
     * @param n
     * @return
     */
    public static List<Entry> remove(List<Entry> list, Entry n) {
        List<Entry> del = get(list, n.node);
        for (Entry e : del) {
            if (e.age == n.age) {
                list.remove(e);
                break;
            }
        }
        /*
        if (del != null) {
            list.remove(del);
        }
        */
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
    // P A R E N T
    // ============================================

    public interface Parent {
        String debug();
    }

    // ============================================
    // E N T R Y
    // ============================================
    public static final class Entry implements Comparable<Entry>, Comparator<Entry> {
        public Node node;
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
            result.isVolatile = false;
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
