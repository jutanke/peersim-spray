package example.paper.scamp;

import example.paper.Dynamic;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;

import java.util.List;

/**
 * Created by julian on 4/9/15.
 */
public class FastScamp implements CDProtocol, Dynamic, Linkable, example.PeerSamplingService {

    private static final String PAR_C = "c";
    private static final String SCAMP_PROT = "0";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_LEASE_MAX = "leaseTimeoutMax";
    private static final String PAR_LEASE_MIN = "leaseTimeoutMin";


    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    public View in;
    public View out;
    private View.ViewEntry current;

    public static int pid;
    public static int tid;
    public static int c;
    public final int MAX_LEASE;
    public final int MIN_LEASE;

    // ===========================================
    // C T O R
    // ===========================================

    public FastScamp(String n) {
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.c = Configuration.getInt(n + "." + PAR_C, 0);
        this.pid = Configuration.lookupPid(SCAMP_PROT);
        this.MAX_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MAX, 2000);
        this.MIN_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MIN, 1000);
        this.in = new View(this.MIN_LEASE, this.MAX_LEASE);
        this.out = new View(this.MIN_LEASE, this.MAX_LEASE);
    }

    @Override
    public Object clone() {
        FastScamp scamp = null;
        try {
            scamp = (FastScamp) super.clone();
            scamp.in = new View(this.MIN_LEASE, this.MAX_LEASE);
            scamp.out = new View(this.MIN_LEASE, this.MAX_LEASE);
        } catch (CloneNotSupportedException e) {
        } // never happens
        // ...
        return scamp;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public boolean isUp() {
        return false;
    }

    @Override
    public void up() {

    }

    @Override
    public void down() {

    }

    @Override
    public int hash() {
        return 0;
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
