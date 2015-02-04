package example.scampXcyclon;

import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by julian on 2/3/15.
 */
public class ScamplonView {

    // ============================================
    // E N T I T Y
    // ============================================

    public final List<Node> in;
    public final List<PartialViewEntry> out;
    private final List<PartialViewEntry> filter;
    private final List<Node> singleFilter;
    private final List<Node> zeroFilter;
    private final List<PartialViewEntry> cut;

    public ScamplonView() {
        this.in = new ArrayList<Node>();
        this.out = new ArrayList<PartialViewEntry>();
        this.filter = new ArrayList<PartialViewEntry>();
        this.cut = new ArrayList<PartialViewEntry>();
        this.singleFilter = new ArrayList<Node>();
        this.zeroFilter = new ArrayList<Node>();
    }

    // ============================================
    // P U B L I C
    // ============================================

    public boolean addToOut(Node n) {
        if (!outContains(n)) {
            this.out.add(new PartialViewEntry(n));
            return true;
        }
        return false;
    }

    public boolean addToIn(Node n) {
        if (!inContains(n)) {
            this.in.add(n);
            return true;
        }
        return false;
    }

    public boolean outContains(Node n) {
        for (PartialViewEntry e : this.out) {
            if (e.node.getID() == n.getID()) {
                return true;
            }
        }
        return false;
    }

    public boolean inContains(Node n) {
        for (Node e : this.in) {
            if (e.getID() == n.getID()) {
                return true;
            }
        }
        return false;
    }

    public int l() {
        return (int) Math.max(Math.ceil(this.c() / 2.0), 1);
    }

    public int c() {
        return this.out.size();
    }

    public PartialViewEntry oldest() {
        PartialViewEntry oldest = null;
        for (PartialViewEntry e : this.out) {
            if (oldest == null || oldest.age < e.age) {
                oldest = e;
            }
        }
        return PartialViewEntry.copy(oldest);
    }

    public List<PartialViewEntry> subsetMinus1(Node filter) {
        singleFilter.clear();
        singleFilter.add(filter);
        return this.subset(singleFilter, l() - 1);
    }

    public List<PartialViewEntry> subset() {
        return this.subset(zeroFilter, l());
    }

    public void incrementAge() {
        for (PartialViewEntry e : this.out) {
            e.age += 1;
        }
    }

    public void merge(Node me, List<PartialViewEntry> sent, List<PartialViewEntry> received, int factor) {
        merge(me, null, sent, received, factor);
    }

    public void merge(Node me, PartialViewEntry oldest, List<PartialViewEntry> sent, List<PartialViewEntry> received, int factor) {
        final int maxLength = this.out.size() + factor;
        //System.err.println("++++++++++++++ MERGE");
        //System.err.println("@" + me.getID() + " oldest:" + oldest + " sent:" + sent + " received:" + received);
        sent = filter(sent, me);
        if (oldest != null) sent.add(oldest);
        received = cut(clone(received), clone(sent));
        received = filter(clone(received), me);
        ArrayList<PartialViewEntry> out = (ArrayList<PartialViewEntry>)this.cut(this.out, clone(sent));
        received = cut(received, out);
        if (sent.size() == 0 && maxLength > 0) {
            // we must remove some elements..
            int l = Math.min(l(), received.size());
            out = (ArrayList<PartialViewEntry>) removeN(out, l);
        }

        while (out.size() > maxLength) {
            out.remove(0);
        }

        Stack<PartialViewEntry> receivedStack = new Stack<PartialViewEntry> ();
        receivedStack.addAll(received);
        boolean someNewNodes = false;
        while (out.size() < maxLength && !receivedStack.isEmpty()) {
            out.add(receivedStack.pop());
            someNewNodes = true;
        }

        if (!someNewNodes && received.size() > 0 && out.size() > 0) {
            // enforce exchange!
            out.remove(0); // TODO make this better!
            out.add(youngest(received));
        }

        Stack<PartialViewEntry> sentStack = new Stack<PartialViewEntry> ();
        receivedStack.addAll(received);
        while (out.size() < maxLength && !sentStack.isEmpty()) {
            out.add(sentStack.pop());
        }

        if (out.size() != maxLength) {
            System.err.println("@" + me.getID() + " SIZE NOT COMPATIBLE!");
            //System.err.println("@" + me.getID() + " -> " + this.toString() + " vs " + maxLength + " -> " + out);
            //System.err.println("oldest:" + oldest + " sent:" + sent + " received:" + received);
            //throw new RuntimeException("SIZE NOT COMPATIBLE! (CYCLON)");
        }

        this.out.clear();
        for (PartialViewEntry e : out) {
            this.out.add(e);
        }
    }

    public boolean p() {
        return CDState.r.nextDouble() < 1.0 / (1.0 + this.out.size());
    }

    // ============================================
    // P R I V A T E
    // ============================================

    private List<PartialViewEntry> clone(List<PartialViewEntry> list) {
        return new ArrayList<PartialViewEntry>(list);
    }

    private PartialViewEntry youngest(List<PartialViewEntry> list) {
        PartialViewEntry y = list.get(0);
        for (PartialViewEntry e : list) {
            if (e.age < y.age) {
                y = e;
            }
        }
        return y;
    }

    public List<PartialViewEntry> removeN(ArrayList<PartialViewEntry> list, int n) {
        if (n >= list.size()) {
            list.clear();
            return list;
        }
        List<PartialViewEntry> copy = (List<PartialViewEntry>)list.clone();
        for (int i = 0; i < n; i++) {
            PartialViewEntry random = copy.get(CDState.r.nextInt(copy.size()));
            copy.remove(random);
        }
        return copy;
    }

    public List<PartialViewEntry> cut(List<PartialViewEntry> list, List<PartialViewEntry> cut) {
        List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
        outer:
        for (PartialViewEntry e : list) {
            inner:
            for (PartialViewEntry c : cut) {
                if (e.node.getID() == c.node.getID()) {
                    continue outer;
                }
            }
            result.add(e);
        }
        return result;
    }

    private List<PartialViewEntry> subset(List<Node> filters, int l) {
        final List<PartialViewEntry> result = new ArrayList<PartialViewEntry>();
        if (this.out.size() > 0 && l > 0) {
            List<PartialViewEntry> out = filter(this.out, filters);
            int L = Math.min(l, out.size());
            if (L > 0) {
                do {
                    PartialViewEntry next = out.get(CommonState.r.nextInt(out.size()));
                    result.add(PartialViewEntry.copy(next));
                    singleFilter.clear();
                    singleFilter.add(next.node);
                    out = filter(out, singleFilter);
                    L = Math.min(l, out.size());
                } while (L-- > 0);
            }
        }
        return result;
    }

    /**
     * @param filter
     * @return
     */
    public List<PartialViewEntry> filter(List<PartialViewEntry> out, Node filter) {
        singleFilter.clear();
        singleFilter.add(filter);
        return this.filter(out, singleFilter);
    }

    /**
     * @param filters
     * @return
     */
    public List<PartialViewEntry> filter(List<PartialViewEntry> out, List<Node> filters) {
        filter.clear();
        outer:
        for (PartialViewEntry e : out) {
            inner:
            for (Node n : filters) {
                if (e.node.getID() == n.getID()) {
                    continue outer;
                }
            }
            this.filter.add(e);
        }
        return filter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IN:[");
        for (Node n : this.in) {
            sb.append(" ");
            sb.append(n.getID());
        }
        sb.append("], OUT:[");
        for (PartialViewEntry e : this.out) {
            sb.append(" ");
            sb.append(e);
        }
        sb.append("]");
        return sb.toString();
    }

}
