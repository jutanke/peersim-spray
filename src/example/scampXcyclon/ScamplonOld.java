package example.scampXcyclon;

import example.scamp.messaging.ScampMessage;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 2/3/15.
 */
public class ScamplonOld implements Linkable, EDProtocol, CDProtocol, example.PeerSamplingService {

    public static int c, tid, pid;

    // ============================================
    // E N T I T Y
    // ============================================

    private static final String PAR_C = "c";
    private static final String SCAMPLON_PROT = "0";
    private static final String PAR_TRANSPORT = "transport";

    private ScamplonView view;

    public ScamplonOld(String n) {
        c = Configuration.getInt(n + "." + PAR_C, 0);
        tid = Configuration.getPid(n + "." + PAR_TRANSPORT);
        pid = Configuration.lookupPid(SCAMPLON_PROT);
        this.view = new ScamplonView();
    }

    @Override
    public Object clone() {
        ScamplonOld s = null;
        try {
            s = (ScamplonOld) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        s.view = new ScamplonView();
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (this.degree() > 0) {

            // cycle..
            this.view.incrementAge();

            Node q = this.view.oldest().node;

            List<PartialViewEntry> nodesToSend = this.view.subsetMinus1(q);
            nodesToSend.add(new PartialViewEntry(node)); // age = 0
            print("@" + node.getID() + " -> " + q.getID() + " -> " + nodesToSend + " db:" + this.debug());

            ScampMessage m = ScampMessage.createShuffle(node, nodesToSend, this.view.oldest(), this.degree());
            this.send(node, q, m);
        } else if (this.view.in.size() == 0) {

            // C H E A T ! !
            ScamplonOld.subscribe(node);

        }

    }

    @Override
    public int degree() {
        return this.view.c();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.view.out.get(i).node;
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        return this.view.addToOut(neighbour);
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.view.outContains(neighbor);
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

        List<PartialViewEntry> received = null;
        List<PartialViewEntry> nodesToSend = null;
        int factor = 0;
        switch (message.type) {
            case ForwardSubscription:
                doSubscribe(node, message);
                break;
            case AcceptSubscription:
                Node acceptor = message.payload;
                this.view.addToIn(acceptor);
                break;
            case Shuffle:

                Node p = message.sender;

                received = message.list;
                nodesToSend = this.view.subset();

                int otherDegree = message.c;
                factor = 0;
                if (Math.abs(otherDegree - this.degree()) > 1) {
                    if (otherDegree > this.degree()) {
                        factor = 1;
                    } else {
                        factor = -1;
                    }
                }

                this.view.merge(node, clone(nodesToSend), clone(received), factor);

                ScampMessage m = ScampMessage.createShuffleResponse(node, nodesToSend, message, -factor);
                this.send(node, p, m);

                break;
            case ShuffleResponse:

                received = message.list;
                nodesToSend = message.list2;
                Node q = message.sender;
                PartialViewEntry oldest = message.oldest;
                factor = message.factor;

                this.view.merge(node, oldest, clone(nodesToSend), clone(received), factor);

                break;
            default:
                throw new NotImplementedException();
        }

    }

    @Override
    public List<Node> getPeers() {
        List<Node> result = new ArrayList<Node>();
        for (PartialViewEntry e : this.view.out) {
            result.add(e.node);
        }
        return result;
    }

    @Override
    public String debug() {
        return this.view.toString();
    }

    // ============================================
    // P R I V A T E
    // ============================================

    /**
     * @param sender
     * @param destination
     * @param m
     */
    public void send(Node sender, Node destination, example.scamp.messaging.ScampMessage m) {
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, destination, m, pid);
    }

    public List<PartialViewEntry> clone(List<PartialViewEntry> list) {
        return new ArrayList<PartialViewEntry>(list);
    }

    /**
     * @param n
     * @param forward
     */
    public static void doSubscribe(final Node n, ScampMessage forward) {
        if (!forward.isExpired()) {
            Node s = forward.payload;
            ScamplonOld pp = get(n);
            if (pp.view.p() && !pp.contains(s) && n.getID() != s.getID()) {
                if (n.getID() == s.getID()) {
                    throw new RuntimeException("@" + n.getID() + "Try to accept myself as subscription");
                } else {
                    ScampMessage m = ScampMessage.createAccept(n, s, n);
                    pp.send(n, s, m);
                    pp.addNeighbor(s);
                    print("@" + n.getID() + " accept subscription " + s.getID());
                }
            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                print("@" + n.getID() + " forward " + s.getID() + " to " + forwardTarget.getID());
                forward = ScampMessage.updateForwardSubscription(n, forward);
                pp.send(n, forwardTarget, forward);
            }
        }
    }

    public static void subscribe(Node s) {

        Node n = Network.get(CDState.r.nextInt(Network.size()));
        while (n.getID() == s.getID()) {
            n = Network.get(CDState.r.nextInt(Network.size()));
        }

        ScamplonOld contact = get(n);
        ScamplonOld subscriber = get(s);
        contact.view.addToIn(s);
        subscriber.addNeighbor(n);

        print("Subscribe " + s.getID() + " to " + n.getID());


        ScampMessage forward = ScampMessage.createForwardSubscription(n, s);

        if (contact.degree() == 0) {
            //doSubscribe(n, ScampMessage.copy(forward));
            doSubscribe(n, forward);
        } else {

            for (int i = 0; i < contact.degree(); ++i) {
                doSubscribe(contact.getNeighbor(i), forward);
            }

            for (int i = 0; i < c; ++i) {
                doSubscribe(
                        contact.getNeighbor(CDState.r.nextInt(contact.degree())),
                        forward);
            }
        }

    }

    /**
     * @param o
     */
    private static void print(Object o) {
        if (false) System.out.println(o);
    }

    /**
     * @param n
     * @return
     */
    private static ScamplonOld get(Node n) {
        return (ScamplonOld) n.getProtocol(pid);
    }

}
