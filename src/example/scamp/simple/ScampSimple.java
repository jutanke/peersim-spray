package example.scamp.simple;

import example.scamp.Scamp;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 29/01/15.
 */
public class ScampSimple implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    // =================== static fields ==================================
    // ====================================================================


    /**
     * Parameter "c" of Scamp . Defaults to 0.
     *
     * @config
     */
    private static final String PAR_C = "c";

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
    private static final String PAR_LEASE = "leaseTimeout";

    protected static final String PAR_TRANSPORT = "transport";

    /**
     * c
     */
    private static int c;

    /**
     * indirection TTL
     */
    private static int indirTTL;

    /**
     * lease timeout
     */
    private static int leaseTimeout;

    protected final int tid;

    // =================== fields =========================================
    // ====================================================================


    private Map<Long, ScampEntry> inView;

    private Map<Long, ScampEntry> outView;

    /**
     *
     */
    private int birthDate;

    // ===================== initialization ================================
    // =====================================================================

    public ScampSimple(String n) {
        ScampSimple.c = Configuration.getInt(n + "." + PAR_C, 0);
        ScampSimple.indirTTL = Configuration.getInt(n + "." + PAR_INDIRTTL, -1);
        ScampSimple.leaseTimeout = Configuration.getInt(n + "." + PAR_LEASE, -1);
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        inView = new HashMap<Long, ScampEntry>();
        outView = new HashMap<Long, ScampEntry>();
        birthDate = CDState.getCycle();
    }

    @Override
    public Object clone(){
        ScampSimple scamp = null;
        try {
            scamp = (ScampSimple) super.clone();
        } catch (CloneNotSupportedException e) { }
        scamp.outView = new HashMap<Long, ScampEntry>();
        scamp.inView = new HashMap<Long, ScampEntry>();

        return scamp;
    }

    // =================== public =========================================
    // ====================================================================

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

        ScampMessage message = (ScampMessage) event;

        switch (message.type) {
            case Subscribe:
                this.subscriptionManagement(node, message.subscriber, pid);
                break;
            case Unsubscribe:
                break;
            case ForwardSubscription:
                handleForwardedSubscription(node, message.subscriber, pid);
                break;
        }

    }

    @Override
    public List<Node> getPeers() {
        List<Node> result = new ArrayList<Node>();
        for (ScampEntry n : this.outView.values()) {
            result.add(n.node);
        }
        return result;
    }

    // =================== internal =======================================
    // ====================================================================

    /**
     *
     * @param me
     * @param subscriber
     */
    private void subscriptionManagement(Node me, Node subscriber, final int pid) {
        for (ScampEntry e : this.outView.values()) {
            forwardSubscription(me, e.node, subscriber, pid);
        }
        for (int j = 0; j < c; j++) {
            Node n = randomNode();
            forwardSubscription(me, n, subscriber, pid);
        }
    }

    /**
     *
     * @param me
     * @param subscriber
     * @param pid
     */
    private void handleForwardedSubscription(Node me, Node subscriber, final int pid) {
        if (p() && !this.outView.containsKey(subscriber.getID())) {

            // add s into the local outview
            // add myself into s`s inview

        } else {
            Node n = randomNode();
            forwardSubscription(me, n, subscriber, pid);
        }
    }

    /**
     * probability of acceptance
     * @return
     */
    private boolean p() {
        return CDState.r.nextDouble() < 1.0 / 1.0 + this.degree();
    }

    /**
     * forward the subscription
     * @param sender
     * @param receiver
     * @param subscriber
     * @param pid
     */
    private void forwardSubscription(Node sender, Node receiver, Node subscriber, final int pid) {
        if (receiver.getID() != subscriber.getID()) {
            ScampMessage message = new ScampMessage(sender, ScampMessage.Type.ForwardSubscription, subscriber);
            Transport tr = (Transport) sender.getProtocol(tid);
            tr.send(sender, receiver, message, pid);
        } else {
            System.err.println("CANNOT FORWARD OWN SUBSCRIPTION!");
        }
    }

    /**
     * wow, that`s ugly...
     * @return
     */
    private Node randomNode() {
        if (this.outView.size() > 0) {
            int pos = CDState.r.nextInt(this.degree());
            int i = 0;
            for (ScampEntry e : this.outView.values()) {
                if (i == pos) {
                    return e.node;
                }
                i++;
            }
        }
        return null;
    }


}
