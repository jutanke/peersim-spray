package example.paper.scamp;

import example.paper.Dynamic;
import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 4/9/15.
 */
public class FastScamp implements CDProtocol, Dynamic, Linkable, example.PeerSamplingService {

    private static final String PAR_C = "c";
    public static final String SCAMP_PROT = "0";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_LEASE_MAX = "leaseTimeoutMax";
    private static final String PAR_LEASE_MIN = "leaseTimeoutMin";


    // ===========================================
    // P R O P E R T I E S
    // ===========================================

    public List<Node> in;
    public List<Node> out;
    private View.ViewEntry current;

    private static final int FORWARD_TTL = 125;
    public static int pid;
    public static int tid;
    public static int c;
    public final int MAX_LEASE;
    public final int MIN_LEASE;
    private boolean isUp;
    private int interval;

    // ===========================================
    // C T O R
    // ===========================================

    public FastScamp(String n) {
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.c = Configuration.getInt(n + "." + PAR_C, 0);
        this.pid = Configuration.lookupPid(SCAMP_PROT);
        this.MAX_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MAX, 2000);
        this.MIN_LEASE = Configuration.getInt(n + "." + PAR_LEASE_MIN, 1000);
        this.in = new ArrayList<Node>();
        this.out = new ArrayList<Node>();
        this.interval = CommonState.r.nextInt(this.MAX_LEASE - this.MIN_LEASE) + this.MIN_LEASE;
    }

    @Override
    public Object clone() {
        FastScamp scamp = null;
        try {
            scamp = (FastScamp) super.clone();
            scamp.in = new ArrayList<Node>();
            scamp.out = new ArrayList<Node>();
            scamp.interval = CommonState.r.nextInt(this.MAX_LEASE - this.MIN_LEASE) + this.MIN_LEASE;
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
        if (this.isUp()) {
            this.leaseOthers();
            if (this.isTimedOut()) {
                lease(node);
            }
        }
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
        return this.out.get(i);
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
        final List<Node> result = new ArrayList<Node>(this.out);
        return result;
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

    private void leaseOthers() {
        outer: while (true) {
            for (int i = 0; i < this.out.size(); i++) {
                final FastScamp N = (FastScamp) this.out.get(i).getProtocol(pid);
                if (N.isTimedOut()) {
                    this.out.remove(i);
                    break outer;
                }
            }
            break;
        }
    }

    private boolean isTimedOut() {
        return CommonState.getTime() > 0 && CommonState.getTime() % this.interval == 0;
    }

    private boolean p() {
        return CommonState.r.nextDouble() < 1.0 / (1.0 + this.degree());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("out:[");
        for (Node n : this.out) {
            sb.append(n.getID());
            sb.append(" ");
        }
        sb.append("] in:");
        for (Node n : this.in) {
            sb.append(n.getID());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    // ===========================================
    // P R O T O C O L
    // ===========================================


    public static void lease(final Node n) {
        final FastScamp N = (FastScamp) n.getProtocol(pid);
        System.err.println("@" + n.getID() + " " + N.toString());
        N.in.clear();
        Node c = Network.get(CommonState.r.nextInt(Network.size()));
        if (N.degree() > 0) {
            c = N.out.get(CommonState.r.nextInt(N.degree()));
        }
        subscribe(n, c, true);
    }

    /**
     *
     * @param node
     */
    public static void unsubscribe(final Node node) {

        final FastScamp current = (FastScamp) node.getProtocol(pid);

        throw new RuntimeException("Not yet impl");

    }

    public static void subscribe(final Node s, final Node c) {
        subscribe(s,c,false);
    }

    /**
     *
     * @param s
     * @param c
     */
    public static void subscribe(final Node s, final Node c, final boolean isLease) {
        final FastScamp subscriber = (FastScamp) s.getProtocol(pid);
        subscriber.in.clear();
        subscriber.out.clear();
        final FastScamp contact = (FastScamp) c.getProtocol(pid);
        if (subscriber.isUp() && contact.isUp()) {
            subscriber.addNeighbor(c);
            for (Node n : contact.getPeers()) {
                forward(s, n, 0);
            }
            if (!isLease) {
                for (int i = 0; i < FastScamp.c && contact.degree() > 0; i++) {
                    Node n = contact.getNeighbor(CommonState.r.nextInt(contact.degree()));
                    forward(s, n, 0);
                }
            }
        } else {
            throw new RuntimeException("@sub:" + s.getID() + "  @con:" + c.getID());
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
        final FastScamp N = (FastScamp) node.getProtocol(pid);
        if (N.isUp()) {
            counter++;
            if (counter < FORWARD_TTL) {
                final FastScamp current = (FastScamp) node.getProtocol(pid);
                if (current.p() && node.getID() != s.getID() && !current.contains(s)) {
                    final FastScamp subscriber = (FastScamp) s.getProtocol(pid);
                    current.addNeighbor(s);
                    subscriber.in.add(node);
                    return true;
                } else if (current.degree() > 0) {
                    Node next = current.out.get(CommonState.r.nextInt(current.degree()));
                    return forward(s, next, counter);
                } else {
                    System.err.println("DEAD END for subscription " + s.getID() + " @" + node.getID());
                    return false;
                }
            } else {
                System.err.println("Forward for " + s.getID() + " timed out @" + node.getID());
                return false;
            }
        }
        return false;
    }


}
