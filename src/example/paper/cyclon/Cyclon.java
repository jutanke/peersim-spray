package example.paper.cyclon;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 3/14/15.
 */
public class Cyclon implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    private static final String PAR_CACHE = "cache";
    private static final String PAR_L = "l";
    private static final String PAR_TRANSPORT = "transport";

    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    private final int size;
    private final int l;
    private final int tid;

    private List<CyclonEntry> cache = null;

    // ===========================================
    // C T O R
    // ===========================================

    public Cyclon(String n) {
        this.size = Configuration.getInt(n + "." + PAR_CACHE);
        this.l = Configuration.getInt(n + "." + PAR_L);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.cache = new ArrayList<CyclonEntry>(size);
    }

    @Override
    public Object clone() {
        Cyclon cyclon = null;
        try {
            cyclon = (Cyclon) super.clone();
            cyclon.cache = new ArrayList<CyclonEntry>(size);
        }
        catch (CloneNotSupportedException e) {} // never happens
        // ...
        return cyclon;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public int degree() {
        return 0;
    }

    @Override
    public Node getNeighbor(int i) {
        return null;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return false;
    }

    @Override
    public boolean contains(Node neighbor) {
        return false;
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

    }

    @Override
    public List<Node> getPeers() {
        return null;
    }

    @Override
    public String debug() {
        return null;
    }

    // ===========================================
    // P R I V A T E
    // ===========================================
}
