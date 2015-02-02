package example.scamp.simple;

import example.scamp.ScampProtocol;
import example.scamp.ScampWithView;
import example.scamp.messaging.ScampMessage;
import peersim.cdsim.CDState;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Created by julian on 01/02/15.
 */
public class Scamp extends ScampWithView {


    public static final boolean ____C_H_E_A_T_I_N_G____ = true;

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
        Scamp.subscribe(me, subscriber);
    }

    @Override
    public void subRejoin(Node me, long newBirthDate) {
        print("Rejoin: " + me.getID() + " -> " + this.debug());
        //this.unsubscribe(me);
        this.inView.clear();

        ScampMessage message = ScampMessage.createKeepAlive(me, birthDate);
        for (Node n : this.partialView.list()) {
            send(me, n, message);
        }
        Node contact = getRandomNode(me);
        if (contact.getID() != me.getID()) {
            Scamp contactPP = (Scamp) contact.getProtocol(pid);
            contactPP.join(contact, me);
        }
    }

    @Override
    public void subNextCycle(Node node) {


        // CHEATING!
        if (____C_H_E_A_T_I_N_G____ && this.inView.length() == 0 && this.partialView.length() == 0) {
            System.err.println("============ CHEATING =========== @" + node.getID());
            Node contact = Network.get(CDState.r.nextInt(Network.size()));
            ScampProtocol.subscribe(contact, node);
        }

    }

    @Override
    public void unsubscribe(Node me) {
        unsubscribeNode(me);
    }


    @Override
    public void subProcessEvent(Node node, ScampMessage message) {

        switch (message.type) {
            case ForwardSubscription:
                Scamp.doSubscribe(node, message);
                break;
            default:
                throw new RuntimeException("NOTHING HANDLED!");
        }
    }

    @Override
    public void subDoSubscribe(Node acceptor, Node subscriber) {
        if (acceptor.getID() == subscriber.getID()) {
            throw new RuntimeException("@" + acceptor.getID() + "Try to accept myself as subscription");
        } else {
            print("Accept [OUT] " + subscriber.getID() + " -> " + acceptor.getID());
            ScampMessage m = ScampMessage.createAccept(acceptor, subscriber, acceptor);

            if (true) {
                this.send(acceptor, subscriber, m);
            } else {
                Scamp pp = (Scamp) subscriber.getProtocol(pid);
                pp.addToInView(acceptor);
            }

            this.addNeighbor(subscriber);
        }
    }

    @Override
    public void handleSubscription(Node n, example.scamp.ScampMessage m) {
        Scamp.doSubscribe(n, m);
    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================

    /**
     * Run the unsubscribe protocol for the given node.
     *
     * @param n not to unsubscribe
     */
    public static void unsubscribeNode(Node n) {

        Scamp pp = (Scamp) n.getProtocol(pid);
        final int l = pp.degree();
        final int ll = pp.inView.length();
        int i = 0;

        // replace ll-(c+1) links to pp
        if (l > 0) {
            for (; i < ll - c - 1; ++i) {
                Node from = pp.inView.get(i).node;
                if (from.isUp()) {
                    Scamp other = (Scamp) from.getProtocol(pid);
                    Node neighbor = pp.getNeighbor(i % l);
                    print("++++ REPLACE @" + from.getID() + " currently: " + other + "  put : " + neighbor.getID() + " for " + n.getID());
                    other.replace(
                            n,
                            neighbor
                    );
                    print("++++ REPLACE @" + from.getID() + " after: " + other);
                }
            }
        }

        // remove the remaining c+1 links to pp
        for (; i < ll; ++i) {
            Node from = pp.inView.get(i).node;
            if (from.isUp()) {
                Scamp other = (Scamp) from.getProtocol(pid);
                print("================================================ REPLACE @" + from.getID() + " currently: " + other);
                other.replace(n, null);
                print("================================================ REPLACE @" + from.getID() + " after: " + other);
            }
        }

    }

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
                this.addToOutView(n2);
            }
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
                print("@" + n.getID() + " keep subscriber " + s.getID());
                pp.subDoSubscribe(n, s);
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

        print("Start subscribe (I):" + s.getID() + " to " + n.getID());

        if (indirTTL > 0.0) {
            n = getRandomNode(n);
        }

        print("Start subscribe (II):" + s.getID() + " to " + n.getID());

        if (!n.isUp()) {
            return; // quietly returning, no feedback
        }

        //TODO MAKE THIS ASYNC
        Scamp contact = (Scamp) n.getProtocol(pid);
        Scamp subscriber = (Scamp) s.getProtocol(pid);
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
