package example.scampXcyclon;

import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.CDScheduler;

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

    public void addToOut(Node n) {
        if (!oldContains(n)) {
            this.out.add(new PartialViewEntry(n));
        }
    }

    public void addToIn(Node n) {
        if (!inContains(n)) {
            this.in.add(n);
        }
    }

    public boolean oldContains(Node n) {
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

    public void merge(Node me, List<PartialViewEntry> sent, List<PartialViewEntry> received) {
        merge(me, null, sent, received);
    }

    public void merge(Node me, PartialViewEntry oldest, List<PartialViewEntry> sent, List<PartialViewEntry> received) {
        final int maxLength = this.out.size();
        sent = filter(sent, me);
        if (oldest != null) sent.add(oldest);
        received = cut(received, sent);
        received = filter(received, me);
        received = cut(received, this.out);
        ArrayList<PartialViewEntry> out = (ArrayList<PartialViewEntry>)this.cut(this.out, sent);
        if (sent.size() == 0 && maxLength > 0) {
            // we must remove some elements..
            int l = Math.min(l(), received.size());
            out = (ArrayList<PartialViewEntry>) removeN(out, l);
        }
        Stack<PartialViewEntry> receivedStack = new Stack<PartialViewEntry> ();
        receivedStack.addAll(received);
        while (out.size() < maxLength && !receivedStack.isEmpty()) {
            out.add(receivedStack.pop());
        }

        Stack<PartialViewEntry> sentStack = new Stack<PartialViewEntry> ();
        receivedStack.addAll(received);
        while (out.size() < maxLength && !sentStack.isEmpty()) {
            out.add(sentStack.pop());
        }

        if (out.size() != maxLength) {
            throw new RuntimeException("SIZE NOT COMPATIBLE! (CYCLON)");
        }

        this.out.clear();
        for (PartialViewEntry e : out) {
            this.out.add(e);
        }
    }


    // ============================================
    // P R I V A T E
    // ============================================

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
        if (this.out.size() > 0) {
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
                } while (L-- >= 0);
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
