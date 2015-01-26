package example.cyclon;

import example.webrtc.cyclon2.Cyclon;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 26/01/15.
 */
public class CyclonSimple implements Linkable, EDProtocol, CDProtocol, PeerSamplingService {

    private static final String PAR_CACHE = "cache";
    private static final String PAR_L = "l";
    private static final String PAR_TRANSPORT = "transport";

    // ======================================================================
    // P R O P E R T I E S
    // ======================================================================

    private final int size;
    private final int l;
    private final int tid;

    private List<CyclonEntry> cache = null;

    public CyclonSimple(int size, int l){
        this.size = size;
        this.l = l;
        this.tid = -1;

    }

    public CyclonSimple(String n) {
        this.size = Configuration.getInt(n + "." + PAR_CACHE);
        this.l = Configuration.getInt(n + "." + PAR_L);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

        cache = new ArrayList<CyclonEntry>(size);
    }

    public Object clone() {
        CyclonSimple cyclon = null;
        try {
            cyclon = (CyclonSimple) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        cyclon.cache = new ArrayList<CyclonEntry>();

        return cyclon;
    }

    // ======================================================================
    // P U B L I C  I N T E R F A C E
    // ======================================================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        // START SHUFFLE
        if (this.cache.size() > 0) {

            increaseAge();

            Node q = selectOldest();

            List<CyclonEntry> nodesToSend = selectNeighbors(l - 1, q.getID());
            nodesToSend.add(new CyclonEntry(0, node));

            CyclonMessage message = new CyclonMessage(node, CyclonMessage.Type.Shuffle, nodesToSend, null);
            Transport tr = (Transport) node.getProtocol(tid);
            tr.send(node, q, message, protocolID);
        }

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        CyclonMessage message = (CyclonMessage) event;

        List<CyclonEntry> received = null;
        List<CyclonEntry> nodesToSend = null;

        switch (message.type) {
            case Shuffle:

                Node p = message.sender;
                ;
                received = message.list;
                nodesToSend = selectNeighbors(l);
                this.cache = merge(node, this.cache, clone(received), clone(nodesToSend));
                CyclonMessage out = new CyclonMessage(node, CyclonMessage.Type.ShuffleResponse, nodesToSend, received);
                Transport tr = (Transport) node.getProtocol(tid);
                tr.send(node, p, message, pid);

                break;
            case ShuffleResponse:

                received = message.list;
                nodesToSend = message.temp;
                this.cache = merge(node, this.cache, clone(received), clone(nodesToSend));

                break;
        }
    }

    @Override
    public int degree() {
        return this.cache.size();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.cache.get(i).n;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        if (contains(neighbour)) return false;
        if (cache.size() >= size) return false;
        CyclonEntry e = new CyclonEntry(0, neighbour);
        cache.add(e);
        return true;
    }

    @Override
    public boolean contains(Node neighbor) {
        for (CyclonEntry e : this.cache) {
            if (e.n.getID() == neighbor.getID()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    // ======================================================================
    // P R I V A T E  I N T E R F A C E
    // ======================================================================

    private List<CyclonEntry> clone(List<CyclonEntry> list) {
        return new ArrayList<CyclonEntry>(list);
    }

    public List<CyclonEntry> merge(Node self, List<CyclonEntry> cache, List<CyclonEntry> received, List<CyclonEntry> sent) {

        System.err.println("ME:" + self +
                " rec:" + printList(received) +
                " sen:" + printList(sent
        ));

        System.err.println("CACHE:" + cache);

        received = delete(received, self);
        sent = delete(sent, self);
        received = discard(received, cache);
        cache = discard(cache, sent);

        int include = size - cache.size();
        if (include < received.size()) throw new Error("why?");

        this.cache.addAll(received);

        Collections.sort(sent, new CyclonEntry());

        while (cache.size() < size && sent.size() > 0) {
            cache.add(popSmallest(sent));
        }
        return cache;
    }

    private String printList(Collection<CyclonEntry> list) {
        StringBuilder sb = new StringBuilder();
        for (CyclonEntry e : list) {
            sb.append(e);
        }
        return sb.toString();
    }

    public CyclonEntry popSmallest(List<CyclonEntry> list) {
        CyclonEntry smallest = new CyclonEntry(Integer.MAX_VALUE, null);
        for (CyclonEntry e : list) {
            if (smallest.age > e.age) {
                smallest = e;
            }
        }
        list.remove(smallest);
        return smallest;
    }

    public List<CyclonEntry> discard(List<CyclonEntry> target, List<CyclonEntry> rem) {
        HashSet<Long> locals = new HashSet<Long>(rem.size());
        for (CyclonEntry r : rem) {
            if (locals.contains(r.n.getID())) {
                System.err.println("SAD: " + locals);
                throw new RuntimeException("NOOOOPEE " + r.n.getID());
            }
            locals.add(r.n.getID());
        }
        List<CyclonEntry> result = new ArrayList<CyclonEntry>();
        for (CyclonEntry e : target) {
            if (!locals.contains(e.n.getID())) {
                result.add(e);
            }
        }
        return result;
    }

    public List<CyclonEntry> delete(List<CyclonEntry> list, Node n) {
        if (list.size() == 0) return list;
        List<CyclonEntry> newlist = new ArrayList<CyclonEntry>(list.size());
        for (CyclonEntry e : list) {
            if (e.n.getID() != n.getID()) {
                newlist.add(e);
            }
        }
        return newlist;
    }

    public void increaseAge() {
        for (CyclonEntry e : this.cache) {
            e.age += 1;
        }
    }

    public Node selectOldest() {
        CyclonEntry oldest = new CyclonEntry(Integer.MIN_VALUE, null);
        for (CyclonEntry e : this.cache) {
            if (oldest.age <= e.age) {
                oldest = e;
            }
        }
        return oldest.n;
    }

    public List<CyclonEntry> selectNeighbors(int l) {
        return selectNeighbors(l, -1);
    }

    public List<CyclonEntry> selectNeighbors(int l, long oldestNode) {
        List<CyclonEntry> result = new ArrayList<CyclonEntry>();
        int dim = Math.min(l, cache.size() - 1);
        List<CyclonEntry> shallowCopy = new ArrayList<CyclonEntry>(this.cache);
        int i = 0;
        while (i < dim && shallowCopy.size() > 0) {
            CyclonEntry ce = cache.remove(CommonState.r.nextInt(cache.size() - 1));
            if (ce.n.getID() != oldestNode) {
                result.add(ce);
                i += 1;
            }
        }
        return result;
    }

    @Override
    public List<Node> getPeers() {
        List<Node> result = new ArrayList<Node>();
        for (CyclonEntry e : this.cache) {
            result.add(e.n);
        }
        return result;
    }
}
