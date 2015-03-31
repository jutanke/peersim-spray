package example.paper.scamplon;

import example.PeerSamplingService;
import example.paper.Dynamic;
import peersim.core.CommonState;
import peersim.core.Node;

import java.util.*;

/**
 * THIS is not EVENT-based due to simplification
 * Created by julian on 3/31/15.
 */
public class FastScamplon  extends example.Scamplon.ScamplonProtocol implements Dynamic {

    // ============================================
    // E N T I T Y
    // ============================================

    private PartialView partialView;
    private Map<Long, Node> inView;
    private boolean isUp = true;
    private static final int FORWARD_TTL = 25;

    public FastScamplon(String prefix) {
        super(prefix);
        this.partialView = new PartialView();
        this.inView = new HashMap<Long, Node>();
    }

    @Override
    public Object clone() {
        FastScamplon s = (FastScamplon) super.clone();
        s.partialView = new PartialView();
        s.inView = new HashMap<Long, Node>();
        return s;
    }


    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {
        if (this.isUp()) {
            this.startShuffle(node);
        }
    }

    @Override
    public boolean isUp() {
        return this.isUp;
    }

    @Override
    public void up() {
        this.isUp = true;
    }

    @Override
    public void down() {
        this.isUp = false;
    }

    @Override
    public int hash() {
        return 0;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // N E V E R  U S E D
    }

    @Override
    public int degree() {
        return this.partialView.degree();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.partialView.get(i);
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return this.partialView.add(neighbour);
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.partialView.contains(neighbor);
    }

    @Override
    public List<Node> getPeers() {
        return this.partialView.list();
    }

    @Override
    public String debug() {
        return "...";
    }

    // ============================================
    // C Y C L O N
    // ============================================

    /**
     *
     *      A* --> B
     *
     * @param me
     */
    public void startShuffle(Node me) {
        this.updateInView(me);
        if (this.isUp()) {
            if (this.degree() > 0) {
                this.partialView.incrementAge();
                final PartialView.Entry q = this.partialView.oldest();
                final List<PartialView.Entry> nodesToSend = this.partialView.subsetMinus1(q);
                nodesToSend.add(new PartialView.Entry(me));
                final FastScamplon Q = (FastScamplon) q.node.getProtocol(pid);
                if (Q.isUp()) {
                    Q.receiveShuffle(q.node, me, PartialView.clone(nodesToSend), this.degree());
                } else {
                    // TIME OUT
                    this.partialView.deleteAll(q.node);
                    this.inView.remove(q.node.getID());
                }
            }
        }
    }

    /**
     *
     *      A --> B*
     *
     * @param me
     * @param sender
     * @param received
     * @param otherPartialViewSize
     */
    public void receiveShuffle(
            final Node me,
            final Node sender,
            final List<PartialView.Entry> received,
            final int otherPartialViewSize) {
        if (this.isUp()) {
            List<PartialView.Entry> nodesToSend = this.partialView.subset();
            this.partialView.merge(me, me, received, otherPartialViewSize);
            this.updateInView(me);
            this.updateOutView(me);
            final FastScamplon P = (FastScamplon) sender.getProtocol(pid);
            P.finishShuffle(
                    sender,
                    me,
                    PartialView.clone(received),
                    PartialView.clone(nodesToSend),
                    this.degree());
        }
    }

    /**
     *
     *      A* --> B
     *
     * @param me
     * @param sender
     * @param sent (FROM A)
     * @param received (FROM B)
     * @param otherPartialViewSize
     */
    public void finishShuffle(
            final Node me,
            final Node sender,
            final List<PartialView.Entry> sent,
            final List<PartialView.Entry> received,
            final int otherPartialViewSize) {
        if (this.isUp()) {
            this.partialView.merge(me, sender, received, otherPartialViewSize);
            this.updateInView(me);
            this.updateOutView(me);
        }
    }

    // ============================================
    // S C A M P
    // ============================================

    /**
     * Transform
     *  a --> (me) --> b
     *  into
     *  a --> b
     *
     * @param node
     */
    public static void unsubscribe(Node node) {
        final FastScamplon current = (FastScamplon) node.getProtocol(pid);
        current.updateInView(node);
        if (current.isUp()) {
            int count = 0;
            current.down();
            final int ls = current.inView.size();
            final int notifyIn = Math.max(ls - c - 1, 0);
            final Queue<Node> in = new LinkedList<Node>(current.inView.values());
            final List<Node> out = current.partialView.list();
            for (int i = 0; i < notifyIn && out.size() > 0; i++) {
                final Node a = in.poll();
                final Node b = out.get(i % out.size());
                count += current.replace(node, a, b);
            }

            while (!in.isEmpty()) {
                final FastScamplon next = (FastScamplon) in.poll().getProtocol(pid);
                count += next.partialView.deleteAll(node);
            }
            current.partialView.clear();
            current.inView.clear();
        }
    }

    /**
     * SUBSCRIBE
     * @param s
     * @param c
     */
    public static void subscribe(final Node s, final Node c) {
        final FastScamplon subscriber = (FastScamplon) s.getProtocol(pid);
        final FastScamplon contact = (FastScamplon) c.getProtocol(pid);
        if (subscriber.isUp() && contact.isUp()) {
            subscriber.addNeighbor(c);
            for (Node n : contact.getPeers()) {
                forward(s, n, 0);
            }
            for (int i = 0; i < FastScamplon.c && contact.degree() > 0; i++) {
                Node n = contact.getNeighbor(CommonState.r.nextInt(contact.degree()));
                forward(s, n, 0);
            }
        } else {
            throw new RuntimeException("@Subscribe (" + s.getID() + " -> " + c.getID() + " not up");
        }
    }

    // =================================================================
    // H E L P E R
    // =================================================================

    private void updateInView(Node me) {
        List<Node> in = new ArrayList<Node>(this.inView.values());
        for (Node n : in) {
            final FastScamplon current = (FastScamplon) n.getProtocol(pid);
            if (!current.isUp() || !current.contains(me)) {
                this.inView.remove(n.getID());
            }
        }
    }

    private void updateOutView(Node me) {
        for (Node n : this.getPeers()) {
            final FastScamplon current = (FastScamplon) n.getProtocol(pid);
            if (!current.inView.containsKey(me.getID())) {
                current.addToInview(n, me);
            }
        }
    }

    /**
     * FORWARD
     * @param s
     * @param node
     * @param counter
     */
    public static void forward(final Node s, final Node node, int counter) {
        counter++;
        if (counter < FORWARD_TTL) {
            final FastScamplon current = (FastScamplon) node.getProtocol(pid);
            if (current.partialView.p() && node.getID() != s.getID()) {
                final FastScamplon subscriber = (FastScamplon) s.getProtocol(pid);
                current.addNeighbor(s);
                subscriber.addToInview(s, node);
            } else if (current.degree() > 0) {
                Node next = current.partialView.get(CommonState.r.nextInt(current.degree()));
                forward(s, next, counter);
            } else {
                System.err.println("DEAD END for subscription " + s.getID() + " @" + node.getID());
            }
        } else {
            System.err.println("Forward for " + s.getID() + " timed out @" + node.getID());
        }
    }


    /**
     * Turn
     *      a --> (me) --> b
     * Into
     *      a --> b
     *
     * @param me
     * @param a
     * @param b
     * @return
     */
    private int replace(Node me, Node a, Node b) {
        int count = 0;
        final FastScamplon A = (FastScamplon) a.getProtocol(pid);
        final FastScamplon B = (FastScamplon) b.getProtocol(pid);
        if (a.isUp() && b.isUp()) {

        } else {
            // either a or b is down, so we just kill all links regarding (me)
            count += A.partialView.deleteAll(me);
            count += (B.inView.remove(me.getID()) != null ? 1 : 0);
        }
        return count;
    }

    private void addToInview(Node me, Node n) {
        if (me.getID() == n.getID()) {
            throw new RuntimeException("cannot put myself");
        }
        if (this.inView.containsKey(n.getID())) {
            this.inView.put(n.getID(), n);
        }
    }

}