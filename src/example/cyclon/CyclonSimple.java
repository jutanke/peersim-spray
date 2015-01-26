package example.cyclon;

import example.webrtc.cyclon2.Cyclon;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by julian on 26/01/15.
 */
public class CyclonSimple implements Linkable, EDProtocol, CDProtocol {

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

        switch (message.type) {
            case Shuffle:

                List<CyclonEntry> nodesToSend = selectNeighbors(l);

                break;
            case ShuffleResponse:
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

    private void merge(Node self, List<CyclonEntry> received, List<CyclonEntry> send) {
        received = delete(received, self);
        send = delete(send, self); // this should never happen but anyway..
        this.cache = discard(this.cache, send);
        received = discard(received, this.cache);

        // TODO GO ON

    }

    private List<CyclonEntry> discard(List<CyclonEntry> target, List<CyclonEntry> rem) {
        HashSet<Long> locals = new HashSet<Long>(rem.size());
        for (CyclonEntry r : rem)  {
            if (locals.contains(r.n.getID())) throw new RuntimeException("NOOOOPEE");
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

    private List<CyclonEntry> delete(List<CyclonEntry> list, Node n) {
        if (list.size() == 0) return list;
        List<CyclonEntry> newlist = new ArrayList<CyclonEntry>(list.size());
        for (CyclonEntry e : list) {
            if (e.n.getID() != n.getID()) {
                newlist.add(e);
            }
        }
        return newlist;
    }

    private void increaseAge() {
        for (CyclonEntry e : this.cache) {
            e.age += 1;
        }
    }

    private Node selectOldest() {
        CyclonEntry oldest = new CyclonEntry(Integer.MIN_VALUE, null);
        for (CyclonEntry e : this.cache) {
            if (oldest.age <= e.age) {
                oldest = e;
            }
        }
        return oldest.n;
    }

    private List<CyclonEntry> selectNeighbors(int l) {
        return selectNeighbors(l, -1);
    }

    private List<CyclonEntry> selectNeighbors(int l, long oldestNode) {
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

}
