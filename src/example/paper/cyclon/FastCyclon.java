package example.paper.cyclon;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 4/1/15.
 */
public class FastCyclon extends CyclonProtocol {

    private static final String PARAM_START_SHUFFLE = "startShuffle";

    // ===========================================
    // C T O R
    // ===========================================

    private final int startShuffle;

    public FastCyclon(String n) {
        super(n);
        this.startShuffle = Configuration.getInt(n + "." + PARAM_START_SHUFFLE, 0);
    }


    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void processMessage(Node me, CyclonMessage message) {
        // NEVER HAPPENS
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        if (this.isUp()) {
            this.startShuffle(node);
        }
    }

    @Override
    public int hash() {
        return 0;
    }

    // ===========================================
    // P R O T O C O L
    // ===========================================

    public void startShuffle(Node p) {
        if (this.isUp() && this.degree() > 0 && CommonState.getTime() > this.startShuffle) {
            this.increaseAge();
            final Node q = this.oldest();
            final FastCyclon Q = (FastCyclon) q.getProtocol(pid);
            final List<CyclonEntry> nodesToSend = this.getSample(l - 1, q);
            nodesToSend.add(me(p));
            final List<CyclonEntry> received = Q.receiveShuffle(q, p, nodesToSend);
            this.insertLists(p, q, received, nodesToSend);
        }
    }

    @Override
    public int degree() {
        int count = 0;
        for (CyclonEntry ce : this.cache) {
            final FastCyclon c = (FastCyclon) ce.n.getProtocol(pid);
            if (c.isUp()) {
                count++;
            }
        }
        return count;
    }


    public List<CyclonEntry> receiveShuffle(Node q, Node p, List<CyclonEntry> received) {
        final List<CyclonEntry> nodesToSend = this.getSample(l);
        this.insertLists(q, q, received, nodesToSend);
        return nodesToSend;
    }

    // ===========================================
    // S T A T I C  P R O T O C O L
    // ===========================================

    /**
     * @param s
     * @param i
     */
    public static void add(Node s, Node i) {
        FastCyclon subscriber = (FastCyclon) s.getProtocol(pid);
        FastCyclon introducer = (FastCyclon) i.getProtocol(pid);
        subscriber.cache.clear();

        subscriber.addNeighbor(i);

        final int RANDOM_WALKS = Math.min(size - 1, introducer.degree());

        //System.err.println("subscribe " + s.getID() + " to " + i.getID() + " with rw: " + RANDOM_WALKS +
        //    introducer.debug());

        //System.err.println("@" + i.getID() + " - " + introducer.debug());
        //System.err.println("Random walks:" + RANDOM_WALKS);
        for (int ii = 0; ii < RANDOM_WALKS && ii < introducer.degree(); ii++) {
            randomWalk(s, introducer.getNeighbor(ii), 5);
        }

    }

    /**
     * @param n
     */
    public static void removeFromNetwork(final Node n) {
        final FastCyclon cyclon = (FastCyclon) n.getProtocol(pid);
        cyclon.cache.clear();
    }

    /**
     * @param s
     * @param c
     * @param ttl
     */
    private static void randomWalk(final Node s, final Node c, int ttl) {
        ttl -= 1;
        final FastCyclon current = (FastCyclon) c.getProtocol(pid);
        final FastCyclon subscriber = (FastCyclon) s.getProtocol(pid);
        if (current.degree() > 0) {
            if (ttl > 0) {
                final Node next = current.getNeighbor(CommonState.r.nextInt(current.degree()));
                randomWalk(s, next, ttl);
            } else { // END OF RANDOM WALK: ADD
                //if (current.degree() >= size) {
                if (s.getID() != c.getID()) {
                    CyclonEntry ce = current.cache.remove(CommonState.r.nextInt(current.degree()));
                    subscriber.cache.add(ce);

                    // sanitize ...
                    if (subscriber.cache.size() > size) {
                        throw new RuntimeException("OVERFEED");
                    }
                    //}
                    current.addNeighbor(s);
                }
            }
        }
    }

    @Override
    public int callsInThisCycle() {
        return 0;
    }

    @Override
    public void clearCallsInCycle() {

    }
}
