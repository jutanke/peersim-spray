package example.paper.scamplon;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import example.Scamplon.*;
import example.paper.Dynamic;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.transport.Transport;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by julian on 3/14/15.
 */
public class Scamplon extends example.Scamplon.ScamplonProtocol implements Dynamic {

    // ============================================
    // E N T I T Y
    // ============================================

    private PartialView partialView;
    private int step;
    public boolean isBlocked = false;
    private int currentSecret = Integer.MIN_VALUE;
    private Queue<Event> events;
    private final int DELTA_T = 35;
    private HashMap<Long, Node> inView;
    private PartialView.Entry lastDestination;
    private Queue<Node> deleteASAP;
    private Queue<Node> insertASAP;

    public Scamplon(String prefix) {
        super(prefix);
        this.step = CommonState.r.nextInt(DELTA_T);
        this.events = new LinkedList<Event>();
        this.partialView = new PartialView();
        this.inView = new HashMap<Long, Node>();
        this.deleteASAP = new LinkedList<Node>();
        this.insertASAP = new LinkedList<Node>();
    }

    @Override
    public Object clone() {
        Scamplon s = (Scamplon) super.clone();
        s.events = new LinkedList<Event>();
        s.step = CommonState.r.nextInt(DELTA_T);
        s.partialView = new PartialView();
        s.inView = new HashMap<Long, Node>();
        s.deleteASAP = new LinkedList<Node>();
        s.insertASAP = new LinkedList<Node>();
        return s;
    }

    // ============================================
    // P U B L I C
    // ============================================

    @Override
    public void nextCycle(Node node, int protocolID) {
        if (this.isUp()) {
            if (!this.isBlocked) {
                while (!this.deleteASAP.isEmpty()) {
                    final Node del = this.deleteASAP.poll();
                    this.partialView.deleteAll(del);
                }

                while (!this.insertASAP.isEmpty()) {
                    final Node s = this.insertASAP.poll();
                    final Scamplon subscriber = (Scamplon) s.getProtocol(pid);
                    this.addNeighbor(s);
                    subscriber.addToInview(s, node);
                }
            }

            // maintain inview
            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            final List<Node> remove = new ArrayList<Node>();
            for (Node i : this.inView.values()) {
                final Scamplon in = (Scamplon) i.getProtocol(pid);
                if (!in.contains(node) || !in.isUp()) {
                    remove.add(i);
                }
            }
            for (Node n : remove) {
                this.removeFromInView(n);
            }

            for (int i = 0; i < this.degree(); i++) {
                final Node o = this.partialView.get(i);
                final Scamplon out = (Scamplon) o.getProtocol(pid);
                if (!out.inView.containsKey(node.getID())) {
                    out.addToInview(o, node);
                }
            }

            // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            if (!this.isBlocked && !this.events.isEmpty()) {
                final Event ev = this.events.poll();
                this.processEvent(node, pid, ev.message);
            }

            if ((this.step % DELTA_T) == 0) {
                this.shuffle(node);
            }
            this.step += 1;
        }
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
        final StringBuilder sb = new StringBuilder();
        sb.append("in:[");
        for (Long l : this.inView.keySet()) {
            if (sb.length() > 4) {
                sb.append(",");
            }
            sb.append(l);
        }
        sb.append("] out:");
        sb.append(this.partialView);
        sb.append(", isUp:");
        sb.append(this.isUp);
        sb.append(", isBlocked:");
        sb.append(this.isBlocked);
        return sb.toString();
    }

    // ============================================
    // C Y C L O N
    // ============================================

    private void addToInview(Node me, Node n) {
        if (me.getID() == n.getID()) {
            throw new RuntimeException("cannot put myself");
        }
        if (!this.inView.containsKey(n.getID())) {
            this.inView.put(n.getID(), n);
        }
    }

    private void removeFromInView(Node n) {
        this.inView.remove(n.getID());
    }


    public void shuffle(Node me) {
        if (this.isUp()) {
            if (this.isBlocked) {
                // delayed too much
                final Scamplon other = (Scamplon) this.lastDestination.node.getProtocol(pid);
                if (!other.isUp()) {
                    this.partialView.delete(this.lastDestination);
                    this.inView.remove(this.lastDestination.node.getID()); // just in case
                }
                this.hash++; // so that we can drop the current request savely
                this.partialView.freeze();
            }

            this.partialView.incrementAge();

            PartialView.Entry q = this.partialView.oldest();
            if (q == null) return;

            List<PartialView.Entry> nodesToSend = this.partialView.subsetMinus1(q);

            nodesToSend.add(new PartialView.Entry(me));

            ScamplonMessage m = ScamplonMessage.shuffleWithSecret(
                    me, nodesToSend, this.partialView.degree(), nextSecret());

            MessageSupervisor.put(q.node, m);

            this.lastDestination = q;
            this.isBlocked = true;
            send(me, q.node, m);
        }
    }

    public void onShuffle(Node me, ScamplonMessage message) {
        if (this.isUp()) {
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
    }

    public void onShuffleResponse(Node me, ScamplonMessage message) {
        if (this.isUp()) {
            if (message.type != ScamplonMessage.Type.ShuffleResponse) throw new RuntimeException("nop4");

            if (this.isCorrectSecret(message) && MessageSupervisor.validate(me, message)) {
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
    }


    /**
     * @param me
     * @param o       node from inview
     * @param replace node from outview
     */
    public int replace(Node me, Node o, Node replace) {
        //System.err.println("@" + me.getID() + " replace " + replace.getID() + " at " + o.getID());
        //System.err.println("@" + me.getID() + " = " + this.debug());
        Scamplon j = (Scamplon) o.getProtocol(pid);         // o -> me
        Scamplon i = (Scamplon) replace.getProtocol(pid);   // me -> replace

        //System.err.println("@" + o.getID() + " = " + j.debug());
        //System.err.println("@" + replace.getID() + " = " + i.debug());

        if (i.isUp() && j.isUp()) {
            if (me.getID() == o.getID() || replace.getID() == me.getID()) {
                throw new RuntimeException("never!");
            }
            if (o.getID() == replace.getID()) {
                int count = 0;
                if (j.isBlocked) {
                    count = j.partialView.count(me);
                    j.deleteASAP.add(me);
                } else {
                    count = j.partialView.deleteAll(me);
                }
                if (i.inView.containsKey(me.getID())) {
                    i.inView.remove(me.getID());
                    count += 1;
                }
                return count;
            } else {
                int count = j.partialView.switchNode(me, replace);
                if (i.inView.containsKey(me.getID())) {
                    count += 1;
                    i.inView.remove(me.getID());
                }
                if (!i.inView.containsKey(o.getID())) {
                    //i.inView.put(o.getID(), o);
                    i.addToInview(me, o);
                    count -= 1;
                }
                return count;
            }
        } else {
            // either i or j is not up: remove all nodes regarding |me|
            int count = 0;
            if (j.isBlocked) {
                count = j.partialView.count(me);
                j.deleteASAP.add(me);
            } else {
                count = j.partialView.deleteAll(me);
            }
            if (i.inView.containsKey(me.getID())) {
                i.inView.remove(me.getID());
                count += 1;
            }
            return count;
        }
    }


    // ============================================
    // S C A M P
    // ============================================

    /**
     * immediatly removes the node
     *
     * @param node
     */
    public static void fastUnsubscribe(Node node) {
        Scamplon current = (Scamplon) node.getProtocol(pid);
        if (current.isUp()) {
            current.down();
            final int ls = current.inView.size();
            final int notifyIn = Math.max(ls - c - 1, 0);
            //final int notifyIn = Math.max(ls - c, 0);
            final Queue<Node> in = new LinkedList<Node>(current.inView.values());
            final List<Node> out = current.partialView.list();
            int count = 0;
            for (int i = 0; i < notifyIn && out.size() > 0; i++) {
                final Node ex = in.poll();
                final Node dest = out.get(i % out.size());
                count += current.replace(node, ex, dest);

                final Scamplon quelle = (Scamplon) ex.getProtocol(pid);
                final Scamplon senke = (Scamplon) dest.getProtocol(pid);
                //System.err.println("after replace: @" + node.getID());
                //System.err.println("Q@" + ex.getID() + " = " + quelle.debug());
                //System.err.println("S@" + dest.getID() + " = " + senke.debug());
            }

            while (!in.isEmpty()) {
                final Scamplon next = (Scamplon) in.poll().getProtocol(pid);
                if (next.isBlocked) {
                    next.deleteASAP.add(node);
                } else {
                    next.partialView.deleteAll(node);
                }
                count++;
            }

            System.err.println("remove " + count + " arcs");

            current.partialView.clear();
            current.inView.clear();

        } else {
            throw new RuntimeException("already down");
        }
    }

    private static final int FORWARD_TTL = 25;

    /**
     * immediatly inserts the node
     *
     * @param s
     * @param c
     */
    public static void fastSubscribe(final Node s, final Node c) {
        final Scamplon subscriber = (Scamplon) s.getProtocol(pid);
        final Scamplon contact = (Scamplon) c.getProtocol(pid);

        if (subscriber.isUp() && contact.isUp()) {
            subscriber.addNeighbor(c);

            for (Node n : contact.partialView.list()) {
                fastForward(s, n, 0);
            }

            for (int i = 0; i < Scamplon.c && contact.degree() > 0; i++) {
                Node n = contact.partialView.get(CommonState.r.nextInt(contact.degree()));
                fastForward(s, n, 0);
            }

        } else {
            throw new RuntimeException("Cannot subscribe when one of the Nodes is down!");
        }

    }

    /**
     * fast forward in one step! (to compete with the remove function)
     *
     * @param s
     * @param node
     * @param counter
     */
    private static void fastForward(final Node s, final Node node, int counter) {
        counter++;
        if (counter < FORWARD_TTL) {
            final Scamplon current = (Scamplon) node.getProtocol(pid);
            if (current.partialView.p() && node.getID() != s.getID()) {
                final Scamplon subscriber = (Scamplon) s.getProtocol(pid);
                if (current.isBlocked) {
                    current.insertASAP.add(s);
                } else {
                    current.addNeighbor(s);
                    subscriber.addToInview(s, node);
                }
            } else if (current.degree() > 0) {
                Node next = current.partialView.get(CommonState.r.nextInt(current.degree()));
                fastForward(s, next, counter);
            } else {
                System.err.println("DEAD END for subscription " + s.getID() + " @" + node.getID());
            }
        } else {
            System.err.println("Forward for " + s.getID() + " timed out @" + node.getID());
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
        final Scamplon contact = (Scamplon) c.getProtocol(pid);
        System.err.println("subscribe " + s.getID() + "(" + subscribe + ") to " + c.getID() + "(" + contact + ")");
        send(s, c, subscribe);
    }

    private void onSubscribe(Node me, ScamplonMessage subscription) {
        if (subscription.type != ScamplonMessage.Type.Subscribe) throw new RuntimeException("nop");
        int count = 0;
        for (Node n : this.partialView.list()) {
            count += 1;
            final ScamplonMessage forward = ScamplonMessage.forward(me, subscription);
            send(me, n, forward);
        }

        for (int i = 0; i < c && this.degree() > 0; i++) {
            count += 1;
            int pos = CommonState.r.nextInt(this.degree());
            final ScamplonMessage forward = ScamplonMessage.forward(me, subscription);
            send(me, this.partialView.get(pos), forward);
        }
        System.err.println("add " + count + " arcs");
    }

    private void onForward(Node me, ScamplonMessage forward) {
        if (forward.type != ScamplonMessage.Type.Subscribe &&
                forward.type != ScamplonMessage.Type.Forward) {
            throw new RuntimeException("nop2");
        }

        if (!forward.isDead() && forward.subscriber.isUp()) {
            final Node subscriber = forward.subscriber;


            //if (this.partialView.p() && !this.contains(subscriber) && me.getID() != subscriber.getID()) {
            if (this.partialView.p() && me.getID() != subscriber.getID()) {
                this.addNeighbor(subscriber);

                //TODO send accept
                Scamplon s = (Scamplon) subscriber.getProtocol(pid);
                s.addToInview(subscriber, me);

            } else if (this.degree() > 0) {
                Node forwardTarget = this.getNeighbor(CommonState.r.nextInt(this.degree()));
                forward = ScamplonMessage.forward(me, forward);
                send(me, forwardTarget, forward);
            } else {
                System.err.println("@" + me.getID() + " = " + this.debug());
                System.err.println("--> " + forward);
                //throw new RuntimeException("no forwarding target");
            }
        }

    }


    private boolean isUp = true;
    private int hash = 0;

    @Override
    public boolean isUp() {
        return this.isUp;
    }

    @Override
    public void up() {
        this.isUp = true;
    }

    @Override
    public int hash() {
        return this.hash;
    }

    @Override
    public void down() {
        this.hash++;
        this.isBlocked = false;
        this.isUp = false;
    }

    @Override
    public String toString() {
        return this.debug();
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
