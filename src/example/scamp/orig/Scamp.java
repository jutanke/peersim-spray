package example.scamp.orig;

import example.cyclon.PeerSamplingService;
import example.scamp.ScampWithView;
import example.scamp.orig.messaging.ScampMessage;
import peersim.cdsim.CDState;
import peersim.core.Node;

/**
 * Created by julian on 01/02/15.
 */
public class Scamp extends ScampWithView {

    // ===================================================
    // E N T I T Y
    // ===================================================

    public Scamp(String s) {
        super(s);
    }

    @Override
    public Object clone() {
        Scamp s = (Scamp) super.clone();
        return s;
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void join(Node me, Node subscriber) {

    }

    @Override
    public void subRejoin(Node me) {

    }

    @Override
    public void unsubscribe(Node me) {

    }

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

        ScampMessage message = (ScampMessage) event;
        switch (message.type) {
            case AcceptSubscription:
                print("Accept [IN] " + message.payload.getID() + " -> " + node.getID());
                Node acceptor = message.payload;
                this.addToInView(acceptor);
                break;
            case ForwardSubscription:
                Scamp.doSubscribe(node, message);
                break;
        }

    }

    @Override
    public void acceptSubscription(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() + "Try to accept myself as subscription");
        } else {
            print("Accept [OUT] " + subscriber.getID() + " -> " + acceptor.getID());
            ScampMessage m = ScampMessage.createAccept(acceptor, subscriber, acceptor);
            this.send(acceptor, subscriber, m);
            this.addNeighbor(subscriber);
        }
    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================

    /**
     * for debugging
     * @param s
     */
    private static void print(Object s) {
        if (true) {
            System.out.println(s);
        }
    }

    /**
     * Performs the forwarding cycle. Applies a little simplification compared
     * to the original SCAMP protocol. To avoid infinite cycles, instead of
     * counters in each node, we allow only a limited number of steps, ie
     * we introduce a TTL instead. It consumes less memory and easier to implement.
     *
     * @param n       the node which receives the given subscription
     * @param forward the subscribing message
     */
    private static void doSubscribe(final Node n, ScampMessage forward) {
        if (!forward.isExpired()) {
            Node s = forward.payload;
            print("subscribe fwd " + s.getID() + " to " + n.getID());
            Scamp pp = (Scamp) n.getProtocol(example.scamp.Scamp.pid);
            if (pp.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //pp.addNeighbor(s);
                pp.acceptSubscription(n, s);
            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                forward = ScampMessage.updateForwardSubscription(n, forward); // we update the TTL of the message
                pp.send(n, forwardTarget, forward);
            }
        } else {
            print("message expired.. " + forward);
        }
    }

    /**
     * This node will act as a contact node forwarding the subscription to nodes
     * from its view and c other random nodes.
     *
     * @param n the contact node
     * @param s the subscribing node
     */
    public static void subscribe(Node n, Node s) {

        if (indirTTL > 0.0) {
            n = getRandomNode(n);
        }

        System.err.println("Start subscribe " + s.getID() + " to " + n.getID());

        if (!n.isUp()) {
            return; // quietly returning, no feedback
        }

        Scamp contact = (Scamp) n.getProtocol(pid);
        Scamp subscriber = (Scamp) s.getProtocol(pid);

        //TODO MAKE THIS ASYNC

        contact.addToInView(s);
        subscriber.addNeighbor(n);

        ScampMessage forward = ScampMessage.createForwardSubscription(n, s);

        if (contact.degree() == 0) {
            System.err.println("SCAMP: zero degree contact node " + s.getID() + " -> " + n.getID());
            Scamp.doSubscribe(n, forward);
        } else {

            for (int i = 0; i < contact.partialView.length(); ++i) {
                Scamp.doSubscribe(contact.getNeighbor(i), forward);
            }

            if (indirTTL > 0.0) {
                for (int i = 0; i < c; ++i) {
                    Scamp.doSubscribe(
                            contact.getNeighbor(CDState.r.nextInt(contact.degree())),
                            forward);
                }
            }
        }
    }
}
