package example.webrtc.cyclon;

import example.webrtc.PeerSamplingService;
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
public class CyclonProtocol implements CDProtocol, Linkable, PeerSamplingService {

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

    public List<Integer> activeCache;

    private String prefix;

    public CyclonProtocol(String prefix) {
        this.prefix = prefix;
        maxCacheSize = Configuration.getInt(prefix + "." + PAR_CACHE);
        l = Configuration.getInt(prefix + "." + PAR_L);
        this.cache = new LinkedList<Neighbor>();
        this.waitingForHandshake = new LinkedList<Neighbor>();
        this.activeCache = new ArrayList<Integer>();
    }

    @Override
    public Object clone() {
        CyclonProtocol c = null;
        try {
            c = (CyclonProtocol) super.clone();
        } catch (CloneNotSupportedException e) {}
        c.cache = new LinkedList<Neighbor>();
        c.waitingForHandshake = new LinkedList<Neighbor>();
        c.activeCache = new ArrayList<Integer>();
        return c;
    }

    // =============== public interface ====================================
    // =====================================================================




    @Override
    public void nextCycle(Node node, int protocolID) {
        this.simulateHandshake();

        this.activeCache.clear();
        for (Neighbor n : this.cache) {
            this.activeCache.add(n.node.getIndex());
        }

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
        return this.activeCache.size();
    }

    @Override
    public Node getNeighbor(int i) {
        return cache.get(i).node;
    }

    public boolean lonely() {
        return this.activeCache.size() == 0 &&
                this.cache.size() == 0 &&
                this.waitingForHandshake.size() == 0;
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

    @Override
    public long id() {
        return 0;
    }

    @Override
    public List<PeerSamplingService> getPeers() {
        List<PeerSamplingService> result = new ArrayList<PeerSamplingService>();


        return result;
    }

    public String printCache(int id) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(id);
        sb.append(")");
        for(Neighbor n : this.cache) {
            sb.append(n);
            sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
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
                cache.add(n);
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
            //System.out.println(n + " vs " + handshakeDuration);
        }
        //System.out.println()
        for (Neighbor n : result) {
            this.waitingForHandshake.remove(n);
            if (this.cache.size() >= maxCacheSize) {
                int diff = this.cache.size() - maxCacheSize + 1;
                Neighbor oldest = null;
                int oldestAge = -1;
                for (int i = 0; i < diff; i++) {
                    for(Neighbor n2 : this.cache) {
                        if (n2.age > oldestAge) {
                            oldest = n2;
                            oldestAge = n2.age;
                        }
                    }
                    this.cache.remove(oldest);
                }
            }
            this.cache.add(n);
        }

        /*
        System.out.println("+++++++++++++++");
        for (Neighbor n : this.cache) {
            System.out.println("n:" + n);
        }
        System.out.println("===============");
        */

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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(node.getIndex());
            sb.append(",");
            sb.append(age);
            sb.append(",");
            sb.append(waiting);
            sb.append(")");
            return sb.toString();
        }

    }
}
