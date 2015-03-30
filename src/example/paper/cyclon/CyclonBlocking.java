package example.paper.cyclon;

import peersim.core.CommonState;
import peersim.core.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by julian on 3/24/15.
 */
public class CyclonBlocking extends CyclonProtocol {

    // ===========================================
    // C T O R
    // ===========================================

    private int step;
    //private final int STEP_RANGE = 10;
    private boolean isBlocked;
    private int currentSecret = Integer.MIN_VALUE;
    private Queue<Event> events;
    private final int DELTA_T = 35;

    public CyclonBlocking(String prefix) {
        super(prefix);
        this.step = CommonState.r.nextInt(DELTA_T);
        this.events = new LinkedList<Event>();
    }

    @Override
    public Object clone() {
        CyclonBlocking e = (CyclonBlocking) super.clone();
        e.step = CommonState.r.nextInt(DELTA_T);
        e.events = new LinkedList<Event>();
        return e;
    }

    // ===========================================
    // P U B L I C
    // ===========================================

    @Override
    public void processMessage(Node me, CyclonMessage message) {
        if (this.isUp()) {
            final Node destination;
            final List<CyclonEntry> sent, received;
            switch (message.type) {

                case Shuffle:
                    if (this.isBlocked) {
                        this.events.offer(new Event(me, message));
                    } else {
                        destination = me; // hacky..
                        sent = this.getSample(l);
                        received = message.send;
                        final CyclonMessage response = CyclonMessage.shuffleResponse(me, sent, message);
                        this.insertLists(me, destination, received, sent);
                        this.send(message.sender, response);
                    }
                    break;
                case ShuffleResponse:
                    if (this.isCorrectSecret(message)) {
                        destination = message.sender;
                        sent = message.received;
                        received = message.send;
                        this.insertLists(me, destination, received, sent);
                        this.isBlocked = false;
                    } else {
                        System.err.println("Message got dropped @" + me.getID() + "! " + message.secret + "|" + this.currentSecret);
                    }
                    break;
                default:
                    throw new RuntimeException("unhandled event");

            }
        }
    }

    @Override
    public void nextCycle(Node node, int protocolID) {

        if (this.isUp()) {
            if (!this.isBlocked && !this.events.isEmpty()) {
                while (!this.events.isEmpty()) {
                    final Event ev = this.events.poll();
                    //CyclonBlocking other = (CyclonBlocking) ev.node.getProtocol(protocolID);
                    //other.processMessage(ev.node, ev.message);
                    this.processMessage(ev.node, ev.message);
                }
            }

            if ((this.step % DELTA_T) == 0) {
                this.increaseAge();
                final Node oldest = this.oldest();
                if (oldest == null) {
                    System.err.println("nop @" + node.getID() + " step:" + this.step);
                } else {
                    final List<CyclonEntry> nodesToSend = this.getSample(l - 1, oldest);
                    nodesToSend.add(me(node));
                    final CyclonMessage message = CyclonMessage.shuffleWithSecret(node, nodesToSend, nextSecret());
                    this.send(oldest, message);
                    this.isBlocked = true;
                }
            }
            this.step += 1;
        }
    }

    // ===========================================
    // P R I V A T E
    // ===========================================

    private boolean isCorrectSecret(CyclonMessage m) {
        return (m.secret == this.currentSecret);
    }

    private int nextSecret() {
        this.currentSecret += 1;
        return this.currentSecret;
    }

    @Override
    public int hash() {
        return 0;
    }

    /**
     * to ensure no overlapping
     */
    private class Event {
        public final Node node;
        public final CyclonMessage message;
        public Event(Node node, CyclonMessage message) {
            this.node = node;
            this.message = message;
        }
    }
}
