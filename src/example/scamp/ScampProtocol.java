package example.scamp;

import example.scamp.simple.ScampMessage;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

import javax.sql.rowset.spi.SyncProvider;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by julian on 31/01/15.
 */
public abstract class ScampProtocol implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

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
    protected static int leaseTimeoutMin;
    protected static int leaseTimeoutMax;

    protected final int tid;

    protected final int pid;

    /**
     *
     */
    public int age;

    //protected Map<Long, Node> inView;
    //protected Map<Long, Node> outView;

    public View inView;
    public View partialView;

    public int randomLeaseTimeout;

    private final List<Node> deleteListA; // can be shared!
    private final List<Node> deleteListB; // can be shared!

    //private List<Node> outViewList;
    //private List<Node> inViewList;


    public ScampProtocol(String n) {
        ScampProtocol.c = Configuration.getInt(n + "." + PAR_C, 0);
        ScampProtocol.indirTTL = Configuration.getInt(n + "." + PAR_INDIRTTL, -1);
        ScampProtocol.leaseTimeoutMax = Configuration.getInt(n + "." + PAR_LEASE_MAX, -1);
        ScampProtocol.leaseTimeoutMin = Configuration.getInt(n + "." + PAR_LEASE_MIN, -1);
        this.deleteListA = new ArrayList<Node>();
        this.deleteListB = new ArrayList<Node>();
        this.tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        this.pid = Configuration.lookupPid(SCAMP_PROT);
        inView = new View();
        partialView = new View();
        age = 0;
        this.randomLeaseTimeout = CDState.r.nextInt(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        System.out.println("Lease:" + this.randomLeaseTimeout);
    }

    public Object clone() {
        ScampProtocol p = null;
        try {
            p = (ScampProtocol) super.clone();
        } catch (CloneNotSupportedException e) {

        }
        p.partialView = new View();
        p.inView = new View();
        p.randomLeaseTimeout = CDState.r.nextInt(leaseTimeoutMax - leaseTimeoutMin) + leaseTimeoutMin;
        System.out.println("Lease:" + p.randomLeaseTimeout);
        return p;
    }

    /*
     * P U B L I C  I N T E R F A C E
     */

    @Override
    public int degree() {
        return this.partialView.length();
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.partialView.contains(neighbor);
    }

    @Override
    public void pack() {

    }

    @Override
    public void onKill() {

    }

    @Override
    public String debug() {
        return this.toString();
    }

    @Override
    public boolean addNeighbor(Node n) {
        return this.addToOutView(n);
    }

    @Override
    public Node getNeighbor(int i) {
        return this.partialView.list().get(i);
    }

    @Override
    public List<Node> getPeers() {
        return this.partialView.list();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("In: [");
        sb.append(this.inView);
        sb.append("], Out: [");
        sb.append(this.partialView);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public void nextCycle(Node node, int protocolID) {

        this.age += 1;

        // remove all expired nodes from our partial view

        this.deleteListA.clear();
        this.deleteListB.clear();
        for (Node n : this.partialView.list()) {
            ScampProtocol scamp = (ScampProtocol) n.getProtocol(pid);
            if (scamp.isExpired()) {
                this.deleteListA.add(n);
                System.err.println("@" + node.getID() + " delete from partialview: " + n.getID());
            }
        }

        for (Node n : this.inView.list()) {
            ScampProtocol scamp = (ScampProtocol) n.getProtocol(pid);
            if (scamp.isExpired()) {
                this.deleteListB.add(n);
                System.err.println("@" + node.getID() + " delete from inview: " + n.getID());
            }
        }

        for (Node n : this.deleteListA) {
            this.partialView.del(n);
        }
        for (Node n : this.deleteListB) {
            this.inView.del(n);
        }
        this.deleteListA.clear();
        this.deleteListB.clear();
        // lease (re-subscription)
        if (this.isExpired()) {
            System.err.println( node.getID() + " is expired!");
            this.lease(node);
        }

        this.updateWeights(node);

        subNextCycle(node, protocolID);
    }

    protected abstract void lease(Node n);

    @Override
    public void processEvent(Node node, int pid, Object event) {

        ScampMessage message = (ScampMessage) event;

        switch (message.type) {
            case WeightUpdate:
                if (message.updateInView) {
                    this.partialView.updateWeight(message.sender, message.weight);
                } else {
                    this.inView.updateWeight(message.sender, message.weight);
                }
                break;
            case GiveContact:
                this.join(node, message.contact);
                break;
            case AcceptedSubscription:
                if (this.inView.contains(message.sender)) {
                    System.out.println("@" + node.getID()+":must not happen.." +
                        message.sender.getID() + " -> " + this.inView);
                }
                this.addToInView(message.sender);
                break;
            default:
                subProcessEvent(node, pid, message);
        }

    }

    protected abstract void subNextCycle(Node node, int protocolID);

    protected abstract void subProcessEvent(Node node, int pid, ScampMessage message);

    /*
     * I N T E R N A L  I N T E R F A C E
     */

    protected void updateWeights(Node me) {

        // ++ INVIEW ++
        for (View.ViewEntry n : this.inView.normalizeWeights()) {
            ScampMessage m = ScampMessage.updateWeightMessageInView(me, n.weight);
            this.send(me, n.node, m);
        }

        // ++ PARTIALVIEW ++
        for (View.ViewEntry n : this.partialView.normalizeWeights()) {
            ScampMessage m = ScampMessage.updateWeightMessagePartialView(me, n.weight);
            this.send(me, n.node, m);
        }

    }

    protected void send(Node me, Node destination, ScampMessage m) {
        Transport tr = (Transport) me.getProtocol(tid);
        tr.send(me, destination, m, pid);
    }

    public boolean isExpired() {
        return (this.age >= randomLeaseTimeout);
    }

    protected boolean addToOutView(Node n) {
        if (this.partialView.contains(n)) {
            return false;
        } else {
            this.partialView.add(n);
            return true;
        }
    }

    protected boolean addToInView(Node n) {
        if (this.inView.contains(n)) {
            return false;
        } else {
            this.inView.add(n);
            return true;
        }
    }

    protected boolean p() {
        return CDState.r.nextDouble() < 1.0 / 1.0 + this.degree();
    }

    protected Node randomOutNode() {
        if (degree() > 0) {
            List<Node> out = this.partialView.list();
            return out.get(CDState.r.nextInt(out.size()));
        }
        return null;
    }

    public Node randomOutNode(Node filter) {
        if (this.partialView.contains(filter)) {
            if (this.partialView.length() > 1) {
                List<Node> out = this.partialView.list(filter);
                return out.get(CDState.r.nextInt(out.size()));
            } else {
                return null;
            }
        }
        return this.randomOutNode();
    }

    protected Node randomInNode() {
        if (degree() > 0) {
            List<Node> in = this.inView.list();
            return in.get(CDState.r.nextInt(in.size()));
        }
        return null;
    }

    public List<Node> pred() {
        return this.inView.list();
    }

    public List<Node> succ() {
        return this.partialView.list();
    }

    /**
     * remove the node from the network
     */
    public void unsubscribe() {
        int l = this.partialView.length();
        int ll = this.inView.length();
        int i = 0;
        if (l > 0) {

        }

    }

    /**
     * this is the first step to enter a network
     *
     * @param contact
     */
    public void join(Node me, Node contact) {
        //this.birthDate = CDState.getCycle();
        this.age = 0;

        if (contact != null) {

            System.err.println("@" + me.getID() + " join -> " + contact.getID());

            this.inView.clear();
            this.partialView.clear();

            this.acceptSubscription(me, contact);

            //this.addNeighbor(contact);
            ScampMessage message = new ScampMessage(me, ScampMessage.Type.Subscribe, me);


            //Transport tr = (Transport) me.getProtocol(tid);
            //System.err.println("SEND MSG: " + message);
            //tr.send(me, contact, message, pid);
            this.send(me, contact, message);
        } else {
            System.err.println("JOIN-ERROR:COULD NOT FIND A CONTACT FOR NODE " + me.getID());
        }
    }

    /**
     * @param sender
     * @param subscriber
     */
    protected void acceptSubscription(Node sender, Node subscriber) {
        if (sender.getID() == subscriber.getID()) {
            throw new RuntimeException("MUST NOT SUBSCRIBE TO MYSELF!");
        }
        this.partialView.add(subscriber);
        ScampMessage message = new ScampMessage(sender, ScampMessage.Type.AcceptedSubscription, null);
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, subscriber, message, pid);
    }

    /**
     * The answer will be sent with the event "GiveContact"
     */
    public abstract void requestConnection(Node me, Node subscriber);

}
