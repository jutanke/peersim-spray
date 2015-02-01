package example.scamp;

import example.scamp.orig.messaging.*;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
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
    protected static int c;

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
        Scamp.c = Configuration.getInt(n + "." + PAR_C, 0);
        Scamp.indirTTL = Configuration.getInt(n + "." + PAR_INDIRTTL, -1);
        Scamp.leaseTimeoutMax = Configuration.getInt(n + "." + PAR_LEASE_MAX, -1);
        Scamp.leaseTimeoutMin = Configuration.getInt(n + "." + PAR_LEASE_MIN, -1);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        pid = Configuration.lookupPid(SCAMP_PROT);
        this.randomLeaseTimeout = CDState.r.nextLong(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        System.out.println("Lease:" + this.randomLeaseTimeout);
        System.out.println("indirTTL:" + indirTTL);
    }

    public Object clone() {
        ScampProtocol p = null;
        try {
            p = (ScampProtocol) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        p.randomLeaseTimeout = CDState.r.nextLong(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        System.out.println("Lease:" + p.randomLeaseTimeout);
        return p;
    }


    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    public void send(Node sender, Node destination, example.scamp.orig.messaging.ScampMessage m) {
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, destination, m, pid);
    }

    public abstract void join(Node me, Node subscriber);

    public abstract void rejoin(Node me);

    public abstract void unsubscribe(Node me);
}
