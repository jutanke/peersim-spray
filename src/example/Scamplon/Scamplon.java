package example.Scamplon;

import example.scamp.messaging.ScampMessage;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.transport.Transport;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * Created by julian on 2/5/15.
 */
public class Scamplon extends ScamplonProtocol {

    // ============================================
    // E N T I T Y
    // ============================================

    private PartialView partialView;

    public Scamplon(String s) {
        super(s);
        this.partialView = new PartialView();
    }

    @Override
    public Object clone() {
        Scamplon s = (Scamplon) super.clone();
        s.partialView = new PartialView();
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (this.degree() > 0 && CommonState.getTime() % 5 == 0) {
            //this.partialView.unhassle();
            this.partialView.incrementAge();
            Node q = this.partialView.oldest().node;

            print("BEFORE:" + this.partialView);

            List<PartialView.Entry> nodesToSend = this.partialView.subsetMinus1(q);

            print("AFTER:" + this.partialView);

            nodesToSend.add(new PartialView.Entry(node));

            print("SHUFFLE (" + node.getID() + "->" + q.getID() + " send:" + nodesToSend + " pv:" + this.partialView);

            print("CLONE:" + PartialView.clone(nodesToSend));

            ScampMessage m = ScampMessage.scamplonShuffle(
                    node,
                    PartialView.clone(nodesToSend),
                    this.partialView.degree());
            send(node, q, m);
        }


    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        ScampMessage message = (ScampMessage) event;

        List<PartialView.Entry> received = null;
        List<PartialView.Entry> nodesToSend = null;
        int otherViewSize;

        switch (message.type) {
            case ForwardSubscription:
                doSubscribe(node, message);
                break;
            case AcceptSubscription:
                print("Accept [IN] " + message.payload.getID() + " -> " + node.getID());
                break;
            case ScamplonShuffle:
                //this.partialView.unhassle();

                print("Bef subset:" + this.partialView + " l:" + this.partialView.l());
                nodesToSend = this.partialView.subset();
                print("Aft subset:" + this.partialView + " => " + nodesToSend);

                Node p = message.sender;
                received = message.nodesToSend;
                otherViewSize = message.partialViewSize;

                ScampMessage m = ScampMessage.scamplonResponse(
                        node,
                        PartialView.clone(nodesToSend),
                        this.partialView.degree());
                send(node, p, m);

                print("A");
                this.partialView.merge(node, p /*not sure if p is correct here..*/, received, otherViewSize);
                break;
            case ScamplonShuffleResponse:

                received = message.nodesToSend;
                otherViewSize = message.partialViewSize;
                Node q = message.sender;

                print("B");
                this.partialView.merge(node, q, received, otherViewSize);

                break;
            default:
                System.err.println(message);
                throw new RuntimeException("should not happen");
        }
    }

    @Override
    public int degree() {
        return this.partialView.degree();
    }

    @Override
    public Node getNeighbor(int i) {
        return this.partialView.get(i);
    }

    @Override
    public boolean addNeighbor(Node neighbour) {
        //return false;
        return this.partialView.add(neighbour);
    }

    @Override
    public boolean contains(Node neighbor) {
        return this.partialView.contains(neighbor);
    }

    @Override
    public List<Node> getPeers() {
        return this.partialView.list();
    }

    @Override
    public String debug() {
        return this.partialView.toString();
    }

    @Override
    public String toString(){
        return this.debug();
    }

    // ============================================
    // P R I V A T E
    // ============================================

    public static void send(Node sender, Node destination, example.scamp.messaging.ScampMessage m) {
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, destination, m, pid);
    }


    // ============================================
    // S T A T I C
    // ============================================

    /**
     * @param n
     * @param forward
     */
    public static void doSubscribe(final Node n, ScampMessage forward) {
        if (!forward.isExpired()) {
            Node s = forward.payload;
            print("subscribe fwd " + s.getID() + " to " + n.getID());
            Scamplon pp = (Scamplon) n.getProtocol(pid);
            if (pp.partialView.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //pp.addNeighbor(s);
                print("@" + n.getID() + " keep subscriber " + s.getID());

                if (n.getID() == s.getID()) {
                    throw new RuntimeException("@" + n.getID() + " cannot accept myself");
                } else {

                    ScampMessage m = ScampMessage.createAccept(n, s, n);
                    send(n, s, m);
                    pp.addNeighbor(s);
                }

            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                forward = ScampMessage.updateForwardSubscription(n, forward); // we update the TTL of the message
                print("here " + forward);
                send(n, forwardTarget, forward);
            }
        } else {
            print("message expired.. " + forward);
        }
    }

    /**
     * enter the network
     *
     * @param s subscriber
     */
    public static void subscribe(Node s) {

        // indirection
        Node n = Network.get(CDState.r.nextInt(Network.size()));

        while (n.getID() == s.getID()) {
            n = Network.get(CDState.r.nextInt(Network.size()));
        }

        Scamplon contact = (Scamplon) n.getProtocol(pid);
        Scamplon subscriber = (Scamplon) s.getProtocol(pid);
        subscriber.addNeighbor(n);

        ScampMessage forward = ScampMessage.createForwardSubscription(n, s);

        if (contact.degree() == 0) {
            Scamplon.doSubscribe(n, forward);
        } else {

            for (int i = 0; i < contact.partialView.degree(); i++) {
                Scamplon.doSubscribe(contact.getNeighbor(i), forward);
            }

            for (int i = 0; i < c; ++i) {
                int pos = (CDState.r.nextInt(contact.degree()));
                Scamplon.doSubscribe(
                        contact.getNeighbor(pos),
                        forward
                );
            }

        }

    }


    public static void print(Object o) {
        if (true) {
            System.err.println(o);
        }
    }
}
