package example.scamp.simple;

import example.scamp.ScampProtocol;
import example.scamp.ScampWithView;
import example.scamp.messaging.ScampMessage;
import example.scamp.nohandshake.ScampNoHandshake;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 2/2/15.
 */
public class ScampHandshake extends ScampWithView {

    public static final boolean ____C_H_E_A_T_I_N_G____ = true;

    // ===================================================
    // E N T I T Y
    // ===================================================

    private List<Indirection> pendingIndirections;

    public ScampHandshake(String s) {
        super(s);
        this.pendingIndirections = new ArrayList<Indirection>();
    }

    @Override
    public Object clone() {
        ScampHandshake s = (ScampHandshake) super.clone();
        s.pendingIndirections = new ArrayList<Indirection>();
        return s;
    }

    // ===================================================
    // P U B L I C  I N T E R F A C E
    // ===================================================

    @Override
    public void handleSubscription(Node n, example.scamp.ScampMessage m) {
        throw new NotImplementedException();
    }

    @Override
    public void subRejoin(Node me, long newBirthDate) {

    }

    @Override
    public void subNextCycle(Node node) {

        //System.out.println("@" + node.getID() + " indir:" + this.pendingIndirections);

        // check if some pending indirections "came back"
        for (Indirection i : this.pendingIndirections) {
            if (i.isReturned()) {
                this.send(node, i.destination, i.message);
            } else {
                System.err.println("COUNTER(b): " + i.counter);
                i.update();
                System.err.println("COUNTER(a): " + i.counter);
            }
        }

        // CHEATING!
        if (____C_H_E_A_T_I_N_G____ && this.inView.length() == 0 && this.partialView.length() == 0) {
            System.err.println("============ CHEATING =========== @" + node.getID());
            Node contact = Network.get(CDState.r.nextInt(Network.size()));
            ScampHandshake.indirection(contact, node);
        }

    }

    public void subDoSubscribe(Node acceptor, Node subscriber) {
        throw new NotImplementedException();
    }

    @Override
    public void join(Node me, Node subscriber) {
        indirection(me, subscriber);
    }

    @Override
    public void unsubscribe(Node me) {
        throw new NotImplementedException();
    }

    @Override
    public void subProcessEvent(Node node, ScampMessage message) {

        switch (message.type) {
            case ForwardSubscriptionHandshake:
                ScampHandshake.doSubscribe(node, message);
                break;
            case IndirectionHandshake:
                Node contact = message.payload2;
                Node subscriber = message.payload;
                ScampHandshake.subscribe(contact, subscriber);
                break;
            case Answer:
                // ALICE -> BOB -- pending...
                backtrack(node, message);
                break;
            case Handshake:
                // BOB -> ALICE -- pending...
                backtrack(node, message);
                break;
            case Connect:
                // ALICE -> BOB -- we are connected now!
                Node connect = message.sender;
                this.addToInView(connect);
                break;
            default:
                throw new RuntimeException("NOTHING HANDLED!");
        }

    }

    // ===================================================
    // P R I V A T E  I N T E R F A C E
    // ===================================================

    /**
     *
     * @param me
     * @param message
     */
    private void backtrack(Node me, ScampMessage message) {
        Node s, source;
        switch (message.type) {
            case Answer:
                // we are looking for the subscriber
                s = message.payload;
                source = message.payload2;
                if (me.getID() == s.getID()) {
                    // FOUND! We are the subscriber
                    ScampMessage handshake = ScampMessage.createHandshake(me, message);
                    Node backtrack = message.route2.pop();
                    this.send(me, backtrack, handshake);
                } else {
                    if (message.route.empty()) {
                        // Directly access:
                        //Node subscriber = message.payload;
                        throw new RuntimeException("@" + me.getID()+ " Could not reroute the answer between " +
                                source.getID() + " -> " + s.getID() + " .. msg:" + message);
                    }
                    ScampMessage answer = ScampMessage.updateAnswer(me, message);
                    Node backtrack = message.route.pop();
                    this.send(me, backtrack, answer);
                }
                break;
            case Handshake:
                s = message.payload2;
                source = message.payload;
                if (source.getID() == me.getID()) {
                    // FOUND!
                    ScampMessage connect = ScampMessage.createConnect(me, message);
                    this.send(me, s, connect);
                    this.addToOutView(s);
                } else {
                    if (message.route.empty()) {
                        throw new RuntimeException("@" + me.getID()+ " (2)Could not reroute the answer between " +
                                source.getID() + " -> " + s.getID());
                    }
                    ScampMessage handshake = ScampMessage.updateHandshake(me, message);
                    Node backtrack = message.route.pop();
                    this.send(me, backtrack, handshake);
                }
                break;
            default:
                throw new RuntimeException("NOTHING HANDLED!");
        }

    }

    /**
     *
     * @param n
     * @param forward
     */
    private static void doSubscribe(final Node n, ScampMessage forward) {
        if (forward.type != ScampMessage.Type.ForwardSubscriptionHandshake) {
            throw new RuntimeException("ForwardSubscriptionHandshake expect but got: " + forward.type);
        }
        if (!forward.isExpired()) {
            Node s = forward.payload;
            ScampWithView pp = (ScampWithView) n.getProtocol(pid);
            if (pp.p() && !pp.contains(s) && n.getID() != s.getID()) {
                //pp.subDoSubscribe(n, s);
                // WE ACCEPT

                ScampMessage answer = ScampMessage.createAnswer(n, forward);
                System.err.println("route:" + forward);
                Node backtrack = forward.route.pop();
                System.err.println("@" + n.getID() + " create Answer for " +
                        forward.payload.getID() + " btrack:" + backtrack);
                pp.send(n,backtrack,answer);


            } else if (pp.degree() > 0) {
                Node forwardTarget = pp.getNeighbor(CDState.r.nextInt(pp.degree()));
                forward = ScampMessage.updateForwardSubscriptionHandshake(n, forward);
                pp.send(n, forwardTarget, forward);
            }
        }
    }

    /**
     *
     * @param n
     * @param s
     */
    private static void indirection(Node n, Node s) {
        if (indirTTL > 0.0) {
            // fake the stuff!
            Node contact = Network.get(CommonState.r.nextInt(Network.size()));
            ScampMessage message = ScampMessage.createIndirectionAnswer(n, s, contact);
            ((ScampHandshake) n.getProtocol(pid)).pendingIndirections.add(new Indirection(message, s));
        } else {
            subscribe(n, s);
        }
    }

    public static void subscribe(Node co, Node s) {

        ScampHandshake contact = (ScampHandshake) co.getProtocol(pid);
        ScampHandshake subscriber = (ScampHandshake) s.getProtocol(pid);

        //TODO maybe make this async as well!
        contact.addToInView(s);
        subscriber.addNeighbor(co);

        ScampMessage forward = ScampMessage.createForwardSubscriptionHandshake(co,s);

        if (contact.degree() == 0) {
            ScampHandshake.doSubscribe(co, forward);
        } else {
            for (int i = 0; i < contact.partialView.length(); ++i) {
                ScampHandshake.doSubscribe(contact.getNeighbor(i), forward);
            }

            if (indirTTL > 0.0) {
                for (int i = 0; i < c; ++i) {
                    ScampHandshake.doSubscribe(
                            contact.getNeighbor(CDState.r.nextInt(contact.degree())),
                            forward);
                }
            }
        }

    }

    // ===================================================
    // S U B  C L A S S E S
    // ===================================================

    private static class Indirection {
        public static final int MAX = 0;
        private int counter = 0;
        public final ScampMessage message;
        public final Node destination;

        public Indirection(ScampMessage m, Node destination) {
            if (m.type != ScampMessage.Type.IndirectionHandshake) {
                throw new RuntimeException("nope, wrong type:" + m.type);
            }
            this.destination = destination;
            this.message = m;
        }

        public void update() {
            this.counter += 1;
        }

        public boolean isReturned() {
            return this.counter >= MAX;
        }

        @Override
        public String toString(){
            return "{dest:" + destination.getID() + " msg:" + message + " ctr:" + counter + "}";
        }

    }

}
