package example.paper.scamplon;

import example.Scamplon.*;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.transport.Transport;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by julian on 3/14/15.
 */
public class Scamplon extends example.Scamplon.ScamplonProtocol {

    // ============================================
    // E N T I T Y
    // ============================================

    private PartialView partialView;
    private int step;
    private boolean isBlocked;
    private int currentSecret = Integer.MIN_VALUE;
    private Queue<Event> events;
    private final int DELTA_T = 35;


    public Scamplon(String prefix) {
        super(prefix);
        this.step = CommonState.r.nextInt(DELTA_T);
        this.events = new LinkedList<Event>();
        this.partialView = new PartialView();
    }

    @Override
    public Object clone() {
        Scamplon s = (Scamplon) super.clone();
        s.events = new LinkedList<Event>();
        s.step = CommonState.r.nextInt(DELTA_T);
        s.partialView = new PartialView();
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {

    }

    @Override
    public void processEvent(Node node, int pid, Object event) {

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

        for (int i = 0; i < c; i++) {
            int pos = CommonState.r.nextInt(this.degree());
            final ScamplonMessage forward = ScamplonMessage.forward(me, subscription);
            send(me, this.partialView.get(i), forward);
        }
    }

    private void onForward(Node me, ScamplonMessage forward) {
        if (forward.type != ScamplonMessage.Type.Subscribe ||
                forward.type != ScamplonMessage.Type.Forward) {
            throw new RuntimeException("nop2");
        }

        if (!forward.isDead()) {
            final Node subscriber = forward.subscriber;
            Scamplon pp = (Scamplon) me.getProtocol(pid);
        }

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
