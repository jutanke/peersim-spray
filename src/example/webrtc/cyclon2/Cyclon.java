package example.webrtc.cyclon2;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created by julian on 25/01/15.
 */
public class Cyclon implements Linkable, EDProtocol, CDProtocol {

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

    public Cyclon(String n) {
        this.size = Configuration.getInt(n + "." + PAR_CACHE);
        this.l = Configuration.getInt(n + "." + PAR_L);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);

        cache = new ArrayList<CyclonEntry>(size);
    }

    public Object clone() {
        Cyclon cyclon = null;
        try {
            cyclon = (Cyclon) super.clone();
        } catch (CloneNotSupportedException e) {
        } // never happens
        cyclon.cache = new ArrayList<CyclonEntry>();

        return cyclon;
    }


    // ======================================================================
    // P U B L I C  I N T E R F A C E
    // ======================================================================

    /**
     * A protocol which is defined by performing an algorithm in more or less
     * regular periodic intervals.
     * This method is called by the simulator engine once in each cycle with
     * the appropriate parameters.
     *
     * @param node       the node on which this component is run
     * @param protocolID
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        // 1. Increase by one the age of all neighbors.
        increaseAgeAndSort();

        // 2. Select neighbor Q with the highest age among all neighbors...
        Node q = selectNeighbor();
        if (q == null) {
            System.err.println("No Peer");
            return;
        }
        //    and l − 1 other random neighbors.
        List<CyclonEntry> nodesToSend = selectNeighbors(l - 1);

        // 3. Replace Q’s entry with a new entry of age 0 and with P’s address.
        cache.set(cache.size() - 1, new CyclonEntry(node, 0));

        // 4. Send the updated subset to peer Q.
        CyclonMessage message = new CyclonMessage(node, nodesToSend, true, null);
        Transport tr = (Transport) node.getProtocol(tid);
        tr.send(node, q, message, protocolID);
    }

    /**
     * This method is invoked by the scheduler to deliver events to the
     * protocol. Apart from the event object, information about the node
     * and the protocol identifier are also provided. Additional information
     * can be accessed through the {@link CommonState} class.
     *
     * @param node  the local node
     * @param pid   the identifier of this protocol
     * @param event the delivered event
     */
    @Override
    public void processEvent(Node node, int pid, Object event) {
        CyclonMessage message = (CyclonMessage) event;

        HashSet<Long> ids = new HashSet<Long>();
        for (CyclonEntry e : this.cache) {
            if (ids.contains(e.n.getID())) throw new RuntimeException("DOUBLE FUCKING DATA");
            ids.add(e.n.getID());
        }

        List<CyclonEntry> nodesToSend = null;
        if (message.isResuest) {
            nodesToSend = selectNeighbors(message.list.size() + 1);

            CyclonMessage msg = new CyclonMessage(node, nodesToSend, false, message.list);
            Transport tr = (Transport) node.getProtocol(tid);
            tr.send(node, message.node, msg, pid);
        } else
            nodesToSend = message.receivedList;

        // 5. Discard entries pointing to P, and entries that are already in P’s cache.
        List<CyclonEntry> list = discardEntries(node, message.list);

        // 6. Update P’s cache to include all remaining entries, by firstly using empty
        //    cache slots (if any), and secondly replacing entries among the ones originally
        //    sent to Q.
        //insertReceivedItems(message.list, nodesToSend);
        insertReceivedItems(list, nodesToSend);
    }

    /**
     * Returns the size of the neighbor list.
     */
    @Override
    public int degree() {
        return cache.size();
    }

    /**
     * Returns the neighbor with the given index. The contract is that
     * listing the elements from index 0 to index degree()-1 should list
     * each element exactly once if this object is not modified in the
     * meantime. It throws an IndexOutOfBounds exception if i is negative
     * or larger than or equal to {@link #degree}.
     *
     * @param i
     */
    @Override
    public Node getNeighbor(int i) {
        return cache.get(i).n;
    }

    private static final List<Node> neighbors = new ArrayList<Node>();

    public List<Node> getNeighbors() {
        neighbors.clear();
        for (CyclonEntry e : this.cache) {
            neighbors.add(e.n);
        }
        return neighbors;
    }

    /**
     * Add a neighbor to the current set of neighbors. If neighbor
     * is not yet a neighbor but it cannot be added from other reasons,
     * this method should not return normally, that is, it must throw
     * a runtime exception.
     *
     * @param neighbour
     * @return true if the neighbor has been inserted; false if the
     * node is already a neighbor of this node
     */
    @Override
    public boolean addNeighbor(Node neighbour) {
        if (contains(neighbour))
            return false;

        if (cache.size() >= size)
            return false;

        CyclonEntry ce = new CyclonEntry(neighbour, 0);
        cache.add(ce);

        return true;
    }

    /**
     * Returns true if the given node is a member of the neighbor set.
     *
     * @param neighbor
     */
    @Override
    public boolean contains(Node neighbor) {
        for (CyclonEntry ne : cache)
            if (ne.n.equals(neighbor))
                return true;

        return false;
    }

    /**
     * A possibility for optimization. An implementation should try to
     * compress its internal representation. Normally this is called
     * by initializers or other components when
     * no increase in the expected size of the neighborhood can be
     * expected.
     */
    @Override
    public void pack() {

    }

    /**
     * Performs cleanup when removed from the network. This is called by the
     * host node when its fail state is set to {Fallible}.
     * It is very important that after calling this method, NONE of the methods
     * of the implementing object are guaranteed to work any longer.
     * They might throw arbitrary exceptions, etc. The idea is that after
     * calling this, typically no one should access this object.
     * However, as a recommendation, at least toString should be guaranteed to
     * execute normally, to aid debugging.
     */
    @Override
    public void onKill() {

    }

    // ======================================================================
    // P R I V A T E  I N T E R F A C E
    // ======================================================================

    private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list) {
        List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
        for (CyclonEntry ce : list)
            if (!ce.n.equals(n) && contains(ce.n))
                newList.add(ce);

        return newList;
    }

    private void increaseAgeAndSort() {
        for (CyclonEntry ce : cache)
            ce.increase();

        Collections.sort(cache, new CyclonEntry());
    }

    private Node selectNeighbor() {
        try {
            return cache.get(cache.size() - 1).n;
        } catch (Exception e) {
            return null;
        }
    }

    private List<CyclonEntry> selectNeighbors(int l) {
        int dim = Math.min(l, cache.size() - 1);
        List<CyclonEntry> list = new ArrayList<CyclonEntry>(l);

        for (int i = 0; i < dim; i++) {
            CyclonEntry ce = cache.remove(CommonState.r.nextInt(cache.size() - 1));
            list.add(ce);
        }

        return list;
    }

        /*private List<CyclonEntry> discardEntries(Node n, List<CyclonEntry> list)
        {
                List<CyclonEntry> newList = new ArrayList<CyclonEntry>();
                for (CyclonEntry ce : list)
                        if (!ce.n.equals(n) && contains(ce.n))
                                newList.add(ce);

                return newList;
        }*/

    private int indexOf(CyclonEntry ce) {
        for (int i = 0; i < cache.size(); i++)
            if (cache.get(i).n.equals(ce.n))
                return i;

        return -1;
    }

    private void insertItems(List<CyclonEntry> list) {
        int pos = 0;

        for (CyclonEntry ce : list)
            if ((pos = indexOf(ce)) < 0)
                cache.add(new CyclonEntry(ce.n, ce.age));
            else {
                CyclonEntry lce = cache.get(pos);
                if (lce.age > ce.age)
                    cache.set(pos, new CyclonEntry(ce.n, ce.age));
            }
    }

    private void insertReceivedItems(List<CyclonEntry> list, List<CyclonEntry> sList) {
        //Add received items to the cache
        insertItems(list);

        if (cache.size() >= size)
            return;

        //I've received less data that one I've sent, so
        //reuse some sent entries
        int cacheSize = cache.size();
        int sListSize = sList.size();
        for (int i = 0; i < Math.min(size - cacheSize, sListSize); i++) {
            int pos = CommonState.r.nextInt(sList.size());
            CyclonEntry ce = sList.remove(pos);
            cache.add(ce);
        }
    }
}
