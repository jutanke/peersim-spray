package example.paper.scamplon;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import example.Scamplon.*;
import example.paper.Dynamic;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 3/14/15.
 */
public class Scamplon extends example.Scamplon.ScamplonProtocol implements Dynamic {

    // ============================================
    // E N T I T Y
    // ============================================

    public static int arcCount = 0;

    private PartialView partialView;
    private int step;
    private boolean isBlocked = false;
    private int currentSecret = Integer.MIN_VALUE;
    private Queue<Event> events;
    private final int DELTA_T = 35;
    private HashMap<Long, Node> inView;
    private PartialView.Entry lastDestination;


    public Scamplon(String prefix) {
        super(prefix);
        this.step = CommonState.r.nextInt(DELTA_T);
        this.events = new LinkedList<Event>();
        this.partialView = new PartialView();
        this.inView = new HashMap<Long, Node>();
    }

    @Override
    public Object clone() {
        Scamplon s = (Scamplon) super.clone();
        s.events = new LinkedList<Event>();
        s.step = CommonState.r.nextInt(DELTA_T);
        s.partialView = new PartialView();
        s.inView = new HashMap<Long, Node>();
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {

        // maintain inview
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        final List<Long> remove = new ArrayList<Long>();
        for (Node in : this.inView.values()) {
            final Scamplon s = (Scamplon) in.getProtocol(pid);
            if (!s.contains(node)) {
                remove.add(in.getID());
            }
        }
        for (long rem: remove) {
            this.inView.remove(rem);
        }
        for (int i = 0; i < this.degree(); i++) {
            final Node n = this.partialView.get(i);
            final Scamplon out = (Scamplon) n.getProtocol(pid);
            if (!out.inView.containsKey(n.getID())) {
                out.inView.put(n.getID(), n);
            }
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        if (!this.isBlocked && !this.events.isEmpty()) {
            final Event ev = this.events.poll();
            this.processEvent(node, pid, ev.message);
        }

        if (node.isUp() && (this.step % DELTA_T) == 0) {
            this.shuffle(node);
        }
        this.step += 1;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        final ScamplonMessage message = (ScamplonMessage) event;

        if (this.isBlocked && message.type != ScamplonMessage.Type.ShuffleResponse) {
            this.events.offer(new Event(node, message));
        } else {
            switch (message.type) {
                case Shuffle:
                    this.onShuffle(node, message);
                    break;
                case ShuffleResponse:
                    this.onShuffleResponse(node, message);
                    break;
                case Subscribe:
                    this.onSubscribe(node, message);
                    break;
                case Forward:
                    this.onForward(node, message);
                    break;
                default:
                    throw new RuntimeException("unhandled");
            }
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
        return null;
    }

    // ============================================
    // C Y C L O N
    // ============================================


    public void shuffle(Node me) {

        if (this.isBlocked) {
            // delayed too much
            this.partialView.delete(this.lastDestination);
            this.partialView.freeze();
            System.err.println("@" + me.getID() + " remove " + lastDestination.node.getID());
        }

        this.partialView.incrementAge();

        PartialView.Entry q = this.partialView.oldest();
        if (q == null) return;

        List<PartialView.Entry> nodesToSend = this.partialView.subsetMinus1(q);

        nodesToSend.add(new PartialView.Entry(me));

        ScamplonMessage m = ScamplonMessage.shuffleWithSecret(
                me, nodesToSend, this.partialView.degree(), nextSecret());

        this.lastDestination = q;
        this.isBlocked = true;
        send(me, q.node, m);

    }

    public void onShuffle(Node me, ScamplonMessage message) {
        if (message.type != ScamplonMessage.Type.Shuffle) throw new RuntimeException("nop3");
        if (this.isBlocked) {
            throw new RuntimeException("nop");
            //this.events.offer(new Event(me, message));
        } else {
            List<PartialView.Entry> nodesToSend = this.partialView.subset();
            Node p = message.sender;
            List<PartialView.Entry> received = message.a;
            final int otherViewSize = message.partialViewSize;

            ScamplonMessage m = ScamplonMessage.shuffleResponse(
                    me, PartialView.clone(nodesToSend), message, this.partialView.degree());

            this.partialView.merge(me, p, received, otherViewSize);
            send(me, p, m);
        }
    }

    public void onShuffleResponse(Node me, ScamplonMessage message) {
        if (message.type != ScamplonMessage.Type.ShuffleResponse) throw new RuntimeException("nop4");

        if (this.isCorrectSecret(message)) {
            if (!this.isBlocked) {
                throw new RuntimeException("must not happen");
            }

            this.isBlocked = false;

            final List<PartialView.Entry> received = message.a;
            final int otherViewSize = message.partialViewSize;
            Node q = message.sender;

            this.partialView.merge(me, q, received, otherViewSize);
        } else {
            System.err.println("Message got dropped!");
        }
    }


    // ============================================
    // S C A M P
    // ============================================

    public static void unsubscribe(Node node) {
        Scamplon current = (Scamplon) node.getProtocol(pid);
        if (current.isUp()) {
            current.down();
            final int ls = current.inView.size();
            final int l = current.degree();
            final int notifyIn = Math.max(ls - c - 1, 0);
            for (int i = 0; i < notifyIn; i++) {

            }
        } else {
            throw new RuntimeException("already down");
        }

    }

    /**
     * We assume that the contact c got selected through indirection!
     *
     * @param s subscriber
     * @param c contact
     */
    public static void subscribe(Node s, Node c) {
        Scamplon subscriber = (Scamplon) s.getProtocol(pid);
        subscriber.addNeighbor(c);
        ScamplonMessage subscribe = ScamplonMessage.subscribe(s);
        send(s, c, subscribe);
    }

    private void onSubscribe(Node me, ScamplonMessage subscription) {
        if (subscription.type != ScamplonMessage.Type.Subscribe) throw new RuntimeException("nop");
        for (Node n : this.partialView.list()) {
            final ScamplonMessage forward = ScamplonMessage.forward(me, subscription);
            send(me, n, forward);
        }

        for (int i = 0; i < c && this.degree() > 0; i++) {
            int pos = CommonState.r.nextInt(this.degree());
            final ScamplonMessage forward = ScamplonMessage.forward(me, subscription);
            send(me, this.partialView.get(pos), forward);
        }
    }

    private void onForward(Node me, ScamplonMessage forward) {
        if (forward.type != ScamplonMessage.Type.Subscribe &&
                forward.type != ScamplonMessage.Type.Forward) {
            throw new RuntimeException("nop2");
        }

        if (!forward.isDead()) {
            final Node subscriber = forward.subscriber;


            if (this.partialView.p() && !this.contains(subscriber) && me.getID() != subscriber.getID()) {
                this.addNeighbor(subscriber);

                //TODO send accept
                Scamplon s = (Scamplon) subscriber.getProtocol(pid);
                s.inView.put(me.getID(), me);
                this.arcCount += 1;

            } else if (this.degree() > 0) {
                Node forwardTarget = this.getNeighbor(CommonState.r.nextInt(this.degree()));
                forward = ScamplonMessage.forward(me, forward);
                send(me, forwardTarget, forward);
            } else {
                throw new RuntimeException("no forwarding target");
            }
        }

    }


    private boolean isUp = true;
    @Override
    public boolean isUp() {
        return this.isUp;
    }

    @Override
    public void up() {
        this.isUp = true;
    }

    @Override
    public void down() {
        this.isUp = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("in:");
        for (Node n : this.inView.values()) {
            if (sb.length() > 3) {
                sb.append(",");
            }
            sb.append(n.getID());
        }
        sb.append(" out:");
        sb.append(this.partialView);
        return sb.toString();
    }

    // ===========================================
    // P R I V A T E
    // ===========================================

    public static void send(Node sender, Node destination, ScamplonMessage m) {
        Transport tr = (Transport) sender.getProtocol(tid);
        tr.send(sender, destination, m, pid);
    }

    private boolean isCorrectSecret(ScamplonMessage m) {
        return (m.secret == this.currentSecret);
    }

    private int nextSecret() {
        this.currentSecret += 1;
        return this.currentSecret;
    }

    /**
     * to ensure no overlapping
     */
    private class Event {
        public final Node node;
        public final ScamplonMessage message;

        public Event(Node node, ScamplonMessage message) {
            this.node = node;
            this.message = message;
        }
    }
}
