package example.scamp.nohandshake;

import example.scamp.ScampMessage;
import peersim.cdsim.CDState;
import peersim.core.Node;

/**
 * Created by julian on 01/02/15.
 */
public class ScampNoHandshake extends Scamp {


    /* =============================================================
     *  P R O P E R T I E S
     * =============================================================*/

    public ScampNoHandshake(String s) {
        super(s);
    }

    /* =============================================================
     *  L I S T E N E R S
     * =============================================================*/

    @Override
    protected void subNextCycle(Node node, int protocolID) {

    }

    @Override
    protected void subProcessEvent(Node node, int pid, ScampMessage message) {

        switch (message.type) {
            case ForwardSubscription:
                ScampNoHandshake.doSubscribe(node, message);
                break;
        }

    }

    @Override
    protected void acceptSubscription(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() +"Try to accept myself as subscription");
        } else {
            ScampMessage m = ScampMessage.acceptMessage(acceptor, subscriber, acceptor);
            this.send(acceptor, subscriber, m);
            this.addNeighbor(subscriber);
        }
    }

    /* =============================================================
     *  S C A M P  P R O T O C O L
     * =============================================================*/

    /**
     * Performs the forwarding cycle. Applies a little simplification compared
     * to the original SCAMP protocol. To avoid infinite cycles, instead of
     * counters in each node, we allow only a limited number of steps, ie
     * we introduce a TTL instead. It consumes less memory and easier to implement.
     *
     * @param n the node which receives the given subscription
     * @param forward the subscribing message
     */
    private static void doSubscribe(final Node n, ScampMessage forward) {
        Node s = forward.subscriber;
        Scamp pp = (Scamp) n.getProtocol(Scamp.pid);
        if (pp.p() && !pp.contains(s)) {
            pp.addNeighbor(s);
        } else if (pp.degree() > 0){
            Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
            forward = ScampMessage.forward(n, forward); // we update the TTL of the message
            pp.send(n, forwardTarget, forward);
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Performs the indirection (a random walk) to get a random element from the
     * network. If the random walk gets stuck because of a node which is down,
     * the node which is down is returned. This models the fact that in the real
     * protocol in fact nothing is returned.
     */
    private static Node getRandomNode(Node n) {

        // TODO MAKE THIS ASYNC/MessageBased!

        double ttl = indirTTL;
        Scamp l = (Scamp) n.getProtocol(pid);
        if (l.degree() > 0) {

            ttl -= 1.0 / l.degree();

            while (n.isUp() && ttl > 0.0) {
                if (l.degree() + l.inView.length() > 0) {
                    int id = CDState.r.nextInt(l.degree() + l.inView.length());
                    if (id < l.degree()) {
                        n = l.getNeighbor(id);
                    } else {
                        n = l.inView.get(id - l.degree()).node;
                    }
                } else break;

                l = (Scamp) n.getProtocol(pid);
                ttl -= 1.0 / l.degree();
            }

        }
        return n;
    }


 }
