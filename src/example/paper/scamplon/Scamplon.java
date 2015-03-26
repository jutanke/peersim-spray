package example.paper.scamplon;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.*;

/**
 * Created by julian on 3/14/15.
 */
public class Scamplon extends example.Scamplon.ScamplonProtocol {

    // ============================================
    // E N T I T Y
    // ============================================

    private PartialView partialView;
    private int step;
    private boolean isBlocked = false;
    private int currentSecret = Integer.MIN_VALUE;
    private Queue<Event> events;
    private final int DELTA_T = 35;
    private HashMap<Long, Node> inView;


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
        this.partialView.incrementAge();

        PartialView.Entry q = this.partialView.oldest();

        List<PartialView.Entry> nodesToSend = this.partialView.subsetMinus1(q);

        nodesToSend.add(new PartialView.Entry(me));

        ScamplonMessage m = ScamplonMessage.shuffleWithSecret(
                me, nodesToSend, this.partialView.degree(), nextSecret());

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
        if (!this.isBlocked) {
            throw new RuntimeException("must not happen");
        }

        this.isBlocked = false;

        final List<PartialView.Entry> received = message.a;
        final int otherViewSize = message.partialViewSize;
        Node q = message.sender;

        this.partialView.merge(me, q, received, otherViewSize);
    }


    // ============================================
    // S C A M P
    // ============================================

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

            } else if (this.degree() > 0) {
                Node forwardTarget = this.getNeighbor(CommonState.r.nextInt(this.degree()));
                forward = ScamplonMessage.forward(me, forward);
                send(me, forwardTarget, forward);
            } else {
                throw new RuntimeException("no forwarding target");
            }
        }

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
