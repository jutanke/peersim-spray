package example.scamp.orig;

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

    }

    @Override
    public void acceptSubscription(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() + "Try to accept myself as subscription");
        } else {
            System.err.println("Accept 1(out) " + subscriber.getID() + " @" + acceptor.getID());
            ScampMessage m = ScampMessage.createAccept(acceptor, subscriber, acceptor);
            this.send(acceptor, subscriber, m);
            this.addNeighbor(subscriber);
        }
    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================

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
            System.err.println("subscribe fwd " + s.getID() + " to " + n.getID());
            Scamp pp = (Scamp) n.getProtocol(example.scamp.Scamp.pid);
            if (pp.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //pp.addNeighbor(s);
                pp.acceptSubscription(n, s);
            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                forward = ScampMessage.updateForwardSubscription(n, forward); // we update the TTL of the message
                pp.send(n, forwardTarget, forward);
            }
        }
    }
}
