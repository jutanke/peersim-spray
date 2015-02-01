package example.scamp.nohandshake;

import example.scamp.Scamp;
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

        if (this.isExpired() && this.degree() > 0) {
            this.inView.clear();
            ScampNoHandshake.subscribe(getNeighbor(CDState.r.nextInt(degree())), node);
        }

        for (Node expired : this.partialView.leaseTimeout()) {
            this.partialView.del(expired);
        }

        for (Node expired : this.inView.leaseTimeout()) {
            this.partialView.del(expired);
        }

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
    public void acceptSubscription(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() + "Try to accept myself as subscription");
        } else {
            System.err.println("Accept 1(out) " + subscriber.getID() + " @" + acceptor.getID());
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
     * @param n       the node which receives the given subscription
     * @param forward the subscribing message
     */
    private static void doSubscribe(final Node n, ScampMessage forward) {
        if (forward.ttl > 0) {
            Node s = forward.subscriber;
            System.err.println("subscribe fwd " + s.getID() + " to " + n.getID() + " ttl:" + forward.ttl);
            Scamp pp = (Scamp) n.getProtocol(Scamp.pid);
            if (pp.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //pp.addNeighbor(s);
                pp.acceptSubscription(n, s);
            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                forward = ScampMessage.forward(n, forward); // we update the TTL of the message
                pp.send(n, forwardTarget, forward);
            }
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

    // ----------------------------------------------------------------------

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
            ScampNoHandshake.doSubscribe(n, forward);
        } else {

            for (int i = 0; i < contact.partialView.length(); ++i) {
                ScampNoHandshake.doSubscribe(contact.getNeighbor(i), forward);
            }

            if (indirTTL > 0.0) {
                for (int i = 0; i < c; ++i) {
                    ScampNoHandshake.doSubscribe(
                            contact.getNeighbor(CDState.r.nextInt(contact.degree())),
                            forward);
                }
            }
        }
    }

    @Override
    public void startSubscribe(Node me, Node s) {
        ScampNoHandshake.subscribe(me, s);
    }

    // ----------------------------------------------------------------------

    /**
     * Replace n1 with n2 in the partial view. Helper method to unsubscribe.
     * This is done taking care of
     * the consistency of the data structure and dates. If n1 is not known,
     * prints a warning and exits doing nothing.
     *
     * @param n1 the node to replace
     * @param n2 the new node. If null, n1 is simply removed.
     */
// XXX could be optimized if turned out to be a performance problem
    private void replace(Node n1, Node n2) {
        if (this.partialView.contains(n1)) {
            this.partialView.del(n1);
            if (n2 != null) {
                this.partialView.add(n2);
            }
        }
    }

    // ----------------------------------------------------------------------

    /**
     * Run the unsubscribe protocol for the given node.
     *
     * @param n not to unsubscribe
     */
    public static void unsubscribe(Node n) {

        Scamp pp = (Scamp) n.getProtocol(pid);
        final int l = pp.degree();
        final int ll = pp.inView.length();
        int i = 0;
        if (l > 0) {
            for (; i < ll - c - 1; ++i) {
                Node from = pp.inView.get(i).node;
                if (from.isUp()) {
                    ((ScampNoHandshake) from.getProtocol(pid)).replace(
                            n,
                            pp.getNeighbor(i % l)
                    );
                }
            }
        }

        // remove remaining c+1 links to pp
        for (; i < ll; ++i) {
            Node from = pp.inView.get(i).node;
            if (from.isUp()) {
                ((ScampNoHandshake) from.getProtocol(pid)).replace(n, null);
            }
        }
    }

    // ----------------------------------------------------------------------



}
