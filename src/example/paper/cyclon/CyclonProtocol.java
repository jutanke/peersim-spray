package example.paper.cyclon;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 3/23/15.
 */
public abstract class CyclonProtocol implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    private static final String PAR_CACHE = "cache";
    private static final String PAR_L = "l";
    private static final String PAR_PROT = "lnk";
    private static final String PAR_TRANSPORT = "transport";

    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    protected final int size;
    protected final int l;
    protected final int tid,pid;
    protected List<CyclonEntry> cache;

    // ===========================================
    // C T O R
    // ===========================================

    public CyclonProtocol(String n) {
        this.size = Configuration.getInt(n + "." + PAR_CACHE);
        this.l = Configuration.getInt(n + "." + PAR_L);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.pid = Configuration.lookupPid(PAR_PROT);
        this.cache = new ArrayList<CyclonEntry>(this.size);
    }

    @Override
    public Object clone() {
        try {
            CyclonProtocol p = (CyclonProtocol) super.clone();
            p.cache = new ArrayList<CyclonEntry>(this.size);
            return p;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        CyclonMessage message = (CyclonMessage) event;
        this.processMessage(node, message);
    }

    public abstract void processMessage(Node me, CyclonMessage message);

    // ===========================================
    // P U B L I C
    // ===========================================

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
        return this.insert(new CyclonEntry(0, neighbour));
    }

    @Override
    public boolean contains(Node neighbor) {
        for (CyclonEntry ce : this.cache) {
            if (ce.n.getID() == neighbor.getID()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String debug() {
        StringBuilder sb = new StringBuilder();
        Collections.sort(this.cache, new CyclonEntry());
        for (CyclonEntry ce : this.cache) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ce);
        }
        return sb.toString();
    }

    @Override
    public List<Node> getPeers() {
        final List<Node> peers = new ArrayList<Node>(size);
        for (CyclonEntry ce : this.cache) {
            peers.add(ce.n);
        }
        return peers;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    // ===========================================
    // C H I L D R E N
    // ===========================================

    protected void increaseAge() {
        for(CyclonEntry ce : this.cache) {
            ce.age += 1;
        }
    }

    protected static List<CyclonEntry> remove(List<CyclonEntry> list, CyclonEntry entry) {
        List<CyclonEntry> result = new ArrayList<CyclonEntry>();
        for (CyclonEntry ce : list) {
            if (ce.n.getID() != entry.n.getID()) {
                result.add(ce);
            }
        }
        return result;
    }

    protected static List<CyclonEntry> sieveOut(List<CyclonEntry> list, List<CyclonEntry> sieve) {
        HashSet<Long> lookup = new HashSet<Long>();
        for (CyclonEntry ce : sieve) {
            lookup.add(ce.n.getID());
        }
        List<CyclonEntry> result = new ArrayList<CyclonEntry>();
        for (CyclonEntry ce : list) {
            if (!lookup.contains(ce.n.getID())) {
                result.add(ce);
            }
        }
        return result;
    }

    protected List<CyclonEntry> getSample(int l) {
        final List<CyclonEntry> subset = null;
        final int max = Math.min(l, this.cache.size());
        final LinkedList<CyclonEntry> temp = new LinkedList<CyclonEntry>(this.cache);
        for (int i = 0; i < max; i++) {
            subset.add(temp.remove(CommonState.r.nextInt(temp.size())).cyclonCopy());
        }
        return subset;
    }

    protected Node oldest() {
        if (this.cache.size() > 0) {
            CyclonEntry oldest = null;
            for (CyclonEntry ce : this.cache) {
                if (oldest == null || oldest.age < ce.age) {
                    oldest = ce;
                }
            }
            return oldest.n;
        } else {
            return null;
        }
    }

    protected void send(Node destination, CyclonMessage message) {
        final Node sender = message.sender;
        if (sender.getID() == destination.getID()) {
            throw new RuntimeException("must not send to oneself");
        }
        Transport tr = (Transport) sender.getProtocol(this.tid);
        tr.send(sender, destination, message, pid);
    }

    protected boolean insert(CyclonEntry ce) {
        if (!this.contains(ce.n) && this.cache.size() < this.size) {
            this.cache.add(ce);
            return true;
        }
        return false;
    }

}
