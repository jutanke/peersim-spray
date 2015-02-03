package example.scamp;

import example.scamp.messaging.*;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

/**
 * Created by julian on 01/02/15.
 */
public abstract class ScampProtocol implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService   {


    // =================== static fields ==================================
    // ====================================================================

    public static final boolean ____C_H_E_A_T_I_N_G____ = true;
    public static int CHEAT_COUNT = 0;

    /**
     * Parameter "c" of Scamp . Defaults to 0.
     *
     * @config
     */
    private static final String PAR_C = "c";

    private static final String SCAMP_PROT = "0";

    /**
     * Time-to-live for indirection. Defaults to -1.
     *
     * @config
     */
    private static final String PAR_INDIRTTL = "indirectionTTL";

    /**
     * Lease timeout. If negative, there is no lease mechanism. Defaults to -1.
     *
     * @config
     */
    private static final String PAR_LEASE_MAX = "leaseTimeoutMax";

    private static final String PAR_LEASE_MIN = "leaseTimeoutMin";

    private static final String PAR_TRANSPORT = "transport";

    /**
     * c
     */
    public static int c;

    /**
     * indirection TTL
     */
    protected static int indirTTL;

    /**
     * lease timeout
     */
    protected static long leaseTimeoutMin;
    protected static long leaseTimeoutMax;

    protected final int tid;

    public static int pid;

    public long randomLeaseTimeout;



    public ScampProtocol(String n) {
        ScampProtocol.c = Configuration.getInt(n + "." + PAR_C, 0);
        ScampProtocol.indirTTL = Configuration.getInt(n + "." + PAR_INDIRTTL, -1);
        ScampProtocol.leaseTimeoutMax = Configuration.getInt(n + "." + PAR_LEASE_MAX, -1);
        ScampProtocol.leaseTimeoutMin = Configuration.getInt(n + "." + PAR_LEASE_MIN, -1);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        pid = Configuration.lookupPid(SCAMP_PROT);

        System.err.println("min:" + leaseTimeoutMin + "  max:" + leaseTimeoutMax);

        this.randomLeaseTimeout = CDState.r.nextLong(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
    }

    public Object clone() {
        ScampProtocol p = null;
        try {
            p = (ScampProtocol) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        p.randomLeaseTimeout = CDState.r.nextLong(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        return p;
    }


    /**
     * for debugging
     * @param s
     */
    protected static void print(Object s) {
        if (false) {
            System.err.println(s);
        }
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    public static void doSubscribe(final Node n, ScampMessage forward) {
        ScampProtocol pp = (ScampProtocol) n.getProtocol(pid);
        pp.handleSubscription(n, forward);
    }

    public abstract void handleSubscription(Node n, ScampMessage m);

    public void send(Node sender, Node destination, example.scamp.messaging.ScampMessage m) {
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, destination, m, pid);
    }

    public static void subscribe(Node n, Node s) {
        ((ScampProtocol) n.getProtocol(pid)).join(n, s);
    }

    //public abstract void subDoSubscribe(Node acceptor, Node subscriber);

    public abstract void join(Node me, Node subscriber);

    public abstract void rejoin(Node me);

    public abstract void unsubscribe(Node me);

}
