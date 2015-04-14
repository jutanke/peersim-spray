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
    private boolean isUp;

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
    public int degree() {
        return this.out.size();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.out.array.get(i).node;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        if (this.out.contains(neighbour)) {
            return false;
        } else {
            this.out.add(neighbour);
            return true;
        }
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.out.contains(neighbor);
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public List<Node> getPeers() {
        return this.out.list();
    }

    @Override
    public String debug() {
        return "{in:" + this.in + ", out:" + this.out + "}";
    }

    @Override
    public int callsInThisCycle() {
        return 0;
    }

    @Override
    public void clearCallsInCycle() {

    }

    // ===========================================
    // P R I V A T E
    // ===========================================


    // ===========================================
    // P R O T O C O L
    // ===========================================

    /**
     *
     * @param node
     */
    public static void unsubscribe(final Node node) {

        final FastScamp current = (FastScamp) node.getProtocol(pid);


    }

    /**
     *
     * @param s
     * @param c
     */
    public static void subscribe(final Node s, final Node c) {
        final FastScamp subscriber = (FastScamp) s.getProtocol(pid);
        subscriber.in.clear();
        subscriber.out.clear();
        final FastScamp contact = (FastScamp) c.getProtocol(pid);
        int count = 0;

        if (subscriber.isUp() && contact.isUp()) {



        } else {
            throw new RuntimeException("@sub");
        }
    }

    /**
     *
     * @param s
     * @param node
     * @param counter
     * @return
     */
    public static boolean forward(final Node s, final Node node, int counter ){


        return false;
    }


}
