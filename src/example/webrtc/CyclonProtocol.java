package example.webrtc;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julian on 22/01/15.
 */
public class CyclonProtocol implements CDProtocol, Linkable {

    // =============== static fields =======================================
    // =====================================================================

    /**
     * Cache size.
     *
     * @config
     */
    private static final String PAR_CACHE = "cache";

    /**
     * Shuffle size.
     *
     * @config
     */
    private static final String PAR_L = "l";

    private static int handshakeDuration = 1;
    private static int maxCacheSize;
    private static int l;

    // =================== fields ==========================================
    // =====================================================================

    /** Neighbors currently in the cache */
    private LinkedList<Neighbor> cache;

    /**
     *
     */
    private LinkedList<Neighbor> waitingForHandshake;

    private String prefix;

    public CyclonProtocol(String prefix) {
        this.prefix = prefix;
        maxCacheSize = Configuration.getInt(prefix + "." + PAR_CACHE);
        l = Configuration.getInt(prefix + "." + PAR_L);
        this.cache = new LinkedList<Neighbor>();
        this.waitingForHandshake = new LinkedList<Neighbor>();
    }

    @Override
    public Object clone() {
        CyclonProtocol c = null;
        try {
            c = (CyclonProtocol) super.clone();
        } catch (CloneNotSupportedException e) {}
        c.cache = new LinkedList<Neighbor>();
        return c;
    }

    // =============== public interface ====================================
    // =====================================================================


    @Override
    public void nextCycle(Node node, int protocolID) {
        this.simulateHandshake();

        Neighbor oldestPeer = this.increasePeerAgeAndRemoveOldest();
        if (oldestPeer == null) return;

        CyclonProtocol otherCyclonProt = (CyclonProtocol) (oldestPeer.node.getProtocol(protocolID));

        // 2. Select l - 1 other random neighbors.
        int numPeersToShuffle = Math.min(l - 1, cache.size());
        LinkedList<Neighbor> peersToShuffle = this.randomSplice(numPeersToShuffle);
        LinkedList<Neighbor> peersToSend = new LinkedList<Neighbor>();

        // Copy NodeWrappers into new list for shuffling
        for (Neighbor n : peersToShuffle) {
            peersToSend.add(n.clone());
        }
        peersToSend.add(new Neighbor(node));

        // 4. Send the updated subset to peer Q.
        LinkedList<Neighbor> responsePeers = otherCyclonProt.shuffle(oldestPeer.node.getID(),
                peersToSend);

        addShuffledPeers(node.getID(), responsePeers, peersToShuffle);
    }

    @Override
    public int degree() {
        return cache.size() + waitingForHandshake.size();
    }

    @Override
    public Node getNeighbor(int i) {
        return cache.get(i).node;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        if (degree() < maxCacheSize) {
            Neighbor n = new Neighbor(neighbour);
            waitingForHandshake.add(n);
        }
        return true;
    }

    @Override
    public boolean contains(Node neighbor) {
        for (Neighbor n : this.cache) {
            if (n.node == neighbor) return true;
        }
        for (Neighbor n : this.waitingForHandshake) {
            if (n.node == neighbor) return true;
        }
        return false;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {
        this.cache = null;
        this.waitingForHandshake = null;
    }

    // =============== private interface ===================================
    // =====================================================================

    public LinkedList<Neighbor> shuffle(long selfId, LinkedList<Neighbor> shufflePeers) {
        int numPeersToShuffle = Math.min(l, cache.size());
        LinkedList<Neighbor> peersToShuffle = randomSplice(numPeersToShuffle);
        LinkedList<Neighbor> peersToSend = new LinkedList<Neighbor>();

        // Create list for returning to calling peer.
        for (Neighbor n : peersToShuffle) {
            peersToSend.add(n.clone());
        }

        addShuffledPeers(selfId, shufflePeers, peersToShuffle);

        return peersToSend;
    }

    private void addShuffledPeers(long selfId, LinkedList<Neighbor> shufflePeers, LinkedList<Neighbor> splicedPeers) {

        while (shufflePeers.size() > 0) {
            Neighbor n = shufflePeers.pop();
            if (n.node.getID() == selfId) continue;
            if (!this.contains(n.node)) {
                waitingForHandshake.add(n);
            }
        }

        while(degree() < maxCacheSize && splicedPeers.size() > 0) {
            Neighbor n = splicedPeers.pop();
            if (!this.contains(n.node)) {
                waitingForHandshake.add(n);
            }
        }
    }

    private void simulateHandshake() {
        List<Neighbor> result = new ArrayList<Neighbor>();
        for (Neighbor n : this.waitingForHandshake) {
            if (n.waiting >= handshakeDuration) {
                result.add(n);
            } else {
                n.waiting += 1;
            }
        }
        for (Neighbor n : result) {
            this.waitingForHandshake.remove(n);
            this.cache.add(n);
        }

    }

    private LinkedList<Neighbor> randomSplice(int num) {
        LinkedList<Neighbor> randomNodes = new LinkedList<Neighbor>();
        Neighbor nw;

        while (num > 0) {
            nw = cache.remove(CommonState.r.nextInt(cache.size()));
            randomNodes.add(nw);
            num--;
        }

        return randomNodes;
    }

    private Neighbor increasePeerAgeAndRemoveOldest() {
        int maxAge = 0;
        int oldest = cache.size() - 1;
        if (cache.size() == 0) return null;
        for (int i = 0; i < cache.size(); i++) {
            cache.get(i).age++;
            if (cache.get(i).age > maxAge) {
                oldest = i;
                maxAge = cache.get(i).age;
            }
        }
        return cache.remove(oldest);
    }

    /**
     *
     */
    private class Neighbor {

        public Neighbor(Node p) {
            this.node = p;
            this.age = 0;
        }

        @Override
        public Neighbor clone(){
            Neighbor clone = new Neighbor(this.node);
            clone.age = this.age;
            clone.waiting = 0;
            return clone;
        }

        public int age;
        public int address;
        public Node node;
        public int waiting = 0;

    }
}
